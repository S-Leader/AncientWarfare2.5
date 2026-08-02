package net.shadowmage.ancientwarfare.core.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.registry.ResearchRegistry;

import javax.annotation.Nonnull;

public abstract class ResearchRecipeBase {
    private ResourceLocation id;
    private String neededResearch;
    private ItemStack output;
    private NonNullList<Ingredient> input;

    ResearchRecipeBase(String research, NonNullList<Ingredient> input, ItemStack output) {
        addResearch(research);
        this.input = input;
        this.output = output;
    }

    public String getNeededResearch() {
        return neededResearch;
    }

    public ItemStack getCraftingResult() {
        return output.copy();
    }

    public ItemStack getRecipeOutput() {
        return output;
    }

    public NonNullList<Ingredient> getIngredients() {
        return input;
    }

    public ResourceLocation getId() {
        return id;
    }

    public void setId(ResourceLocation id) {
        this.id = id;
    }

    private void addResearch(String name) {
        if (!ResearchRegistry.researchExists(name)) {
            throw new IllegalArgumentException("COULD NOT LOCATE RESEARCH GOAL FOR NAME: " + name);
        }

        neededResearch = name;
    }

    abstract boolean matches(@Nonnull CraftingContainer inv, @Nonnull Level world);

}
