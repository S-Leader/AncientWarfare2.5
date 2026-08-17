package net.shadowmage.ancientwarfare.core.util;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class SortItemsFirstComparator implements Comparator<ItemStack> {
    private Map<Predicate<ItemStack>, Integer> firstElements = new HashMap<>();

    public SortItemsFirstComparator(Object... firstElements) {
        for (int i = 0; i < firstElements.length; i++) {
            Object element = firstElements[i];

            Predicate<ItemStack> matches;
            if (element instanceof Item item) {
                matches = s -> s.getItem() == item;
            } else if (element instanceof Block block) {
                matches = s -> s.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == block;
            } else if (element instanceof Class<?> type && Block.class.isAssignableFrom(type)) {
                matches = s -> s.getItem() instanceof BlockItem blockItem
                        && type.isAssignableFrom(blockItem.getBlock().getClass());
            } else if (element instanceof Class<?> type && Item.class.isAssignableFrom(type)) {
                matches = s -> type.isAssignableFrom(s.getItem().getClass());
            } else {
                // RegistryObject/Supplier/etc. are not sort keys. Callers using DeferredRegister
                // must pass the resolved registered value (RegistryObject#get()).
                continue;
            }

            this.firstElements.put(matches, firstElements.length - i);
        }
    }

    @Override
    public int compare(ItemStack o1, ItemStack o2) {
        if (o1 == o2 || o1.getItem() == o2.getItem()) {
            return 0;
        }

        int sortWeight1 = 0;
        int sortWeight2 = 0;
        for (Map.Entry<Predicate<ItemStack>, Integer> entry : firstElements.entrySet()) {
            if (entry.getKey().test(o1)) {
                sortWeight1 = entry.getValue();
            }
            if (entry.getKey().test(o2)) {
                sortWeight2 = entry.getValue();
            }

            if (sortWeight1 > 0 && sortWeight2 > 0) {
                break;
            }
        }

        return sortWeight2 - sortWeight1;
    }
}
