package net.shadowmage.ancientwarfare.structure.template.build;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.Zone;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;

public class StructureBB extends Zone {

    public StructureBB(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        super(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
    }

    public StructureBB(BlockPos pos, Direction face, StructureTemplate template) {
        this(pos, face, template.getSize(), template.getOffset());
    }

    public StructureBB(BlockPos pos, Direction face, Vec3i size, Vec3i offset) {
        /*
         * we simply take the clicked on position
         * and walk left/forward/down by the structure offsets
         */
        BlockPos c1 = pos.relative(face.getCounterClockWise(), offset.getX()).relative(face, offset.getZ()).above(1 - offset.getY());
        /*
         * the second corner starts as a copy of the first corner
         * which then walks right, backwards, and up to arrive at the actual second corner
         */
        BlockPos c2 = c1.relative(face.getClockWise(), size.getX() - 1).relative(face, -(size.getZ() - 1)).above(size.getY() - 1);
        /*
         * finally, set the min/max of this BB to the min/max of the two corners
         */
        min = BlockTools.getMin(c1, c2);
        max = BlockTools.getMax(c1, c2);
    }

    public StructureBB(BlockPos pos1, BlockPos pos2) {
        super(pos1, pos2);
    }

    private StructureBB() {

    }

    @Override
    public String toString() {
        return min.toString() + " : " + max.toString();
    }

    /*
     * can be used to contract by specifying negative amounts...
     */
    public StructureBB expand(int x, int y, int z) {
        min = min.subtract(new Vec3i(x, y, z));
        max = max.offset(x, y, z);
        return this;
    }

    public StructureBB add(int x, int y, int z) {
        min = min.offset(x, y, z);
        max = max.offset(x, y, z);
        return this;
    }

    public int getXSize() {
        return max.getX() - min.getX() + 1;
    }

    public int getZSize() {
        return max.getZ() - min.getZ() + 1;
    }

    public int getYSize() {
        return max.getY() - min.getY() + 1;
    }

    public BlockPos getCenter() {
        return new BlockPos((min.getX() + max.getX()) / 2, (min.getY() + max.getY()) / 2, (min.getZ() + max.getZ()) / 2);
    }

    public int getCenterX() {
        return min.getX() + (getXSize() / 2) - 1;
    }

    public int getCenterZ() {
        return min.getZ() + (getZSize() / 2) - 1;
    }

    /*
     * 0-- z++==forward x++==left
     * 1-- x--==forward z++==left
     * 2-- z--==forward x--==left
     * 3-- x++==forward z--==left
     */
    public StructureBB getFrontCorners(Direction face) {
        BlockPos min = getFLCorner(face, BlockPos.ZERO);
        BlockPos max = getFRCorner(face, BlockPos.ZERO);
        int minX = Math.min(min.getX(), max.getX());
        int maxX = Math.max(min.getX(), max.getX());
        int minZ = Math.min(min.getZ(), max.getZ());
        int maxZ = Math.max(min.getZ(), max.getZ());
        StructureBB result = new StructureBB();
        result.min = new BlockPos(minX, min.getY(), minZ);
        result.max = new BlockPos(maxX, max.getY(), maxZ);
        return result;
    }

    public StructureBB getLeftCorners(Direction face) {
        BlockPos min = getFLCorner(face, BlockPos.ZERO);
        BlockPos max = getRLCorner(face, BlockPos.ZERO);
        int minX = Math.min(min.getX(), max.getX());
        int maxX = Math.max(min.getX(), max.getX());
        int minZ = Math.min(min.getZ(), max.getZ());
        int maxZ = Math.max(min.getZ(), max.getZ());
        StructureBB result = new StructureBB();
        result.min = new BlockPos(minX, min.getY(), minZ);
        result.max = new BlockPos(maxX, max.getY(), maxZ);
        return result;
    }

    public StructureBB getRearCorners(Direction face) {
        BlockPos min = getRLCorner(face, BlockPos.ZERO);
        BlockPos max = getRRCorner(face, BlockPos.ZERO);
        int minX = Math.min(min.getX(), max.getX());
        int maxX = Math.max(min.getX(), max.getX());
        int minZ = Math.min(min.getZ(), max.getZ());
        int maxZ = Math.max(min.getZ(), max.getZ());
        StructureBB result = new StructureBB();
        result.min = new BlockPos(minX, min.getY(), minZ);
        result.max = new BlockPos(maxX, max.getY(), maxZ);
        return result;
    }

    public StructureBB getRightCorners(Direction face) {
        BlockPos min = getFRCorner(face, BlockPos.ZERO);
        BlockPos max = getRRCorner(face, BlockPos.ZERO);
        int minX = Math.min(min.getX(), max.getX());
        int maxX = Math.max(min.getX(), max.getX());
        int minZ = Math.min(min.getZ(), max.getZ());
        int maxZ = Math.max(min.getZ(), max.getZ());
        StructureBB result = new StructureBB();
        result.min = new BlockPos(minX, min.getY(), minZ);
        result.max = new BlockPos(maxX, max.getY(), maxZ);
        return result;
    }

    private BlockPos getFLCorner(Direction face, BlockPos out) {
        switch (face) {
            case SOUTH:
                return new BlockPos(max.getX(), min.getY(), min.getZ());

            case WEST:
                return new BlockPos(max.getX(), min.getY(), max.getZ());

            case NORTH:
                return new BlockPos(min.getX(), min.getY(), max.getZ());

            case EAST:
                return min;
            default:
                return out;
        }
    }

    private BlockPos getFRCorner(Direction face, BlockPos out) {
        switch (face) {
            case SOUTH:
                return min;

            case WEST:
                return new BlockPos(max.getX(), min.getY(), min.getZ());

            case NORTH:
                return new BlockPos(max.getX(), min.getY(), max.getZ());

            case EAST:
                return new BlockPos(min.getX(), min.getY(), max.getZ());
            default:
                return out;
        }
    }

    public BlockPos getRLCorner(Direction face, BlockPos out) {
        switch (face) {
            case SOUTH:
                return new BlockPos(max.getX(), min.getY(), max.getZ());

            case WEST:
                return new BlockPos(min.getX(), min.getY(), max.getZ());

            case NORTH:
                return min;

            case EAST:
                return new BlockPos(max.getX(), min.getY(), min.getZ());
            default:
                return out;
        }
    }

    private BlockPos getRRCorner(Direction face, BlockPos out) {
        switch (face) {
            case SOUTH:
                return new BlockPos(min.getX(), min.getY(), max.getZ());

            case WEST:
                return min;

            case NORTH:
                return new BlockPos(max.getX(), min.getY(), min.getZ());

            case EAST:
                return new BlockPos(max.getX(), min.getY(), max.getZ());
            default:
                return out;
        }
    }

    public StructureBB copy() {
        return new StructureBB(min, max);
    }

    public double getDistanceTo(StructureBB bb) {
        int xDistance = Math.abs(getCenterX() - bb.getCenterX());
        int zDistance = Math.abs(getCenterZ() - bb.getCenterZ());
        return Math.sqrt((double) xDistance * xDistance + zDistance * zDistance);
    }

    public AABB getAABB() {
        // StructureBB stores inclusive block coordinates. AABB max coordinates are
        // exclusive, so add one block or entities standing in the max edge blocks
        // are omitted from conquest/chest-protection checks.
        return new AABB(min.getX(), min.getY(), min.getZ(),
                max.getX() + 1.0D, max.getY() + 1.0D, max.getZ() + 1.0D);
    }
}
