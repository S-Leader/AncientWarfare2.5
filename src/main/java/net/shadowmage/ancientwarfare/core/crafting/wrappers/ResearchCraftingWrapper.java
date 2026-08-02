package net.shadowmage.ancientwarfare.core.crafting.wrappers;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.shadowmage.ancientwarfare.core.crafting.ICraftingRecipe;
import net.shadowmage.ancientwarfare.core.crafting.RecipeResourceLocation;
import net.shadowmage.ancientwarfare.core.crafting.ResearchRecipeBase;
import net.shadowmage.ancientwarfare.core.crafting.ShapedResearchRecipe;

import java.util.Optional;

public class ResearchCraftingWrapper implements ICraftingRecipe {
    private static final NonNullList<ItemStack> EMPTY_STACK_LIST = NonNullList.withSize(9, ItemStack.EMPTY);
    private final ResearchRecipeBase recipe;
    private final RecipeResourceLocation registryName;

    public ResearchCraftingWrapper(ResearchRecipeBase researchRecipe) {
        this.recipe = researchRecipe;
        if (researchRecipe.getId() == null) {
            throw new IllegalArgumentException("Null registryName recipes are not allowed here");
        }
        this.registryName = new RecipeResourceLocation(RecipeResourceLocation.RecipeType.RESEARCH, researchRecipe.getId());
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
        return recipe.getCraftingResult();
    }

    @Override
    public ItemStack getRecipeOutput() {
        return recipe.getRecipeOutput();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer invCrafting) {
        return EMPTY_STACK_LIST;
    }

    @Override
    public RecipeResourceLocation getRegistryName() {
        return registryName;
    }

    @Override
    public Optional<String> getNeededResearch() {
        return Optional.of(recipe.getNeededResearch());
    }

    @Override
    public int getRecipeWidth() {
        return recipe instanceof ShapedResearchRecipe ? ((ShapedResearchRecipe) recipe).getRecipeWidth() : 3;
    }

    @Override
    public int getRecipeHeight() {
        return recipe instanceof ShapedResearchRecipe ? ((ShapedResearchRecipe) recipe).getRecipeHeight() : 3;
    }
}
