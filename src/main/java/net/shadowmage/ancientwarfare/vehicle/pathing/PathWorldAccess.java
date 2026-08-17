package net.shadowmage.ancientwarfare.vehicle.pathing;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.tile.TEGateProxy;

public class PathWorldAccess {

    public boolean canOpenDoors;
    public boolean canSwim;
    public boolean canDrop;
    public boolean canUseLaders;
    private boolean canGoOnLand = true;

    private Level world;

    public PathWorldAccess(Level world) {
        this.world = world;
    }

    public void setCanGoOnLand(boolean val) {
        this.canGoOnLand = val;
        if (!val) {
            this.canSwim = true;
        }
    }

    public Block getBlock(BlockPos pos) {
        return world.getBlockState(pos).getBlock();
    }

    public int getTravelCost(BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getFluidState().is(FluidTags.WATER)) {//can't swim check
            if (!canGoOnLand) {
                return 10;
            }
            return 30;
        }
        if (!canGoOnLand) {
            return 30;
        }
        return 10;
    }

    /**
     * checks the collision bounds of the block at x,y,z to make sure it is <= 0.5 tall (pathable)
     *
     * @return true if it is a pathable block, false if it fails bounds checks
     */
    public boolean checkBlockBounds(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (state.getFluidState().is(FluidTags.WATER)) {
            return true;
        } else if (block instanceof TrapDoorBlock) {
            return state.getValue(TrapDoorBlock.OPEN);
        }
        if (!state.isAir()) {
            VoxelShape shape = state.getCollisionShape(world, pos);
            if (shape.isEmpty()) {
                return true;
            }
            if (shape.bounds().maxY >= 0.5d) {
                return false;
            }
        }
        return true;
    }

    private boolean isWalkable2(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        Block block = getBlock(pos);
        Block blockDown = getBlock(pos.below());
        Block blockUp = getBlock(pos.above());
        boolean cube = !checkBlockBounds(x, y, z);
        boolean cube2 = !checkBlockBounds(x, y - 1, z);
        boolean cube3 = !checkBlockBounds(x, y + 1, z);
        if (isFence(blockDown) || (isDoor(pos.below()) && isDoor(pos)) || (block == Blocks.CACTUS || blockDown == Blocks.CACTUS || blockUp == Blocks.CACTUS)) {
            return false;
        }
        if (canGoOnLand) {
            if (canUseLaders && isLadder(block)) {
                return true;
            }
            if (canOpenDoors && isDoor(pos) && cube2) {
                return true;
            }
            if (!cube && !cube3 && (cube2 || canSupport(block, pos)))//finally, check if block and blockY+1 are clear and blockY-1 is solid
            {
                return true;
            }
        }
        return canSwim && isWater(block) && blockUp == Blocks.AIR;
    }

    public boolean isPartialBlock(BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!state.isAir()) {
            VoxelShape shape = state.getCollisionShape(world, pos);
            if (shape.isEmpty()) {
                return false;
            }
            AABB bb = shape.bounds();
            if (bb.maxY <= 0.75d && bb.minX < 0.35 && bb.maxX > 0.65 && bb.minZ < 0.35 && bb.maxZ > 0.65) {
                return true;
            }
        }
        return false;
    }

    private boolean canSupport(Block block, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (block instanceof TrapDoorBlock) {
            return !state.getValue(TrapDoorBlock.OPEN) && state.getValue(TrapDoorBlock.HALF) == Half.BOTTOM;
        }
        VoxelShape shape = state.getCollisionShape(world, pos);
        if (shape.isEmpty()) {
            return false;
        }
        AABB bb = shape.bounds();
        return bb.maxY <= 0.5d && bb.minX < 0.35 && bb.maxX > 0.65 && bb.minZ < 0.35 && bb.maxZ > 0.65;
    }

    private boolean isFence(Block block) {
        return block instanceof FenceBlock || block instanceof FenceGateBlock || block == Blocks.COBBLESTONE_WALL;
    }

    public boolean isWalkable(int x, int y, int z) {
        return isWalkable2(x, y, z);
    }

    private boolean isWater(Block block) {
        return block == Blocks.WATER;
    }

    public boolean isDoor(BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block == AWStructureBlocks.GATE_PROXY.get()) {
            return WorldTools.getTile(world, pos, TEGateProxy.class)
                    .map(proxy -> proxy.getOwner().map(p -> p.getGateType().canSoldierActivate()).orElse(false)).orElse(true);
        }
        return (block instanceof DoorBlock && state.is(BlockTags.WOODEN_DOORS)) || block instanceof FenceGateBlock;
    }

    private boolean isLadder(Block block) {
        return block == Blocks.LADDER || block == Blocks.VINE;
    }

    protected boolean isLadder(BlockPos pos) {
        return world.getBlockState(pos).is(BlockTags.CLIMBABLE);
    }

    public boolean isWalkable(int x, int y, int z, Node src) {
        return this.isWalkable(x, y, z);
    }

    public boolean isRemote() {
        return world.isClientSide;
    }

}
