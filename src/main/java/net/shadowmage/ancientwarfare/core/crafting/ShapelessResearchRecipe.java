package net.shadowmage.ancientwarfare.core.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ShapelessResearchRecipe extends ResearchRecipeBase {
    public ShapelessResearchRecipe(String research, NonNullList<Ingredient> ingredients, ItemStack result) {
        super(research, ingredients, result);
    }

    @Override
    public boolean matches(@Nonnull CraftingContainer inv, @Nonnull Level level) {
        List<Ingredient> remaining = new ArrayList<>(getIngredients());
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            int match = -1;
            for (int ingredient = 0; ingredient < remaining.size(); ingredient++) {
                if (remaining.get(ingredient).test(stack)) {
                    match = ingredient;
                    break;
                }
            }
            if (match < 0) return false;
            remaining.remove(match);
        }
        return remaining.isEmpty();
    }
}
