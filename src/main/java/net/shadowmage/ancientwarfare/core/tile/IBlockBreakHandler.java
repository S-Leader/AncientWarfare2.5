package net.shadowmage.ancientwarfare.core.tile;

import net.minecraft.world.level.block.state.BlockState;

public interface IBlockBreakHandler {
    void onBlockBroken(BlockState state);
}
