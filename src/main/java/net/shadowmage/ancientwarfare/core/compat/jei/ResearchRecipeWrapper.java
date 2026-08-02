package net.shadowmage.ancientwarfare.core.compat.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.crafting.ResearchRecipeBase;
import net.shadowmage.ancientwarfare.core.registry.ResearchRegistry;
import net.shadowmage.ancientwarfare.core.research.ResearchIngredientDisplay;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ResearchRecipeWrapper {
    protected final ResearchRecipeBase recipe;

    ResearchRecipeWrapper(ResearchRecipeBase recipe) {
        this.recipe = recipe;
    }

    public List<List<ItemStack>> getInputs() {
        return recipe.getIngredients().stream()
                .map(ResearchIngredientDisplay::getDisplayStacks)
                .collect(Collectors.toList());
    }

    public List<ItemStack> getOutputs() {
        return Collections.singletonList(recipe.getRecipeOutput());
    }

    @OnlyIn(Dist.CLIENT)
    public void drawInfo(GuiGraphics guiGraphics) {
        //noinspection ConstantConditions
        String research = AWCoreStatics.useResearchSystem ? I18n.get(ResearchRegistry.getResearch(recipe.getNeededResearch()).getDescriptionId()) : "Research disabled";
        guiGraphics.drawString(Minecraft.getInstance().font, research, 60, 0, 0x444444, false);
    }
}
