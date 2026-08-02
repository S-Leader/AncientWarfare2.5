package net.shadowmage.ancientwarfare.core.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ItemQuantityMap {

    private final Map<ItemHashEntry, Integer> map = new HashMap<>();

    /*
     * is not a PUT operation -- merges quantities (values) instead of overwriting
     */
    public void addAll(ItemQuantityMap incoming) {
        for (ItemHashEntry entry : incoming.map.keySet()) {
            if (map.containsKey(entry)) {
                map.put(entry, map.get(entry) + incoming.getCount(entry));
            } else {
                map.put(entry, incoming.map.get(entry));
            }
        }
    }

    /*
     * removes given counts of items from this map, if the resulting count is 0 removes that entry as well.
     */
    public void removeAll(ItemQuantityMap toRemove) {
        for (ItemHashEntry entry : toRemove.map.keySet()) {
            int currentCount = map.get(entry);
            int countToRemove = toRemove.map.get(entry);

            if (countToRemove >= currentCount) {
                remove(entry);
            } else {
                map.put(entry, currentCount - countToRemove);
            }
        }
    }

    public int getCount(ItemHashEntry entry) {
        if (map.containsKey(entry)) {
            return map.get(entry);
        }
        return 0;
    }

    public int getCount(ItemStack item) {
        return getCount(new ItemHashEntry(item));
    }

    public void addCount(ItemStack item, int count) {
        addCount(new ItemHashEntry(item), count);
    }

    private void addCount(ItemHashEntry entry, int count) {
        if (!map.containsKey(entry)) {
            map.put(entry, count);
        } else {
            map.put(entry, map.get(entry) + count);
        }
    }

    public void decreaseCount(ItemStack item, int count) {
        decreaseCount(new ItemHashEntry(item), count);
    }

    private void decreaseCount(ItemHashEntry entry, int count) {
        if (map.containsKey(entry)) {
            int itemCount = map.get(entry) - count;
            if (itemCount <= 0) {
                map.remove(entry);
            } else {
                map.put(entry, itemCount);
            }
        }
    }

    public void remove(ItemHashEntry entry) {
        map.remove(entry);
    }

    public void put(ItemStack item, int count) {
        put(new ItemHashEntry(item), count);
    }

    public void put(ItemHashEntry wrap, int count) {
        map.put(wrap, count);
    }

    public void clear() {
        this.map.clear();
    }

    public Set<ItemHashEntry> keySet() {
        return map.keySet();
    }

    public boolean contains(ItemHashEntry entry) {
        return map.containsKey(entry);
    }

    public boolean contains(ItemStack item) {
        return contains(new ItemHashEntry(item));
    }

    /*
     * Return the most compact set of item-stacks that can represent the contents of this map.<br>
     * May return multiple stacks of the same item if the quantity contained is > maxStackSize.<br>
     */
    public NonNullList<ItemStack> getItems() {
        NonNullList<ItemStack> items = NonNullList.create();
        ItemStack outStack;
        int qty;
        for (Map.Entry<ItemHashEntry, Integer> entry : map.entrySet()) {
            qty = entry.getValue();
            while (qty > 0) {
                outStack = entry.getKey().getItemStack().copy();
                outStack.setCount(qty > outStack.getMaxStackSize() ? outStack.getMaxStackSize() : qty);
                qty -= outStack.getCount();
                items.add(outStack);
            }
        }
        return items;
    }

    public Map<ItemHashEntry, Integer> getItemCounts() {
        return map;
    }

    public void readFromNBT(CompoundTag tag) {
        ListTag entryList = tag.getList("entryList", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < entryList.size(); i++) {
            CompoundTag entryTag = entryList.getCompound(i);
            putEntryFromNBT(entryTag);
        }
    }

    public void putEntryFromNBT(CompoundTag entryTag) {
        CompoundTag itemTag = entryTag.getCompound("item");
        ItemHashEntry entry = ItemHashEntry.readFromNBT(itemTag);
        if (!entry.getItemStack().isEmpty()) {
            int qty = entryTag.getInt("quantity");
            if (qty == 0) { // when deserializing from NBT just remove all entries with 0
                map.remove(entry);
            } else {
                map.put(entry, qty);
            }
        } else {
            AncientWarfareCore.LOG.warn("Unable to read item from NBT into quantity map, probably no longer exists. {}", itemTag.toString());
        }
    }

    public CompoundTag writeToNBT(CompoundTag tag) {
        ListTag entryList = new ListTag();
        for (ItemHashEntry entry : this.keySet()) {
            entryList.add(writeEntryToNBT(entry));
        }
        tag.put("entryList", entryList);
        return tag;
    }

    public CompoundTag writeEntryToNBT(ItemHashEntry entry) {
        CompoundTag entryTag = new CompoundTag();
        entryTag.put("item", entry.writeToNBT());
        entryTag.putInt("quantity", getCount(entry));
        return entryTag;
    }

    public int getTotalItemCount() {
        return map.values().stream().mapToInt(i -> i).sum();
    }

}
