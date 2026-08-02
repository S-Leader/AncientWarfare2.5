package net.shadowmage.ancientwarfare.core.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * Small compatibility index for Ancient Warfare's metadata sub-items.
 * Normal 1.20.1 recipes should use item tags; this remains for legacy code that
 * differentiates variants stored in ItemStack damage/NBT.
 */
public final class LegacyOreDictionary {
    private static final Map<String, List<ItemStack>> ENTRIES = new HashMap<>();

    private LegacyOreDictionary() {
    }

    public static void registerOre(String name, Item item) {
        registerOre(name, new ItemStack(item));
    }

    public static void registerOre(String name, ItemStack stack) {
        if (name == null || name.isBlank() || stack.isEmpty()) {
            return;
        }
        ENTRIES.computeIfAbsent(name, ignored -> new ArrayList<>()).add(stack.copy());
    }

    public static List<ItemStack> getOres(String name) {
        List<ItemStack> entries = ENTRIES.get(name);
        if (entries == null) {
            return Collections.emptyList();
        }
        return entries.stream().map(ItemStack::copy).toList();
    }

    public static boolean itemMatches(ItemStack target, ItemStack input, boolean strict) {
        if (target.isEmpty() || input.isEmpty() || !target.is(input.getItem())) {
            return false;
        }
        return !strict || ItemStack.isSameItemSameTags(target, input);
    }
}
