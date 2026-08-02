package net.shadowmage.ancientwarfare.vehicle.missiles;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

public class AmmoIronBallShot extends Ammo {

    public AmmoIronBallShot() {
        super("ammo_iron_ball_shot");
        renderScale = 0.05f;
        ammoWeight = 1.f;
        entityDamage = 8;
        vehicleDamage = 8;
        isCraftable = false;
        configName = "iron_ball_shot";
        modelTexture = new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/ammo/ammo_stone_shot.png");
        isCraftable = false;
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
