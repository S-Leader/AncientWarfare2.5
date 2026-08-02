package net.shadowmage.ancientwarfare.vehicle.missiles;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

public class AmmoBallShot extends Ammo {

    public AmmoBallShot() {
        super("ammo_ball_shot");
        renderScale = 0.15f;
        ammoWeight = 1.f;
        entityDamage = 5;
        vehicleDamage = 5;
        isPersistent = false;
        isCraftable = false;
        configName = "ball_shot";
        modelTexture = new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/ammo/ammo_stone_shot.png");
    }

    @Override
    public void onImpactWorld(Level world, float x, float y, float z, MissileBase missile, HitResult hit) {
        //NOOP
    }

    @Override
    public void onImpactEntity(Level world, Entity ent, float x, float y, float z, MissileBase missile) {
        if (!world.isClientSide) {
            ent.hurt(DamageType.causeEntityMissileDamage(missile.shooterLiving, false, false), getEntityDamage());
        }
    }
}
