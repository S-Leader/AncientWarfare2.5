package net.shadowmage.ancientwarfare.core.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.event.ForgeEventFactory;
import net.shadowmage.ancientwarfare.core.crafting.ICraftingRecipe;
import net.shadowmage.ancientwarfare.core.crafting.IIngredientCount;
import net.shadowmage.ancientwarfare.core.tile.CraftingRecipeMemory;

import java.util.ArrayList;
import java.util.List;

/**
 * Result slot for Ancient Warfare research recipes. It consumes the custom
 * ingredient counts itself instead of relying on vanilla Recipe#getRemainingItems.
 */
public class SlotResearchCrafting extends Slot {
    private final CraftingContainer craftMatrix;
    private final Player player;
    private final CraftingRecipeMemory craftingRecipeMemory;
    private int amountCrafted;

    public SlotResearchCrafting(Player player, CraftingRecipeMemory craftingRecipeMemory, CraftingContainer craftingInventory,
                                Container resultInventory, int slotIndex, int xPosition, int yPosition) {
        super(resultInventory, slotIndex, xPosition, yPosition);
        this.player = player;
        this.craftMatrix = craftingInventory;
        this.craftingRecipeMemory = craftingRecipeMemory;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack remove(int amount) {
        if (hasItem()) {
            amountCrafted += Math.min(amount, getItem().getCount());
        }
        return super.remove(amount);
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int amount) {
        amountCrafted += amount;
        checkTakeAchievements(stack);
    }

    @Override
    protected void onSwapCraft(int amount) {
        amountCrafted += amount;
    }

    @Override
    protected void checkTakeAchievements(ItemStack stack) {
        ResultContainer result = (ResultContainer) container;
        Recipe<?> recipe = result.getRecipeUsed();
        if (amountCrafted > 0) {
            stack.onCraftedBy(player.level(), player, amountCrafted);
            ForgeEventFactory.firePlayerCraftingEvent(player, stack, craftMatrix);
        }
        amountCrafted = 0;
        if (recipe != null && !recipe.isSpecial()) {
            player.awardRecipes(List.of(recipe));
            result.setRecipeUsed(null);
        }
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        checkTakeAchievements(stack);

        ICraftingRecipe recipe = craftingRecipeMemory.getRecipe();
        NonNullList<ItemStack> remainingItems = recipe.getRemainingItems(craftMatrix);
        List<Integer> usedIngredients = new ArrayList<>();

        for (int slot = 0; slot < remainingItems.size(); ++slot) {
            ItemStack input = craftMatrix.getItem(slot);
            ItemStack remainder = remainingItems.get(slot);
            if (!input.isEmpty()) {
                NonNullList<Ingredient> ingredients = recipe.getIngredients();
                ItemStack inputForMatch = input;
                Ingredient matched = ingredients.stream()
                        .filter(ingredient -> !usedIngredients.contains(ingredients.indexOf(ingredient)) && ingredient.test(inputForMatch))
                        .findFirst()
                        .orElse(Ingredient.EMPTY);
                int ingredientIndex = ingredients.indexOf(matched);
                if (ingredientIndex >= 0) {
                    usedIngredients.add(ingredientIndex);
                }
                int consumeCount = matched instanceof IIngredientCount counted ? counted.getCount() : 1;
                craftMatrix.removeItem(slot, consumeCount);
                input = craftMatrix.getItem(slot);
            }

            if (!remainder.isEmpty()) {
                if (input.isEmpty()) {
                    craftMatrix.setItem(slot, remainder);
                } else if (ItemStack.isSameItemSameTags(input, remainder)) {
                    remainder.grow(input.getCount());
                    craftMatrix.setItem(slot, remainder);
                } else if (!this.player.getInventory().add(remainder)) {
                    this.player.drop(remainder, false);
                }
            }
        }
        super.onTake(player, stack);
    }
}
