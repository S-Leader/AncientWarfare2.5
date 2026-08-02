package net.shadowmage.ancientwarfare.automation.tile.warehouse2;


import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.automation.container.ContainerWarehouseStorage;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.interfaces.IInteractableTile;
import net.shadowmage.ancientwarfare.core.inventory.InventorySlotlessBasic;
import net.shadowmage.ancientwarfare.core.inventory.ItemQuantityMap;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.tile.IBlockBreakHandler;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.NBTHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TileWarehouseStorage extends TileControlled implements IWarehouseStorageTile, IInteractableTile, IBlockBreakHandler {

    private InventorySlotlessBasic inventory;
    private final List<WarehouseStorageFilter> filters = new ArrayList<>();

    private final Set<ContainerWarehouseStorage> viewers = new HashSet<>();

    public TileWarehouseStorage() {
        inventory = new InventorySlotlessBasic(getStorageAdditionSize());
    }

    @Override
    public ItemStack tryAdd(ItemStack cursorStack) {
        int moved = insertItem(cursorStack, cursorStack.getCount());
        getController().ifPresent(controller -> {
            ItemStack filter = cursorStack.copy();
            filter.setCount(1);
            controller.changeCachedQuantity(filter, moved);
        });
        cursorStack.shrink(moved);
        if (cursorStack.getCount() <= 0) {
            return ItemStack.EMPTY;
        }
        return cursorStack;
    }

    @Override
    protected void updateTile() {
        //noop
    }

    @Override
    public void onBlockBroken(BlockState state) {
        ItemQuantityMap qtm = new ItemQuantityMap();
        addItems(qtm);
        NonNullList<ItemStack> list = qtm.getItems();
        for (ItemStack stack : list) {
            InventoryTools.dropItemInWorld(world, stack, pos);
        }
    }

    @Override
    public int getStorageAdditionSize() {
        return 9 * 64;
    }

    @Override
    public void onWarehouseInventoryUpdated(TileWarehouseBase warehouse) {
        //noop
    }

    @Override
    public List<WarehouseStorageFilter> getFilters() {
        return filters;
    }

    @Override
    public void setFilters(List<WarehouseStorageFilter> filters) {
        List<WarehouseStorageFilter> old = new ArrayList<>();
        old.addAll(this.filters);
        this.filters.clear();
        this.filters.addAll(filters);
        getController().ifPresent(controller -> controller.onStorageFilterChanged(this, old, this.filters));
        updateViewers();
        markDirty();
    }

    @Override
    public void addItems(ItemQuantityMap map) {
        inventory.getItems(map);
    }

    @Override
    public int getQuantityStored(ItemStack filter) {
        return inventory.getQuantityStored(filter);
    }

    @Override
    public int getAvailableSpaceFor(ItemStack filter) {
        return inventory.getAvailableSpaceFor(filter);
    }

    @Override
    public int extractItem(ItemStack filter, int amount) {
        int removed = inventory.extractItem(filter, amount);
        updateViewersForInventory();
        if (removed > 0) {
            markDirty();
        }
        return removed;
    }

    @Override
    public int insertItem(ItemStack filter, int amount) {
        int inserted = inventory.insertItem(filter, amount);
        updateViewersForInventory();
        if (inserted > 0) {
            markDirty();
        }
        return inserted;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        inventory.readFromNBT(tag.getCompound("inventory"));
        filters.addAll(NBTHelper.deserializeListFrom(tag, "filterList", WarehouseStorageFilter::new));
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.put("inventory", inventory.writeToNBT(new CompoundTag()));
        NBTHelper.writeSerializablesTo(tag, "filterList", filters);
        return tag;
    }

    @Override
    public void addViewer(ContainerWarehouseStorage containerWarehouseStorage) {
        if (!hasWorld() || world.isClientSide) {
            return;
        }
        viewers.add(containerWarehouseStorage);
    }

    @Override
    public void removeViewer(ContainerWarehouseStorage containerWarehouseStorage) {
        viewers.remove(containerWarehouseStorage);
    }

    private void updateViewers() {
        for (ContainerWarehouseStorage viewer : viewers) {
            viewer.onFilterListUpdated();
        }
    }

    private void updateViewersForInventory() {
        for (ContainerWarehouseStorage viewer : viewers) {
            viewer.onStorageInventoryUpdated();
        }
    }

    @Override
    public boolean onBlockClicked(Player player, @Nullable InteractionHand hand) {
        if (!player.level().isClientSide) {
            AWMenuTypes.open(player, NetworkHandler.GUI_WAREHOUSE_STORAGE, pos);
        }
        return true;
    }

    @Override
    public void handleSlotClick(Player player, ItemStack filter, boolean shiftClick, boolean rightClick) {
        if (!shiftClick && !player.containerMenu.getCarried().isEmpty()) {
            tryAddItem(player, player.containerMenu.getCarried());
        } else {
            tryGetItem(player, filter, shiftClick, rightClick);
        }
    }

    private void tryAddItem(Player player, ItemStack cursorStack) {
        int stackSize = cursorStack.getCount();
        int moved;
        moved = insertItem(cursorStack, cursorStack.getCount());
        getController().ifPresent(controller -> {
            ItemStack filter = cursorStack.copy();
            filter.setCount(1);
            controller.changeCachedQuantity(filter, moved);
        });
        cursorStack.shrink(moved);
        if (cursorStack.getCount() <= 0) {
            player.containerMenu.setCarried(ItemStack.EMPTY);
        }
        if (stackSize != cursorStack.getCount()) {
            ((ServerPlayer) player).containerMenu.broadcastChanges();
        }
    }

    private void tryGetItem(Player player, ItemStack filter, boolean shiftClick, boolean rightClick) {
        int stackSize = 0;
        if (!player.containerMenu.getCarried().isEmpty()) {
            stackSize = player.containerMenu.getCarried().getCount();
            ItemStack comparableStack = player.containerMenu.getCarried().copy();
            comparableStack.setCount(filter.getCount());
            if (!ItemStack.matches(filter, comparableStack))
                return;
        }

        int count = getQuantityStored(filter);
        int toMoveMax = filter.getMaxStackSize();
        if (rightClick && (toMoveMax > 1)) {
            if (shiftClick) {
                toMoveMax = Math.min(stackSize + 1, toMoveMax);
            } else {
                if (toMoveMax > count) {
                    toMoveMax = count;
                }
                toMoveMax = (int) Math.ceil(toMoveMax / 2.0);
            }
        }
        int toMove = toMoveMax - stackSize;
        toMove = toMove > count ? count : toMove;
        if (toMove > 0) {
            extractItem(filter, toMove);
            int cacheChange = toMove; //because we need final variable for lambda
            getController().ifPresent(controller -> controller.changeCachedQuantity(filter, -cacheChange));
        }
        ItemStack newCursorStack = filter.copy();
        newCursorStack.setCount(stackSize + toMove);
        InventoryTools.updateCursorItem((ServerPlayer) player, newCursorStack, !rightClick && shiftClick);
    }
}
