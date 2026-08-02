package net.shadowmage.ancientwarfare.core.compat.jei;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.container.ICraftingContainer;
import net.shadowmage.ancientwarfare.core.crafting.AWCraftingManager;
import net.shadowmage.ancientwarfare.core.crafting.ICraftingRecipe;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MultiRecipeTransferHandler<C extends AbstractContainerMenu & ICraftingContainer, R> implements IRecipeTransferHandler<C, R> {
    private final IRecipeTransferHandlerHelper handlerHelper;
    private final Class<C> containerClass;
    private final RecipeType<R> recipeType;

    public MultiRecipeTransferHandler(Class<C> containerClass, RecipeType<R> recipeType, IRecipeTransferHandlerHelper handlerHelper) {
        this.handlerHelper = handlerHelper;
        this.containerClass = containerClass;
        this.recipeType = recipeType;
    }

    @Override
    public Class<? extends C> getContainerClass() {
        return containerClass;
    }

    @Override
    public Optional<MenuType<C>> getMenuType() {
        return Optional.empty();
    }

    @Override
    public RecipeType<R> getRecipeType() {
        return recipeType;
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public IRecipeTransferError transferRecipe(C container, R jeiRecipe, IRecipeSlotsView recipeSlotsView, Player player, boolean maxTransfer, boolean doTransfer) {
        List<IRecipeSlotView> outputSlots = recipeSlotsView.getSlotViews(RecipeIngredientRole.OUTPUT);
        ItemStack result = outputSlots.isEmpty() ? ItemStack.EMPTY : outputSlots.get(0).getDisplayedItemStack().orElse(ItemStack.EMPTY);
        List<IRecipeSlotView> inputSlots = recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT);
        NonNullList<ItemStack> inputs = NonNullList.create();
        for (IRecipeSlotView inputSlot : inputSlots) {
            inputs.add(getNonNullIngredientStack(inputSlot.getItemStacks().collect(Collectors.toList())));
        }
        ICraftingRecipe recipe = AWCraftingManager.findMatchingRecipe(Minecraft.getInstance().level, inputs, result);

        if (AWCoreStatics.useResearchSystem && recipe.getNeededResearch().isPresent()) {
            if (container.getCraftingMemoryContainer().getCrafterName() == null) {
                Component tooltipMessage = Component.translatable("jei.tooltip.error.recipe.transfer.no.research_book");
                return handlerHelper.createUserErrorWithTooltip(tooltipMessage);
            }

            if (!AWCraftingManager.canPlayerCraft(Minecraft.getInstance().level, container.getCraftingMemoryContainer().getCrafterName(), recipe.getNeededResearch().get())) {
                Component tooltipMessage = Component.translatable("jei.tooltip.error.recipe.transfer.missing.research");
                return handlerHelper.createUserErrorWithTooltip(tooltipMessage);
            }
        }

        List<Slot> craftingSlots = container.getCraftingMemoryContainer().getCraftingMatrixSlots();

        NonNullList<ItemStack> craftingMatrixStacks = NonNullList.create();
        for (Slot slot : craftingSlots) {
            final ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
                craftingMatrixStacks.add(stack.copy());
            }
        }

        IItemHandlerModifiable inventories = new CombinedInvWrapper(container.getInventories());
        IItemHandlerModifiable allInventories = new CombinedInvWrapper(new ItemStackHandler(craftingMatrixStacks), inventories);

        // check if we have enough inventory space to put crafting slots into inventories
        if (!InventoryTools.insertItems(inventories, craftingMatrixStacks, true).isEmpty()) {
            Component message = Component.translatable("jei.tooltip.error.recipe.transfer.inventory.full");
            return handlerHelper.createUserErrorWithTooltip(message);
        }
        @SuppressWarnings("squid:S00108") List<Integer> missingItems = getMissingItems(inputs, recipe, allInventories);

        if (!missingItems.isEmpty()) {
            Component message = Component.translatable("jei.tooltip.error.recipe.transfer.missing");
            List<IRecipeSlotView> missingSlots = missingItems.stream().filter(index -> index >= 0 && index < inputSlots.size()).map(inputSlots::get).collect(Collectors.toList());
            return handlerHelper.createUserErrorForMissingSlots(message, missingSlots);
        }

        if (doTransfer) {
            NetworkHandler.sendToServer(new PacketTransferRecipe(recipe));
        }

        return null;
    }

    private List<Integer> getMissingItems(NonNullList<ItemStack> inputs, ICraftingRecipe recipe, IItemHandlerModifiable allInventories) {
        return AWCraftingManager.getRecipeInventoryMatch(recipe, inputs, s -> InventoryTools.hasCountOrMore(allInventories, s), allInventories,
                ArrayList::new, (a, i, s) -> {
                }, (a, in) -> addMissingItem(a, in, inputs));
    }

    private ItemStack getNonNullIngredientStack(List<ItemStack> allIngredients) {
        for (ItemStack stack : allIngredients) {
            //because some mods apparently link null stacks to the ingredients in JEI
            if (stack != null) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private void addMissingItem(List<Integer> missingItems, Ingredient missingIngredient, NonNullList<ItemStack> inputs) {
        List<ItemStack> matchingStacks = inputs.stream().filter(s -> missingIngredient.test(s) && !missingItems.contains(getInputIndex(inputs.indexOf(s)))).collect(Collectors.toList());
        if (!matchingStacks.isEmpty()) {
            ItemStack matched = matchingStacks.get(matchingStacks.size() - 1);
            for (int i = inputs.size() - 1; i >= 0; i--) {
                if (!missingItems.contains(getInputIndex(i)) && inputs.get(i) == matched) {
                    missingItems.add(getInputIndex(i));
                }
            }
        }
    }

    private int getInputIndex(int craftMatrixIndex) {
        // JEI 15 slot views are already filtered to inputs only, so no offset for the output slot is needed
        return craftMatrixIndex;
    }
}
