package net.shadowmage.ancientwarfare.automation.container;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.shadowmage.ancientwarfare.automation.tile.worksite.TileAutoCrafting;
import net.shadowmage.ancientwarfare.core.container.ContainerCraftingRecipeMemory;
import net.shadowmage.ancientwarfare.core.container.ContainerTileBase;
import net.shadowmage.ancientwarfare.core.container.ICraftingContainer;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

import java.util.List;

public class ContainerWorksiteAutoCrafting extends ContainerTileBase<TileAutoCrafting> implements ICraftingContainer {
    private static final int BOOK_SLOT = 1;
    public ContainerCraftingRecipeMemory containerCrafting;

    public ContainerWorksiteAutoCrafting(Player player, int x, int y, int z) {
        super(player, x, y, z);

        //slot 0 = outputSlot
        //slot 1 = bookSlot
        //slot 2-10 = craftMatrix
        //slot 11-28 = resourceInventory
        //slot 29-37 = outputSlots
        //slot 38-73 = playerInventory

        containerCrafting = new ContainerCraftingRecipeMemory(tileEntity.craftingRecipeMemory, player);
        for (Slot slot : containerCrafting.getSlots()) {
            addSlotToContainer(slot);
        }

        Slot slot;
        int x2, y2, slotNum;
        for (int y1 = 0; y1 < 2; y1++) {
            y2 = y1 * 18 + 8 + 4 * 18 + 4 + 4;
            for (int x1 = 0; x1 < 9; x1++) {
                x2 = x1 * 18 + 8;
                slotNum = y1 * 9 + x1;
                slot = new SlotItemHandler(tileEntity.resourceInventory, slotNum, x2, y2);
                addSlotToContainer(slot);
            }
        }

        y2 = 8 + 3 * 18 + 4;
        for (int x1 = 0; x1 < 9; x1++) {
            x2 = x1 * 18 + 8;
            slotNum = x1;
            slot = new SlotItemHandler(tileEntity.outputInventory, slotNum, x2, y2) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            };
            addSlotToContainer(slot);
        }

        int y1 = 8 + 8 + 3 * 18 + 2 * 18 + 4 + 4 + 18;
        y1 = this.addPlayerSlots(y1);
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains("recipe")) {
            containerCrafting.handleRecipeUpdate(tag);
        }
        if (!player.level().isClientSide && tag.contains("craft")) {
            tileEntity.tryCraftItem();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeContents(int stateId, List<ItemStack> stacks, ItemStack carried) {
        containerCrafting.setOpening(true);
        super.initializeContents(stateId, stacks, carried);
        containerCrafting.setOpening(false);
    }

    @Override
    public void setItem(int slotId, int stateId, ItemStack stack) {
        containerCrafting.setOpening(true);
        super.setItem(slotId, stateId, stack);
        containerCrafting.setOpening(false);
    }

    @Override
    public ItemStack quickMoveStack(Player par1EntityPlayer, int slotClickedIndex) {
        ItemStack slotStackCopy = ItemStack.EMPTY;
        Slot theSlot = this.getSlot(slotClickedIndex);
        if (theSlot != null && theSlot.hasItem()) {
            ItemStack slotStack = theSlot.getItem();
            slotStackCopy = slotStack.copy();

            int storageSlotsStart = 1 + 1 + containerCrafting.getCraftingMatrixSlotCount();
            int outputSlotsStart = storageSlotsStart + tileEntity.resourceInventory.getSlots();
            int playerSlotStart = outputSlotsStart + tileEntity.outputInventory.getSlots();
            int playerSlotEnd = playerSlotStart + playerSlots;
            if (slotClickedIndex < 2)//result or book slot
            {
                if (!this.mergeItemStack(slotStack, playerSlotStart, playerSlotEnd, false))//merge into player inventory
                {
                    return ItemStack.EMPTY;
                }
            } else if (slotClickedIndex < storageSlotsStart)//craft matrix
            {
                if (!this.mergeItemStack(slotStack, storageSlotsStart, outputSlotsStart, false))//merge into storage
                {
                    return ItemStack.EMPTY;
                }
            } else if (slotClickedIndex < playerSlotStart)//storage slots
            {
                if (!this.mergeItemStack(slotStack, playerSlotStart, playerSlotEnd, false))//merge into player inventory
                {
                    return ItemStack.EMPTY;
                }
            } else if (slotClickedIndex < playerSlotEnd)//player slots, merge into book slot and then storage
            {
                if (!mergeItemStack(slotStack, BOOK_SLOT, BOOK_SLOT + 1, false) && !this.mergeItemStack(slotStack, storageSlotsStart, outputSlotsStart, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (slotStack.getCount() == 0) {
                theSlot.set(ItemStack.EMPTY);
            } else {
                theSlot.setChanged();
            }
            if (slotStack.getCount() == slotStackCopy.getCount()) {
                return ItemStack.EMPTY;
            }
            theSlot.onTake(par1EntityPlayer, slotStack);
        }
        return slotStackCopy;
    }

    public void craft() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("craft", true);
        sendDataToServer(tag);
    }

    @Override
    public ContainerCraftingRecipeMemory getCraftingMemoryContainer() {
        return containerCrafting;
    }

    @Override
    public IItemHandlerModifiable[] getInventories() {
        return new IItemHandlerModifiable[]{tileEntity.resourceInventory, tileEntity.outputInventory, new PlayerInvWrapper(player.getInventory())};
    }

    @Override
    public boolean pushCraftingMatrixToInventories() {
        IItemHandler craftMatrixWrapper = new InvWrapper(tileEntity.craftingRecipeMemory.craftMatrix);
        NonNullList<ItemStack> craftingItems = InventoryTools.getItems(craftMatrixWrapper);

        IItemHandler inventories = new CombinedInvWrapper(tileEntity.resourceInventory, new PlayerInvWrapper(player.getInventory()));

        if (InventoryTools.insertItems(inventories, craftingItems, true).isEmpty()) {
            InventoryTools.insertItems(inventories, craftingItems, false);
            InventoryTools.removeItems(craftMatrixWrapper, craftingItems);
            return true;
        }

        return false;
    }
}
