package net.shadowmage.ancientwarfare.core.crafting.wrappers;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.shadowmage.ancientwarfare.core.crafting.ICraftingRecipe;
import net.shadowmage.ancientwarfare.core.crafting.RecipeResourceLocation;

import java.util.Optional;

public class RegularCraftingWrapper implements ICraftingRecipe {
    private final Recipe<CraftingContainer> recipe;
    private final RegistryAccess registryAccess;
    private final RecipeResourceLocation registryName;

    public RegularCraftingWrapper(Recipe<CraftingContainer> recipe, RegistryAccess registryAccess) {
        this.recipe = recipe;
        this.registryAccess = registryAccess;
        registryName = new RecipeResourceLocation(RecipeResourceLocation.RecipeType.REGULAR, recipe.getId());
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return recipe.getIngredients();
    }

    @Override
    public ItemStack getCraftingResult(CraftingContainer inv) {
        return recipe.assemble(inv, registryAccess);
    }

    @Override
    public ItemStack getRecipeOutput() {
        return recipe.getResultItem(registryAccess);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        return recipe.getRemainingItems(inv);
    }

    @Override
    public RecipeResourceLocation getRegistryName() {
        return registryName;
    }

    @Override
    public Optional<String> getNeededResearch() {
        return Optional.empty();
    }

    @Override
    public int getRecipeWidth() {
        return recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : 3;
    }

    @Override
    public int getRecipeHeight() {
        return recipe instanceof ShapedRecipe shaped ? shaped.getHeight() : 3;
    }
}
