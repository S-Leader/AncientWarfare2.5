package net.shadowmage.ancientwarfare.core.compat.jei;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.shadowmage.ancientwarfare.core.container.ICraftingContainer;
import net.shadowmage.ancientwarfare.core.crafting.AWCraftingManager;
import net.shadowmage.ancientwarfare.core.crafting.ICraftingRecipe;
import net.shadowmage.ancientwarfare.core.crafting.RecipeResourceLocation;
import net.shadowmage.ancientwarfare.core.network.PacketBase;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

import java.io.IOException;
import java.util.List;

public class PacketTransferRecipe extends PacketBase {
    private ICraftingRecipe recipe;

    public PacketTransferRecipe() {
    }

    public PacketTransferRecipe(ICraftingRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(data);
        buffer.writeUtf(recipe.getRegistryName().toString());
    }

    @Override
    protected void readFromStream(ByteBuf data) throws IOException {
        FriendlyByteBuf buffer = new FriendlyByteBuf(data);
        recipe = AWCraftingManager.getRecipe(RecipeResourceLocation.deserialize(buffer.readUtf(256)));
    }

    @Override
    protected void execute(Player player) {
        if (player.containerMenu instanceof ICraftingContainer) {
            ICraftingContainer craftingContainer = (ICraftingContainer) player.containerMenu;

            if (craftingContainer.pushCraftingMatrixToInventories()) {
                IItemHandler handler = new CombinedInvWrapper(craftingContainer.getInventories());

                NonNullList<ItemStack> resources = AWCraftingManager.getRecipeInventoryMatch(recipe, handler);

                if (resources.isEmpty()) {
                    return;
                }

                List<Slot> slots = craftingContainer.getCraftingMemoryContainer().getCraftingMatrixSlots();

                for (int x = 0; x < recipe.getRecipeWidth(); x++) {
                    for (int y = 0; y < recipe.getRecipeHeight(); y++) {
                        int slotIndex = y * 3 + x;
                        int ingredientIndex = y * recipe.getRecipeWidth() + x;
                        Slot slot = slots.get(slotIndex);
                        slot.set(resources.get(ingredientIndex));
                    }
                }
                craftingContainer.getCraftingMemoryContainer().setRecipe(recipe, true);
                craftingContainer.getCraftingMemoryContainer().setUpdatePending();
                craftingContainer.getCraftingMemoryContainer().updateClients();

                InventoryTools.removeItems(handler, resources);
            }
        }
    }
}
