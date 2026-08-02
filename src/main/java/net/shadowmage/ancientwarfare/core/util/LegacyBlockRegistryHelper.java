package net.shadowmage.ancientwarfare.core.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.shadowmage.ancientwarfare.core.block.BlockBase;

/**
 * Transitional registration bridge for blocks and their BlockItems.
 */
public final class LegacyBlockRegistryHelper {
    private LegacyBlockRegistryHelper() {
    }

    public static <T extends Block> T register(RegisterEvent.RegisterHelper<Block> helper, T block) {
        helper.register(getId(block), block);
        return block;
    }

    public static <T extends BlockItem> T registerItem(RegisterEvent.RegisterHelper<Item> helper, T item) {
        helper.register(getId(item.getBlock()), item);
        return item;
    }

    public static ResourceLocation getId(Block block) {
        if (block instanceof BlockBase legacyBlock) {
            return legacyBlock.getLegacyRegistryName();
        }
        if (block instanceof ILegacyRegistryName named) {
            return named.getRegistryName();
        }
        ResourceLocation registered = ForgeRegistries.BLOCKS.getKey(block);
        //The blocks registry is defaulted: unregistered blocks come back as minecraft:air, not null.
        if (registered != null && !registered.equals(ForgeRegistries.BLOCKS.getDefaultKey())) {
            return registered;
        }
        throw new IllegalArgumentException("Block has no legacy registry id: " + block.getClass().getName());
    }
}
