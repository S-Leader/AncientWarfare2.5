package net.shadowmage.ancientwarfare.vehicle.missiles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.shadowmage.ancientwarfare.core.entity.AWEntityRegistry;
import net.shadowmage.ancientwarfare.core.owner.Owner;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.vehicle.AncientWarfareVehicles;
import net.shadowmage.ancientwarfare.vehicle.config.AWVehicleStatics;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Ammo implements IAmmo {
    int entityDamage;
    int vehicleDamage;
    private static final float GRAVITY_FACTOR = 9.81f * 0.05f * 0.05f;
    String configName = "none";
    ResourceLocation modelTexture = new ResourceLocation("missingno");
    boolean isRocket = false;
    boolean isArrow = false;
    boolean isPersistent = false;
    boolean isFlaming = false;
    boolean isProximityAmmo = false;
    boolean isCraftable = true;
    boolean isEnabled = true;
    float groundProximity = 0.f;
    float entityProximity = 0.f;
    float ammoWeight = 10;
    float renderScale = 1.f;
    int secondaryAmmoCount = 0;

    private ResourceLocation registryName;

    public Ammo(String regName) {
        registryName = new ResourceLocation(AncientWarfareVehicles.MOD_ID, regName);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return registryName;
    }

    @Override
    public void setEntityDamage(int damage) {
        if (damage < 0) {
            damage = 0;
        }
        this.entityDamage = damage;
    }

    @Override
    public void setVehicleDamage(int damage) {
        if (damage < 0) {
            damage = 0;
        }
        this.vehicleDamage = damage;
    }

    @Override
    public String getConfigName() {
        return configName;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void setEnabled(boolean val) {
        this.isEnabled = val;
    }

    @Override
    public ResourceLocation getModelTexture() {
        return modelTexture;
    }

    @Override
    public boolean isRocket() {
        return isRocket;
    }

    @Override
    public boolean isPersistent() {
        return isPersistent;
    }

    @Override
    public boolean updateAsArrow() {
        return isArrow;
    }

    @Override
    public boolean isAvailableAsItem() {
        return this.isCraftable;
    }

    @Override
    public float getAmmoWeight() {
        return ammoWeight;
    }

    /**
     * override to implement upgrade-specific ammo use.  this method will be checked before a vehicle will accept ammo into its bay, or fire ammo from its bay
     */
    @Override
    public boolean isAmmoValidFor(VehicleBase vehicle) {
        return true;
    }

    @Override
    public float getRenderScale() {
        return renderScale;
    }

    @Override
    public float getGravityFactor() {
        return GRAVITY_FACTOR;
    }

    @Override
    public int getEntityDamage() {
        return entityDamage;
    }

    @Override
    public int getVehicleDamage() {
        return vehicleDamage;
    }

    @Override
    public boolean isFlaming() {
        return isFlaming;
    }

    @Override
    public boolean isPenetrating() {
        return false;
    }

    @Override
    public IAmmo getSecondaryAmmoType() {
        return null;
    }

    @Override
    public int getSecondaryAmmoTypeCount() {
        return secondaryAmmoCount;
    }

    @Override
    public boolean hasSecondaryAmmo() {
        return false;
    }

    @Override
    public boolean isProximityAmmo() {
        return isProximityAmmo;
    }

    @Override
    public float entityProximity() {
        return entityProximity;
    }

    @Override
    public float groundProximity() {
        return groundProximity;
    }

    private void breakBlockAndDrop(Level world, BlockPos pos) {
        if (!AWVehicleStatics.generalSettings.shotsDestroysBlocks) {
            return;
        }
        BlockTools.breakBlockAndDrop(world, pos);
    }

    protected void igniteBlock(Level world, int x, int y, int z, int maxSearch) {
        if (!AWVehicleStatics.generalSettings.blockFires) {
            return;
        }
        for (int i = 0; i < maxSearch && y - i >= 1; i++) {
            BlockPos curPos = new BlockPos(x, y - i, z);
            if (!world.isEmptyBlock(curPos) && world.isEmptyBlock(curPos.above())) {
                world.setBlock(curPos.above(), Blocks.FIRE.defaultBlockState(), 3);
            }
        }
    }

    public static boolean shouldEffectEntity(Level world, Entity entity, MissileBase missile) {
        if (!AWVehicleStatics.generalSettings.allowFriendlyFire && missile.shooterLiving instanceof NpcBase) {
            @Nonnull NpcBase npc = ((NpcBase) missile.shooterLiving);
            if (entity instanceof NpcBase) {
                Owner targetNpcOwner = ((NpcBase) entity).getOwner();
                return !npc.getOwner().isOwnerOrSameTeamOrFriend(world, targetNpcOwner.getUUID(), targetNpcOwner.getName());
            } else if (entity instanceof Player) {
                return !npc.getOwner().isOwnerOrSameTeamOrFriend(world, entity.getUUID(), entity.getName().getString());
            }
        }
        return true;
    }

    protected void setBlockToLava(Level world, int x, int y, int z, int maxSearch) {
        if (!AWVehicleStatics.generalSettings.blockFires) {
            return;
        }
        for (int i = 0; i < maxSearch && y - i >= 1; i++) {
            BlockPos curPos = new BlockPos(x, y - i, z);
            if (!world.isEmptyBlock(curPos)) {
                if (world.isEmptyBlock(curPos.above())) {
                    world.setBlock(curPos.above(), Blocks.LAVA.defaultBlockState(), 3);
                }
                break;
            }
        }
    }

    protected void createExplosion(Level world, @Nullable MissileBase missile, float x, float y, float z, float power) {
        boolean destroyBlocks = AWVehicleStatics.generalSettings.shotsDestroysBlocks;
        boolean fires = AWVehicleStatics.generalSettings.blockFires;

        world.explode(missile, x, y, z, power, fires, destroyBlocks ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
    }

    protected void spawnGroundBurst(Level world, HitResult hit, float maxVelocity, IAmmo type, int count, float minPitch, Entity shooter) {
        Direction sideHit = hit instanceof BlockHitResult blockHit ? blockHit.getDirection() : Direction.UP;
        Vec3i dirVec = sideHit.getNormal();
        Vec3 hitVec = hit.getLocation().add(dirVec.getX() * 0.2f, dirVec.getY() * 0.2f, dirVec.getZ() * 0.2f);
        spawnBurst(world, maxVelocity, type, count, minPitch, shooter, sideHit, (float) hitVec.x, (float) hitVec.y, (float) hitVec.z);
    }

    private void spawnBurst(Level world, float maxVelocity, IAmmo type, int count, float minPitch, Entity shooter, Direction sideHit, float x, float y, float z) {
        world.explode(null, x, y, z, 0.25f, false, Level.ExplosionInteraction.TNT);
        createExplosion(world, null, x, y, z, 1.f);
        float randRange = 90 - minPitch;
        if (type.hasSecondaryAmmo()) {
            count = type.getSecondaryAmmoTypeCount();
            type = type.getSecondaryAmmoType();
        }
        for (int i = 0; i < count; i++) {
            float yaw;
            float pitch;
            float randVelocity;
            float velocity;
            if (sideHit.getAxis().isVertical()) {
                pitch = 90 - (world.getRandom().nextFloat() * randRange);
                yaw = world.getRandom().nextFloat() * 360.f;
                randVelocity = world.getRandom().nextFloat();
                randVelocity = randVelocity < 0.5f ? 0.5f : randVelocity;
                velocity = maxVelocity * randVelocity;
            } else {
                float minYaw = getMinYaw(sideHit);
                float maxYaw = getMaxYaw(sideHit);
                if (minYaw > maxYaw) {
                    float tmp = maxYaw;
                    maxYaw = minYaw;
                    minYaw = tmp;
                }
                float yawRange = maxYaw - minYaw;
                pitch = 90 - (world.getRandom().nextFloat() * randRange);
                yaw = minYaw + (world.getRandom().nextFloat() * yawRange);
                randVelocity = world.getRandom().nextFloat();
                randVelocity = randVelocity < 0.5f ? 0.5f : randVelocity;
                velocity = maxVelocity * randVelocity;
            }
            MissileBase missile = getMissileByType(type, world, x, y, z, yaw, pitch, velocity, shooter);
            if (missile != null) {
                world.addFreshEntity(missile);
            }
        }
    }

    private float getMinYaw(Direction side) {
        switch (side) {
            case NORTH://north
                return 360f - 45f;
            case SOUTH://south
                return 180f - 45f;
            case WEST://west
                return 90f - 45f;
            case EAST://east
                return 270f - 45f;
            default:
                return 0;
        }
    }

    private float getMaxYaw(Direction side) {
        switch (side) {
            case NORTH://north
                return 360f + 45f;
            case SOUTH://south
                return 180f + 45f;
            case WEST://west
                return 90f + 45f;
            case EAST://east
                return 270f + 45f;
            default:
                return 0;
        }
    }

    protected void spawnAirBurst(Level world, float x, float y, float z, float maxVelocity, IAmmo type, int count, Entity shooter) {
        spawnBurst(world, maxVelocity, type, count, -90, shooter, Direction.DOWN, x, y, z);
    }

    protected void breakAroundOnLevel(Level world, BlockPos origin, BlockPos center, float maxHardness) {
        affectBlock(world, origin, center, maxHardness);
        affectBlock(world, origin, center.north(), maxHardness);
        affectBlock(world, origin, center.east(), maxHardness);
        affectBlock(world, origin, center.west(), maxHardness);
        affectBlock(world, origin, center.south(), maxHardness);
        affectBlock(world, origin, center.north().east(), maxHardness);
        affectBlock(world, origin, center.east().south(), maxHardness);
        affectBlock(world, origin, center.south().west(), maxHardness);
        affectBlock(world, origin, center.west().north(), maxHardness);
    }

    private void affectBlock(Level world, BlockPos origin, BlockPos pos, float maxHardness) {
        double distanceAdjustedHardness = maxHardness - Math.sqrt(origin.distSqr(pos)) * 15;
        if (distanceAdjustedHardness > 0 && distanceAdjustedHardness > world.getBlockState(pos).getDestroySpeed(world, pos)) {
            breakBlockAndDrop(world, pos);
        }
    }

    @Nullable
    private MissileBase getMissileByType(IAmmo type, Level world, float x, float y, float z, float yaw, float pitch, float velocity, Entity shooter) {
        MissileBase missile = AWEntityRegistry.createEntity(
                new ResourceLocation(AncientWarfareVehicles.MOD_ID, AWEntityRegistry.MISSILE),
                world, MissileBase.class);
        if (missile == null) {
            return null;
        }
        missile.setShooter(shooter);
        missile.setMissileParams2(type, x, y, z, yaw, pitch, velocity);
        return missile;
    }
}
