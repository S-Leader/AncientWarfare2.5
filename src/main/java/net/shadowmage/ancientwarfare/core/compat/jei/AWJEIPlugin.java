package net.shadowmage.ancientwarfare.core.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.automation.container.ContainerWarehouseCraftingStation;
import net.shadowmage.ancientwarfare.automation.container.ContainerWorksiteAutoCrafting;
import net.shadowmage.ancientwarfare.automation.init.AWAutomationBlocks;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.container.ContainerEngineeringStation;
import net.shadowmage.ancientwarfare.core.container.ICraftingContainer;
import net.shadowmage.ancientwarfare.core.crafting.AWCraftingManager;
import net.shadowmage.ancientwarfare.core.crafting.ShapedResearchRecipe;
import net.shadowmage.ancientwarfare.core.crafting.ShapelessResearchRecipe;
import net.shadowmage.ancientwarfare.core.init.AWCoreBlocks;
import net.shadowmage.ancientwarfare.npc.init.AWNPCItems;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.init.AWStructureItems;
import net.shadowmage.ancientwarfare.vehicle.init.AWVehicleItems;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

@JeiPlugin
public class AWJEIPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_UID = new ResourceLocation(AncientWarfareCore.MOD_ID, "core");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerItemSubtypes(@Nonnull ISubtypeRegistration subtypeRegistry) {
        subtypeRegistry.useNbtForSubtypes(AWNPCItems.NPC_SPAWNER);
        subtypeRegistry.useNbtForSubtypes(AWStructureBlocks.FIRE_PIT.asItem());
        subtypeRegistry.useNbtForSubtypes(AWNPCItems.COIN);
        subtypeRegistry.useNbtForSubtypes(AWStructureItems.TOTEM_PART);
        //noinspection ConstantConditions
        subtypeRegistry.registerSubtypeInterpreter(AWVehicleItems.SPAWNER, (itemStack, context) -> Integer.toString(itemStack.getDamageValue()) + ":" + (itemStack.hasTag() ? itemStack.getTag().toString() : ""));
    }

    @Override
    public void registerCategories(@Nonnull IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();

        registry.addRecipeCategories(new ShapedResearchRecipeCategory(guiHelper));
        registry.addRecipeCategories(new ShapelessResearchRecipeCategory(guiHelper));
    }

    @Override
    public void registerRecipes(@Nonnull IRecipeRegistration registration) {
        List<ResearchRecipeWrapper> shapedResearchRecipes = AWCraftingManager.getRecipes().stream().filter(r -> r instanceof ShapedResearchRecipe).map(r -> (ResearchRecipeWrapper) new ShapedResearchRecipeWrapper((ShapedResearchRecipe) r)).collect(Collectors.toList());
        registration.addRecipes(ShapedResearchRecipeCategory.TYPE, shapedResearchRecipes);

        List<ResearchRecipeWrapper> shapelessResearchRecipes = AWCraftingManager.getRecipes().stream().filter(r -> r instanceof ShapelessResearchRecipe).map(ResearchRecipeWrapper::new).collect(Collectors.toList());
        registration.addRecipes(ShapelessResearchRecipeCategory.TYPE, shapelessResearchRecipes);
    }

    @Override
    public void registerRecipeTransferHandlers(@Nonnull IRecipeTransferRegistration transferRegistry) {
        if (AWCraftingManager.getRecipes().stream().anyMatch(r -> r instanceof ShapedResearchRecipe)) {
            registerMultiRecipeTransferHandler(ContainerWorksiteAutoCrafting.class, ShapedResearchRecipeCategory.TYPE, transferRegistry);
            registerMultiRecipeTransferHandler(ContainerWarehouseCraftingStation.class, ShapedResearchRecipeCategory.TYPE, transferRegistry);
            registerMultiRecipeTransferHandler(ContainerEngineeringStation.class, ShapedResearchRecipeCategory.TYPE, transferRegistry);
        }

        if (AWCraftingManager.getRecipes().stream().anyMatch(r -> r instanceof ShapelessResearchRecipe)) {
            registerMultiRecipeTransferHandler(ContainerWorksiteAutoCrafting.class, ShapelessResearchRecipeCategory.TYPE, transferRegistry);
            registerMultiRecipeTransferHandler(ContainerWarehouseCraftingStation.class, ShapelessResearchRecipeCategory.TYPE, transferRegistry);
            registerMultiRecipeTransferHandler(ContainerEngineeringStation.class, ShapelessResearchRecipeCategory.TYPE, transferRegistry);
        }

        registerMultiRecipeTransferHandler(ContainerWorksiteAutoCrafting.class, RecipeTypes.CRAFTING, transferRegistry);
        registerMultiRecipeTransferHandler(ContainerWarehouseCraftingStation.class, RecipeTypes.CRAFTING, transferRegistry);
        registerMultiRecipeTransferHandler(ContainerEngineeringStation.class, RecipeTypes.CRAFTING, transferRegistry);
    }

    @Override
    public void registerRecipeCatalysts(@Nonnull IRecipeCatalystRegistration registry) {
        if (AWCraftingManager.getRecipes().stream().anyMatch(r -> r instanceof ShapedResearchRecipe)) {
            registry.addRecipeCatalyst(new ItemStack(AWAutomationBlocks.AUTO_CRAFTING), ShapedResearchRecipeCategory.TYPE);
            registry.addRecipeCatalyst(new ItemStack(AWAutomationBlocks.WAREHOUSE_CRAFTING), ShapedResearchRecipeCategory.TYPE);
            registry.addRecipeCatalyst(new ItemStack(AWCoreBlocks.ENGINEERING_STATION), ShapedResearchRecipeCategory.TYPE);
        }

        if (AWCraftingManager.getRecipes().stream().anyMatch(r -> r instanceof ShapelessResearchRecipe)) {
            registry.addRecipeCatalyst(new ItemStack(AWAutomationBlocks.AUTO_CRAFTING), ShapelessResearchRecipeCategory.TYPE);
            registry.addRecipeCatalyst(new ItemStack(AWAutomationBlocks.WAREHOUSE_CRAFTING), ShapelessResearchRecipeCategory.TYPE);
            registry.addRecipeCatalyst(new ItemStack(AWCoreBlocks.ENGINEERING_STATION), ShapelessResearchRecipeCategory.TYPE);
        }

        registry.addRecipeCatalyst(new ItemStack(AWAutomationBlocks.AUTO_CRAFTING), RecipeTypes.CRAFTING);
        registry.addRecipeCatalyst(new ItemStack(AWAutomationBlocks.WAREHOUSE_CRAFTING), RecipeTypes.CRAFTING);
        registry.addRecipeCatalyst(new ItemStack(AWCoreBlocks.ENGINEERING_STATION), RecipeTypes.CRAFTING);
    }

    private <C extends AbstractContainerMenu & ICraftingContainer, R> void registerMultiRecipeTransferHandler(Class<C> containerClass, RecipeType<R> recipeType, IRecipeTransferRegistration transferRegistry) {
        MultiRecipeTransferHandler<C, R> handler = new MultiRecipeTransferHandler<>(containerClass, recipeType, transferRegistry.getTransferHelper());
        transferRegistry.addRecipeTransferHandler(handler, recipeType);
    }
}
