package net.shadowmage.ancientwarfare.core.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects the variant stacks formerly supplied by getSubItems/getSubBlocks.
 */
public final class LegacyCreativeTabContents {
    private LegacyCreativeTabContents() {
    }

    /**
     * Registration-time-safe check: does this item (or its block) declare a legacy
     * variant supplier? Must not construct ItemStacks — registries are not ready
     * while registry population is still in progress.
     */
    public static boolean suppliesVariants(Item item) {
        if (hasMethod(item, "getSubItems")) {
            return true;
        }
        return item instanceof BlockItem blockItem && hasMethod(blockItem.getBlock(), "getSubBlocks");
    }

    private static boolean hasMethod(Object target, String name) {
        try {
            target.getClass().getMethod(name, CreativeModeTab.class, NonNullList.class);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

    public static List<ItemStack> stacksFor(Item item) {
        NonNullList<ItemStack> variants = NonNullList.create();
        boolean supplied = invoke(item, "getSubItems", variants);
        if (item instanceof BlockItem blockItem) {
            supplied |= invoke(blockItem.getBlock(), "getSubBlocks", variants);
        }
        variants.removeIf(ItemStack::isEmpty);
        if (!supplied || variants.isEmpty()) {
            return List.of(new ItemStack(item));
        }
        return new ArrayList<>(variants);
    }

    private static boolean invoke(Object target, String name, NonNullList<ItemStack> output) {
        try {
            Method method = target.getClass().getMethod(name, CreativeModeTab.class, NonNullList.class);
            method.invoke(target, null, output);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        } catch (IllegalAccessException | InvocationTargetException ex) {
            AncientWarfareCore.LOG.error("Unable to collect legacy creative variants from {}", target.getClass().getName(), ex);
            return false;
        }
    }
}
