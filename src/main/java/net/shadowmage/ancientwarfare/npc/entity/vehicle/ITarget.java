package net.shadowmage.ancientwarfare.npc.entity.vehicle;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public interface ITarget {
    double getX();

    double getY();

    double getZ();

    AABB getBoundigBox();

    boolean exists(Level world);
}
