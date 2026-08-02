package net.shadowmage.ancientwarfare.core.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Optional;

public interface ICraftingRecipe {
    boolean isValid();

    NonNullList<Ingredient> getIngredients();

    ItemStack getCraftingResult(CraftingContainer inv);

    ItemStack getRecipeOutput();

    NonNullList<ItemStack> getRemainingItems(CraftingContainer invCrafting);

    RecipeResourceLocation getRegistryName();

    Optional<String> getNeededResearch();

    int getRecipeWidth();

    int getRecipeHeight();
}
