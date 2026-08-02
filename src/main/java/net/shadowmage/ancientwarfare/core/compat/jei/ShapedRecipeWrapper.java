package net.shadowmage.ancientwarfare.core.compat.jei;

import net.minecraftforge.common.crafting.IShapedRecipe;

/**
 * 1.12 extended JEI's internal ShapelessRecipeWrapper to expose shaped dimensions;
 * JEI 15 has no recipe wrappers, so this is a plain holder for the shaped dimensions.
 */
public class ShapedRecipeWrapper {
    private final IShapedRecipe<?> recipe;

    public ShapedRecipeWrapper(IShapedRecipe<?> recipe) {
        this.recipe = recipe;
    }

    public int getWidth() {
        return recipe.getRecipeWidth();
    }

    public int getHeight() {
        return recipe.getRecipeHeight();
    }
}
