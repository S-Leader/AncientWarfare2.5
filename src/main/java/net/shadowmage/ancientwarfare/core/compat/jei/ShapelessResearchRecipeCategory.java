package net.shadowmage.ancientwarfare.core.compat.jei;

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

public class ShapelessResearchRecipeCategory extends ResearchRecipeCategory {
    public static final RecipeType<ResearchRecipeWrapper> TYPE = RecipeType.create(AncientWarfareCore.MOD_ID, "shapeless_research_recipe", ResearchRecipeWrapper.class);
    private final Component localizedName = Component.translatable("jei.recipe.shapeless_research_recipe");

    public ShapelessResearchRecipeCategory(IGuiHelper guiHelper) {
        super(guiHelper);
    }

    @Override
    public RecipeType<ResearchRecipeWrapper> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return localizedName;
    }

}
