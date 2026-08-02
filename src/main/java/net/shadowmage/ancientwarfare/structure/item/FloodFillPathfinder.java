package net.shadowmage.ancientwarfare.structure.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.structure.config.AWStructureStatics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FloodFillPathfinder {

    private final int maxDist;

    Level world;
    private BlockPos startingPos;
    Block block;//target flood fill block
    private BlockState targetState;
    boolean searchUpwards;
    boolean searchDownwards;

    ArrayList<BlockPos> openList = new ArrayList<>();
    Set<BlockPos> closedList = new HashSet<>();
    Set<BlockPos> neighborCache = new HashSet<>();
    Set<BlockPos> returnSet = new HashSet<>();

    public FloodFillPathfinder(Level world, BlockPos startingPos, Block block, BlockState targetState, boolean up, boolean down) {
        this.world = world;
        this.startingPos = startingPos;
        this.block = block;
        this.targetState = targetState;
        this.searchUpwards = up;
        this.searchDownwards = down;
        this.maxDist = AWStructureStatics.constructionFloodFillMaxDistance;
    }

    public Set<BlockPos> doFloodFill() {
        openList.add(startingPos);
        BlockPos pos;
        while (!openList.isEmpty()) {
            pos = openList.remove(0);
            returnSet.add(pos);
            addNeighbors(pos);
            for (BlockPos p1 : neighborCache) {
                if (returnSet.contains(p1) || closedList.contains(p1) || openList.contains(p1)) {
                    continue;
                }
                if (isValidPosition(p1)) {
                    openList.add(p1);
                }
            }
            neighborCache.clear();
        }
        return returnSet;
    }

    private boolean isValidPosition(BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return isWithinDist(pos) && state.getBlock() == block && state.equals(targetState);
    }

    private boolean isWithinDist(BlockPos pos) {
        return pos.getX() >= startingPos.getX() - maxDist && pos.getX() <= startingPos.getX() + maxDist && pos.getY() >= startingPos.getY() - maxDist && pos.getY() <= startingPos.getY() + maxDist && pos.getZ() >= startingPos.getZ() - maxDist && pos.getZ() <= startingPos.getZ() + maxDist;
    }

    private void addNeighbors(BlockPos pos) {
        neighborCache.add(new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ()));
        neighborCache.add(new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ()));
        neighborCache.add(new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1));
        neighborCache.add(new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1));
        if (searchUpwards) {
            neighborCache.add(new BlockPos(pos.getX() - 1, pos.getY() + 1, pos.getZ()));
            neighborCache.add(new BlockPos(pos.getX() + 1, pos.getY() + 1, pos.getZ()));
            neighborCache.add(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ() - 1));
            neighborCache.add(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ() + 1));
        }
        if (searchDownwards) {
            neighborCache.add(new BlockPos(pos.getX() - 1, pos.getY() - 1, pos.getZ()));
            neighborCache.add(new BlockPos(pos.getX() + 1, pos.getY() - 1, pos.getZ()));
            neighborCache.add(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ() - 1));
            neighborCache.add(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ() + 1));
        }
    }

}
