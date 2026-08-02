package net.shadowmage.ancientwarfare.core.container;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import net.shadowmage.ancientwarfare.core.compat.CapabilityItemHandler;
import net.shadowmage.ancientwarfare.core.init.AWCoreItems;
import net.shadowmage.ancientwarfare.core.util.EntityTools;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

public class ContainerBackpack extends ContainerBase {

    private ItemStack backpackStack;
    public final int backpackSlotIndex;
    public final InteractionHand hand;
    public final int guiHeight;

    private final IItemHandler handler;

    public ContainerBackpack(Player player, int x, int y, int z) {
        super(player);

        this.hand = EntityTools.getHandHoldingItem(player, AWCoreItems.BACKPACK);
        backpackStack = player.getItemInHand(hand);
        backpackSlotIndex = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : -1;

        handler = InventoryTools.cloneItemHandler(backpackStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElseThrow(NullPointerException::new));

        int xPos, yPos;
        for (int i = 0; i < handler.getSlots(); i++) {
            xPos = (i % 9) * 18 + 8;
            yPos = (i / 9) * 18 + 8;
            addSlotToContainer(new SlotItemHandler(handler, i, xPos, yPos) {
                @Override
                public boolean mayPlace(ItemStack itemStack) {
                    return itemStack.getItem() != AWCoreItems.BACKPACK && super.mayPlace(itemStack);
                }
            });
        }
        int height = (backpackStack.getDamageValue() + 1) * 18 + 8;
        guiHeight = addPlayerSlots(height + 8) + 8;
    }

    @Override
    protected int addPlayerSlots(int tx, int ty, int gap) {
        int y;
        int x;
        int slotNum;
        int xPos;
        int yPos;
        IItemHandler playerInventory = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP).orElseThrow(NullPointerException::new);
        for (x = 0; x < 9; ++x)//add player hotbar slots
        {
            slotNum = x;
            if (slotNum == backpackSlotIndex) {
                xPos = tx + x * 18;
                yPos = ty + gap + 3 * 18;
                this.addSlotToContainer(new SlotItemHandler(playerInventory, x, xPos, yPos) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public boolean mayPickup(Player player) {
                        return false;
                    }
                });
                continue;
            }
            xPos = tx + x * 18;
            yPos = ty + gap + 3 * 18;
            this.addSlotToContainer(new SlotItemHandler(playerInventory, x, xPos, yPos));
        }
        for (y = 0; y < 3; ++y) {
            for (x = 0; x < 9; ++x) {
                slotNum = y * 9 + x + 9;// +9 is to increment past hotbar slots
                xPos = tx + x * 18;
                yPos = ty + y * 18;
                this.addSlotToContainer(new SlotItemHandler(playerInventory, slotNum, xPos, yPos));
            }
        }
        playerSlots = 36;
        return ty + (4 * 18) + gap;
    }

    @Override
    public void onContainerClosed(Player playerIn) {
        IItemHandlerModifiable backpackHandler = (IItemHandlerModifiable) backpackStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElseThrow(NullPointerException::new);

        for (int slot = 0; slot < handler.getSlots(); slot++) {
            backpackHandler.setStackInSlot(slot, handler.getStackInSlot(slot));
        }

        super.onContainerClosed(playerIn);
    }

    @Override
    public ItemStack quickMoveStack(Player par1EntityPlayer, int slotClickedIndex) {
        ItemStack slotStackCopy = ItemStack.EMPTY;
        Slot theSlot = this.getSlot(slotClickedIndex);
        int size = handler.getSlots();
        if (theSlot != null && theSlot.hasItem()) {
            ItemStack slotStack = theSlot.getItem();
            slotStackCopy = slotStack.copy();
            if (slotClickedIndex < size)//clicked in backpack
            {
                if (!this.mergeItemStack(slotStack, size, size + playerSlots, false))//merge into player inventory
                {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.mergeItemStack(slotStack, 0, size, false))//merge into backpack
                {
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

}
