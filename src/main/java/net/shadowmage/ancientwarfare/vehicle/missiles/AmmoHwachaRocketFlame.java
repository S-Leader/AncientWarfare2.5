package net.shadowmage.ancientwarfare.vehicle.missiles;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.vehicle.config.AWVehicleStatics;

public class AmmoHwachaRocketFlame extends Ammo {

    public AmmoHwachaRocketFlame() {
        super("ammo_hwacha_rocket_flame");
        this.entityDamage = AWVehicleStatics.vehicleStats.ammoHwachaRocketFlameDamage;
        this.vehicleDamage = AWVehicleStatics.vehicleStats.ammoHwachaRocketFlameDamage;
        this.isArrow = true;
        this.isPersistent = true;
        this.isRocket = true;
        this.isFlaming = true;
        this.ammoWeight = 1.1f;
        this.renderScale = 0.2f;
        this.renderScale = 0.2f;
        this.configName = "hwacha_rocket_flame";
        this.modelTexture = new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/ammo/arrow_wood.png");
    }

    @Override
    public void onImpactWorld(Level world, float x, float y, float z, MissileBase missile, HitResult hit) {
        if (!world.isClientSide) {
            igniteBlock(world, (int) x, (int) y + 2, (int) z, 5);
        }
    }

    @Override
    public void onImpactEntity(Level world, Entity ent, float x, float y, float z, MissileBase missile) {
        if (!world.isClientSide) {
            ent.hurt(DamageType.causeEntityMissileDamage(missile.shooterLiving, true, false), this.getEntityDamage());
            ent.setSecondsOnFire(2);
        }
    }
}
