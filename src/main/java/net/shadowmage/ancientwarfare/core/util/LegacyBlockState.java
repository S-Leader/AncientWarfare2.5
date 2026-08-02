package net.shadowmage.ancientwarfare.core.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.block.BlockBase;

import java.util.List;

/**
 * Transitional codec for AW2 data that still stores the old 0-15 block metadata.
 * Custom AW blocks retain their explicit mapping methods. For foreign blocks,
 * metadata is mapped to the registered state's stable possible-state order so
 * old templates remain loadable instead of crashing on removed 1.12 methods.
 */
public final class LegacyBlockState {
    private LegacyBlockState() {
    }

    public static BlockState fromMeta(Block block, int meta) {
        if (block instanceof BlockBase legacyBlock) {
            return legacyBlock.getStateFromMeta(meta);
        }
        List<BlockState> states = block.getStateDefinition().getPossibleStates();
        if (states.isEmpty()) {
            return block.defaultBlockState();
        }
        return states.get(Math.floorMod(meta, states.size()));
    }

    public static int toMeta(BlockState state) {
        if (state.getBlock() instanceof BlockBase legacyBlock) {
            return legacyBlock.getMetaFromState(state);
        }
        int index = state.getBlock().getStateDefinition().getPossibleStates().indexOf(state);
        return Math.max(index, 0);
    }
}
