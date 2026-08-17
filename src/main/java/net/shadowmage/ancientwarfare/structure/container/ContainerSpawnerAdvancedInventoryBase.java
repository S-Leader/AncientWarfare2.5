package net.shadowmage.ancientwarfare.structure.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.network.PacketGui;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.tile.SpawnerSettings;

public class ContainerSpawnerAdvancedInventoryBase extends ContainerBase {
    private static final int SETTINGS_INVENTORY_X = 8;

    SpawnerSettings settings;
    IItemHandler inventory;

    public ContainerSpawnerAdvancedInventoryBase(Player player, int x, int y, int z) {
        super(player);
    }

    protected void addSettingsInventorySlots() {
        int xPos;
        int yPos;
        int slotNum;

        for (int y = 0; y < 3; y++) {
            yPos = y * 18 + 8;
            for (int x = 0; x < 3; x++) {
                xPos = x * 18 + SETTINGS_INVENTORY_X;
                slotNum = y * 3 + x;
                addSlotToContainer(new SlotItemHandler(inventory, slotNum, xPos, yPos) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        if (stack.getItem() instanceof BlockItem) {
                            BlockItem block = (BlockItem) stack.getItem();
                            if (block.getBlock() == AWStructureBlocks.ADVANCED_SPAWNER.get()) {
                                return false;
                            }
                        }
                        return true;
                    }
                });
            }
        }
    }

    public void sendSettingsToServer() {
        CompoundTag tag = new CompoundTag();
        settings.writeToNBT(tag);

        PacketGui pkt = new PacketGui();
        pkt.setTag("spawnerSettings", tag);
        pkt.setMenuTarget(containerId);
        NetworkHandler.sendToServer(pkt);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotClickedIndex) {
        ItemStack slotStackCopy = ItemStack.EMPTY;
        Slot theSlot = this.getSlot(slotClickedIndex);
        if (theSlot != null && theSlot.hasItem()) {
            ItemStack slotStack = theSlot.getItem();
            slotStackCopy = slotStack.copy();
            int playerSlotEnd = playerSlots;
            int storageSlots = playerSlotEnd + 9;
            if (slotClickedIndex < playerSlotEnd)//player slots...
            {
                if (!this.mergeItemStack(slotStack, playerSlotEnd, storageSlots, false))//merge into storage inventory
                {
                    return ItemStack.EMPTY;
                }
            } else if (slotClickedIndex < storageSlots)//storage slots, merge to player inventory
            {
                if (!this.mergeItemStack(slotStack, 0, playerSlotEnd, true))//merge into player inventory
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
            theSlot.onTake(player, slotStack);
        }
        return slotStackCopy;
    }

}
