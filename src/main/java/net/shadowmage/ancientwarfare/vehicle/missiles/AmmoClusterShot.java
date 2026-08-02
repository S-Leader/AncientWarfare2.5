package net.shadowmage.ancientwarfare.vehicle.missiles;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.vehicle.registry.AmmoRegistry;

public class AmmoClusterShot extends Ammo {

    public AmmoClusterShot(int weight) {
        super("ammo_cluster_shot_" + weight);
        this.ammoWeight = weight;
        float scaleFactor = weight + 45.f;
        this.renderScale = (weight / scaleFactor) * 2;
        this.configName = "cluster_shot_" + weight;
        this.modelTexture = new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/ammo/ammo_stone_shot.png");

        this.entityDamage = 5;
        this.vehicleDamage = 5;
    }

    @Override
    public void onImpactWorld(Level world, float x, float y, float z, MissileBase missile, HitResult hit) {
        if (!world.isClientSide) {
            spawnGroundBurst(world, hit, 10, AmmoRegistry.ammoBallShot, (int) ammoWeight, 35, missile.shooterLiving);
        }
    }

    @Override
    public void onImpactEntity(Level world, Entity ent, float x, float y, float z, MissileBase missile) {
        if (!world.isClientSide) {
            spawnAirBurst(world, (float) ent.getX(), (float) ent.getY() + ent.getBbHeight(), (float) ent.getZ(), 10, AmmoRegistry.ammoBallShot, (int) ammoWeight, missile.shooterLiving);
        }
    }

}
