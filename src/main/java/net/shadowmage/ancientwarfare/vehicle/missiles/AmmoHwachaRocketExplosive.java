package net.shadowmage.ancientwarfare.vehicle.missiles;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.vehicle.config.AWVehicleStatics;

public class AmmoHwachaRocketExplosive extends Ammo {

    public AmmoHwachaRocketExplosive() {
        super("ammo_hwacha_rocket_explosive");
        this.entityDamage = AWVehicleStatics.vehicleStats.ammoHwachaRocketExplosiveDamage;
        this.vehicleDamage = AWVehicleStatics.vehicleStats.ammoHwachaRocketExplosiveDamage;
        this.isArrow = true;
        this.isPersistent = false;
        this.isRocket = true;
        this.ammoWeight = 1.3f;
        this.renderScale = 0.2f;
        this.configName = "hwacha_rocket_explosive";
        this.modelTexture = new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/ammo/arrow_wood.png");
    }

    @Override
    public void onImpactWorld(Level world, float x, float y, float z, MissileBase missile, HitResult hit) {
        if (!world.isClientSide) {
            Vec3i dirVec = (hit instanceof BlockHitResult blockHit ? blockHit.getDirection() : Direction.UP).getNormal();
            Vec3 hitVec = hit.getLocation().add(dirVec.getX() * 0.2d, dirVec.getY() * 0.2d, dirVec.getZ() * 0.2d);
            createExplosion(world, missile, (float) hitVec.x, (float) hitVec.y, (float) hitVec.z, 0.6f);
        }
    }

    @Override
    public void onImpactEntity(Level world, Entity ent, float x, float y, float z, MissileBase missile) {
        if (!world.isClientSide) {
            ent.hurt(DamageType.causeEntityMissileDamage(missile.shooterLiving, false, true), this.getEntityDamage());
            ent.setSecondsOnFire(3);
            createExplosion(world, missile, x, y, z, 0.8f);
        }
    }
}
