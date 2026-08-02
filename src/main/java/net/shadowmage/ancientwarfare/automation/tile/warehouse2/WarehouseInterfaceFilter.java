package net.shadowmage.ancientwarfare.automation.tile.warehouse2;

import com.google.common.base.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.shadowmage.ancientwarfare.core.inventory.ItemHashEntry;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

import javax.annotation.Nullable;

public final class WarehouseInterfaceFilter implements Predicate<ItemStack>, INBTSerializable<CompoundTag> {
    private static final String FILTER_TAG = "filter";

    private ItemStack filterItem = ItemStack.EMPTY;
    private int quantity;

    @Override
    @SuppressWarnings("squid:S4449")
    //if we got null stack here there is something seriously wrong in the code using this and it should be fixed instead
    public boolean apply(@Nullable ItemStack item) {
        //noinspection ConstantConditions
        return InventoryTools.doItemStacksMatchRelaxed(filterItem, item);
    }

    @Override
    public boolean equals(@Nullable Object object) {
        return object instanceof WarehouseInterfaceFilter && ((WarehouseInterfaceFilter) object).quantity == this.quantity && ItemStack.matches(this.filterItem, ((WarehouseInterfaceFilter) object).filterItem);
    }

    @Override
    public int hashCode() {
        int result = !getFilterItem().isEmpty() ? new ItemHashEntry(getFilterItem()).hashCode() : 0;
        return 31 * result + quantity;
    }

    public final ItemStack getFilterItem() {
        return filterItem;
    }

    public final void setFilterItem(ItemStack item) {
        this.filterItem = item;
    }

    public final int getFilterQuantity() {
        return quantity;
    }

    public final void setFilterQuantity(int filterQuantity) {
        this.quantity = filterQuantity;
    }

    @Override
    public String toString() {
        return "Filter item: " + filterItem + " quantity: " + quantity;
    }

    public WarehouseInterfaceFilter copy() {
        WarehouseInterfaceFilter filter = new WarehouseInterfaceFilter();
        if (!this.filterItem.isEmpty())
            filter.setFilterItem(this.filterItem.copy());
        filter.setFilterQuantity(this.quantity);
        return filter;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("quantity", quantity);
        if (!filterItem.isEmpty()) {
            tag.put(FILTER_TAG, filterItem.save(new CompoundTag()));
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        quantity = tag.getInt("quantity");
        if (tag.contains(FILTER_TAG)) {
            filterItem = ItemStack.of(tag.getCompound(FILTER_TAG));
        }
    }
}
