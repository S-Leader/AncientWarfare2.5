package net.shadowmage.ancientwarfare.core.util;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class BlockFinder {
    private final Level world;
    private final BlockState targetState;
    private final List<BlockPos> positions;

    public BlockFinder(Level worldIn, BlockState targetState, int size) {
        world = worldIn;
        this.targetState = targetState;
        positions = new ArrayList<>(size);
    }

    /*
     * Collect block type in a cross pattern
     * @param center the center of the cross pattern
     * @param max the arms max length of the cross pattern
     * @return the corners of the box containing the cross
     */
    public Pair<BlockPos, BlockPos> cross(BlockPos initial) {
        List<BlockPos> allConnected = getAllConnectedBlocks(initial);

        int minX = initial.getX();
        int maxX = initial.getX();
        int minY = initial.getY();
        int maxY = initial.getY();
        int minZ = initial.getZ();
        int maxZ = initial.getZ();

        for (BlockPos pos : allConnected) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        return Pair.of(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
    }

    /*
     * Collect blocks between corners in a box pattern
     * Fail fast if block type doesn't apply
     * @param corners Bottom North West corner and Upper South East corner
     * @return true if all blocks between corners apply conditions
     */
    public boolean box(Pair<BlockPos, BlockPos> corners) {
        BlockPos temp;
        for (int x = corners.getLeft().getX(); x <= corners.getRight().getX(); x++) {
            for (int y = corners.getLeft().getY(); y <= corners.getRight().getY(); y++) {
                for (int z = corners.getLeft().getZ(); z <= corners.getRight().getZ(); z++) {
                    temp = new BlockPos(x, y, z);
                    if (!positions.contains(temp)) {
                        if (isTypeAt(temp))
                            positions.add(temp);
                        else
                            return false;
                    }
                }
            }
        }
        return true;
    }

    /*
     * Collect all blocks that apply
     * @param corner Bottom North West corner
     * @param limit the max size parameters
     */
    public void connect(BlockPos corner, BlockPos limit) {
        BlockPos temp;
        for (int i = 0; i < limit.getX(); i++) {
            for (int j = 0; j < limit.getY(); j++) {
                for (int k = 0; k < limit.getZ(); k++) {
                    temp = corner.offset(i, j, k);
                    if (!positions.contains(temp) && isTypeAt(temp)) {
                        positions.add(temp);
                    }
                }
            }
        }
    }

    /*
     * The conditions applied on the block type
     */
    private boolean isTypeAt(int x, int y, int z) {
        return isTypeAt(new BlockPos(x, y, z));
    }

    private boolean isTypeAt(BlockPos pos) {
        return world.getBlockState(pos).equals(targetState);
    }

    /*
     * The collected block positions
     */
    public List<BlockPos> getPositions() {
        return positions;
    }

    public List<BlockPos> getAllConnectedBlocks(BlockPos initialPos) {
        int maxRadius = 20;
        List<BlockPos> connectedBlocks = Lists.newArrayList();
        List<BlockPos> searchedPositions = Lists.newArrayList();

        connectedBlocks.add(initialPos);
        searchedPositions.add(initialPos);

        getConnectedBlocks(maxRadius, connectedBlocks, searchedPositions, initialPos, initialPos);

        return connectedBlocks;
    }

    private void getConnectedBlocks(int maxRadius, List<BlockPos> connectedBlocks, List<BlockPos> searchedPositions, BlockPos currentPos, BlockPos initialPos) {
        if (currentPos.distSqr(initialPos) < (double) maxRadius * maxRadius) {
            for (Direction facing : Direction.values()) {
                BlockPos offsetPos = currentPos.relative(facing);
                if (!searchedPositions.contains(offsetPos) && isTypeAt(offsetPos)) {
                    connectedBlocks.add(offsetPos);
                    searchedPositions.add(offsetPos);
                    getConnectedBlocks(maxRadius, connectedBlocks, searchedPositions, offsetPos, initialPos);
                }
            }
        }
    }
}
