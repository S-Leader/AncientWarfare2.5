package net.shadowmage.ancientwarfare.core.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.shadowmage.ancientwarfare.core.item.ItemBase;

/**
 * Transitional helper for items that still keep their 1.12 registry id on the item instance.
 */
public final class LegacyRegistryHelper {
    private LegacyRegistryHelper() {
    }

    public static <T extends Item> T register(RegisterEvent.RegisterHelper<Item> helper, T item) {
        helper.register(getId(item), item);
        return item;
    }

    public static ResourceLocation getId(Item item) {
        if (item instanceof ItemBase legacyItem) {
            return legacyItem.getLegacyRegistryName();
        }
        ResourceLocation registered = ForgeRegistries.ITEMS.getKey(item);
        if (registered != null) {
            return registered;
        }
        throw new IllegalArgumentException("Item has no legacy registry id: " + item.getClass().getName());
    }
}
