package net.shadowmage.ancientwarfare.structure.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface IStructureBuilder {

    boolean placeBlock(BlockPos pos, BlockState state, int priority);
}
