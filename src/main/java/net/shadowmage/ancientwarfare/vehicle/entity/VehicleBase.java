package net.shadowmage.ancientwarfare.vehicle.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.entity.AWEntityRegistry;
import net.shadowmage.ancientwarfare.core.owner.IOwnable;
import net.shadowmage.ancientwarfare.core.owner.Owner;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.MathUtils;
import net.shadowmage.ancientwarfare.core.util.Trig;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.npc.entity.faction.NpcFactionSiegeEngineer;
import net.shadowmage.ancientwarfare.vehicle.AncientWarfareVehicles;
import net.shadowmage.ancientwarfare.vehicle.VehicleVarHelpers.DummyVehicleHelper;
import net.shadowmage.ancientwarfare.vehicle.armors.IVehicleArmor;
import net.shadowmage.ancientwarfare.vehicle.config.AWVehicleStatics;
import net.shadowmage.ancientwarfare.vehicle.entity.materials.IVehicleMaterial;
import net.shadowmage.ancientwarfare.vehicle.entity.types.VehicleType;
import net.shadowmage.ancientwarfare.vehicle.helpers.*;
import net.shadowmage.ancientwarfare.vehicle.inventory.VehicleInventory;
import net.shadowmage.ancientwarfare.vehicle.missiles.AmmoHwachaRocket;
import net.shadowmage.ancientwarfare.vehicle.missiles.IAmmo;
import net.shadowmage.ancientwarfare.vehicle.pathing.Navigator;
import net.shadowmage.ancientwarfare.vehicle.pathing.Node;
import net.shadowmage.ancientwarfare.vehicle.pathing.PathWorldAccessEntity;
import net.shadowmage.ancientwarfare.vehicle.registry.VehicleRegistry;
import net.shadowmage.ancientwarfare.vehicle.upgrades.IVehicleUpgradeType;

import javax.annotation.Nullable;
import java.util.List;

public class VehicleBase extends Entity implements IEntityAdditionalSpawnData, IMissileHitCallback, IPathableEntity, IOwnable {

    private static final EntityDataAccessor<Float> VEHICLE_HEALTH = SynchedEntityData.defineId(VehicleBase.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Byte> FORWARD_INPUT = SynchedEntityData.defineId(VehicleBase.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> STRAFE_INPUT = SynchedEntityData.defineId(VehicleBase.class, EntityDataSerializers.BYTE);

    /**
     * these are the current max stats.  set from setVehicleType().
     * these are local cached bases, after application of material factors
     * should not be altered at all after vehicle is first initialized
     */
    public float baseForwardSpeed;
    public float baseStrafeSpeed;
    public float basePitchMin;
    public float basePitchMax;
    private float baseTurretRotationMax;
    private float baseLaunchSpeedMax;
    public float baseHealth = 100;
    private float baseAccuracy = 1.f;
    public float baseWeight = 1000;//kg
    private int baseReloadTicks = 100;
    private float baseGenericResist = 0.f;
    private float baseFireResist = 0.f;
    private float baseExplosionResist = 0.f;
    private int hurtInvulTicks = 0;

    /**
     * local current stats, fully updated and modified from upgrades/etc. should not be altered aside from
     * upgrades/armor
     */
    public float currentForwardSpeedMax = 0.42f;
    public float currentStrafeSpeedMax = 2.0f;

    /**
     * how many ticks is a reloadCycle, at current upgrade status?
     * the currentReload status is stored in firingHelper
     */
    public int currentReloadTicks = 100;
    public float currentTurretPitchMin = 0.f;
    public float currentTurretPitchMax = 90.f;
    public float currentLaunchSpeedPowerMax = 32.321f;
    public float currentGenericResist = 0.f;
    public float currentFireResist = 0.f;
    public float currentExplosionResist = 0.f;
    public float currentWeight = 1000.f;
    public float currentTurretPitchSpeed = 0.f;
    public float currentTurretYawSpeed = 0.f;
    public float currentAccuracy = 1.f;
    public float currentTurretRotationMax = 45.f;

    /**
     * local variables, may be altered by input/etc...
     */
    public float localTurretRotationHome = 0.f;
    public float localTurretRotation = 0.f;
    public float localTurretDestRot = 0.f;
    private float localTurretRotInc = 1.f;
    public float localTurretPitch = 45.f;
    public float localTurretDestPitch = 45.f;
    private float localTurretPitchInc = 1.f;
    public float localLaunchPower = 31.321f;

    /**
     * set by move helper on movement update. used during client rendering to update
     * wheel rotation and other movement speed based animations (airplanes use for prop,
     * helicopter uses for main and tail rotors).
     */
    public float wheelRotation = 0.f;
    public float wheelRotationPrev = 0.f;

    /**
     * used to determine if it should allow interaction (setup time on vehicle placement)
     */
    private boolean isSettingUp = false;

    /**
     * set client-side when incoming damage is taken
     */
    public int hitAnimationTicks = 0;

    private NpcBase assignedRider = null;

    /**
     * complex stat tracking helpers, move, ammo, upgrades, general stats
     */
    public VehicleAmmoHelper ammoHelper;
    public VehicleUpgradeHelper upgradeHelper;
    public VehicleMoveHelper moveHelper;
    public VehicleFiringHelper firingHelper;
    public VehicleFiringVarsHelper firingVarsHelper;
    public VehicleInventory inventory;
    public Navigator nav;
    public PathWorldAccessEntity worldAccess;
    public IVehicleType vehicleType = VehicleRegistry.CATAPULT_STAND_FIXED;//set to dummy vehicle so it is never null...
    public int vehicleMaterialLevel = 0;//the current material level of this vehicle. should be read/set prior to calling updateBaseStats
    private Owner owner = Owner.EMPTY;
    private LazyOptional<net.minecraftforge.items.IItemHandler> itemHandlerCapability = LazyOptional.empty();

    public VehicleBase(Level par1World) {
        this((EntityType<?>) AWEntityRegistry.currentConstructionType(), par1World);
    }

    public VehicleBase(EntityType<?> type, Level level) {
        super(type, level);
        this.upgradeHelper = new VehicleUpgradeHelper(this);
        this.moveHelper = new VehicleMoveHelper(this);
        this.ammoHelper = new VehicleAmmoHelper(this);
        this.firingHelper = new VehicleFiringHelper(this);
        this.firingVarsHelper = new DummyVehicleHelper(this);
        this.inventory = new VehicleInventory(this);
        this.worldAccess = new PathWorldAccessEntity(level, this);
        this.nav = new Navigator(this);
        this.nav.setStuckCheckTicks(100);
        this.setOnGround(false);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(VEHICLE_HEALTH, 100f);
        entityData.define(FORWARD_INPUT, (byte) 0);
        entityData.define(STRAFE_INPUT, (byte) 0);
    }

    @Override
    public ItemStack getPickedResult(HitResult target) {
        return this.vehicleType.getStackForLevel(vehicleMaterialLevel);
    }

    /**
     * overriden to help with vision checks for vehicles
     */
    @Override
    protected float getEyeHeight(Pose pose, EntityDimensions dimensions) {
        return 1.6F;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(vehicleType.getWidth(), vehicleType.getHeight());
    }

    public void setHealth(float health) {
        if (health > this.baseHealth) {
            health = this.baseHealth;
        }
        entityData.set(VEHICLE_HEALTH, health);
    }

    public boolean canTurretTurn() {
        return !MathUtils.epsilonEquals(baseTurretRotationMax, 0);
    }

    public float getHealth() {
        return entityData.get(VEHICLE_HEALTH);
    }

    public void setVehicleType(IVehicleType vehicle, int materialLevel) {
        this.vehicleType = vehicle;
        this.vehicleMaterialLevel = materialLevel;
        VehicleFiringVarsHelper help = vehicle.getFiringVarsHelper(this);
        if (help != null) {
            this.firingVarsHelper = help;
        }
        this.refreshDimensions();
        for (IAmmo ammo : vehicleType.getValidAmmoTypes()) {
            this.ammoHelper.addUseableAmmo(ammo);
        }
        for (IVehicleUpgradeType up : this.vehicleType.getValidUpgrades()) {
            this.upgradeHelper.addValidUpgrade(up);
        }
        for (IVehicleArmor armor : this.vehicleType.getValidArmors()) {
            this.upgradeHelper.addValidArmor(armor);
        }
        this.inventory.setInventorySizes(vehicle.getUpgradeBaySize(), vehicle.getAmmoBaySize(), vehicle.getArmorBaySize(), vehicle.getStorageBaySize());
        this.updateBaseStats();
        this.resetCurrentStats();

        if (this.localTurretPitch < currentTurretPitchMin) {
            this.localTurretPitch = currentTurretPitchMin;
        } else if (this.localTurretPitch > currentTurretPitchMax) {
            this.localTurretPitch = currentTurretPitchMax;
        }
        this.localLaunchPower = this.firingHelper.getAdjustedMaxMissileVelocity();
        if (!this.canAimRotate()) {
            this.localTurretRotation = this.getYRot();
        }
        this.nav.setCanGoOnLand(vehicleType.getMovementType() == VehicleMovementType.GROUND);
    }

    private int setupTicks = 0;

    public void setSetupState(boolean state, int ticks) {
        this.isSettingUp = state;
        if (state) {
            setupTicks = ticks;
        } else {
            setupTicks = 0;
        }
    }

    public void setInitialHealth() {
        this.setHealth(this.baseHealth);
    }

    private void updateBaseStats() {
        IVehicleMaterial material = vehicleType.getMaterialType();
        int level = this.vehicleMaterialLevel;
        baseForwardSpeed = vehicleType.getBaseForwardSpeed() * material.getSpeedForwardFactor(level);
        baseStrafeSpeed = vehicleType.getBaseStrafeSpeed() * material.getSpeedStrafeFactor(level);
        basePitchMin = vehicleType.getBasePitchMin();
        basePitchMax = vehicleType.getBasePitchMax();
        baseTurretRotationMax = vehicleType.getBaseTurretRotationAmount();
        baseLaunchSpeedMax = vehicleType.getBaseMissileVelocityMax();
        baseHealth = vehicleType.getBaseHealth() * material.getHPFactor(level);
        baseAccuracy = vehicleType.getBaseAccuracy() * material.getAccuracyFactor(level);
        baseWeight = vehicleType.getBaseWeight() * material.getWeightFactor(level);
        baseExplosionResist = 0.f;
        baseFireResist = 0.f;
        baseGenericResist = 0.f;
        if (getHealth() > baseHealth) {
            this.setHealth(baseHealth);
        }
    }

    @Override
    public void playerTouch(Player player) {
        // Player collision is intentionally handled by normal entity pushing. The old
        // unfinished flight-ability experiment never changed state here.
    }

    /**
     * return an itemStack tagged appropriately for this vehicle
     *
     * @return
     */
    private ItemStack getItemForVehicle() {
        ItemStack stack = this.vehicleType.getStackForLevel(vehicleMaterialLevel);
        stack.getTag().getCompound("spawnData").putFloat("health", getHealth());
        return stack;
    }

    private float getHorizontalMissileOffset() {
        return this.vehicleType.getMissileHorizontalOffset();
    }

    private float getVerticalMissileOffset() {
        return this.vehicleType.getMissileVerticalOffset();
    }

    private float getForwardsMissileOffset() {
        return this.vehicleType.getMissileForwardsOffset();
    }

    public boolean isAimable() {
        return !this.isSettingUp && vehicleType.isCombatEngine();
    }

    public boolean canAimRotate() {
        return !this.isSettingUp && vehicleType.canAdjustYaw();
    }

    public boolean canAimPitch() {
        return !this.isSettingUp && vehicleType.canAdjustPitch();
    }

    public boolean canAimPower() {
        return !this.isSettingUp && vehicleType.canAdjustPower();
    }

    /**
     * used by inputHelper to determine if it should check movement input keys and send info to server..
     *
     * @return
     */
    public boolean isDrivable() {
        return !this.isSettingUp && vehicleType.isDrivable();
    }

    public boolean isMountable() {
        return !this.isSettingUp && vehicleType.isMountable();
    }

    private float getRiderForwardOffset() {
        return vehicleType.getRiderForwardsOffset();
    }

    public float getRiderVerticalOffset() {
        return vehicleType.getRiderVerticalOffest();
    }

    private float getRiderHorizontalOffset() {
        return vehicleType.getRiderHorizontalOffset();
    }

    /**
     * should return the maximum range allowed in order to hit a point at a given vertical offset
     * will vary by vehicle type (power/angle/missile offset) and current ammo selection
     * need to figure out....yah....
     *
     * @param verticalOffset
     * @return
     */
    public float getEffectiveRange(float verticalOffset) {
        if (vehicleType.isCombatEngine() && vehicleType.getValidAmmoTypes().isEmpty()) {
            return vehicleType.getMinAttackDistance() + 0.5F;
        }
        float angle;
        if (currentTurretPitchMin < 45 && currentTurretPitchMax > 45)//if the angle stradles 45, return 45
        {
            angle = 45;
        } else if (currentTurretPitchMin < 45 && currentTurretPitchMax < 45)//else if both are below 45, get the largest
        {
            angle = currentTurretPitchMax;
        } else {
            angle = currentTurretPitchMin;//else get the lowest
        }
        return getEffectiveRange(verticalOffset, angle, firingHelper.getAdjustedMaxMissileVelocity(), ammoHelper.isCurrentAmmoRocket());
    }

    private float getEffectiveRange(float y, float angle, float velocity, boolean rocket) {
        float motX = Trig.sinDegrees(angle) * velocity * 0.05f;
        float motY = Trig.cosDegrees(angle) * velocity * 0.05f;
        float rocketX = 0;
        float rocketY = 0;
        if (rocket) {
            int rocketBurnTime = (int) (velocity * AmmoHwachaRocket.BURN_TIME_FACTOR);
            float motX0 = (motX / (velocity * 0.05f)) * AmmoHwachaRocket.ACCELERATION_FACTOR;
            float motY0 = (motY / (velocity * 0.05f)) * AmmoHwachaRocket.ACCELERATION_FACTOR;
            motX = motX0;
            motY = motY0;
            while (rocketBurnTime > 0) {
                rocketX += motX;
                rocketY += motY;
                rocketBurnTime--;
                motX += motX0;
                motY += motY0;
            }
            y -= rocketY;
        }
        motX *= 20.f;
        motY *= 20.f;
        float gravity = 9.81f;
        float t = motY / gravity;
        float tQ = Mth.sqrt(((motY * motY) / (gravity * gravity)) - ((2 * y) / gravity));
        float tPlus = t + tQ;
        float tMinus = t - tQ;
        t = tPlus > tMinus ? tPlus : tMinus;
        return (motX * t) + rocketX;
    }

    /**
     * get a fully translated offset position for missile spawn for the current aim and vehicle params
     *
     * @return
     */
    public Vec3 getMissileOffset() {
        float x1 = this.vehicleType.getTurretPosX();
        float y1 = this.vehicleType.getTurretPosY();
        float z1 = this.vehicleType.getTurretPosZ();
        float angle = 0;
        float len = 0;
        if (x1 != 0 || z1 != 0) {
            angle = Trig.toDegrees((float) Math.atan2(z1, x1));
            len = Mth.sqrt(x1 * x1 + z1 * z1);
            angle += this.getYRot();
            x1 = Trig.cosDegrees(angle) * len;
            z1 = -Trig.sinDegrees(angle) * len;
        }

        float x = this.getHorizontalMissileOffset();
        float y = this.getVerticalMissileOffset();
        float z = this.getForwardsMissileOffset();
        if (x != 0 || z != 0 || y != 0) {
            angle = Trig.toDegrees((float) Math.atan2(z, x));
            len = Mth.sqrt(x * x + z * z + y * y);
            angle += this.localTurretRotation;
            x = Trig.cosDegrees(angle) * Trig.sinDegrees(localTurretPitch + getXRot()) * len;
            z = -Trig.sinDegrees(angle) * Trig.sinDegrees(localTurretPitch + getXRot()) * len;
            y = Trig.cosDegrees(localTurretPitch + getXRot()) * len;
        }
        x += x1;
        z += z1;
        y += y1;
        return new Vec3(x, y, z);
    }

    /**
     * called on every tick that the vehicle is 'firing' to update the firing animation and to call
     * launchMissile when animation has reached launch point
     */
    public void onFiringUpdate() {
        this.firingVarsHelper.onFiringUpdate();
    }

    /**
     * called every tick after the vehicle has fired, until reload timer is complete, to update animations
     */
    public void onReloadUpdate() {
        this.firingVarsHelper.onReloadUpdate();
    }

    protected void onInsideBlock(BlockState state, BlockPos pos) {
        if (state.getBlock() == Blocks.LILY_PAD) {
            level().destroyBlock(pos, true);
        }
    }

    /**
     * called every tick after startLaunching() is called, until setFinishedLaunching() is called...
     */
    public void onLaunchingUpdate() {
        this.firingVarsHelper.onLaunchingUpdate();
    }

    /**
     * reset all upgradeable stats back to the base for this vehicle
     */
    public void resetCurrentStats() {
        this.currentForwardSpeedMax = this.baseForwardSpeed;
        this.currentStrafeSpeedMax = this.baseStrafeSpeed;
        this.currentTurretPitchMin = this.basePitchMin;
        this.currentTurretPitchMax = this.basePitchMax;
        this.currentTurretRotationMax = this.baseTurretRotationMax;
        this.currentReloadTicks = this.baseReloadTicks;
        this.currentLaunchSpeedPowerMax = this.baseLaunchSpeedMax;
        this.currentExplosionResist = this.baseExplosionResist;
        this.currentFireResist = this.baseFireResist;
        this.currentGenericResist = this.baseGenericResist;
        this.currentWeight = this.baseWeight;
        this.currentAccuracy = this.baseAccuracy;
    }

    public void setDead() {
        if (!level().isClientSide && !isRemoved() && getHealth() <= 0) {
            InventoryTools.dropItemsInWorld(level(), inventory.ammoInventory, getX(), getY(), getZ());
            InventoryTools.dropItemsInWorld(level(), inventory.armorInventory, getX(), getY(), getZ());
            InventoryTools.dropItemsInWorld(level(), inventory.upgradeInventory, getX(), getY(), getZ());
            InventoryTools.dropItemsInWorld(level(), inventory.storageInventory, getX(), getY(), getZ());
        }
        discard();
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return !getPassengers().isEmpty() && getPassengers().get(0) instanceof LivingEntity living ? living : null;
    }

    private void normalizeYaw() {
        setYRot(Mth.wrapDegrees(getYRot()));
    }

    @Override
    public void tick() {
        long tickStart = AncientWarfareVehicles.LOG.isDebugEnabled() ? System.nanoTime() : 0L;
        super.tick();
        normalizeYaw();
        if (level().isClientSide) {
            this.onUpdateClient();
        } else {
            this.onUpdateServer();
        }
        this.updateTurretPitch();
        this.updateTurretRotation();
        this.moveHelper.onUpdate();
        this.firingHelper.onTick();
        this.firingVarsHelper.onTick();
        if (this.hitAnimationTicks > 0) {
            this.hitAnimationTicks--;
        }
        if (this.isSettingUp) {
            this.setupTicks--;
            if (this.setupTicks <= 0) {
                this.isSettingUp = false;
            }
        }
        if (this.hurtInvulTicks > 0) {
            this.hurtInvulTicks--;
        }
        if (this.assignedRider != null) {
            int searchRange = AWVehicleStatics.generalSettings.assignedRiderSearchRange;
            if (assignedRider.isRemoved() || assignedRider.getVehicle() != this || !assignedRider.isPassenger()
                    || this.distanceToSqr(assignedRider) > (double) searchRange * searchRange) {
                this.assignedRider = null;
            }
        }
        if (tickStart != 0L) {
            long elapsed = System.nanoTime() - tickStart;
            if (elapsed > 10_000_000L) {
                AncientWarfareVehicles.LOG.debug("Slow vehicle tick: {} took {} ms", getId(), elapsed / 1_000_000.0D);
            }
        }
    }

    /**
     * client-side updates
     */
    private void onUpdateClient() {
        if (getControllingPassenger() instanceof NpcBase) {
            this.updatePassenger(getControllingPassenger());
        }
    }

    /**
     * server-side updates...
     */
    private void onUpdateServer() {
        if (this.getControllingPassenger() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) this.getControllingPassenger();
            if (player.isShiftKeyDown()) {
                this.handleDismount(player);
                player.setShiftKeyDown(false);
            }
        }
    }

    private void handleDismount(LivingEntity rider) {
        float width = getBbWidth();
        int xMin = Mth.floor(this.getX() - width / 2);
        int zMin = Mth.floor(this.getZ() - width / 2);
        int yMin = Mth.floor(getY()) - 2;

        if (rider instanceof ServerPlayer player) player.getAbilities().flying = false;
        rider.stopRiding();

        searchLabel:
        for (int y = yMin; y <= yMin + 3; y++) {
            for (int x = xMin; x <= xMin + (int) width; x++) {
                for (int z = zMin; z <= zMin + (int) width; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level().getBlockState(pos);
                    if (state.isFaceSturdy(level(), pos, Direction.UP) || LegacyMaterial.of(state) == LegacyMaterial.WATER) {
                        if (level().isEmptyBlock(pos.above()) && level().isEmptyBlock(pos.above(2))) {
                            rider.teleportTo(x + 0.5d, y + 1, z + 0.5d);
                            break searchLabel;
                        }
                    }
                }
            }
        }
    }

    private void updateTurretPitch() {
        float prevPitch = this.localTurretPitch;
        if (!Trig.isAngleBetween(localTurretPitch, currentTurretPitchMin, currentTurretPitchMax)) {
            localTurretPitch = currentTurretPitchMin;
        }

        if (!Trig.isAngleBetween(localTurretDestPitch, currentTurretPitchMin, currentTurretPitchMax)) {
            localTurretDestPitch = currentTurretPitchMin;
        }

        if (!canAimPitch()) {
            localTurretDestPitch = localTurretPitch;
        }

        if (!Trig.anglesEqual(localTurretPitch, localTurretDestPitch)) {
            if (Math.abs(Trig.getAngleDiffSigned(localTurretDestPitch, localTurretPitch)) < localTurretPitchInc) {
                localTurretPitch = localTurretDestPitch;
            } else {
                localTurretPitch += Trig.getAngleDiffSigned(localTurretPitch, localTurretDestPitch) > 0 ? localTurretPitchInc : -localTurretPitchInc;
            }
        }
        this.currentTurretPitchSpeed = prevPitch - this.localTurretPitch;
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (passenger instanceof NpcFactionSiegeEngineer) {
            currentTurretPitchMin = vehicleType.getBasePitchMin() - 4 * 3;
            currentTurretPitchMax = vehicleType.getBasePitchMax() + 4 * 3;
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (passenger instanceof NpcFactionSiegeEngineer) {
            upgradeHelper.updateUpgradeStats();
        }
    }

    private void updateTurretRotation() {
        float prevYaw = this.localTurretRotation;
        this.localTurretRotationHome = Trig.wrapTo360(this.getYRot());
        if (!canAimRotate()) {
            localTurretRotation = Trig.wrapTo360(this.getYRot());
            localTurretDestRot = localTurretRotation;
        }
        if (Math.abs(localTurretDestRot - localTurretRotation) > localTurretRotInc) {
            while (localTurretRotation < 0) {
                localTurretRotation += 360;
                prevYaw += 360;
            }
            while (localTurretRotation >= 360) {
                localTurretRotation -= 360;
                prevYaw -= 360;
            }
            localTurretDestRot = Trig.wrapTo360(localTurretDestRot);
            float curMod = localTurretRotation;
            float destMod = localTurretDestRot;
            float diff = curMod > destMod ? curMod - destMod : destMod - curMod;
            float turnDir = 0;
            if (curMod > destMod) {
                if (diff < 180) {
                    turnDir = -1;
                } else {
                    turnDir = 1;
                }
            } else if (curMod < destMod) {
                if (diff < 180) {
                    turnDir = 1;
                } else {
                    turnDir = -1;
                }
            }
            localTurretRotation += localTurretRotInc * turnDir;
        } else {
            localTurretRotation = localTurretDestRot;
        }
        if (Math.abs(localTurretDestRot - localTurretRotation) < localTurretRotInc) {
            localTurretRotation = localTurretDestRot;
        }
        this.currentTurretYawSpeed = this.localTurretRotation - prevYaw;
        if (this.currentTurretYawSpeed > 180) {
            this.currentTurretYawSpeed -= 360.f;
        }
        if (this.currentTurretYawSpeed < -180) {
            this.currentTurretYawSpeed += 360.f;
        }
    }

    public void updateTurretAngles(float pitch, float rotation) {
        this.localTurretPitch = pitch;
        this.localTurretRotation = rotation;
        this.localTurretDestPitch = this.localTurretPitch;
        this.localTurretDestRot = this.localTurretRotation;
    }

    /**
     * spits out inventory into world, and packs the vehicle into an item, also spat into the world
     */
    public void packVehicle() {
        if (!level().isClientSide) {
            InventoryTools.dropItemInWorld(level(), getItemForVehicle(), getX(), getY(), getZ());
            InventoryTools.dropItemsInWorld(level(), inventory.ammoInventory, getX(), getY(), getZ());
            InventoryTools.dropItemsInWorld(level(), inventory.armorInventory, getX(), getY(), getZ());
            InventoryTools.dropItemsInWorld(level(), inventory.upgradeInventory, getX(), getY(), getZ());
            InventoryTools.dropItemsInWorld(level(), inventory.storageInventory, getX(), getY(), getZ());
            this.discard();
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (!damageSource.is(DamageTypeTags.IS_EXPLOSION)) {
            if (hurtInvulTicks > 0) {
                return false;
            }
            hurtInvulTicks = 10;
        }

        if (level().isClientSide) {
            hitAnimationTicks = 20;
            return false;
        }
        super.hurt(damageSource, amount);
        float adjDmg = upgradeHelper.getScaledDamage(damageSource, amount);
        this.setHealth(getHealth() - adjDmg);
        if (getHealth() <= 0) {
            setDead();
            return false;
        }
        return true;
    }

    @Override
    public void push(Entity entity) {
        if (entity != getControllingPassenger() && !(entity instanceof NpcBase))//skip if it if it is the rider
        {
            double xDiff = entity.getX() - this.getX();
            double zDiff = entity.getZ() - this.getZ();
            double entityDistance = Mth.absMax(xDiff, zDiff);

            if (entityDistance >= 0.009999999776482582D) {
                entityDistance = Math.sqrt(entityDistance);
                xDiff /= entityDistance;
                zDiff /= entityDistance;
                double normalizeToDistance = 1.0D / entityDistance;

                if (normalizeToDistance > 1.0D) {
                    normalizeToDistance = 1.0D;
                }

                xDiff *= normalizeToDistance;
                zDiff *= normalizeToDistance;
                xDiff *= 0.05000000074505806D;//wtf..normalize to ticks?
                zDiff *= 0.05000000074505806D;
                xDiff *= 0.1D;
                zDiff *= 0.1D;
                this.push(-xDiff, 0.0D, -zDiff);
                entity.push(xDiff, 0.0D, zDiff);
            }
        }
    }

    public ResourceLocation getTexture() {
        return vehicleType.getTextureForMaterialLevel(vehicleMaterialLevel);
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (!hasPassenger(passenger)) return;
        double posX = this.getX();
        double posY = this.getY() + this.getRiderVerticalOffset();
        double posZ = this.getZ();

        float yaw = this.vehicleType.moveRiderWithTurret() ? localTurretRotation : getYRot();
        posX += Trig.sinDegrees(yaw) * -this.getRiderForwardOffset();
        posX += Trig.sinDegrees(yaw + 90) * this.getRiderHorizontalOffset();
        posZ += Trig.cosDegrees(yaw) * -this.getRiderForwardOffset();
        posZ += Trig.cosDegrees(yaw + 90) * this.getRiderHorizontalOffset();
        if (passenger instanceof NpcBase) {
            moveFunction.accept(passenger, posX, posY + passenger.getMyRidingOffset(), posZ);
            passenger.setYRot(180 - localTurretRotation);
            if (passenger instanceof LivingEntity living) living.setYBodyRot(180 - localTurretRotation);
        } else {
            moveFunction.accept(passenger, posX, posY + passenger.getMyRidingOffset(), posZ);
            passenger.setYRot(passenger.getYRot() - this.moveHelper.getRotationSpeed());
        }
    }

    public void updatePassenger(Entity passenger) {
        positionRider(passenger, Entity::setPos);
    }

    @Override
    public net.minecraft.world.InteractionResult interact(Player player, InteractionHand hand) {
        if (this.isSettingUp) {
            if (!player.level().isClientSide) {
                player.sendSystemMessage(Component.literal("Vehicle is currently being set-up. It has " + setupTicks + " ticks remaining."));
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        return this.firingVarsHelper.interact(player) ? net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide) : net.minecraft.world.InteractionResult.PASS;
    }

    @Override
    public String toString() {
        Vec3 velocity = getDeltaMovement();
        return String.format("%s::%s @ %.2f, %.2f, %.2f -- y:%.2f p:%.2f -- m: %.2f, %.2f, %.2f", vehicleType.getDisplayName(), getId(), getX(), getY(), getZ(), getYRot(), getXRot(), velocity.x, velocity.y, velocity.z);
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
    }

    @Override
    public boolean shouldRiderSit() {
        return this.vehicleType.shouldRiderSit();
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf pb) {
        pb.writeFloat(this.getHealth());
        pb.writeInt(this.vehicleType.getGlobalVehicleType());
        pb.writeInt(this.vehicleMaterialLevel);
        pb.writeNbt(upgradeHelper.serializeNBT());
        pb.writeNbt(ammoHelper.serializeNBT());
        pb.writeNbt(moveHelper.serializeNBT());
        pb.writeNbt(firingHelper.serializeNBT());
        pb.writeNbt(firingVarsHelper.serializeNBT());
        pb.writeFloat(localLaunchPower);
        pb.writeFloat(localTurretPitch);
        pb.writeFloat(localTurretRotation);
        pb.writeFloat(localTurretDestPitch);
        pb.writeFloat(localTurretDestRot);
        owner.serializeToBuffer(pb);
        pb.writeFloat(localTurretRotationHome);
        pb.writeBoolean(this.isSettingUp);
        if (this.isSettingUp) {
            pb.writeInt(this.setupTicks);
        }
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket() {
        return net.minecraftforge.network.NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf pb) {
        this.setHealth(pb.readFloat());
        IVehicleType type = VehicleType.getVehicleType(pb.readInt());
        this.setVehicleType(type, pb.readInt());
        this.upgradeHelper.deserializeNBT(pb.readNbt());
        this.ammoHelper.deserializeNBT(pb.readNbt());
        this.moveHelper.deserializeNBT(pb.readNbt());
        this.firingHelper.deserializeNBT(pb.readNbt());
        this.firingVarsHelper.deserializeNBT(pb.readNbt());
        this.localLaunchPower = pb.readFloat();
        this.localTurretPitch = pb.readFloat();
        this.localTurretRotation = pb.readFloat();
        this.localTurretDestPitch = pb.readFloat();
        this.localTurretDestRot = pb.readFloat();
        this.firingHelper.clientLaunchSpeed = localLaunchPower;
        this.firingHelper.clientTurretPitch = localTurretPitch;
        this.firingHelper.clientTurretYaw = localTurretRotation;
        this.upgradeHelper.updateUpgradeStats();
        owner = new Owner(pb);
        this.localTurretRotationHome = pb.readFloat();
        this.isSettingUp = pb.readBoolean();
        if (this.isSettingUp) {
            this.setupTicks = pb.readInt();
        }
        this.refreshDimensions();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        IVehicleType vehType = VehicleType.getVehicleType(tag.getInt("vehType"));
        int level = tag.getInt("matLvl");
        this.setVehicleType(vehType, level);
        this.setHealth(tag.getFloat("health"));
        this.localTurretRotationHome = tag.getFloat("turHome");
        this.inventory.readFromNBT(tag);
        this.upgradeHelper.deserializeNBT(tag.getCompound("upgrades"));
        this.ammoHelper.deserializeNBT(tag.getCompound("ammo"));
        this.moveHelper.deserializeNBT(tag.getCompound("move"));
        this.firingHelper.deserializeNBT(tag.getCompound("fire"));
        this.firingVarsHelper.deserializeNBT(tag.getCompound("vars"));
        this.localLaunchPower = tag.getFloat("lc");
        this.localTurretPitch = tag.getFloat("tp");
        this.localTurretDestPitch = tag.getFloat("tpd");
        this.localTurretRotation = tag.getFloat("tr");
        this.localTurretDestRot = tag.getFloat("trd");
        this.upgradeHelper.updateUpgrades();
        this.ammoHelper.updateAmmoCounts();
        owner = Owner.deserializeFromNBT(tag);
        this.isSettingUp = tag.getBoolean("setup");
        if (this.isSettingUp) {
            this.setupTicks = tag.getInt("sTick");
        }
        this.refreshDimensions();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("vehType", this.vehicleType.getGlobalVehicleType());
        tag.putInt("matLvl", this.vehicleMaterialLevel);
        tag.putFloat("health", this.getHealth());
        tag.putFloat("turHome", this.localTurretRotationHome);
        this.inventory.writeToNBT(tag);//yah..I wrote this one a long time ago, is why it is different.....
        tag.put("upgrades", this.upgradeHelper.serializeNBT());
        tag.put("ammo", this.ammoHelper.serializeNBT());
        tag.put("move", this.moveHelper.serializeNBT());
        tag.put("fire", this.firingHelper.serializeNBT());
        tag.put("vars", this.firingVarsHelper.serializeNBT());
        tag.putFloat("lc", localLaunchPower);
        tag.putFloat("tp", localTurretPitch);
        tag.putFloat("tpd", localTurretDestPitch);
        tag.putFloat("tr", localTurretRotation);
        tag.putFloat("trd", localTurretDestRot);
        owner.serializeToNBT(tag);
        tag.putBoolean("setup", this.isSettingUp);
        if (this.isSettingUp) {
            tag.putInt("sTick", this.setupTicks);
        }
    }

    /**
     * missile callback methods...
     */
    @Override
    public void onMissileImpact(Level world, double x, double y, double z) {
        if (getVehicle() instanceof IMissileHitCallback callback) {
            callback.onMissileImpact(world, x, y, z);
        }
    }

    @Override
    public void onMissileImpactEntity(Level world, Entity entity) {
        if (getVehicle() instanceof IMissileHitCallback callback) {
            callback.onMissileImpactEntity(world, entity);
        }
    }

    @Override
    public void setMoveTo(double x, double y, double z, float moveSpeed) {
        this.moveHelper.setMoveTo(x, y, z);
    }

    @Override
    public boolean isPathableEntityOnLadder() {
        return false;
    }

    @Override
    public Entity getEntity() {
        return this;
    }

    @Override
    public void setPath(List<Node> path) {
        this.nav.forcePath(path);
    }

    public void clearPath() {
        this.nav.clearPath();
    }

    @Override
    public float getDefaultMoveSpeed() {
        return this.currentForwardSpeedMax;
    }

    @Override
    public void onStuckDetected() {
        if (getControllingPassenger() instanceof NpcBase) {
            ((NpcBase) getControllingPassenger()).onStuckDetected();
        }
    }

    @Nullable
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            if (!itemHandlerCapability.isPresent())
                itemHandlerCapability = LazyOptional.of(() -> inventory.storageInventory);
            return itemHandlerCapability.cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerCapability.invalidate();
        itemHandlerCapability = LazyOptional.empty();
    }

    @Override
    public void setOwner(Player player) {
        owner = new Owner(player);
    }

    @Override
    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    @Override
    public Owner getOwner() {
        return owner;
    }

    @Override
    public boolean isOwner(Player player) {
        return owner.isOwnerOrSameTeamOrFriend(player);
    }

    public boolean isAmmoLoaded() {
        return vehicleType.getValidAmmoTypes().stream().anyMatch(a -> ammoHelper.getCountOf(a) > 0);
    }
}
