package net.shadowmage.ancientwarfare.core.render.model;

import net.minecraft.world.level.block.Block;
import net.shadowmage.ancientwarfare.core.render.BlockStateKeyGenerator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LegacyModelBakery {
    static final Map<Block, BlockStateKeyGenerator> KEY_GENERATORS = new ConcurrentHashMap<>();
    static final Map<Block, LegacyBakery> BAKERIES = new ConcurrentHashMap<>();

    private LegacyModelBakery() {
    }

    public static void registerBlockKeyGenerator(Block block, BlockStateKeyGenerator generator) {
        KEY_GENERATORS.put(block, generator);
        if (block instanceof LegacyBakeryProvider provider) {
            BAKERIES.put(block, provider.getBakery());
        }
    }
}
