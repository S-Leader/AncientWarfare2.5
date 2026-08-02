package net.shadowmage.ancientwarfare.npc.trade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/*
 * Created by Olivier on 23/03/2015.
 */
public abstract class POTradeTransferEntry {
    private TransferType type = getDefaultType();
    private ItemStack filter = ItemStack.EMPTY;

    protected abstract TransferType getDefaultType();

    public abstract void toggleType();

    protected abstract TransferType getTypeFrom(int type);

    public final ItemStack getFilter() {
        return filter;
    }

    public final void setFilter(ItemStack stack) {
        filter = stack;
    }

    public final void setType(TransferType type) {
        this.type = type == null ? getDefaultType() : type;
    }

    public final TransferType getType() {
        return type;
    }

    public final void process(IItemHandler storage, IItemHandler move) {
        if (!filter.isEmpty())
            type.doTransfer(storage, move, filter);
    }

    public final void readFromNBT(CompoundTag tag) {
        if (tag.contains("item")) {
            filter = ItemStack.of(tag.getCompound("item"));
        }
        type = getTypeFrom(tag.getInt("type"));
    }

    public final CompoundTag writeToNBT(CompoundTag tag) {
        if (!filter.isEmpty()) {
            tag.put("item", filter.save(new CompoundTag()));
        }
        tag.putInt("type", type.ordinal());
        return tag;
    }

    public interface TransferType {

        void doTransfer(IItemHandler storage, IItemHandler move, ItemStack filter);

        int ordinal();
    }
}
