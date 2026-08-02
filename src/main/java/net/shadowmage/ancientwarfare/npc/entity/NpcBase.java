package net.shadowmage.ancientwarfare.npc.entity;


import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.entity.AWEntityRegistry;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.interfaces.IEntityPacketHandler;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.network.PacketEntity;
import net.shadowmage.ancientwarfare.core.owner.IOwnable;
import net.shadowmage.ancientwarfare.core.owner.Owner;
import net.shadowmage.ancientwarfare.core.util.EntityTools;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.npc.ai.NpcAIBlockWithShield;
import net.shadowmage.ancientwarfare.npc.ai.NpcNavigator;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.item.ItemCommandBaton;
import net.shadowmage.ancientwarfare.npc.item.ItemNpcSpawner;
import net.shadowmage.ancientwarfare.npc.registry.NpcDefault;
import net.shadowmage.ancientwarfare.npc.skin.NpcSkinSettings;
import net.shadowmage.ancientwarfare.vehicle.entity.IPathableEntity;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;
import net.shadowmage.ancientwarfare.vehicle.pathing.Node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static net.shadowmage.ancientwarfare.npc.config.AWNPCStatics.npcLevelDamageMultiplier;

public abstract class NpcBase extends PathfinderMob implements IEntityAdditionalSpawnData, IOwnable, IEntityPacketHandler, IPathableEntity, Npc {
    /**
     * Shared selectors keep legacy addTask calls source-compatible across all NPC subclasses.
     */
    protected final LegacyGoalTasks tasks = new LegacyGoalTasks(goalSelector);
    protected final LegacyGoalTasks targetTasks = new LegacyGoalTasks(targetSelector);
    @Deprecated
    protected final Level world;

    private static final EntityDataAccessor<Integer> AI_TASKS = SynchedEntityData.defineId(NpcBase.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SHIELD_DISABLED_TICK = SynchedEntityData.defineId(NpcBase.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> BED_POS = SynchedEntityData.defineId(NpcBase.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Byte> BED_DIRECTION = SynchedEntityData.defineId(NpcBase.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> IS_SLEEPING = SynchedEntityData.defineId(NpcBase.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SWINGING_ARMS = SynchedEntityData.defineId(NpcBase.class, EntityDataSerializers.BOOLEAN);
    private static final String SLOT_NUM_TAG = "slotNum";
    private static final String BED_DIRECTION_TAG = "bedDirection";
    private static final String IS_SLEEPING_TAG = "isSleeping";
    private static final String CACHED_BED_POS_TAG = "cachedBedPos";
    private static final String BED_POS_TAG = "bedPos";
    private static final String FOUND_BED_TAG = "foundBed";
    private static final String ORDERS_STACK_TAG = "ordersStack";
    private static final String UPKEEP_STACK_TAG = "upkeepStack";
    private static final String LEVELING_STATS_TAG = "levelingStats";
    private static final String MAX_HEALTH_TAG = "maxHealth";
    private static final String HEALTH_TAG = "health";
    private static final String ATTACK_DAMAGE_OVERRIDE_TAG = "attackDamageOverride";
    private static final String ARMOR_VALUE_OVERRIDE_TAG = "armorValueOverride";
    private static final String AI_ENABLED_TAG = "aiEnabled";
    private static final String HAS_CUSTOM_EQUIPMENT_TAG = "hasCustomEquipment";
    public static final int ORDER_SLOT = 6;
    public static final int UPKEEP_SLOT = 7;
    private static final String ATTACK_TARGET_TAG = "attackTarget";

    public ItemStack ordersStack = ItemStack.EMPTY;
    private NpcAIBlockWithShield shieldBlockAI = null;
    private static final String DO_NOT_PURSUE = "donotpursue";
    private Owner owner = Owner.EMPTY;

    private String followingPlayerName;//set/cleared onInteract from player if player.team==this.team
    private NpcLevelingStats levelingStats;

    private NpcSkinSettings skinSettings = new NpcSkinSettings();

    public ItemStack upkeepStack = ItemStack.EMPTY;

    // used for flee/distress AI
    // this isn't really useful yet, combat NPC's will attack any mob they come in
    // contact with during a distress response
    public Set<Entity> nearbyHostiles = new LinkedHashSet<Entity>();

    private boolean aiEnabled = true;
    public boolean doNotPursue = false; //if the npc should not pursue targets away from its position/route
    private boolean hasCustomEquipment = false; //faction based only

    private int attackDamage = -1;//faction based only
    private int armorValue = -1;//faction based only
    private int maxHealthOverride = -1;

    private BlockPos cachedBedPos;
    private boolean foundBed = false;
    private boolean rainedOn = false;
    private float originalWidth;
    private float originalHeight;

    private int stopAIControlFlags = 0;

    public NpcBase(Level par1World) {
        this((EntityType<? extends PathfinderMob>) AWEntityRegistry.currentConstructionType(), par1World);
    }

    public NpcBase(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        world = level;
        levelingStats = new NpcLevelingStats(this);
        setPathfindingMalus(BlockPathTypes.DOOR_WOOD_CLOSED, 0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new NpcNavigator(this);
    }

    public void stopAIControlFlag(int flag) {
        stopAIControlFlags |= flag;
    }

    public void startAIControlFlag(int flag) {
        stopAIControlFlags &= ~flag;
    }

    public boolean isAIFlagStopped(int flag) {
        return (stopAIControlFlags & flag) > 0;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(AI_TASKS, 0);
        entityData.define(BED_POS, BlockPos.ZERO);
        entityData.define(BED_DIRECTION, (byte) Direction.NORTH.ordinal());
        entityData.define(IS_SLEEPING, false);
        entityData.define(SWINGING_ARMS, false);
        entityData.define(SHIELD_DISABLED_TICK, 0);
    }

    private ItemStack getShieldStack() {
        return getItemInHand(InteractionHand.OFF_HAND);
    }

    public void setShieldStack(ItemStack stack) {
        setItemInHand(InteractionHand.OFF_HAND, stack);
    }

    public int getMaxHealthOverride() {
        return maxHealthOverride;
    }

    public void setMaxHealthOverride(int maxHealthOverride) {
        this.maxHealthOverride = maxHealthOverride;
        if (maxHealthOverride > 0) {
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealthOverride);
            if (getHealth() < getMaxHealth()) {
                setHealth(getMaxHealth());
            }
        }
    }

    public void setAttackDamageOverride(int attackDamage) {
        this.attackDamage = attackDamage;
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(attackDamage);
    }

    public void setArmorValueOverride(int armorValue) {
        this.armorValue = armorValue;
    }

    public int getArmorValueOverride() {
        return armorValue;
    }

    public int getAttackDamageOverride() {
        return attackDamage;
    }

    public void setCustomEquipmentOverride(boolean val) {
        hasCustomEquipment = val;
    }

    public boolean getCustomEquipmentOverride() {
        return hasCustomEquipment;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        // Modern Mob#doHurtTarget already applies attack attributes, enchantments,
        // knockback, fire aspect and shield disabling. Off-hand modifiers are handled
        // by the living-entity equipment attribute map.
        return super.doHurtTarget(target);
    }

    protected static final class LegacyGoalTasks {
        private final GoalSelector selector;

        private LegacyGoalTasks(GoalSelector selector) {
            this.selector = selector;
        }

        public void addTask(int priority, Goal goal) {
            selector.addGoal(priority, goal);
        }

        public void removeTask(Goal goal) {
            selector.removeGoal(goal);
        }
    }

    /*
     * Proper calculations for all types of armors, including shields
     */
    @Override
    public int getArmorValue() {
        if (getArmorValueOverride() >= 0) {
            return getArmorValueOverride();
        }
        return super.getArmorValue();
    }

    @Override
    public final double getMyRidingOffset() {
        return -0.35D;
    }

    @Override
    public int getMaxFallDistance() {
        return getTarget() == null ? 4 : 4 + (int) (getHealth() / 3);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader levelReader) {
        BlockState stateBelow = level().getBlockState(pos.below());
        if (LegacyMaterial.of(stateBelow) == LegacyMaterial.LAVA || LegacyMaterial.of(stateBelow) == LegacyMaterial.CACTUS)//Avoid cacti and lava when wandering
        {
            return -10;
        } else if (LegacyMaterial.of(stateBelow).isLiquid())//Don't try swimming too much
        {
            return 0;
        }
        float level = getLitBlockWeight(pos);//Prefer lit areas
        if (level < 0) {
            return 0;
        } else {
            return level + (stateBelow.isFaceSturdy(level(), pos.below(), Direction.UP) ? 1 : 0);
        }
    }

    protected float getLitBlockWeight(BlockPos pos) {
        return level().getBrightness(LightLayer.BLOCK, pos) / 15F;
    }

    public double getDistanceSqFromHome() {
        if (!hasRestriction()) {
            return 0;
        }
        BlockPos home = getRestrictCenter();
        return distanceToSqr(home.getX() + 0.5d, home.getY(), home.getZ() + 0.5d);
    }

    public Optional<BlockPos> getTownHallPosition() {
        return Optional.empty();//NOOP on non-player owned npc
    }

    public void setHomeAreaAtCurrentPosition() {
        restrictTo(blockPosition(), getHomeRange());
    }

    public int getHomeRange() {
        if (hasRestriction()) {
            return Mth.floor(getRestrictRadius());
        }
        return 5;
    }

    /*
     * Return true if this NPC should be within his home range.<br>
     * Should still allow for a combat NPC to attack targets outside his home range.
     */
    public boolean shouldBeAtHome() {
        if (getTarget() != null || !hasRestriction()) {
            return false;
        }
        if (level().isRainingAt(blockPosition())) {
            setRainedOn(true);
        }
        return shouldSleep() || isWaitingForRainToStop();
    }

    private boolean isWaitingForRainToStop() {
        if (worksInRain() || !level().isRaining()) {
            // rain has stopped, reset
            setRainedOn(false);
            return false;
        }
        return rainedOn;
    }

    public boolean worksInRain() {
        return false;
    }

    public boolean isPassive() {
        return true;
    }

    private void setRainedOn(boolean rainedOn) {
        this.rainedOn = rainedOn;
    }

    public void setIsAIEnabled(boolean val) {
        aiEnabled = val;
    }

    public boolean getIsAIEnabled() {
        return aiEnabled && !AWNPCStatics.npcAIDebugMode;
    }

    @Override
    protected net.minecraft.world.InteractionResult mobInteract(Player player, InteractionHand hand) {
        return tryCommand(player) ? net.minecraft.world.InteractionResult.sidedSuccess(level().isClientSide) : net.minecraft.world.InteractionResult.PASS;
    }

    /*
     * should be implemented by any npc that wishes to open a GUI on interact<br>
     * must be called from interact code to actually open the GUI<br>
     * allows for subtypes/etc to vary the opened GUI without re-implementing the interact logic
     */
    public void openGUI(Player player) {
        AWMenuTypes.open(player, NetworkHandler.GUI_NPC_INVENTORY, getId(), 0, 0);
    }

    /*
     * if this npc has an alt-control GUI, open it here.<br>
     * should called from the npc inventory gui.
     */
    public void openAltGui(Player player) {

    }

    /*
     * used by the npc inventory gui to determine if it should display the 'alt control gui' button<br>
     * this setting must return true -on the client- if the button is to be displayed.
     */
    public boolean hasAltGui() {
        return false;
    }

    protected boolean tryCommand(Player player) {
        boolean baton = !EntityTools.getItemFromEitherHand(player, ItemCommandBaton.class).isEmpty();
        if (!baton) {
            if (!level().isClientSide) {
                if (player.isShiftKeyDown()) {
                    String playerName = player.getGameProfile().getName();
                    if (followingPlayerName != null && followingPlayerName.equals(playerName)) {
                        followingPlayerName = null;
                    } else {
                        followingPlayerName = playerName;
                    }
                } else {
                    openGUI(player);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public final boolean hurt(DamageSource source, float damage) {
        if (isSleeping()) { // prevent suffocation damage (allows bunk beds and such)
            return false;
        }
        if (source.getEntity() != null && !canBeAttackedBy(source.getEntity())) {
            return false;
        }
        if (!level().isClientSide)
            processPreDamageLogic(source, damage);
        return super.hurt(source, damage);
    }

    private void processPreDamageLogic(DamageSource source, float damage) {
        if (shieldBlockAI != null) {
            shieldBlockAI.onPreDamage(source, damage);
        }
    }

    @Override
    public void aiStep() {
        if (isUsingItem() && !isPassenger()) {
            setSpeed(getSpeed() * 0.2F);
            setSprinting(false);
        }
        super.aiStep();
    }

    @Override
    protected void blockUsingShield(LivingEntity attacker) {
        attacker.knockback(0.1F, getX() - attacker.getX(), getZ() - attacker.getZ());
    }

    private void disableShield() {
        if (random.nextFloat() < 1F) {
            setShieldDisabledTick(80);
            stopUsingItem();
            level().broadcastEntityEvent(this, (byte) 30);
        }
    }

    @Override
    public void setTarget(@Nullable LivingEntity entity) {
        if (entity != null && !canTarget(entity)) {
            return;
        }
        super.setTarget(entity);

        if (!level().isClientSide && entity != null) {
            updateAttackTargetClient(entity);
        }
    }

    private void updateAttackTargetClient(LivingEntity entity) {
        PacketEntity pkt = new PacketEntity(this);
        pkt.packetData.putInt(ATTACK_TARGET_TAG, entity.getId());
        NetworkHandler.sendToAllTracking(this, pkt);
    }

    @Override
    public final void setLastHurtByMob(@Nullable LivingEntity entity) {
        if (entity != null && !canTarget(entity)) {
            return;
        }
        super.setLastHurtByMob(entity);
    }

    public final ItemStack getItemBySlot(int slot) {
        if (slot == ORDER_SLOT) {
            return ordersStack;
        } else if (slot == UPKEEP_SLOT) {
            return upkeepStack;
        } else if (slot >= 0 && slot < EquipmentSlot.values().length) {
            return super.getItemBySlot(EquipmentSlot.values()[slot]);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
        if (slot == EquipmentSlot.MAINHAND) {
            onWeaponInventoryChanged();
        }
        if (slot == EquipmentSlot.OFFHAND) {
            onOffhandInventoryChanged();
        }
    }

    public final void setItemSlot(int slot, ItemStack stack) {
        if (slot >= 0 && slot < EquipmentSlot.values().length) {
            setItemSlot(EquipmentSlot.values()[slot], stack);
        } else if (slot == UPKEEP_SLOT) {
            upkeepStack = stack;
        } else if (slot == ORDER_SLOT) {
            ordersStack = stack;
            onOrdersInventoryChanged();
        }
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity par1EntityLivingBase) {
        boolean result = super.killedEntity(level, par1EntityLivingBase);
        if (!level().isClientSide) {
            addExperience(AWNPCStatics.npcXpFromKill);
            if (par1EntityLivingBase == getTarget()) {
                setTarget(null);
            }
        }
        return result;
    }

    /*
     * return the bitfield containing all of the currently executing AI tasks<br>
     * used by player-owned npcs for rendering ai-tasks
     */
    public final int getAITasks() {
        return entityData.get(AI_TASKS);
    }

    /*
     * add a task to the bitfield of currently executing tasks<br>
     * input should be a ^2, or combination of (e.g. 1+2 or 2+4)<br>
     */
    public final void addAITask(int task) {
        int tasks = getAITasks();
        int tc = tasks;
        tasks = tasks | task;
        if (tc != tasks) {
            setAITasks(tasks);
        }
    }

    /*
     * remove a task from the bitfield of currently executing tasks<br>
     * input should be a ^2, or combination of (e.g. 1+2 or 2+4)<br>
     */
    public final void removeAITask(int task) {
        int tasks = getAITasks();
        int tc = tasks;
        tasks = tasks & (~task);
        if (tc != tasks) {
            setAITasks(tasks);
        }
    }

    /*
     * set ai tasks -- only used internally
     */
    private void setAITasks(int tasks) {
        entityData.set(AI_TASKS, tasks);
    }

    /*
     * add an amount of experience to this npcs leveling stats<br>
     * experience is added for base level, and subtype level(if any)
     */
    public final void addExperience(int amount) {
        getLevelingStats().addExperience(amount);
    }

    /*
     * implementations should read in any data written during {@link #writeAdditionalItemData(CompoundTag)}
     */
    public void readAdditionalItemData(CompoundTag tag) {
        ListTag equipmentList = tag.getList("equipment", Tag.TAG_COMPOUND);
        ItemStack stack;
        CompoundTag equipmentTag;
        for (int i = 0; i < equipmentList.size(); i++) {
            equipmentTag = equipmentList.getCompound(i);
            stack = ItemStack.of(equipmentTag);
            if (equipmentTag.contains(SLOT_NUM_TAG)) {
                setItemSlot(equipmentTag.getInt(SLOT_NUM_TAG), stack);
            }
        }
        readBaseTags(tag);
    }

    /*
     * Implementations should write out any persistent entity-data needed to restore entity-state from an item-stack.<br>
     * This should include inventory, levels, orders, faction / etc
     */
    public void writeAdditionalItemData(CompoundTag tag) {
        ListTag equipmentList = new ListTag();
        ItemStack stack;
        CompoundTag equipmentTag;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            stack = getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            equipmentTag = stack.save(new CompoundTag());
            equipmentTag.putInt(SLOT_NUM_TAG, slot.ordinal());
            equipmentList.add(equipmentTag);
        }
        tag.put("equipment", equipmentList);
        writeBaseTags(tag);
    }

    /*
     * is the input stack a valid orders-item for this npc?<br>
     * only used by player-owned NPCs
     */
    @SuppressWarnings("squid:S1172")
    public boolean isValidOrdersStack(ItemStack stack) {
        return false;
    }

    /*
     * callback for when orders-stack changes.  implementations should inform any necessary AI tasks of the
     * change to order-items
     */
    public void onOrdersInventoryChanged() {
    }

    /*
     * callback for when weapon slot has been changed.<br>
     * Implementations should re-set any subtype or inform any AI that need to know when
     * weapon was changed.
     */
    public void onWeaponInventoryChanged() {
    }

    /*
     * callback for when the offhand item slot has been changed.<br>
     * Implementations should re-set any subtype or inform any AI that need to know when
     * weapon was changed.
     */
    public void onOffhandInventoryChanged() {
        if (!level().isClientSide) {
            if (shieldBlockAI != null) {
                goalSelector.removeGoal(shieldBlockAI);
                shieldBlockAI = null;
            }
            ItemStack mainhandStack = getMainHandItem();
            ItemStack offhandStack = getOffhandItem();
            if (offhandStack.canPerformAction(ToolActions.SHIELD_BLOCK) && !isBow(mainhandStack.getItem())) {
                shieldBlockAI = initShieldAI();
                goalSelector.addGoal(3, shieldBlockAI);
            }
        }
    }

    /*
     * return the NPCs subtype.<br>
     * this subtype may vary at runtime.
     */
    public abstract String getNpcSubType();

    /*
     * return the NPCs type.  This type should be unique for the class of entity,
     * or at least unique pertaining to the entity registration.
     */
    public abstract String getNpcType();

    /*
     * return the full NPC type for this npc<br>
     * returns npcType if subtype is empty, else npcType.npcSubtype
     */
    public String getNpcFullType() {
        String type = getNpcType();
        if (type == null || type.isEmpty()) {
            throw new RuntimeException("Type must not be null or empty:");
        }
        String sub = getNpcSubType();
        if (sub == null) {
            throw new RuntimeException("Subtype must not be null...type: " + type);
        }
        if (!sub.isEmpty()) {
            type = type + "." + sub;
        }
        return type;
    }

    public String getNpcName() {
        String name = I18n.get("entity.ancientwarfarenpc." + getNpcFullType() + ".name");
        if (hasCustomName()) {
            name = name + " : " + getCustomName().getString();
        }
        return name;
    }

    public final NpcLevelingStats getLevelingStats() {
        return levelingStats;
    }

    private ItemStack getItemToSpawn() {
        return ItemNpcSpawner.getSpawnerItemForNpc(this);
    }

    public final long getIDForSkin() {
        return getUUID().getLeastSignificantBits();
    }

    @Override
    public final ItemStack getPickedResult(HitResult target) {
        return getItemToSpawn();
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeLong(getUUID().getMostSignificantBits());
        buffer.writeLong(getUUID().getLeastSignificantBits());
        owner.serializeToBuffer(buffer);
        skinSettings.serializeToBuffer(buffer);
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket() {
        return net.minecraftforge.network.NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        long l1 = buffer.readLong();
        long l2 = buffer.readLong();
        setUUID(new UUID(l1, l2));
        owner = new Owner(buffer);
        skinSettings = NpcSkinSettings.deserializeFromBuffer(buffer);
    }

    @Override
    public void tick() {
        level().getProfiler().push("AWNpcTick");
        if (getShieldDisabledTick() > 0) {
            decrementShieldDisabledTick();
        }
        if (tickCount % 200 == 0 && getHealth() < getMaxHealth() && isAlive() && (!requiresUpkeep() || getFoodRemaining() > 0)) {
            setHealth(getHealth() + 1);
        }
        super.tick();
        if (!getMainHandItem().isEmpty()) {
            getMainHandItem().inventoryTick(level(), this, 0, true);
        }
        level().getProfiler().pop();
    }

    public boolean canAttackClass(Class claz) {
        return !FlyingMob.class.isAssignableFrom(claz);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    final void updateDamageFromLevel() {
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(getLeveledAttack());
    }

    private double getLeveledAttack() {
        double dmg = getBaseAttack();
        int level = getLevelingStats().getLevel();
        return dmg * (1 + level * npcLevelDamageMultiplier);
    }

    private double getBaseAttack() {
        return getNpcDefault().getBaseAttack();
    }

    public abstract NpcDefault getNpcDefault();

    public int getFoodRemaining() {
        return 0;//NOOP in non-player owned
    }

    public void setFoodRemaining(int food) {
        //NOOP in non-player owned
    }

    public boolean requiresUpkeep() {
        return this instanceof IKeepFood;//NOOP in non-player owned
    }

    @Override
    public void setOwner(Player player) {
        owner = new Owner(player);
    }

    @Override
    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public void setOwnerName(String name) {
        owner = new Owner(level(), name);
        if (!level().isClientSide && !name.equals(owner.getName())) {
            PacketEntity pkt = new PacketEntity(this);
            pkt.packetData = owner.serializeToNBT(new CompoundTag());
            NetworkHandler.sendToAllTracking(this, pkt);
        }
    }

    @Override
    public boolean isOwner(Player player) {
        return owner.isOwnerOrSameTeamOrFriend(player);
    }

    @Override
    public Owner getOwner() {
        return owner;
    }

    @Override
    public Team getTeam() {
        return level().getScoreboard().getPlayersTeam(owner.getName());
    }

    public boolean hasCommandPermissions(Owner owner) {
        return hasCommandPermissions(owner.getUUID(), owner.getName());
    }

    public boolean hasCommandPermissions(UUID playerId, String playerName) {
        return owner.playerHasCommandPermissions(level(), playerId, playerName);
    }

    @Override
    public int getExperienceReward() {
        Player attacker = lastHurtByPlayer;
        if (attacker == null || (isHostileTowards(attacker) && canBeAttackedBy(attacker))) {
            return super.getExperienceReward();
        }
        return 0;
    }

    public abstract boolean isHostileTowards(Entity e);

    public abstract boolean canTarget(Entity e);

    public abstract boolean canBeAttackedBy(Entity e);

    public final LivingEntity getFollowingEntity() {
        if (followingPlayerName == null) {
            return null;
        }
        return level().getServer() == null ? null : level().getServer().getPlayerList().getPlayerByName(followingPlayerName);
    }

    public final void setFollowingEntity(LivingEntity entity) {
        if (entity instanceof Player player && hasCommandPermissions(entity.getUUID(), player.getGameProfile().getName())) {
            followingPlayerName = player.getGameProfile().getName();
        }
    }

    public final void clearFollowingEntity() {
        followingPlayerName = null;
    }

    public void setDoNotPursue(boolean val) {
        doNotPursue = val;
    }

    public boolean getDoNotPursue() {
        return doNotPursue;
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    public final void repackEntity(Player player) {
        // we already hide the button but we need to prevent repack server-side too
        if (AWNPCStatics.repackCreativeOnly && !player.getAbilities().instabuild) {
            return;
        }
        if (!player.level().isClientSide && isAlive()) {
            onRepack();
            //noinspection ConstantConditions
            @Nonnull
            ItemStack item = player.getCapability(ForgeCapabilities.ITEM_HANDLER).map(handler -> InventoryTools.mergeItemStack(handler, getItemToSpawn())).orElse(getItemToSpawn());
            if (!item.isEmpty()) {
                player.spawnAtLocation(item);
            }
        }
        discard();
    }

    /*
     * called when NPC is being repacked into item-form.  Called prior to item being created and prior to entity being set-dead.<br>
     * Main function is for faction-mounted NPCs to disappear their mounts when repacked.
     */
    protected void onRepack() {

    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            setItemSlot(slot, ItemStack.EMPTY);
        }
        super.readAdditionalSaveData(tag);
        if (tag.contains("home")) {
            restrictTo(BlockPos.of(tag.getLong("home")), tag.getInt("homeRange"));
        }
        if (tag.contains(BED_DIRECTION_TAG)) {
            setBedDirection(Direction.from3DDataValue(tag.getByte(BED_DIRECTION_TAG)));
        }
        if (tag.contains(IS_SLEEPING_TAG)) {
            setSleeping(tag.getBoolean(IS_SLEEPING_TAG));
        }
        if (tag.contains(CACHED_BED_POS_TAG)) {
            cachedBedPos = BlockPos.of(tag.getLong(CACHED_BED_POS_TAG));
        }
        if (tag.contains(BED_POS_TAG)) {
            setBedPosition(BlockPos.of(tag.getLong(BED_POS_TAG)));
        }
        if (tag.contains(FOUND_BED_TAG)) {
            foundBed = tag.getBoolean(FOUND_BED_TAG);
        }

        originalWidth = tag.getFloat("originalWidth");
        originalHeight = tag.getFloat("originalHeight");

        readBaseTags(tag);
        onWeaponInventoryChanged();
        onOffhandInventoryChanged();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!hasRestriction()) {
            Optional<BlockPos> position = getTownHallPosition();
            if (position.isPresent()) {
                restrictTo(position.get(), getHomeRange());
            } else {
                setHomeAreaAtCurrentPosition();
            }
        }
        tag.putLong("home", getRestrictCenter().asLong());
        tag.putInt("homeRange", getHomeRange());
        tag.putByte(BED_DIRECTION_TAG, (byte) getBedDirection().ordinal());
        tag.putBoolean(IS_SLEEPING_TAG, isSleeping());
        if (cachedBedPos != null) {
            tag.putLong(CACHED_BED_POS_TAG, cachedBedPos.asLong());
        }
        BlockPos bedPos = getBedPosition();
        tag.putLong(BED_POS_TAG, bedPos.asLong());
        tag.putBoolean(FOUND_BED_TAG, foundBed);
        tag.putFloat("originalWidth", originalWidth);
        tag.putFloat("originalHeight", originalHeight);

        writeBaseTags(tag);
    }

    private void readBaseTags(CompoundTag tag) {
        if (tag.contains(ORDERS_STACK_TAG)) {
            setItemSlot(ORDER_SLOT, ItemStack.of(tag.getCompound(ORDERS_STACK_TAG)));
        }
        if (tag.contains(UPKEEP_STACK_TAG)) {
            setItemSlot(UPKEEP_SLOT, ItemStack.of(tag.getCompound(UPKEEP_STACK_TAG)));
        }
        if (tag.contains(LEVELING_STATS_TAG)) {
            getLevelingStats().readFromNBT(tag.getCompound(LEVELING_STATS_TAG));
        }
        if (tag.contains(MAX_HEALTH_TAG)) {
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(tag.getFloat(MAX_HEALTH_TAG));
        }
        if (tag.contains(HEALTH_TAG)) {
            setHealth(tag.getFloat(HEALTH_TAG));
        }
        if (tag.contains("name")) {
            setCustomName(Component.literal(tag.getString("name")));
        }
        if (tag.contains("food")) {
            setFoodRemaining(tag.getInt("food"));
        }
        if (tag.contains(ATTACK_DAMAGE_OVERRIDE_TAG)) {
            setAttackDamageOverride(tag.getInt(ATTACK_DAMAGE_OVERRIDE_TAG));
        }
        if (tag.contains(ARMOR_VALUE_OVERRIDE_TAG)) {
            setArmorValueOverride(tag.getInt(ARMOR_VALUE_OVERRIDE_TAG));
        }
        if (tag.contains(AI_ENABLED_TAG)) {
            setIsAIEnabled(tag.getBoolean(AI_ENABLED_TAG));
        }
        if (tag.contains(DO_NOT_PURSUE)) {
            setDoNotPursue(tag.getBoolean(DO_NOT_PURSUE));
        }
        if (tag.contains(HAS_CUSTOM_EQUIPMENT_TAG)) {
            setCustomEquipmentOverride(tag.getBoolean(HAS_CUSTOM_EQUIPMENT_TAG));
        }
        setSkinSettings(NpcSkinSettings.deserializeNBT(tag.getCompound("skinSettings")).minimizeData());
        owner = Owner.deserializeFromNBT(tag);
    }

    private void writeBaseTags(CompoundTag tag) {
        if (!ordersStack.isEmpty()) {
            tag.put(ORDERS_STACK_TAG, ordersStack.save(new CompoundTag()));
        }
        if (!upkeepStack.isEmpty()) {
            tag.put(UPKEEP_STACK_TAG, upkeepStack.save(new CompoundTag()));
        }
        tag.put(LEVELING_STATS_TAG, getLevelingStats().writeToNBT(new CompoundTag()));
        tag.putFloat(MAX_HEALTH_TAG, getMaxHealth());
        tag.putFloat(HEALTH_TAG, getHealth());
        if (hasCustomName()) {
            tag.putString("name", getCustomName().getString());
        }
        tag.putInt("food", getFoodRemaining());
        tag.putInt(ATTACK_DAMAGE_OVERRIDE_TAG, attackDamage);
        tag.putInt(ARMOR_VALUE_OVERRIDE_TAG, armorValue);
        tag.putBoolean(AI_ENABLED_TAG, aiEnabled);
        tag.putBoolean(DO_NOT_PURSUE, doNotPursue);
        tag.putBoolean(HAS_CUSTOM_EQUIPMENT_TAG, hasCustomEquipment);
        tag.put("skinSettings", skinSettings.serializeNBT());
        owner.serializeToNBT(tag);
    }

    public final ResourceLocation getTexture() {
        return skinSettings.getTexture(this);
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains("ownerName")) {
            owner = Owner.deserializeFromNBT(tag);
        } else if (tag.contains(NpcSkinSettings.PACKET_TAG_NAME)) {
            skinSettings.handlePacketData(tag);
        } else if (tag.contains(ATTACK_TARGET_TAG)) {
            int entityId = tag.getInt(ATTACK_TARGET_TAG);
            setTarget((LivingEntity) level().getEntity(entityId));
        }
    }

    public double getDistanceSq(BlockPos pos) {
        return distanceToSqr(pos.getX() + 0.5d, pos.getY(), pos.getZ() + 0.5d);
    }

    public BlockPos findBed() {
        if (!foundBed) {
            int originX = Mth.floor(getX());
            int originY = Mth.floor(getY());
            int originZ = Mth.floor(getZ());
            int maxSearchRange = 6;
            int minX = originX - maxSearchRange;
            int maxX = originX + maxSearchRange;
            int minY = originY - maxSearchRange;
            int maxY = originY + maxSearchRange;
            int minZ = originZ - maxSearchRange;
            int maxZ = originZ + maxSearchRange;
            List<BlockPos> foundBeds = new ArrayList<>();
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        BlockState state = level().getBlockState(new BlockPos(x, y, z));
                        if (state.getBlock() instanceof BedBlock) {
                            if (state.getValue(BedBlock.PART) != BedPart.FOOT) {
                                continue;
                            }
                            if (!state.getValue(BedBlock.OCCUPIED)) { // occupied check
                                foundBeds.add(new BlockPos(x, y, z));
                            }
                        }
                    }
                }
            }

            int closetBedIndex = -1;
            for (int i = 0; i < foundBeds.size(); i++) {
                double bedDistance = foundBeds.get(i).distToCenterSqr(originX, originY, originZ);
                if ((closetBedIndex == -1) || (bedDistance < foundBeds.get(closetBedIndex).distToCenterSqr(originX, originY, originZ))) {
                    closetBedIndex = i;
                }
            }
            if (closetBedIndex == -1) {
                foundBed = false;
                return null;
            }
            foundBed = true;
            cachedBedPos = foundBeds.get(closetBedIndex);
        }

        BlockState state = level().getBlockState(cachedBedPos);
        if (state.getBlock() instanceof BedBlock) {
            return cachedBedPos;
        } else {
            foundBed = false;
            return null; // try again in a while
        }
    }

    public boolean lieDown(BlockPos pos) {
        if (!foundBed) {
            return false;
        }
        if (level().hasChunkAt(pos) && level().getBlockState(pos).getBlock() instanceof BedBlock) {
            BlockState state = level().getBlockState(pos);
            if (state.getValue(BedBlock.OCCUPIED)) {
                // occupied check
                foundBed = false;
                return false;
            }
            state = state.setValue(BedBlock.OCCUPIED, true);
            level().setBlock(pos, state, 3);
            setBedPosition(pos);
            setBedDirection(state.getValue(BedBlock.FACING));
            setSleeping(true);
            originalHeight = getBbHeight();
            originalWidth = getBbWidth();
            refreshDimensions();
            setPositionToBed();
            return true;
        }
        return false;
    }

    public void wakeUp() {
        setSleeping(false);
        BlockPos bedPos = getBedPosition();
        // set vacant
        BlockState bedState = level().getBlockState(bedPos);
        if (bedState.getBlock() instanceof BedBlock) {
            level().setBlock(bedPos, bedState.setValue(BedBlock.OCCUPIED, false), 4);
        }

        // Try placing the NPC to an empty spot next to the bed. We don't want them standing on top of the bed, chance for suffocation
        Direction bedDirection = getBedDirection();

        refreshDimensions();

        if (tryMovingToBedside(bedPos.relative(bedDirection.getClockWise()))) {
            return;
        }
        if (tryMovingToBedside(bedPos.relative(bedDirection.getCounterClockWise()))) {
            return;
        }

        BlockPos offsetPos = bedPos.relative(bedDirection.getOpposite());
        if (tryMovingToBedside(offsetPos)) {
            return;
        }
        if (tryMovingToBedside(offsetPos.relative(bedDirection.getClockWise()))) {
            return;
        }
        tryMovingToBedside(offsetPos.relative(bedDirection.getCounterClockWise()));
    }

    private boolean tryMovingToBedside(BlockPos posToMove) {
        if (LegacyMaterial.of(level().getBlockState(posToMove)).blocksMovement()) {
            return false;
        }
        if (LegacyMaterial.of(level().getBlockState(posToMove.above())).blocksMovement()) {
            return false;
        }
        if (!LegacyMaterial.of(level().getBlockState(posToMove.below())).blocksMovement()) {
            return false;
        }
        aiEnabled = false;
        setPos(posToMove.getX() + 0.5, posToMove.getY() + 0.5, posToMove.getZ() + 0.5);
        aiEnabled = true;
        return true;
    }

    private BlockPos getBedPosition() {
        return entityData.get(BED_POS);
    }

    private void setBedPosition(BlockPos pos) {
        entityData.set(BED_POS, pos);
    }

    public void setPositionToBed() {
        float xOffset = 0.5F;
        float yOffset = 0.6F;
        float zOffset = 0.5F;

        setPos(cachedBedPos.getX() + xOffset, cachedBedPos.getY() + yOffset, cachedBedPos.getZ() + zOffset);
    }

    @Override
    public void travel(Vec3 movement) {
        if (isSleeping()) {
            setJumping(false);
            super.travel(Vec3.ZERO);
        } else {
            super.travel(movement);
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return isSleeping() ? EntityDimensions.scalable(0.2F, 0.2F) : super.getDimensions(pose);
    }

    public boolean isBedCacheValid() {
        return (level().getBlockState(cachedBedPos).getBlock() instanceof BedBlock);
    }

    @Override
    public void playerTouch(Player player) {
    }

    @Override
    public boolean isPushable() {
        return (!isSleeping());
    }

    @Override
    public void push(Entity entity) {
        if (!isSleeping()) super.push(entity);
    }

    private void setSleeping(boolean isSleeping) {
        entityData.set(IS_SLEEPING, isSleeping);
    }

    public boolean isSleeping() {
        return entityData == null ? false : entityData.get(IS_SLEEPING);
    }

    private void setBedDirection(Direction direction) {
        entityData.set(BED_DIRECTION, (byte) direction.ordinal());
    }

    public Direction getBedDirection() {
        return Direction.from3DDataValue(entityData.get(BED_DIRECTION));
    }

    public boolean shouldSleep() {
        return !WorldTools.isDaytimeInDimension(level());
    }

    // Only used by the renderer
    @OnlyIn(Dist.CLIENT)
    public float getBedOrientationInDegrees() {
        BlockPos bedLocation = getBedPosition();
        BlockState state = bedLocation == null ? null : level().getBlockState(bedLocation);
        if (state != null && state.isBed(level(), bedLocation, this)) {
            Direction enumfacing = state.getBedDirection(level(), bedLocation);

            switch (enumfacing) {
                case SOUTH:
                    return 90.0F;
                case WEST:
                    return 0.0F;
                case NORTH:
                    return 270.0F;
                case EAST:
                    return 180.0F;
            }
        }

        return 0.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isSwingingArms() {
        return entityData.get(SWINGING_ARMS);
    }

    public void setSwingingArms(boolean swingingArms) {
        entityData.set(SWINGING_ARMS, swingingArms);
    }

    //1.12 wrote the public activeItemStackUseCount field directly; bridge the protected 1.20 field.
    public void setUseItemRemainingTicks(int ticks) {
        useItemRemaining = ticks;
    }

    public int getShieldDisabledTick() {
        return entityData.get(SHIELD_DISABLED_TICK);
    }

    public void setShieldDisabledTick(int count) {
        entityData.set(SHIELD_DISABLED_TICK, count);
    }

    public void decrementShieldDisabledTick() {
        entityData.set(SHIELD_DISABLED_TICK, (entityData.get(SHIELD_DISABLED_TICK)) - 1);
    }

    // Vehicle-specific path delegation is isolated behind IPathableEntity methods below.
    @Nullable
    private VehicleBase getRidingVehicle() {
        if (getVehicle() instanceof VehicleBase vehicle) {
            return vehicle;
        }
        return null;
    }

    @Override
    public void setPath(List<Node> path) {
        VehicleBase vehicle = getRidingVehicle();
        if (vehicle != null) {
            vehicle.nav.forcePath(path);
        } else if (path == null || path.isEmpty()) {
            getNavigation().stop();
        } else {
            List<net.minecraft.world.level.pathfinder.Node> vanillaNodes = new ArrayList<>(path.size());
            for (Node node : path) {
                vanillaNodes.add(new net.minecraft.world.level.pathfinder.Node(node.x, node.y, node.z));
            }
            BlockPos target = path.get(path.size() - 1).getPos();
            getNavigation().moveTo(new Path(vanillaNodes, target, true), getDefaultMoveSpeed());
        }
    }

    @Override
    public void setMoveTo(double x, double y, double z, float moveSpeed) {
        getMoveControl().setWantedPosition(x, y, z, moveSpeed);
    }

    @Override
    public float getDefaultMoveSpeed() {
        return 1f;
    }

    @Override
    public boolean isPathableEntityOnLadder() {
        return onClimbable();
    }

    @Override
    public Entity getEntity() {
        return this;
    }

    @Override
    public void onStuckDetected() {
        //noop
    }

    public boolean isFemale() {
        return false;
    }

    public NpcSkinSettings getSkinSettings() {
        return skinSettings;
    }

    public void setSkinSettings(NpcSkinSettings skinSettings) {
        this.skinSettings = skinSettings;
        this.skinSettings.onNpcSet(this);
    }

    public boolean isBow(Item item) {
        return item instanceof BowItem;
    }

    private NpcAIBlockWithShield initShieldAI() {
        if (shieldBlockAI == null) {
            shieldBlockAI = new NpcAIBlockWithShield(this);
        }
        return shieldBlockAI;
    }
}
