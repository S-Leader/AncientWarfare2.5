package net.shadowmage.ancientwarfare.structure.container;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.shadowmage.ancientwarfare.core.compat.CapabilityItemHandler;
import net.shadowmage.ancientwarfare.core.container.ContainerTileBase;
import net.shadowmage.ancientwarfare.structure.tile.TileLootBasket;

import java.util.Optional;

public class ContainerLootBasket extends ContainerTileBase<TileLootBasket> {
    public ContainerLootBasket(Player player, int x, int y, int z) {
        super(player, x, y, z);

        getItemHandler().ifPresent(inventory -> {
            int yBase = 18;
            int xBase = 8;

            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                addSlotToContainer(new SlotItemHandler(inventory, slot, xBase + (slot % 9) * 18, yBase + (slot / 9) * 18));
            }

            addPlayerSlots(8, yBase + ((inventory.getSlots()) / 9) * 18 + 13, 4);
        });
    }

    public Optional<IItemHandler> getItemHandler() {
        return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).resolve();
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int slotClickedIndex) {
        return getItemHandler().map(inventory -> {
            int slotNum = inventory.getSlots();
            Slot slot = inventorySlots.get(slotClickedIndex);
            ItemStack itemstack = ItemStack.EMPTY;

            if (slot != null && slot.hasItem()) {
                ItemStack itemstack1 = slot.getItem();
                itemstack = itemstack1.copy();

                if (slotClickedIndex < slotNum) {
                    if (!mergeItemStack(itemstack1, slotNum, this.inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.mergeItemStack(itemstack1, 0, slotNum, false)) {
                    return ItemStack.EMPTY;
                }

                if (itemstack1.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }
            }

            return itemstack;
        }).orElse(ItemStack.EMPTY);
    }
}
