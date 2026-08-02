package net.shadowmage.ancientwarfare.structure.render;

import net.minecraft.world.level.block.Block;

/**
 * Compatibility marker retained for old callers. Extended state containers were
 * replaced by Forge ModelData in 1.13 and later.
 */
@Deprecated
public final class BlankExtendedBlockStateContainer {
    private final Block block;

    public BlankExtendedBlockStateContainer(Block block) {
        this.block = block;
    }

    public Block block() {
        return block;
    }
}
