package net.shadowmage.ancientwarfare.vehicle.missiles;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.vehicle.registry.AmmoRegistry;

public class AmmoHwachaRocketAirburst extends Ammo {

    public AmmoHwachaRocketAirburst() {
        super("ammo_hwacha_rocket_airburst");
        this.entityDamage = 0;
        this.vehicleDamage = 0;
        this.isArrow = true;
        this.isPersistent = false;
        this.isRocket = true;
        this.isProximityAmmo = true;
        this.groundProximity = 12.f;
        this.entityProximity = 10f;
        this.ammoWeight = 1.4f;
        this.renderScale = 0.2f;
        this.configName = "hwacha_rocket_airburst";
        this.modelTexture = new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/ammo/arrow_wood.png");
    }

    @Override
    public void onImpactWorld(Level world, float x, float y, float z, MissileBase missile, HitResult hit) {
        if (!world.isClientSide) {
            this.spawnAirBurst(world, x, y, z, 10, AmmoRegistry.ammoBallShot, 4, missile.shooterLiving);
            missile.discard();
        }
    }

    @Override
    public void onImpactEntity(Level world, Entity ent, float x, float y, float z, MissileBase missile) {
        if (!world.isClientSide) {
            this.spawnAirBurst(world, x, y, z, 10, AmmoRegistry.ammoBallShot, 4, missile.shooterLiving);
            missile.discard();
        }
    }

}
