package net.shadowmage.ancientwarfare.vehicle.missiles;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

public class AmmoFlameShot extends Ammo {

    public AmmoFlameShot(int weight) {
        super("ammo_flame_shot_" + weight);
        this.isPersistent = false;
        this.isArrow = false;
        this.isRocket = false;
        this.isFlaming = true;
        this.ammoWeight = weight;
        float scaleFactor = weight + 45.f;
        this.renderScale = (weight / scaleFactor) * 2;
        this.configName = "flame_shot_" + weight;
        this.vehicleDamage = 8;
        this.entityDamage = 8;
        this.modelTexture = new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/ammo/ammo_stone_shot.png");
    }

    @Override
    public void onImpactWorld(Level world, float x, float y, float z, MissileBase missile, HitResult hit) {
        if (!world.isClientSide) {
            int bx = (int) x;
            int by = (int) y + 2;
            int bz = (int) z;
            this.igniteBlock(world, bx, by, bz, 5);
            if (this.ammoWeight >= 15) {
                this.igniteBlock(world, bx - 1, by, bz, 5);
                this.igniteBlock(world, bx + 1, by, bz, 5);
                this.igniteBlock(world, bx, by, bz - 1, 5);
                this.igniteBlock(world, bx, by, bz + 1, 5);
            }
            if (ammoWeight >= 30) {
                this.igniteBlock(world, bx - 1, by, bz - 1, 5);
                this.igniteBlock(world, bx - 1, by, bz + 1, 5);
                this.igniteBlock(world, bx + 1, by, bz - 1, 5);
                this.igniteBlock(world, bx + 1, by, bz + 1, 5);
                this.igniteBlock(world, bx - 2, by, bz, 5);
                this.igniteBlock(world, bx + 2, by, bz, 5);
                this.igniteBlock(world, bx, by, bz - 2, 5);
                this.igniteBlock(world, bx, by, bz + 2, 5);
            }
            if (ammoWeight >= 45) {
                this.igniteBlock(world, bx - 1, by, bz - 2, 5);
                this.igniteBlock(world, bx + 1, by, bz - 2, 5);
                this.igniteBlock(world, bx - 1, by, bz + 2, 5);
                this.igniteBlock(world, bx + 1, by, bz + 2, 5);
                this.igniteBlock(world, bx - 2, by, bz - 1, 5);
                this.igniteBlock(world, bx - 2, by, bz + 1, 5);
                this.igniteBlock(world, bx + 2, by, bz - 1, 5);
                this.igniteBlock(world, bx + 2, by, bz + 1, 5);
            }
        }
    }

    @Override
    public void onImpactEntity(Level world, Entity ent, float x, float y, float z, MissileBase missile) {
        if (!world.isClientSide) {
            ent.hurt(DamageType.causeEntityMissileDamage(missile.shooterLiving, true, false), this.getEntityDamage());
            ent.setSecondsOnFire(3);
            onImpactWorld(world, x, y, z, missile, null);
        }
    }

}
