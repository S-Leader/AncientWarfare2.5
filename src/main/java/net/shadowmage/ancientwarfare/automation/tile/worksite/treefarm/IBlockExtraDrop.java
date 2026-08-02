package net.shadowmage.ancientwarfare.automation.tile.worksite.treefarm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface IBlockExtraDrop {
    boolean matches(BlockState state);

    NonNullList<ItemStack> getDrops(BlockGetter world, BlockPos pos, BlockState state, int fortune);
}
