package net.shadowmage.ancientwarfare.vehicle.missiles;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

public class AmmoNapalmShot extends Ammo {

    public AmmoNapalmShot(int weight) {
        super("ammo_napalm_shot_" + weight);
        this.ammoWeight = weight;
        this.entityDamage = weight;
        this.vehicleDamage = weight;
        float scaleFactor = weight + 45.f;
        this.renderScale = (weight / scaleFactor) * 2;
        this.configName = "napalm_shot_" + weight;
        this.modelTexture = new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/ammo/ammo_stone_shot.png");
        this.isFlaming = true;
    }

    @Override
    public void onImpactWorld(Level world, float x, float y, float z, MissileBase missile, HitResult hit) {
        int bx = Mth.floor(x);
        int by = Mth.floor(y);
        int bz = Mth.floor(z);
        setBlockToLava(world, bx, by, bz, 5);
        double dx = missile.getDeltaMovement().x;
        double dz = missile.getDeltaMovement().z;
        if (Math.abs(dx) > Math.abs(dz)) {
            dz = 0;
        } else {
            dx = 0;
        }
        dx = dx < 0 ? -1 : dx > 0 ? 1 : dx;
        dz = dz < 0 ? -1 : dz > 0 ? 1 : dz;
        if (ammoWeight >= 15)//set the 'forward' block to lava as well
        {
            setBlockToLava(world, bx + (int) dx, by, bz + (int) dz, 5);
        }
        if (ammoWeight >= 30)//set the 'rear' block to lava as well
        {
            setBlockToLava(world, bx - (int) dx, by, bz - (int) dz, 5);
        }
        if (ammoWeight >= 45) {
            if (dx == 0)//have already done Z's
            {
                setBlockToLava(world, bx + 1, by, bz, 5);
                setBlockToLava(world, bx - 1, by, bz, 5);
            } else {
                setBlockToLava(world, bx, by, bz + 1, 5);
                setBlockToLava(world, bx, by, bz - 1, 5);
            }
        }
    }

    @Override
    public void onImpactEntity(Level world, Entity ent, float x, float y, float z, MissileBase missile) {
        if (!world.isClientSide) {
            ent.hurt(DamageType.causeEntityMissileDamage(missile.shooterLiving, true, false), this.getEntityDamage());
            ent.setSecondsOnFire(3);
            onImpactWorld(world, x, (float) ent.getY(), z, missile, null);
        }
    }

}
