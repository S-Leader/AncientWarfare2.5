package net.shadowmage.ancientwarfare.vehicle.missiles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.shadowmage.ancientwarfare.core.entity.AWEntityRegistry;
import net.shadowmage.ancientwarfare.core.util.Trig;
import net.shadowmage.ancientwarfare.vehicle.entity.IMissileHitCallback;
import net.shadowmage.ancientwarfare.vehicle.registry.AmmoRegistry;

import java.util.List;

public class MissileBase extends Entity implements IEntityAdditionalSpawnData {
    public IAmmo ammoType = AmmoRegistry.ammoArrow;
    public Entity launcher;
    public Entity shooterLiving;
    private IMissileHitCallback shooter;
    private int rocketBurnTime;
    private boolean inGround;
    private boolean hasImpacted;
    private BlockPos persistentBlockPos = BlockPos.ZERO;
    private BlockState persistentBlock = Blocks.AIR.defaultBlockState();
    private float accelerationX;
    private float accelerationY;
    private float accelerationZ;

    public MissileBase(Level level) {
        super((EntityType<?>) AWEntityRegistry.currentConstructionType(), level);
    }

    public MissileBase(EntityType<?> type, Level level) {
        super(type, level);
    }

    private void setMissileParams(IAmmo type, float x, float y, float z, float dx, float dy, float dz) {
        ammoType = type;
        setPos(x, y, z);
        xOld = x;
        yOld = y;
        zOld = z;
        setDeltaMovement(dx, dy, dz);
        accelerationX = dx;
        accelerationY = dy;
        accelerationZ = dz;
        if (ammoType.updateAsArrow()) updateArrowRotation();
        xRotO = getXRot();
        yRotO = getYRot();
        if (ammoType.isRocket()) {
            float speed = Mth.sqrt(dx * dx + dy * dy + dz * dz);
            rocketBurnTime = (int) (speed * 20F * AmmoHwachaRocket.BURN_TIME_FACTOR);
            if (speed > 0.0001F) {
                accelerationX = dx / speed * AmmoHwachaRocket.ACCELERATION_FACTOR;
                accelerationY = dy / speed * AmmoHwachaRocket.ACCELERATION_FACTOR;
                accelerationZ = dz / speed * AmmoHwachaRocket.ACCELERATION_FACTOR;
                setDeltaMovement(accelerationX, accelerationY, accelerationZ);
            }
        }
    }

    public void setMissileParams2(IAmmo ammo, float x, float y, float z, float yaw, float angle, float velocity) {
        float dx = -Trig.sinDegrees(yaw) * Trig.cosDegrees(angle) * velocity * 0.05F;
        float dy = Trig.sinDegrees(angle) * velocity * 0.05F;
        float dz = -Trig.cosDegrees(yaw) * Trig.cosDegrees(angle) * velocity * 0.05F;
        setMissileParams(ammo, x, y, z, dx, dy, dz);
    }

    public void setShooter(Entity shooter) {
        shooterLiving = shooter;
    }

    public void setLaunchingEntity(Entity entity) {
        launcher = entity;
    }

    public void setMissileCallback(IMissileHitCallback callback) {
        shooter = callback;
    }

    public void onImpactEntity(Entity entity, float x, float y, float z) {
        if (Ammo.shouldEffectEntity(level(), entity, this)) {
            ammoType.onImpactEntity(level(), entity, x, y, z, this);
            if (shooter != null) shooter.onMissileImpactEntity(level(), entity);
        }
    }

    public void onImpactWorld(HitResult hit) {
        BlockPos pos = hit instanceof BlockHitResult blockHit ? blockHit.getBlockPos() : BlockPos.containing(hit.getLocation());
        ammoType.onImpactWorld(level(), pos.getX(), pos.getY(), pos.getZ(), this, hit);
        if (shooter != null) shooter.onMissileImpact(level(), pos.getX(), pos.getY(), pos.getZ());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean displayFireAnimation() {
        return ammoType.isFlaming();
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public void tick() {
        super.tick();
        movementTick();
        if (!level().isClientSide && tickCount > 6000) discard();
    }

    private void checkProximity() {
        if (getDeltaMovement().y > 0) return;
        BlockPos current = blockPosition();
        if (ammoType.groundProximity() > 0) {
            for (int distance = 1; distance <= ammoType.groundProximity(); distance++) {
                BlockPos tested = current.below(distance);
                if (!level().isEmptyBlock(tested)) {
                    onImpactWorld(new BlockHitResult(Vec3.atCenterOf(tested), Direction.UP, tested, false));
                    return;
                }
            }
        }
        if (ammoType.entityProximity() <= 0) return;
        double proximity = ammoType.entityProximity();
        AABB area = getBoundingBox().inflate(proximity);
        List<Entity> entities = level().getEntities(this, area, entity -> entity != this && !(entity instanceof MissileBase));
        for (Entity entity : entities) {
            if (distanceTo(entity) < proximity) {
                onImpactEntity(entity, (float) getX(), (float) getY(), (float) getZ());
                return;
            }
        }
    }

    private void movementTick() {
        if (inGround && !persistentBlock.equals(level().getBlockState(persistentBlockPos))) inGround = false;
        if (inGround) {
            setDeltaMovement(Vec3.ZERO);
            return;
        }

        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) hit).getEntity();
            onImpactEntity(entity, (float) getX(), (float) getY(), (float) getZ());
            hasImpacted = true;
            if (!ammoType.isPenetrating()) {
                if (!level().isClientSide) discard();
                return;
            }
            setDeltaMovement(getDeltaMovement().scale(0.65D));
        } else if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            onImpactWorld(blockHit);
            hasImpacted = true;
            if (!ammoType.isPenetrating()) {
                setPos(blockHit.getLocation().subtract(getDeltaMovement().normalize().scale(0.05D)));
                inGround = true;
                persistentBlockPos = blockHit.getBlockPos();
                persistentBlock = level().getBlockState(persistentBlockPos);
                if (!ammoType.isPersistent() && !level().isClientSide) discard();
                return;
            }
            setDeltaMovement(getDeltaMovement().scale(0.65D));
        }

        if (ammoType.isProximityAmmo() && tickCount > 20) {
            checkProximity();
            if (isRemoved()) return;
        }

        Vec3 velocity = getDeltaMovement();
        setPos(getX() + velocity.x, getY() + velocity.y, getZ() + velocity.z);
        if (ammoType.isRocket() && rocketBurnTime-- > 0) {
            setDeltaMovement(velocity.add(accelerationX, accelerationY, accelerationZ));
            if (level().isClientSide) level().addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0, 0, 0);
        } else {
            setDeltaMovement(velocity.add(0, -ammoType.getGravityFactor(), 0));
        }
        if (ammoType.updateAsArrow()) updateArrowRotation();
    }

    private boolean canHitEntity(Entity entity) {
        if (!entity.isPickable() || entity == launcher || entity == shooterLiving || entity == shooter) return false;
        return launcher == null || entity != launcher.getControllingPassenger();
    }

    private void updateArrowRotation() {
        Vec3 velocity = getDeltaMovement();
        double horizontal = velocity.horizontalDistance();
        setYRot(Trig.toDegrees((float) Math.atan2(velocity.x, velocity.z)) - 90F);
        setXRot(Trig.toDegrees((float) Math.atan2(velocity.y, horizontal)) - 90F);
        yRotO = Mth.rotLerp(0.2F, yRotO, getYRot());
        xRotO = Mth.rotLerp(0.2F, xRotO, getXRot());
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
        setPos(x, y, z);
        setYRot(yaw);
        setXRot(pitch);
    }

    public ResourceLocation getTexture() {
        return ammoType.getModelTexture();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ammoType = AmmoRegistry.getAmmo(new ResourceLocation(tag.getString("ammoRegistryName")));
        if (ammoType == null) ammoType = AmmoRegistry.ammoArrow;
        inGround = tag.getBoolean("inGround");
        persistentBlockPos = BlockPos.of(tag.getLong("persistentBlockPos"));
        persistentBlock = NbtUtils.readBlockState(level().holderLookup(Registries.BLOCK), tag.getCompound("persistentBlock"));
        tickCount = tag.getInt("ticks");
        accelerationX = tag.getFloat("mX");
        accelerationY = tag.getFloat("mY");
        accelerationZ = tag.getFloat("mZ");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("ammoRegistryName", ammoType.getRegistryName().toString());
        tag.putBoolean("inGround", inGround);
        tag.putLong("persistentBlockPos", persistentBlockPos.asLong());
        tag.put("persistentBlock", NbtUtils.writeBlockState(persistentBlock));
        tag.putInt("ticks", tickCount);
        tag.putFloat("mX", accelerationX);
        tag.putFloat("mY", accelerationY);
        tag.putFloat("mZ", accelerationZ);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeUtf(ammoType.getRegistryName().toString());
        buffer.writeFloat(getYRot());
        buffer.writeFloat(getXRot());
        buffer.writeBoolean(inGround);
        buffer.writeLong(persistentBlockPos.asLong());
        buffer.writeInt(Block.getId(persistentBlock));
        buffer.writeInt(rocketBurnTime);
        buffer.writeBoolean(launcher != null);
        if (launcher != null) buffer.writeInt(launcher.getId());
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket() {
        return net.minecraftforge.network.NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        ammoType = AmmoRegistry.getAmmo(new ResourceLocation(buffer.readUtf(64)));
        yRotO = buffer.readFloat();
        setYRot(yRotO);
        xRotO = buffer.readFloat();
        setXRot(xRotO);
        inGround = buffer.readBoolean();
        persistentBlockPos = BlockPos.of(buffer.readLong());
        persistentBlock = Block.stateById(buffer.readInt());
        rocketBurnTime = buffer.readInt();
        if (buffer.readBoolean()) launcher = level().getEntity(buffer.readInt());
    }
}
