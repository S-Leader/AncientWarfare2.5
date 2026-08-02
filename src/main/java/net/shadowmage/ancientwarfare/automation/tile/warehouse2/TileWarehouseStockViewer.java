package net.shadowmage.ancientwarfare.automation.tile.warehouse2;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.INBTSerializable;
import net.shadowmage.ancientwarfare.automation.container.ContainerWarehouseStockViewer;
import net.shadowmage.ancientwarfare.core.block.BlockBase;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.interfaces.IInteractableTile;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.owner.IOwnable;
import net.shadowmage.ancientwarfare.core.owner.Owner;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.NBTHelper;

import javax.annotation.Nullable;
import java.util.*;

public class TileWarehouseStockViewer extends TileControlled implements IOwnable, IInteractableTile {

    private static final String FILTER_LIST_TAG = "filterList";
    private final List<WarehouseStockFilter> filters = new ArrayList<>();
    private Owner owner = Owner.EMPTY;

    private final Set<ContainerWarehouseStockViewer> viewers = new HashSet<>();

    public int getBlockMetadata() {
        BlockState state = getBlockState();
        return state.getBlock() instanceof BlockBase ? ((BlockBase) state.getBlock()).getMetaFromState(state) : 0;
    }

    private void updateViewers() {
        for (ContainerWarehouseStockViewer viewer : viewers) {
            viewer.onFiltersChanged();
        }
    }

    public void addViewer(ContainerWarehouseStockViewer viewer) {
        viewers.add(viewer);
    }

    public void removeViewer(ContainerWarehouseStockViewer viewer) {
        viewers.remove(viewer);
    }

    public List<WarehouseStockFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<WarehouseStockFilter> filters) {
        this.filters.clear();
        this.filters.addAll(filters);
        recountFilters();//recount filters, do not send update
        BlockTools.notifyBlockUpdate(this); //to re-send description packet to client with new filters
    }

    /*
     * should be called whenever controller tile is set or warehouse inventory updated
     */
    private void recountFilters() {
        Optional<TileWarehouseBase> controller = getController();
        if (!controller.isPresent()) {
            for (WarehouseStockFilter filter : this.filters) {
                filter.setQuantity(0);
            }
        } else {
            for (WarehouseStockFilter filter : this.filters) {
                filter.setQuantity(filter.getFilterItem().isEmpty() ? 0 : controller.get().getCountOf(filter.getFilterItem()));
            }
        }
    }

    @Override
    public boolean isValidController(IControllerTile tile) {
        return BlockTools.isPositionWithinBounds(getPos(), tile.getWorkBoundsMin().offset(-1, 0, -1), tile.getWorkBoundsMax().offset(1, 0, 1));
    }

    @Override
    protected void onControllerChanged() {
        onWarehouseInventoryUpdated();
    }

    @Override
    public boolean isOwner(Player player) {
        return owner.isOwnerOrSameTeamOrFriend(player);
    }

    @Override
    public void setOwner(Player player) {
        owner = new Owner(player);
    }

    @Override
    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    @Override
    public Owner getOwner() {
        return owner;
    }

    @Override
    public boolean onBlockClicked(Player player, @Nullable InteractionHand hand) {
        if (!player.level().isClientSide && isOwner(player)) {
            AWMenuTypes.open(player, NetworkHandler.GUI_WAREHOUSE_STOCK, pos);
        }
        return true;
    }

    @Override
    protected void updateTile() {
        //noop
    }

    /*
     * should be called on SERVER whenever warehouse inventory changes
     */
    void onWarehouseInventoryUpdated() {
        BlockTools.notifyBlockUpdate(this);
        recountFilters();
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        super.writeUpdateNBT(tag);
        NBTHelper.writeSerializablesTo(tag, FILTER_LIST_TAG, filters);
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        super.handleUpdateNBT(tag);
        this.filters.clear();
        this.filters.addAll(NBTHelper.deserializeListFrom(tag, TileWarehouseStockViewer.FILTER_LIST_TAG, WarehouseStockFilter::new));
        BlockTools.notifyBlockUpdate(this);
        updateViewers();
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        filters.addAll(NBTHelper.deserializeListFrom(tag, TileWarehouseStockViewer.FILTER_LIST_TAG, WarehouseStockFilter::new));
        owner = Owner.deserializeFromNBT(tag);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        NBTHelper.writeSerializablesTo(tag, FILTER_LIST_TAG, filters);
        owner.serializeToNBT(tag);

        return tag;
    }

    public static class WarehouseStockFilter implements INBTSerializable<CompoundTag> {
        private static final String ITEM_TAG = "item";
        private static final String QUANTITY_TAG = "quantity";
        private ItemStack item = ItemStack.EMPTY;
        private int quantity;

        public WarehouseStockFilter() {
        }

        public WarehouseStockFilter(ItemStack item, int qty) {
            setQuantity(qty);
            setItem(item);
        }

        public void setItem(ItemStack item) {
            this.item = item;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public ItemStack getFilterItem() {
            return item;
        }

        public int getQuantity() {
            return quantity;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            if (!item.isEmpty()) {
                tag.put(ITEM_TAG, item.save(new CompoundTag()));
            }
            tag.putInt(QUANTITY_TAG, quantity);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            setItem(tag.contains(ITEM_TAG) ? ItemStack.of(tag.getCompound(ITEM_TAG)) : ItemStack.EMPTY);
            setQuantity(tag.getInt(QUANTITY_TAG));
        }
    }
}
