package net.shadowmage.ancientwarfare.automation.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.TileWarehouse;
import net.shadowmage.ancientwarfare.core.container.ContainerTileBase;
import net.shadowmage.ancientwarfare.core.inventory.ItemHashEntry;
import net.shadowmage.ancientwarfare.core.inventory.ItemQuantityMap;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.InventoryTools.ComparatorItemHashEntry.SortOrder;
import net.shadowmage.ancientwarfare.core.util.InventoryTools.ComparatorItemHashEntry.SortType;

import javax.annotation.Nullable;

public class ContainerWarehouseControl extends ContainerTileBase<TileWarehouse> {
    private static final String SLOT_CLICK_TAG = "slotClick";
    private static final String REQ_ITEM_TAG = "reqItem";
    private static final String CHANGE_LIST_TAG = "changeList";
    private static final String MAX_STORAGE_TAG = "maxStorage";
    private static final String SORT_TYPE_TAG = "sortType";
    private static final String SORT_ORDER_TAG = "sortOrder";
    public ItemQuantityMap itemMap = new ItemQuantityMap();
    private final ItemQuantityMap cache = new ItemQuantityMap();
    private boolean shouldUpdate = true;
    public int maxStorage = 0;
    public int currentStored = 0;

    public ContainerWarehouseControl(Player player, int x, int y, int z) {
        super(player, x, y, z);
        addPlayerSlots(142);
        tileEntity.addViewer(this);
    }

    @Override
    public void onContainerClosed(Player par1EntityPlayer) {
        tileEntity.removeViewer(this);
        super.onContainerClosed(par1EntityPlayer);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotClickedIndex) {
        if (player.level().isClientSide) {
            return ItemStack.EMPTY;
        }
        Slot slot = this.getSlot(slotClickedIndex);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        stack = tileEntity.tryAdd(stack);
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        }
        detectAndSendChanges();
        return ItemStack.EMPTY;
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains(SLOT_CLICK_TAG)) {
            CompoundTag reqTag = tag.getCompound(SLOT_CLICK_TAG);
            ItemStack item = ItemStack.EMPTY;
            if (reqTag.contains(REQ_ITEM_TAG)) {
                item = ItemStack.of(reqTag.getCompound(REQ_ITEM_TAG));
            }
            tileEntity.handleSlotClick(player, item, reqTag.getBoolean("isShiftClick"), reqTag.getBoolean("isRightClick"));
        } else if (tag.contains(CHANGE_LIST_TAG)) {
            handleChangeList(tag.getList(CHANGE_LIST_TAG, Constants.NBT.TAG_COMPOUND));
        } else {
            if (tag.contains(MAX_STORAGE_TAG)) {
                maxStorage = tag.getInt(MAX_STORAGE_TAG);
            }
            if (tag.contains(SORT_TYPE_TAG)) {
                tileEntity.setSortType(SortType.values()[tag.getByte(SORT_TYPE_TAG)]);
            }
            if (tag.contains(SORT_ORDER_TAG)) {
                tileEntity.setSortOrder(SortOrder.values()[tag.getByte(SORT_ORDER_TAG)]);
            }
        }
        currentStored = itemMap.getTotalItemCount();
        refreshGui();
    }

    public void handleClientRequestSpecific(@Nullable CompoundTag itemTag, int count, boolean isShiftClick, boolean isRightClick) {
        CompoundTag tag = new CompoundTag();
        if (itemTag != null) {
            ItemStack copy = ItemStack.of(itemTag);
            copy.setCount(Math.min(count, copy.getMaxStackSize()));
            tag.put(REQ_ITEM_TAG, copy.save(new CompoundTag()));
        }
        tag.putBoolean("isShiftClick", isShiftClick);
        tag.putBoolean("isRightClick", isRightClick);
        CompoundTag pktTag = new CompoundTag();
        pktTag.put(SLOT_CLICK_TAG, tag);
        sendDataToServer(pktTag);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (shouldUpdate) {
            synchItemMaps();
            shouldUpdate = false;
        }
        if (maxStorage != tileEntity.getMaxStorage()) {
            maxStorage = tileEntity.getMaxStorage();
            CompoundTag tag = new CompoundTag();
            tag.putInt(MAX_STORAGE_TAG, maxStorage);
            tag.putByte(SORT_ORDER_TAG, (byte) getSortOrder().ordinal());
            tag.putByte(SORT_TYPE_TAG, (byte) getSortType().ordinal());
            sendDataToClient(tag);
        }
    }

    private void handleChangeList(ListTag changeList) {
        for (int i = 0; i < changeList.size(); i++) {
            CompoundTag tag = changeList.getCompound(i);
            itemMap.putEntryFromNBT(tag);
        }
    }

    private void synchItemMaps() {
        /*
         *
         * need to loop through this.itemMap and compare quantities to tileEntity.itemMap
         *    add any changes to change-list
         * need to loop through tileEntity.itemMap and find new entries
         *    add any new entries to change-list
         */

        cache.clear();
        tileEntity.getItems(cache);
        ItemQuantityMap warehouseItemMap = cache;
        int qty;
        ListTag changeList = new ListTag();
        for (ItemHashEntry wrap : this.itemMap.keySet()) {
            qty = this.itemMap.getCount(wrap);
            if (qty != warehouseItemMap.getCount(wrap)) {
                qty = warehouseItemMap.getCount(wrap);
                changeList.add(warehouseItemMap.writeEntryToNBT(wrap));
                this.itemMap.put(wrap, qty);
            }
        }
        for (ItemHashEntry entry : warehouseItemMap.keySet()) {
            if (!itemMap.contains(entry)) {
                qty = warehouseItemMap.getCount(entry);
                changeList.add(warehouseItemMap.writeEntryToNBT(entry));
                this.itemMap.put(entry, qty);
            }
        }
        if (changeList.size() > 0) {
            CompoundTag tag = new CompoundTag();
            tag.put(CHANGE_LIST_TAG, changeList);
            sendDataToClient(tag);
        }
    }

    public void onWarehouseInventoryUpdated() {
        shouldUpdate = true;
    }

    public SortType getSortType() {
        return tileEntity.getSortType();
    }

    public void setSortType(SortType sortType) {
        tileEntity.setSortType(sortType);
        CompoundTag tag = new CompoundTag();
        tag.putByte(SORT_TYPE_TAG, (byte) sortType.ordinal());
        sendDataToServer(tag);
    }

    public SortOrder getSortOrder() {
        return tileEntity.getSortOrder();
    }

    public void setSortOrder(SortOrder sortOrder) {
        tileEntity.setSortOrder(sortOrder);
        CompoundTag tag = new CompoundTag();
        tag.putByte(SORT_ORDER_TAG, (byte) sortOrder.ordinal());
        sendDataToServer(tag);
    }

    public boolean isWarehouseFull() {
        return currentStored == maxStorage;
    }
}
