package net.shadowmage.ancientwarfare.core.compat.jei;

import net.shadowmage.ancientwarfare.core.crafting.ShapedResearchRecipe;

public class ShapedResearchRecipeWrapper extends ResearchRecipeWrapper {
    private final ShapedResearchRecipe shapedRecipe;

    public ShapedResearchRecipeWrapper(ShapedResearchRecipe recipe) {
        super(recipe);
        this.shapedRecipe = recipe;
    }

    public int getWidth() {
        return shapedRecipe.getRecipeWidth();
    }

    public int getHeight() {
        return shapedRecipe.getRecipeHeight();
    }
}
