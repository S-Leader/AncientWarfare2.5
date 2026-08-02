package net.shadowmage.ancientwarfare.automation.tile.worksite.treefarm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ChorusFlowerDrop implements IBlockExtraDrop {

    @Override
    public boolean matches(BlockState state) {
        return state.getBlock() == Blocks.CHORUS_FLOWER;
    }

    @Override
    public NonNullList<ItemStack> getDrops(BlockGetter world, BlockPos pos, BlockState state, int fortune) {
        return NonNullList.of(ItemStack.EMPTY, new ItemStack(Blocks.CHORUS_FLOWER.asItem()));
    }
}
