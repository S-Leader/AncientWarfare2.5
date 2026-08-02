package net.shadowmage.ancientwarfare.vehicle.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import net.shadowmage.ancientwarfare.vehicle.item.ItemAmmo;
import net.shadowmage.ancientwarfare.vehicle.item.ItemArmor;
import net.shadowmage.ancientwarfare.vehicle.item.ItemUpgrade;

public class ContainerVehicleInventory extends ContainerVehicle {

    private Slot[] storageSlots;

    public int storageY;

    public int extrasY;

    public int playerY;

    /**
     * @param openingPlayer
     */
    public ContainerVehicleInventory(Player openingPlayer, int entityId, int dummy1, int dummy2) {
        super(openingPlayer, entityId, dummy1, dummy2);
        int y;
        int x;
        int slotNum;
        int xPos;
        int yPos;

        storageY = 4 + 10 + 20;
        int invHeight = (vehicle.inventory.storageInventory.getSlots() / 9 + (vehicle.inventory.storageInventory.getSlots() % 9 == 0 ? 0 : 1)) * 18;
        invHeight = invHeight > 3 * 18 ? 3 * 18 : invHeight;
        extrasY = storageY + (invHeight == 0 ? 0 : 10) + invHeight;
        playerY = extrasY + 4 + 10 + 2 * 18;
        this.addPlayerSlots(8, playerY, 4);

        for (y = 0; y < 2; y++) {
            for (x = 0; x < 3; x++) {
                slotNum = y * 3 + x;
                if (slotNum < vehicle.inventory.ammoInventory.getSlots()) {
                    xPos = 8 + x * 18;
                    yPos = y * 18 + extrasY;
                    this.addSlotToContainer(new SlotItemHandler(vehicle.inventory.ammoInventory, slotNum, xPos, yPos));
                }
            }
        }
        for (y = 0; y < 2; y++) {
            for (x = 0; x < 3; x++) {

                slotNum = y * 3 + x;
                if (slotNum < vehicle.inventory.upgradeInventory.getSlots()) {
                    xPos = 8 + x * 18 + 3 * 18 + 5;
                    yPos = y * 18 + extrasY;
                    this.addSlotToContainer(new SlotItemHandler(vehicle.inventory.upgradeInventory, slotNum, xPos, yPos));
                }
            }
        }
        for (y = 0; y < 2; y++) {
            for (x = 0; x < 3; x++) {
                slotNum = y * 3 + x;
                if (slotNum < vehicle.inventory.armorInventory.getSlots()) {
                    xPos = 8 + x * 18 + 6 * 18 + 2 * 5;
                    yPos = y * 18 + extrasY;
                    this.addSlotToContainer(new SlotItemHandler(vehicle.inventory.armorInventory, slotNum, xPos, yPos));
                }
            }
        }

        storageSlots = new Slot[vehicle.inventory.storageInventory.getSlots()];
        for (y = 0; y < vehicle.inventory.storageInventory.getSlots() / 9; y++) {
            for (x = 0; x < 9; x++) {
                slotNum = y * 9 + x;
                if (slotNum < vehicle.inventory.storageInventory.getSlots()) {
                    xPos = 8 + x * 18;
                    yPos = y * 18 + storageY;
                    if (slotNum >= 27) {
                        xPos = -1000;
                        yPos = -1000;
                    }
                    Slot slot = new SlotItemHandler(vehicle.inventory.storageInventory, slotNum, xPos, yPos);
                    storageSlots[slotNum] = slot;
                    this.addSlotToContainer(slot);
                }
            }
        }
    }

    private int currentTopStorageRow = 0;

    public void nextRow() {
        this.setCurrentTopStorageRow(currentTopStorageRow + 1);
    }

    public void prevRow() {
        this.setCurrentTopStorageRow(currentTopStorageRow - 1);
    }

    private void setCurrentTopStorageRow(int row) {
        if (row < 0 || row >= storageSlots.length / 9) {
            return;
        }
        currentTopStorageRow = row;
        int x;
        int y;
        int slotNum;
        int xPos;
        int yPos;

        int curRow = 0;

        for (y = 0; y < vehicle.inventory.storageInventory.getSlots() / 9; y++) {
            for (x = 0; x < 9; x++) {
                slotNum = y * 9 + x;
                if (slotNum < vehicle.inventory.storageInventory.getSlots()) {

                    if (y < row || y >= row + 3) {
                        xPos = -1000;
                        yPos = -1000;
                    } else {
                        xPos = 8 + x * 18;
                        yPos = storageY + curRow * 18;
                    }
                    repositionStorageSlot(slotNum, xPos, yPos);
                }
            }
            if (y >= row && y < row + 3) {
                curRow++;
            }
        }
    }

    /*
     * Slot x/y coordinates are final in 1.20 - moving a slot requires replacing the slot instance
     * at the same menu index with one created at the new coordinates.
     */
    private void repositionStorageSlot(int slotNum, int xPos, int yPos) {
        Slot oldSlot = storageSlots[slotNum];
        if (oldSlot.x == xPos && oldSlot.y == yPos) {
            return;
        }
        Slot newSlot = new SlotItemHandler(vehicle.inventory.storageInventory, slotNum, xPos, yPos);
        newSlot.index = oldSlot.index;
        slots.set(oldSlot.index, newSlot);
        storageSlots[slotNum] = newSlot;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotClickedIndex) {
        ItemStack slotStackCopy = ItemStack.EMPTY;
        Slot theSlot = this.inventorySlots.get(slotClickedIndex);
        if (theSlot != null && theSlot.hasItem()) {
            ItemStack slotStack = theSlot.getItem();
            slotStackCopy = slotStack.copy();
            int ammoSlots = vehicle.inventory.ammoInventory.getSlots();
            int upgradeSlots = vehicle.inventory.upgradeInventory.getSlots();
            int armorSlots = vehicle.inventory.armorInventory.getSlots();
            int storageSlots = vehicle.inventory.storageInventory.getSlots();
            if (slotClickedIndex < 36)//player slots...
            {
                if (slotStackCopy.getItem() instanceof ItemAmmo) {
                    if (!this.mergeItemStack(slotStack, 36, 36 + ammoSlots, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotStackCopy.getItem() instanceof ItemUpgrade) {
                    if (!this.mergeItemStack(slotStack, 36 + ammoSlots, 36 + ammoSlots + upgradeSlots, false))//merge into upgrade inventory
                    {
                        return ItemStack.EMPTY;
                    }
                } else if (slotStackCopy.getItem() instanceof ItemArmor) {
                    if (!this.mergeItemStack(slotStack, 36 + ammoSlots + upgradeSlots, 36 + ammoSlots + upgradeSlots + armorSlots, false))//merge into armor inventory
                    {
                        return ItemStack.EMPTY;
                    }
                } else//attempt merge into storage inventory, if vehicle has one...
                {
                    if (!this.mergeItemStack(slotStack, 36 + ammoSlots + upgradeSlots + armorSlots, 36 + ammoSlots + upgradeSlots + armorSlots + storageSlots, false))//merge into storage inventory
                    {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (slotClickedIndex >= 36 && slotClickedIndex < 36 + ammoSlots + upgradeSlots + armorSlots + storageSlots
                    && !this.mergeItemStack(slotStack, 0, 36, true)) {
                return ItemStack.EMPTY;
            }
            theSlot.setChanged();
            vehicle.ammoHelper.updateAmmoCounts();
            vehicle.upgradeHelper.updateUpgrades();

            if (slotStack.getCount() == slotStackCopy.getCount()) {
                return ItemStack.EMPTY;
            }
            theSlot.onTake(player, slotStack);
        }
        return slotStackCopy;
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && player != null && player.distanceTo(vehicle) < 8.d;
    }

}
