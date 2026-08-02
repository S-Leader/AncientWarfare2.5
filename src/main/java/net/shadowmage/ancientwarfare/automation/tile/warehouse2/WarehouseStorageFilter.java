package net.shadowmage.ancientwarfare.automation.tile.warehouse2;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.shadowmage.ancientwarfare.core.inventory.ItemHashEntry;

public class WarehouseStorageFilter implements INBTSerializable<CompoundTag> {
    ItemHashEntry hashKey;
    ItemStack item = ItemStack.EMPTY;

    public WarehouseStorageFilter() {
    }

    public WarehouseStorageFilter(ItemStack filter) {
        setFilterItem(filter);
    }

    public ItemStack getFilterItem() {
        return item;
    }

    public void setFilterItem(ItemStack itemStack) {
        item = itemStack;
        hashKey = item.isEmpty() ? null : new ItemHashEntry(item);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("item", item.save(new CompoundTag()));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("item"))
            setFilterItem(ItemStack.of(tag.getCompound("item")));
        else
            setFilterItem(ItemStack.EMPTY);
    }
}
