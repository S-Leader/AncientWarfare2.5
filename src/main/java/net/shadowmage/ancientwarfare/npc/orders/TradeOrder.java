package net.shadowmage.ancientwarfare.npc.orders;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.shadowmage.ancientwarfare.npc.item.ItemTradeOrder;
import net.shadowmage.ancientwarfare.npc.trade.POTradeList;
import net.shadowmage.ancientwarfare.npc.trade.POTradeRestockData;
import net.shadowmage.ancientwarfare.npc.trade.POTradeRoute;

public class TradeOrder implements INBTSerializable<CompoundTag> {

    private POTradeRoute tradeRoute = new POTradeRoute();
    private POTradeRestockData restockEntry = new POTradeRestockData();
    private POTradeList tradeList = new POTradeList();

    public TradeOrder() {
    }

    public static TradeOrder getTradeOrder(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemTradeOrder) {
            TradeOrder order = new TradeOrder();
            if (stack.hasTag() && stack.getTag().contains("orders")) {
                order.deserializeNBT(stack.getTag().getCompound("orders"));
            }
            return order;
        }
        return null;
    }

    public void write(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemTradeOrder) {
            stack.getOrCreateTag().put("orders", serializeNBT());
        }
    }

    public POTradeList getTradeList() {
        return tradeList;
    }

    public POTradeRoute getRoute() {
        return tradeRoute;
    }

    public POTradeRestockData getRestockData() {
        return restockEntry;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("tradeList", tradeList.serializeNBT());
        tag.put("tradeRoute", tradeRoute.writeToNBT(new CompoundTag()));
        tag.put("restockEntry", restockEntry.writeToNBT(new CompoundTag()));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        tradeList = new POTradeList();
        tradeRoute = new POTradeRoute();
        restockEntry = new POTradeRestockData();
        tradeList.deserializeNBT(tag.getCompound("tradeList"));
        tradeRoute.readFromNBT(tag.getCompound("tradeRoute"));
        restockEntry.readFromNBT(tag.getCompound("restockEntry"));
    }
}
