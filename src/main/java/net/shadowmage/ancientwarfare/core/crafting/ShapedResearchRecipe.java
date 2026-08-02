package net.shadowmage.ancientwarfare.core.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class ShapedResearchRecipe extends ResearchRecipeBase {
    private final int width;
    private final int height;

    public ShapedResearchRecipe(String research, ItemStack output, int width, int height, NonNullList<Ingredient> input) {
        super(research, input, output);
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean matches(@Nonnull CraftingContainer inv, @Nonnull Level level) {
        for (int x = 0; x <= inv.getWidth() - width; x++) {
            for (int y = 0; y <= inv.getHeight() - height; y++) {
                if (checkMatch(inv, x, y, false) || checkMatch(inv, x, y, true)) return true;
            }
        }
        return false;
    }

    private boolean checkMatch(CraftingContainer inv, int startX, int startY, boolean mirror) {
        for (int x = 0; x < inv.getWidth(); x++) {
            for (int y = 0; y < inv.getHeight(); y++) {
                int subX = x - startX;
                int subY = y - startY;
                Ingredient target = Ingredient.EMPTY;
                if (subX >= 0 && subY >= 0 && subX < width && subY < height) {
                    target = getIngredients().get((mirror ? width - subX - 1 : subX) + subY * width);
                }
                if (!target.test(inv.getItem(x + y * inv.getWidth()))) return false;
            }
        }
        return true;
    }

    public int getRecipeWidth() {
        return width;
    }

    public int getRecipeHeight() {
        return height;
    }
}
