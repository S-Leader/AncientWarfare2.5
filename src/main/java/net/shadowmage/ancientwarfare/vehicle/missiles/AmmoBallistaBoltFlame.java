package net.shadowmage.ancientwarfare.vehicle.missiles;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.vehicle.config.AWVehicleStatics;
import net.shadowmage.ancientwarfare.vehicle.init.AWVehicleSounds;

public class AmmoBallistaBoltFlame extends Ammo {

    public AmmoBallistaBoltFlame() {
        super("ammo_ballista_bolt_flame");
        ammoWeight = 2.2f;
        renderScale = 0.3f;
        vehicleDamage = AWVehicleStatics.vehicleStats.ammoBallistaBoltFlameDamage;
        entityDamage = AWVehicleStatics.vehicleStats.ammoBallistaBoltFlameDamage;
        isArrow = true;
        isRocket = false;
        isPersistent = true;
        isFlaming = true;
        configName = "ballist_bolt_flame";
        modelTexture = new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/ammo/arrow_wood.png");
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
            // using Level.playSound instead of Entity.playSound, because Entity.playSound plays the sound to everyone nearby except(!) this player
            world.playSound(null, x, y, z, AWVehicleSounds.BALLISTA_BOLT_HIT_ENTITY, SoundSource.NEUTRAL, 2, 1);
            ent.hurt(DamageType.causeEntityMissileDamage(missile.shooterLiving, true, false), getEntityDamage());
            ent.setSecondsOnFire(4);
        }
    }

}
