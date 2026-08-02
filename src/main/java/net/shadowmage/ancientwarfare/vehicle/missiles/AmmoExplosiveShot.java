package net.shadowmage.ancientwarfare.vehicle.missiles;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

public class AmmoExplosiveShot extends Ammo {

    private boolean bigExplosion;

    public AmmoExplosiveShot(int weight, boolean bigExplosion) {
        super("ammo_explosive_shot_" + weight + (bigExplosion ? "_big" : ""));
        this.ammoWeight = weight;
        this.bigExplosion = bigExplosion;
        this.entityDamage = weight;
        this.vehicleDamage = weight;
        float scaleFactor = weight + 45.f;
        this.renderScale = (weight / scaleFactor) * 2;

        if (bigExplosion) {
            this.configName = "high_explosive_" + weight;
        } else {
            this.configName = "explosive_" + weight;
        }
        this.modelTexture = new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/ammo/ammo_stone_shot.png");
    }

    @Override
    public void onImpactWorld(Level world, float x, float y, float z, MissileBase missile, HitResult hit) {
        if (!world.isClientSide) {
            Vec3i dirVec = (hit instanceof BlockHitResult blockHit ? blockHit.getDirection() : Direction.UP).getNormal();
            Vec3 hitVec = hit.getLocation().add(dirVec.getX() * 0.2d, dirVec.getY() * 0.2d, dirVec.getZ() * 0.2d);
            explode(world, (float) hitVec.x, (float) hitVec.y, (float) hitVec.z, missile);
        }
    }

    @Override
    public void onImpactEntity(Level world, Entity ent, float x, float y, float z, MissileBase missile) {
        if (!world.isClientSide) {
            explode(world, x, y, z, missile);
        }
    }

    private void explode(Level world, float x, float y, float z, MissileBase missile) {
        float maxPower = bigExplosion ? 7.f : 3.5f;
        float powerPercent = ammoWeight / 45.f;
        float power = maxPower * powerPercent;
        createExplosion(world, missile, x, y, z, power);
    }
}
