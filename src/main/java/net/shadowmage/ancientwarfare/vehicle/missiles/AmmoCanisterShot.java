package net.shadowmage.ancientwarfare.vehicle.missiles;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.vehicle.config.AWVehicleStatics;
import net.shadowmage.ancientwarfare.vehicle.registry.AmmoRegistry;

public class AmmoCanisterShot extends Ammo {

    public AmmoCanisterShot(int weight) {
        super("ammo_canister_shot_" + weight);
        this.ammoWeight = weight;
        float scaleFactor = weight + 45.f;
        this.renderScale = (weight / scaleFactor) * 2;
        this.configName = "canister_shot_" + weight;
        this.modelTexture = new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/ammo/ammo_stone_shot.png");
        this.entityDamage = AWVehicleStatics.vehicleStats.ammoCanisterDamage;
        this.vehicleDamage = AWVehicleStatics.vehicleStats.ammoCanisterDamage;
    }

    @Override
    public void onImpactWorld(Level world, float x, float y, float z, MissileBase missile, HitResult hit) {
        if (!world.isClientSide) {
            spawnGroundBurst(world, hit, 10, AmmoRegistry.ammoBallIronShot, (int) ammoWeight, 35, missile.shooterLiving);
        }
    }

    @Override
    public void onImpactEntity(Level world, Entity ent, float x, float y, float z, MissileBase missile) {
        if (!world.isClientSide) {
            spawnAirBurst(world, (float) ent.getX(), (float) ent.getY() + ent.getBbHeight(), (float) ent.getZ(), 10, AmmoRegistry.ammoBallIronShot, (int) ammoWeight, missile.shooterLiving);
        }
    }

}
