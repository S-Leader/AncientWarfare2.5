package net.shadowmage.ancientwarfare.npc.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class BlockPosTarget implements ITarget {
    private final BlockPos pos;
    private final AABB bounds;

    BlockPosTarget(BlockPos pos) {
        this.pos = pos;
        bounds = new AABB(pos, pos.offset(1, 1, 1));
    }

    @Override
    public double getX() {
        return pos.getX() + 0.5D;
    }

    @Override
    public double getY() {
        return pos.getY() + 0.5D;
    }

    @Override
    public double getZ() {
        return pos.getZ() + 0.5D;
    }

    @Override
    public AABB getBoundigBox() {
        return bounds;
    }

    @Override
    public boolean exists(Level world) {
        return !world.isEmptyBlock(pos);
    }

    public CompoundTag serializeToNBT(CompoundTag tag) {
        tag.putLong("targetPos", pos.asLong());
        return tag;
    }
}
