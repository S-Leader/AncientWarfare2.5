package net.shadowmage.ancientwarfare.automation.tile.warehouse2;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.InventoryTools.ComparatorItemHashEntry.SortOrder;
import net.shadowmage.ancientwarfare.core.util.InventoryTools.ComparatorItemHashEntry.SortType;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

import java.util.List;

public class TileWarehouse extends TileWarehouseBase {
    public TileWarehouse(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private SortType sortType = SortType.NAME;
    private SortOrder sortOrder = SortOrder.DESCENDING;

    @Override
    public void invalidate() {
        BlockPos max = getWorkBoundsMax();
        BlockPos min = getWorkBoundsMin();
        List<BlockEntity> tiles = WorldTools.getTileEntitiesInArea(world, min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
        for (BlockEntity te : tiles) {
            if (te instanceof IControlledTile && ((IControlledTile) te).getController().map(controller -> controller == this).orElse(false)) {
                ((IControlledTile) te).setController(null);
            }
        }
    }

    @Override
    public void handleSlotClick(Player player, ItemStack filter, boolean shiftClick, boolean rightClick) {
        if (!shiftClick && !player.containerMenu.getCarried().isEmpty()) {
            tryAddItem(player, player.containerMenu.getCarried());
        } else if (!filter.isEmpty()) {
            tryGetItem(player, filter, shiftClick, rightClick);
        }
    }

    private void tryAddItem(Player player, ItemStack cursorStack) {
        if (cursorStack.isEmpty()) {
            return;
        }
        ItemStack result = tryAddItem(cursorStack, cursorStack.getCount());
        if (result.getCount() != cursorStack.getCount()) {
            player.containerMenu.setCarried(result);
            ((ServerPlayer) player).containerMenu.broadcastChanges();
        }
    }

    private ItemStack tryAddItem(ItemStack stack, int count) {
        List<IWarehouseStorageTile> destinations = storageMap.getDestinations(stack);
        int addedTotal = 0;
        for (IWarehouseStorageTile tile : destinations) {
            int moved = tile.insertItem(stack, count - addedTotal);
            addedTotal += moved;
            changeCachedQuantity(stack, moved);
            updateViewers();
            if (addedTotal >= count) {
                return ItemStack.EMPTY;
            }
        }

        ItemStack result = stack.copy();
        result.shrink(addedTotal);
        return result;
    }

    private void tryGetItem(Player player, ItemStack filter, boolean shiftClick, boolean rightClick) {
        int stackSize = 0;
        if (!player.containerMenu.getCarried().isEmpty()) {
            if (shiftClick && rightClick) {
                stackSize = player.containerMenu.getCarried().getCount();
            }
            ItemStack comparableStack = player.containerMenu.getCarried().copy();
            comparableStack.setCount(filter.getCount());
            if (!ItemStack.matches(filter, comparableStack))
                return;
        }

        List<IWarehouseStorageTile> destinations = storageMap.getDestinations();
        int toMoveMax = filter.getMaxStackSize();
        if (rightClick && (toMoveMax > 1)) {
            if (shiftClick) {
                toMoveMax = Math.min(stackSize + 1, toMoveMax);
            } else {
                int available = 0;
                for (IWarehouseStorageTile tile : destinations) {
                    available += tile.getQuantityStored(filter);
                }
                if (toMoveMax > available) {
                    toMoveMax = available;
                }
                toMoveMax = (int) Math.ceil(toMoveMax / 2.0);
            }
        }
        int toRemove = toMoveMax - stackSize;
        ItemStack removedStack = tryGetItem(filter, toRemove);

        ItemStack newCursorStack = filter.copy();
        newCursorStack.setCount(stackSize + removedStack.getCount());
        InventoryTools.updateCursorItem((ServerPlayer) player, newCursorStack, !rightClick && shiftClick);
    }

    private ItemStack tryGetItem(ItemStack filter, int toRemove) {
        if (world.isClientSide) {
            return tryGetItemClient(filter, toRemove);
        }

        int removed = 0;
        for (IWarehouseStorageTile tile : storageMap.getDestinations()) {
            int count = tile.getQuantityStored(filter);
            int removeFromTile = Math.min(toRemove - removed, count);
            if (removeFromTile > 0) {
                removed += removeFromTile;
                tile.extractItem(filter, removeFromTile);
                changeCachedQuantity(filter, -removeFromTile);
                updateViewers();
            }
            if (removed >= toRemove) {
                break;
            }
        }

        if (removed == 0) {
            return ItemStack.EMPTY;
        }
        ItemStack result = filter.copy();
        result.setCount(removed);
        return result;
    }

    private ItemStack tryGetItemClient(ItemStack filter, int toRemove) {
        int maxRemove = cachedItemMap.getCount(filter);
        int removed = Math.min(toRemove, maxRemove);
        cachedItemMap.decreaseCount(filter, removed);
        ItemStack result = filter.copy();
        result.setCount(removed);
        return result;
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        super.writeUpdateNBT(tag);
        writeSort(tag);
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        super.handleUpdateNBT(tag);
        readSort(tag);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        writeSort(tag);
        return tag;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        readSort(tag);
    }

    private void writeSort(CompoundTag tag) {
        tag.putByte("sortOrder", (byte) getSortOrder().ordinal());
        tag.putByte("sortType", (byte) getSortType().ordinal());
    }

    private void readSort(CompoundTag tag) {
        setSortOrder(SortOrder.values()[tag.getByte("sortOrder")]);
        setSortType(SortType.values()[tag.getByte("sortType")]);
    }

    public SortType getSortType() {
        return sortType;
    }

    public void setSortType(SortType sortType) {
        this.sortType = sortType;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public IItemHandlerModifiable getItemHandler() {
        int additionalSlots = 10;
        NonNullList<ItemStack> cachedItems = cachedItemMap.getItems();

        for (int i = 0; i < additionalSlots; i++) {
            cachedItems.add(ItemStack.EMPTY);
        }

        return new WarehouseItemHandler(cachedItems);
    }

    private class WarehouseItemHandler implements IItemHandlerModifiable {
        private final NonNullList<ItemStack> cachedItems;

        private WarehouseItemHandler(NonNullList<ItemStack> cachedItems) {
            this.cachedItems = cachedItems;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            ItemStack currentStack = cachedItems.get(slot);

            if (currentStack.isEmpty()) {
                insertItem(slot, stack, false);
            } else {
                int countChange = stack.getCount() - currentStack.getCount();

                if (countChange == 0) {
                    return;
                }

                if (countChange > 0) {
                    ItemStack copy = stack.copy();
                    copy.setCount(countChange);
                    insertItem(slot, copy, false);
                } else {
                    extractItem(slot, -countChange, false);
                }
            }
        }

        @Override
        public int getSlots() {
            return cachedItems.size();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot < cachedItems.size() ? cachedItems.get(slot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            ItemStack cachedStack = cachedItems.get(slot);
            if (cachedStack.isEmpty() || InventoryTools.doItemStacksMatchRelaxed(stack, cachedStack)) {
                int maxToAdd = Math.min(stack.getMaxStackSize() - cachedStack.getCount(), stack.getCount());
                if (!simulate) {
                    cachedStack.setCount(cachedStack.getCount() + maxToAdd);
                    ItemStack result = tryAddItem(stack, maxToAdd);
                    if (maxToAdd == stack.getCount()) {
                        return result;
                    } else {
                        ItemStack ret = stack.copy();
                        ret.setCount(stack.getCount() - maxToAdd + result.getCount());
                        return ret;
                    }
                }
                ItemStack ret = ItemStack.EMPTY;
                if (maxToAdd < stack.getCount()) {
                    ret = stack.copy();
                    ret.setCount(stack.getCount() - maxToAdd);
                }

                return ret;
            }

            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack cachedStack = cachedItems.get(slot);

            if (cachedStack.isEmpty()) {
                return ItemStack.EMPTY;
            }

            int maxToRemove = Math.min(cachedStack.getCount(), amount);

            if (!simulate) {
                ItemStack result = tryGetItem(cachedStack, maxToRemove);
                cachedStack.setCount(cachedStack.getCount() - maxToRemove);
                return result;
            }

            ItemStack ret = cachedStack.copy();
            ret.setCount(maxToRemove);
            return ret;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }
    }
}
