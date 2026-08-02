package net.shadowmage.ancientwarfare.automation.tile.worksite.treefarm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChorusScanner implements ITreeScanner {
    @Override
    public ITree scanTree(Level world, BlockPos pos, BlockPos minPos, BlockPos maxPos) {
        Branch branch = new Branch();
        Set<BlockPos> scannedPositions = new HashSet<>();
        scanBranchHarvestableBlocks(scannedPositions, branch, world, pos, Direction.DOWN);
        return branch;
    }

    private void scanBranchHarvestableBlocks(Set<BlockPos> scannedPositions, Branch parentBranch, Level world, BlockPos startPos, Direction avoidDirection) {
        if (!scannedPositions.add(startPos)) {
            return;
        }

        boolean continueSearch;
        BlockPos currentPos = startPos;
        Direction avoidNext = avoidDirection;

        Branch childBranch = new Branch();
        parentBranch.addChildBranch(childBranch);

        do {
            continueSearch = false;

            childBranch.addTrunkPos(currentPos);

            BlockState state = world.getBlockState(currentPos);
            if (state.getBlock() == Blocks.CHORUS_FLOWER) {
                if (state.getValue(ChorusFlowerBlock.AGE) == 5) {
                    childBranch.setMature();
                }
                return;
            }

            List<Direction> connectedSides = getConnectedSides(avoidNext, world, currentPos);

            if (connectedSides.size() == 1) {
                continueSearch = true;
                Direction nextFacing = connectedSides.get(0);
                currentPos = currentPos.relative(nextFacing);
                if (!scannedPositions.add(currentPos)) {
                    return;
                }

                avoidNext = nextFacing.getOpposite();
            } else if (connectedSides.size() > 1) {
                //multiple branches attached
                scanConnectedBranchsBlocks(scannedPositions, childBranch, world, currentPos, connectedSides);
                childBranch.updateMature();
                return;
            }
        } while (continueSearch);

        //there's no chorus flower at the end of this branch so let's harvest it
        childBranch.setMature();
    }

    private void scanConnectedBranchsBlocks(Set<BlockPos> scannedPositions, Branch parentBranch, Level world, BlockPos currentPos, List<Direction> connectedSides) {
        for (Direction side : connectedSides) {
            scanBranchHarvestableBlocks(scannedPositions, parentBranch, world, currentPos.relative(side), side.getOpposite());
        }
    }

    private List<Direction> getConnectedSides(Direction avoidDirection, Level world, BlockPos pos) {
        List<Direction> connectedSides = new ArrayList<>();
        for (Direction facing : Direction.values()) {
            if (facing != Direction.DOWN && facing != avoidDirection && plantIsConnectedOnSide(world, pos, facing)) {
                connectedSides.add(facing);
            }
        }
        return connectedSides;
    }

    private boolean plantIsConnectedOnSide(Level world, BlockPos pos, Direction side) {
        Block block = world.getBlockState(pos.relative(side)).getBlock();

        return block == Blocks.CHORUS_PLANT || block == Blocks.CHORUS_FLOWER;
    }

    @Override
    public boolean matches(BlockState state) {
        return state.getBlock() == Blocks.CHORUS_PLANT;
    }
}
