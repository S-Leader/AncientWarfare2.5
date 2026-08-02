package net.shadowmage.ancientwarfare.npc.trade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.OrderingList;

public abstract class TradeList<T extends Trade> extends OrderingList<T> implements INBTSerializable<CompoundTag> {
    public final void addNewTrade() {
        add(getNewTrade());
    }

    protected abstract T getNewTrade();

    public void performTrade(Player player, IItemHandler storage, int integer) {
        get(integer).performTrade(player, storage);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (T aTrade : this.points) {
            list.add(aTrade.writeToNBT(new CompoundTag()));
        }
        tag.put("tradeList", list);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        clear();
        ListTag list = tag.getList("tradeList", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            T t = getNewTrade();
            t.readFromNBT(list.getCompound(i));
            if (t.isValid()) {
                add(t);
            }
        }
    }
}
