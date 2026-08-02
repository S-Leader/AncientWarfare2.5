package net.shadowmage.ancientwarfare.automation.tile.worksite.cropfarm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.util.parsing.BlockStateMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class CropTall extends CropDefault {
    private int height = 1;
    private BlockStateMatcher stateMatcher;

    public CropTall(BlockStateMatcher stateMatcher, int height) {
        this.stateMatcher = stateMatcher;
        this.height = height;
    }

    @Override
    public List<BlockPos> getPositionsToHarvest(Level world, BlockPos pos, BlockState state) {
        List<BlockPos> ret = new ArrayList<>();
        applyToCrop(world, pos, (p, s) -> {
            if (s.getBlock() instanceof BonemealableBlock growable && !growable.isValidBonemealTarget(world, p, s, world.isClientSide)) {
                ret.add(p);
            }
            return true;
        });
        return ret;
    }

    @Override
    protected boolean breakCrop(Level world, BlockPos pos, BlockState state) {
        return applyToCrop(world, pos, (p, s) -> super.breakCrop(world, p, s));
    }

    @Override
    protected void getDrops(NonNullList<ItemStack> stacks, Level world, BlockPos pos, BlockState state, int fortune) {
        applyToCrop(world, pos, (p, s) -> {
            super.getDrops(stacks, world, p, s, fortune);
            return true;
        });
    }

    private boolean applyToCrop(Level world, BlockPos pos, BiFunction<BlockPos, BlockState, Boolean> applyToBlock) {
        boolean ret = true;
        for (BlockPos curPos = new BlockPos(pos.getX(), pos.getY() + (height - 1), pos.getZ()); curPos.getY() >= pos.getY(); curPos = curPos.below()) {
            BlockState curState = world.getBlockState(curPos);
            ret = ret && applyToBlock.apply(curPos, curState);
        }
        return ret;
    }

    @Override
    public boolean matches(BlockState state) {
        return stateMatcher.test(state);
    }
}
