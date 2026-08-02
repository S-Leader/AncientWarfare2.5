package net.shadowmage.ancientwarfare.automation.tile.worksite.treefarm;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ITreeScanner {
    ITree scanTree(Level world, BlockPos pos, BlockPos minPos, BlockPos maxPos);

    boolean matches(BlockState state);
}
