package net.shadowmage.ancientwarfare.structure.entity;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.interfaces.IEntityPacketHandler;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.network.PacketEntity;
import net.shadowmage.ancientwarfare.core.owner.Owner;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.structure.gates.types.Gate;
import net.shadowmage.ancientwarfare.structure.gates.types.GateRotatingBridge;

import java.util.Optional;

/*
 * an class to represent ALL gate types
 *
 * @author Shadowmage
 */
@SuppressWarnings("squid:S2160")
// no reason to override equals because the default implementation comparing entityId is enough
public class EntityGate extends Entity implements IEntityAdditionalSpawnData, IEntityPacketHandler {
    private static final String HEALTH_TAG = "health";
    private static final EntityDataAccessor<Byte> DATA_GATE_STATUS =
            SynchedEntityData.defineId(EntityGate.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Float> DATA_EDGE_POSITION =
            SynchedEntityData.defineId(EntityGate.class, EntityDataSerializers.FLOAT);
    public BlockPos pos1;
    public BlockPos pos2;

    public float edgePosition;//the bottom/opening edge of the gate (closed should correspond to pos1)
    public float edgeMax;//the 'fully extended' position of the gate

    public float openingSpeed = 0.f;//calculated speed of the opening gate -- used during animation

    public Gate gateType = Gate.getGateByID(0);

    private Owner owner = Owner.EMPTY;
    private int health = 0;
    public int hurtAnimationTicks = 0;
    private byte gateStatus = 0;
    public Direction gateOrientation = Direction.SOUTH;
    private int hurtInvulTicks = 0;

    private boolean hasSetWorldEntityRadius = false;
    private boolean wasPoweredA = false;
    private boolean wasPoweredB = false;
    private AABB renderBoundingBox = new AABB(0, 0, 0, 0, 0, 0);

    private BlockPos renderedTilePos = null;
    private int soundTicks = 15;

    public EntityGate(EntityType<?> type, Level level) {
        super(type, level);
        noCulling = true;
        blocksBuilding = true;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public void setOwner(Player player) {
        owner = new Owner(player);
    }

    public void setPositions(BlockPos pos1, BlockPos pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    @Override
    public Team getTeam() {
        return level().getScoreboard().getPlayersTeam(getOwner().getName());
    }

    public Gate getGateType() {
        return gateType;
    }

    public void setGateType(Gate type) {
        gateType = type;
        setHealth(type.getMaxHealth());
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_GATE_STATUS, (byte) 0);
        entityData.define(DATA_EDGE_POSITION, 0.0F);
    }

    @Override
    public ItemStack getPickedResult(HitResult target) {
        return Gate.getItemToConstruct(gateType.getGlobalID());
    }

    public void repackEntity() {
        if (level().isClientSide || isRemoved()) {
            return;
        }
        gateType.onGateStartClose(this);//
        ItemStack item = Gate.getItemToConstruct(gateType.getGlobalID());
        ItemEntity entity = new ItemEntity(level(), getX(), getY() + 0.5d, getZ(), item);
        level().addFreshEntity(entity);
        setDead();
    }

    public void setDead() {
        discard();
        if (!level().isClientSide) {
            //catch gates that have proxy blocks still in the world
            gateType.onGateStartClose(this);
            playSound(gateType.breakSound, 1, 1);
        }
    }

    private void setOpeningStatus(byte op) {
        gateStatus = op;
        if (!level().isClientSide) {
            entityData.set(DATA_GATE_STATUS, op);
            // Retain the old entity event for compatibility with already-spawned
            // clients, but SynchedEntityData is now the authoritative animation state.
            level().broadcastEntityEvent(this, op);
        }
        if (op == -1) {
            gateType.onGateStartClose(this);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public int getBrightnessForRender() {
        int i = Mth.floor(getX());
        int j = Mth.floor(getZ());
        int k = Mth.floor(getY());
        if (pos1.getY() > k) {
            k = pos1.getY();
        }
        if (pos2.getY() > k) {
            k = pos2.getY();
        }
        return level().getMaxLocalRawBrightness(new BlockPos(i, k, j));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleEntityEvent(byte par1) {
        if (par1 == -1 || par1 == 0 || par1 == 1) {
            // Compatibility event only: never run gate world-mutation callbacks on
            // the client. The server owns proxy placement/removal, while synced
            // entity data owns the render animation state.
            gateStatus = par1;
        }
        super.handleEntityEvent(par1);
    }

    public boolean isClosed() {
        return gateStatus == 0 && edgePosition == 0;
    }

    public float getRenderEdgePosition(float partialTicks) {
        // openingSpeed is previousEdge - currentEdge, preserving the original
        // 1.12 interpolation formula while allowing edgePosition to come from
        // SynchedEntityData on the client.
        return edgePosition + openingSpeed * (1.0F - partialTicks);
    }

    public byte getOpeningStatus() {
        return gateStatus;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int val) {
        int newHealth = Math.max(val, 0);
        if (newHealth < health) {
            hurtAnimationTicks = 20;
        }
        if (newHealth < health && !level().isClientSide) {
            PacketEntity pkt = new PacketEntity(this);
            pkt.packetData.putInt(HEALTH_TAG, newHealth);
            NetworkHandler.sendToAllTracking(this, pkt);
        }
        health = newHealth;
    }

    @Override
    public void setPos(double par1, double par3, double par5) {
        super.setPos(par1, par3, par5);
        if (gateType != null) {
            gateType.setCollisionBoundingBox(this);
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        boolean gateOwner = getOwner().isOwnerOrSameTeamOrFriend(player);
        if (player.isCreative() || getOwner() == Owner.EMPTY || gateOwner) {
            if (player.isCrouching()) {
                if (player.isCreative()) {
                    AWMenuTypes.open(player, NetworkHandler.GUI_GATE_CONTROL_CREATIVE, getId(), 0, 0);
                } else if (gateOwner) {
                    AWMenuTypes.open(player, NetworkHandler.GUI_GATE_CONTROL, getId(), 0, 0);
                }
            } else {
                activateGate();
            }
            return InteractionResult.CONSUME;
        } else {
            player.sendSystemMessage(Component.translatable("guistrings.gate.use_error"));
        }
        return InteractionResult.FAIL;
    }

    public void activateGate() {
        if (gateStatus == 1 && gateType.canActivate(this, false)) {
            setOpeningStatus((byte) -1);
        } else if (gateStatus == -1 && gateType.canActivate(this, true)) {
            setOpeningStatus((byte) 1);
        } else if (edgePosition == 0 && gateType.canActivate(this, true)) {
            setOpeningStatus((byte) 1);
        } else if (gateType.canActivate(this, false))//gate is already open/opening, set to closing
        {
            setOpeningStatus((byte) -1);
        }
    }

    private void playGateMoveSound(BlockPos pos, Gate gateType) {
        level().playSound(null, pos, gateType.moveSound, SoundSource.AMBIENT, 2, 1);
        soundTicks = 0;
    }

    @Override
    @SuppressWarnings("squid:S2696")
    //Level.MAX_ENTITY_RADIUS is static and can only be set this way, also just running in the main thread makes this safe
    public void tick() {
        super.tick();
        float prevEdge = edgePosition;

        if (level().isClientSide) {
            // GateProxyRenderer renders a client copy of EntityGate. Entity events
            // alone are not a reliable animation transport after chunk/entity
            // tracking changes, so consume the authoritative synced state.
            gateStatus = entityData.get(DATA_GATE_STATUS);
            edgePosition = entityData.get(DATA_EDGE_POSITION);
        } else {
            if (gateStatus == 1) {
                edgePosition += gateType.getMoveSpeed();
                if (soundTicks == 15) {
                    playGateMoveSound(pos1, gateType);
                }
                soundTicks++;
                if (edgePosition >= edgeMax) {
                    edgePosition = edgeMax;
                    setOpeningStatus((byte) 0);
                    soundTicks = 15;
                    gateType.onGateFinishOpen(this);
                }
            } else if (gateStatus == -1) {
                edgePosition -= gateType.getMoveSpeed();
                if (soundTicks == 15) {
                    playGateMoveSound(pos1, gateType);
                }
                soundTicks++;
                if (edgePosition <= 0) {
                    edgePosition = 0;
                    setOpeningStatus((byte) 0);
                    soundTicks = 15;
                    gateType.onGateFinishClose(this);
                }
            }
            if (Float.compare(entityData.get(DATA_EDGE_POSITION), edgePosition) != 0) {
                entityData.set(DATA_EDGE_POSITION, edgePosition);
            }
        }

        openingSpeed = prevEdge - edgePosition;
        setPos(getX(), getY(), getZ());
        if (hurtInvulTicks > 0) {
            hurtInvulTicks--;
        }
        checkForPowerUpdates();
        gateType.setRenderedTileIfNotPresent(this);
        if (hurtAnimationTicks > 0) {
            hurtAnimationTicks--;
        }

        if (!hasSetWorldEntityRadius) {
            hasSetWorldEntityRadius = true;
            BlockPos min = BlockTools.getMin(pos1, pos2);
            BlockPos max = BlockTools.getMax(pos1, pos2);
            int xSize = max.getX() - min.getX() + 1;
            int zSize = max.getZ() - min.getZ() + 1;
            int ySize = max.getY() - min.getY() + 1;
            int largest = Math.max(xSize, ySize);
            largest = Math.max(largest, zSize);
            largest = (largest / 2) + 1;
            // Modern entity tracking no longer uses a mutable global entity radius.
        }

    }

    private void checkForPowerUpdates() {
        if (level().isClientSide) {
            return;
        }
        boolean activate = false;
        int y = Math.min(pos2.getY(), pos1.getY());
        boolean foundPowerA = level().hasNeighborSignal(new BlockPos(pos1.getX(), y, pos1.getZ()));
        boolean foundPowerB = level().hasNeighborSignal(new BlockPos(pos2.getX(), y, pos2.getZ()));
        if (foundPowerA && !wasPoweredA) {
            activate = true;
        }
        if (foundPowerB && !wasPoweredB) {
            activate = true;
        }
        wasPoweredA = foundPowerA;
        wasPoweredB = foundPowerB;
        if (activate) {
            activateGate();
        }
    }

    private boolean isInsensitiveTo(DamageSource source) {
        return source.is(DamageTypes.FALLING_ANVIL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.DROWN)
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.FALLING_BLOCK)
                || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.STARVE);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (isInsensitiveTo(damageSource) || amount < 0) {
            return false;
        }
        if (level().isClientSide) {
            return true;
        }
        if (!damageSource.is(DamageTypeTags.IS_EXPLOSION)) {
            if (hurtInvulTicks > 0) {
                return false;
            }
            hurtInvulTicks = 10;
        } else {
            amount *= 10;
        }

        playSound(gateType.hurtSound, 1, 1);
        if (damageSource.getDirectEntity() instanceof AbstractArrow) { // ignore arrow dmg
            return false;
        }
        if (damageSource.getDirectEntity() instanceof LivingEntity) {
			/*  getTrueSource is no good here because that would make it work for ranged attacks like arrows and vehicles too,
			we only want to reduce melee damage */
            LivingEntity entitylivingbase = (LivingEntity) damageSource.getDirectEntity();
            if ((!entitylivingbase.getItemInHand(InteractionHand.MAIN_HAND).isEmpty())) {
                Item heldItem = entitylivingbase.getItemInHand(InteractionHand.MAIN_HAND).getItem();
                if (gateType.isWood(gateType.getVariant())) { // wooden gates
                    amount = (heldItem instanceof AxeItem) ? amount / 2 : amount / 4; // half dmg for axe, 1/4 for anything else

                } else { // iron gates
                    if ((heldItem instanceof PickaxeItem)) {
                        amount = ((PickaxeItem) heldItem).getTier() == Tiers.DIAMOND ? amount / 2 : amount / 3; // half dmg for diamond pickaxe, 1/3 for any other axes
                    } else { // anything but a pickaxe
                        amount = amount / 4;
                    }
                }
            }
        }
        setHealth((int) (getHealth() - amount));

        if (getHealth() <= 0) {
            setDead();
        }
        return !isRemoved();
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    public boolean handleWaterMovement() {
        return false;
    }

    @Override
    public void push(double moveX, double moveY, double moveZ) {
        //NOOP
    }

    public AABB getCollisionBoundingBox() {
        return getBoundingBox();
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    public float getCollisionBorderSize() {
        return 0.1F;
    }

    public boolean canBePushed() {
        return true;
    }

    @Override
    public void push(Entity entity) {
        super.push(entity);
        if (isInside(entity)) {
            entity.push(0, -gateStatus * 0.5, 0);
        }
    }

    @Override
    public void playerTouch(Player entity) {
        if (isInside(entity)) {
            entity.push(0, -gateStatus * 0.5, 0);
        }
    }

    private boolean isInside(Entity entity) {
        return gateType instanceof GateRotatingBridge && getBoundingBox().intersects(entity.getBoundingBox());
    }

    @Override
    public boolean startRiding(Entity entityIn, boolean force) {
        return false;
    }

    //Rendering
    public String getTexture() {
        return "textures/models/gate/" + gateType.getTexture();
    }

    //Data
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setPositions(BlockPos.of(tag.getLong("pos1")), BlockPos.of(tag.getLong("pos2")));
        realignEntityPosition();
        setGateType(Gate.getGateByID(tag.getInt("type")));
        owner = Owner.deserializeFromNBT(tag);
        edgePosition = tag.getFloat("edge");
        edgeMax = tag.getFloat("edgeMax");
        if (!level().isClientSide) {
            entityData.set(DATA_EDGE_POSITION, edgePosition);
        }
        setHealth(tag.getInt(HEALTH_TAG));
        gateStatus = tag.getByte("status");
        if (!level().isClientSide) {
            entityData.set(DATA_GATE_STATUS, gateStatus);
        }
        gateOrientation = Direction.from3DDataValue(tag.getByte("orient"));
        wasPoweredA = tag.getBoolean("power");
        wasPoweredB = tag.getBoolean("power2");
        gateType.updateRenderBoundingBox(this);
    }

    private void realignEntityPosition() {
        BlockPos min = BlockTools.getMin(pos1, pos2);
        BlockPos max = BlockTools.getMax(pos1, pos2);
        setPos(min.getX() + ((max.getX() - min.getX() + 1) / 2d), min.getY(), min.getZ() + ((max.getZ() - min.getZ() + 1) / 2d));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putLong("pos1", pos1.asLong());
        tag.putLong("pos2", pos2.asLong());
        tag.putInt("type", gateType.getGlobalID());
        getOwner().serializeToNBT(tag);
        tag.putFloat("edge", edgePosition);
        tag.putFloat("edgeMax", edgeMax);
        tag.putInt(HEALTH_TAG, getHealth());
        tag.putByte("status", gateStatus);
        tag.putByte("orient", (byte) gateOrientation.ordinal());
        tag.putBoolean("power", wasPoweredA);
        tag.putBoolean("power2", wasPoweredB);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf data) {
        data.writeLong(pos1.asLong());
        data.writeLong(pos2.asLong());
        data.writeInt(gateType.getGlobalID());
        data.writeFloat(edgePosition);
        data.writeFloat(edgeMax);
        data.writeByte(gateStatus);
        data.writeByte(gateOrientation.ordinal());
        data.writeInt(health);
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket() {
        return net.minecraftforge.network.NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf data) {
        setPositions(BlockPos.of(data.readLong()), BlockPos.of(data.readLong()));
        gateType = Gate.getGateByID(data.readInt());
        edgePosition = data.readFloat();
        entityData.set(DATA_EDGE_POSITION, edgePosition);
        edgeMax = data.readFloat();
        gateStatus = data.readByte();
        entityData.set(DATA_GATE_STATUS, gateStatus);
        gateOrientation = Direction.from3DDataValue(data.readByte());
        health = data.readInt();
        gateType.updateRenderBoundingBox(this);
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains(HEALTH_TAG)) {
            health = tag.getInt(HEALTH_TAG);
            hurtAnimationTicks = 20;
        }
    }

    public Owner getOwner() {
        return owner;
    }

    public AABB getRenderBoundingBox() {
        return renderBoundingBox;
    }

    public void setRenderBoundingBox(AABB renderBoundingBox) {
        this.renderBoundingBox = renderBoundingBox;
    }

    public Optional<BlockPos> getRenderedTilePos() {
        return Optional.ofNullable(renderedTilePos);
    }

    public void setRenderedTilePos(BlockPos pos) {
        renderedTilePos = pos;
    }
}
