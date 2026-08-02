package net.shadowmage.ancientwarfare.npc.entity.vehicle;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.lang.ref.WeakReference;

public class EntityTarget implements ITarget {
    private WeakReference<LivingEntity> entity;

    public EntityTarget(LivingEntity entity) {
        this.entity = new WeakReference<>(entity);
    }

    @Override
    public double getX() {
        LivingEntity e = entity.get();
        return e == null ? 0D : e.getX();
    }

    @Override
    public double getY() {
        LivingEntity e = entity.get();
        return e == null ? 0D : e.getY() + e.getEyeHeight();
    }

    @Override
    public double getZ() {
        LivingEntity e = entity.get();
        return e == null ? 0D : e.getZ();
    }

    @Override
    public AABB getBoundigBox() {
        LivingEntity e = entity.get();
        return e == null ? new AABB(0, 0, 0, 0, 0, 0) : e.getBoundingBox();
    }

    @Override
    public boolean exists(Level world) {
        LivingEntity e = entity.get();
        return e != null && !e.isRemoved();
    }
}
