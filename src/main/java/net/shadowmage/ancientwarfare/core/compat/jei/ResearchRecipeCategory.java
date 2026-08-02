package net.shadowmage.ancientwarfare.core.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

import java.util.List;

public abstract class ResearchRecipeCategory implements IRecipeCategory<ResearchRecipeWrapper> {
    private static final int WIDTH = 116;
    private static final int HEIGHT = 54;
    private final IDrawable background;
    private final ICraftingGridHelper craftingGridHelper;
    private final IDrawable icon;

    public ResearchRecipeCategory(IGuiHelper guiHelper) {
        background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        craftingGridHelper = guiHelper.createCraftingGridHelper();
        icon = guiHelper.drawableBuilder(new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/items/core/research_book.png"), 0, 0, 16, 16).setTextureSize(16, 16).build();
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ResearchRecipeWrapper recipeWrapper, IFocusGroup focuses) {
        List<List<ItemStack>> inputs = recipeWrapper.getInputs();

        if (recipeWrapper instanceof ShapedResearchRecipeWrapper) {
            ShapedResearchRecipeWrapper shapedWrapper = (ShapedResearchRecipeWrapper) recipeWrapper;
            craftingGridHelper.createAndSetInputs(builder, inputs, shapedWrapper.getWidth(), shapedWrapper.getHeight());
        } else {
            craftingGridHelper.createAndSetInputs(builder, inputs, 0, 0);
        }
        craftingGridHelper.createAndSetOutputs(builder, recipeWrapper.getOutputs());
    }

    @Override
    public void draw(ResearchRecipeWrapper recipeWrapper, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        recipeWrapper.drawInfo(guiGraphics);
    }
}
