package net.shadowmage.ancientwarfare.automation.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.TileWarehouseStorage;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.WarehouseStorageFilter;
import net.shadowmage.ancientwarfare.core.container.ContainerTileBase;
import net.shadowmage.ancientwarfare.core.inventory.ItemHashEntry;
import net.shadowmage.ancientwarfare.core.inventory.ItemQuantityMap;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.NBTHelper;

import java.util.ArrayList;
import java.util.List;

public class ContainerWarehouseStorage extends ContainerTileBase<TileWarehouseStorage> {
    private static final String REQ_ITEM_TAG = "reqItem";
    private static final String IS_SHIFT_CLICK_TAG = "isShiftClick";
    private static final String IS_RIGHT_CLICK_TAG = "isRightClick";
    private static final String SLOT_CLICK_TAG = "slotClick";
    private static final String FILTER_LIST_TAG = "filterList";
    private static final String CHANGE_LIST_TAG = "changeList";
    private boolean shouldSynch = true;
    private ItemQuantityMap cache = new ItemQuantityMap();
    public ItemQuantityMap itemMap = new ItemQuantityMap();
    public List<WarehouseStorageFilter> filters = new ArrayList<>();

    public ContainerWarehouseStorage(Player player, int x, int y, int z) {
        super(player, x, y, z);
        tileEntity.addViewer(this);

        filters.addAll(tileEntity.getFilters());
        addPlayerSlots(148 + 8);
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

    public void handleClientRequestSpecific(ItemStack stack, boolean isShiftClick, boolean isRightClick) {
        CompoundTag tag = new CompoundTag();
        if (!stack.isEmpty()) {
            ItemStack copy = stack.copy();
            copy.setCount(Math.min(stack.getCount(), stack.getMaxStackSize()));
            tag.put(REQ_ITEM_TAG, copy.save(new CompoundTag()));
        }
        tag.putBoolean(IS_SHIFT_CLICK_TAG, isShiftClick);
        tag.putBoolean(IS_RIGHT_CLICK_TAG, isRightClick);
        CompoundTag pktTag = new CompoundTag();
        pktTag.put(SLOT_CLICK_TAG, tag);
        sendDataToServer(pktTag);
    }

    @Override
    public void sendInitData() {
        CompoundTag tag = new CompoundTag();
        NBTHelper.writeSerializablesTo(tag, FILTER_LIST_TAG, filters);
        sendDataToClient(tag);
    }

    public void sendFiltersToServer() {
        CompoundTag tag = new CompoundTag();
        NBTHelper.writeSerializablesTo(tag, FILTER_LIST_TAG, filters);
        sendDataToServer(tag);
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains(FILTER_LIST_TAG)) {
            List<WarehouseStorageFilter> deserializedFilters = NBTHelper.deserializeListFrom(tag, FILTER_LIST_TAG, WarehouseStorageFilter::new);
            if (player.level().isClientSide) {
                this.filters = deserializedFilters;
                refreshGui();
            } else {
                tileEntity.setFilters(deserializedFilters);
            }
        } else if (tag.contains(SLOT_CLICK_TAG)) {
            CompoundTag reqTag = tag.getCompound(SLOT_CLICK_TAG);
            ItemStack item = ItemStack.EMPTY;
            if (reqTag.contains(REQ_ITEM_TAG)) {
                item = ItemStack.of(reqTag.getCompound(REQ_ITEM_TAG));
            }
            tileEntity.handleSlotClick(player, item, reqTag.getBoolean(IS_SHIFT_CLICK_TAG), reqTag.getBoolean(IS_RIGHT_CLICK_TAG));
        } else if (tag.contains(CHANGE_LIST_TAG)) {
            handleChangeList(tag.getList(CHANGE_LIST_TAG, Constants.NBT.TAG_COMPOUND));
            refreshGui();
        }
        super.handlePacketData(tag);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (shouldSynch) {
            synchItemMaps();
            shouldSynch = false;
        }
    }

    private void handleChangeList(ListTag changeList) {
        CompoundTag tag;
        for (int i = 0; i < changeList.size(); i++) {
            tag = changeList.getCompound(i);
            itemMap.putEntryFromNBT(tag);
        }
    }

    private void synchItemMaps() {
        /*
         *
         * need to loop through this.itemMap and compare quantities to warehouse.itemMap
         *    add any changes to change-list
         * need to loop through warehouse.itemMap and find new entries
         *    add any new entries to change-list
         */

        cache.clear();
        tileEntity.addItems(cache);
        ItemQuantityMap warehouseItemMap = cache;
        ListTag changeList = new ListTag();
        for (ItemHashEntry wrap : this.itemMap.keySet()) {
            int qty = this.itemMap.getCount(wrap);
            if (qty != warehouseItemMap.getCount(wrap)) {
                qty = warehouseItemMap.getCount(wrap);
                changeList.add(warehouseItemMap.writeEntryToNBT(wrap));
                this.itemMap.put(wrap, qty);
            }
        }
        for (ItemHashEntry entry : warehouseItemMap.keySet()) {
            if (!itemMap.contains(entry)) {
                int qty = warehouseItemMap.getCount(entry);
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

    public void onStorageInventoryUpdated() {
        shouldSynch = true;
    }

    public void onFilterListUpdated() {
        this.filters.clear();
        this.filters.addAll(tileEntity.getFilters());
        sendInitData();
    }

    @Override
    public void onContainerClosed(Player par1EntityPlayer) {
        tileEntity.removeViewer(this);
        super.onContainerClosed(par1EntityPlayer);
    }

}
