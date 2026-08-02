package net.shadowmage.ancientwarfare.core.research;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Filters synthetic Forge ingredient entries before they reach research GUIs.
 */
public final class ResearchIngredientDisplay {
    private ResearchIngredientDisplay() {
    }

    public static List<ItemStack> getDisplayStacks(Ingredient ingredient) {
        if (ingredient == null) {
            return Collections.emptyList();
        }

        List<ItemStack> displayStacks = new ArrayList<>();
        for (ItemStack stack : ingredient.getItems()) {
            if (isDisplayStack(stack)) {
                displayStacks.add(stack.copy());
            }
        }
        return displayStacks;
    }

    public static ItemStack getFirstDisplayStack(Ingredient ingredient) {
        List<ItemStack> stacks = getDisplayStacks(ingredient);
        return stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0);
    }

    private static boolean isDisplayStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        /*
         * Forge represents an unresolved item tag in Ingredient#getItems() with a
         * synthetic barrier named "Empty Tag: <id>". It is a display diagnostic,
         * not a valid substitute for the research ingredient.
         */
        if (!stack.is(Items.BARRIER)) {
            return true;
        }
        return !stack.getHoverName().getString().startsWith("Empty Tag:");
    }
}
