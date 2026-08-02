package net.shadowmage.ancientwarfare.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class Zone implements INBTSerializable<CompoundTag> {
    public BlockPos min;
    public BlockPos max;

    public Zone(BlockPos p1, BlockPos p2) {
        min = BlockTools.getMin(p1, p2);
        max = BlockTools.getMax(p1, p2);
    }

    public Zone() {

    }

    private boolean intersects(BlockPos min, BlockPos max) {
        return intersects(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }

    public boolean intersects(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return max.getX() >= minX && max.getY() >= minY && max.getZ() >= minZ && min.getX() <= maxX && min.getY() <= maxY && min.getZ() <= maxZ;
    }

    /*
     * does the input share any block position with this zone ?
     */
    public boolean intersects(Zone z) {
        return intersects(z.min, z.max);
    }

    public boolean equals(BlockPos min, BlockPos max) {
        return min.equals(this.min) && max.equals(this.max);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Zone && this.equals(((Zone) object).min, ((Zone) object).max);
    }

    @Override
    public int hashCode() {
        return 31 * min.hashCode() + max.hashCode();
    }

    @Override
    public String toString() {
        return String.format("From %s to %s", min, max);
    }

    public boolean contains(BlockPos pos) {
        return pos.getX() >= min.getX() && pos.getX() <= max.getX()
                && pos.getY() >= min.getY() && pos.getY() <= max.getY()
                && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("min", min.asLong());
        tag.putLong("max", max.asLong());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        min = BlockPos.of(tag.getLong("min"));
        max = BlockPos.of(tag.getLong("max"));
    }
}
