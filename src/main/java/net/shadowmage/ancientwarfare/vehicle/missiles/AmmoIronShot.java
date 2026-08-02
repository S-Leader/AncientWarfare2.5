package net.shadowmage.ancientwarfare.vehicle.missiles;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

public class AmmoIronShot extends Ammo {

    public AmmoIronShot(int weight, int damage) {
        super("ammo_iron_shot_" + weight);
        ammoWeight = weight;
        entityDamage = damage;
        vehicleDamage = damage;
        float scaleFactor = weight + 45.f;
        renderScale = (weight / scaleFactor) * 2;
        configName = "iron_shot_" + weight;
        modelTexture = new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/ammo/ammo_stone_shot.png");
    }

    @Override
    public void onImpactWorld(Level world, float x, float y, float z, MissileBase missile, HitResult hit) {
        if (world.isClientSide) {
            return;
        }

        BlockPos origin = BlockPos.containing(x, y, z);
        float maxHardness = 5 + (ammoWeight * 0.2f + ammoWeight * 0.8f * world.getRandom().nextFloat());

        breakAroundOnLevel(world, origin, origin, maxHardness);
        breakAroundOnLevel(world, origin, origin.above(), maxHardness);
        breakAroundOnLevel(world, origin, origin.below(), maxHardness);
    }

    @Override
    public void onImpactEntity(Level world, Entity ent, float x, float y, float z, MissileBase missile) {
        if (!world.isClientSide) {
            ent.hurt(DamageType.causeEntityMissileDamage(missile.shooterLiving, false, false), getEntityDamage());
        }
    }

}
