package net.shadowmage.ancientwarfare.npc.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.shadowmage.ancientwarfare.core.compat.CapabilityItemHandler;
import net.shadowmage.ancientwarfare.core.init.AWCoreItems;
import net.shadowmage.ancientwarfare.core.util.EntityTools;
import net.shadowmage.ancientwarfare.npc.entity.NpcTrader;
import net.shadowmage.ancientwarfare.npc.trade.POTradeList;

public class ContainerNpcPlayerOwnedTrade extends ContainerNpcBase<NpcTrader> {

    private InteractionHand hand;
    public POTradeList tradeList;
    public final IItemHandler storage;

    public ContainerNpcPlayerOwnedTrade(Player player, int x, int y, int z) {
        super(player, x);
        this.tradeList = entity.getTradeList();
        this.entity.startTrade(player);

        addPlayerSlots();
        this.hand = EntityTools.getHandHoldingItem(entity, AWCoreItems.BACKPACK);
        storage = hand != null ? entity.getItemInHand(hand).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElse(null) : null;
        if (storage != null) {
            for (int i = 0; i < storage.getSlots(); i++) {
                /*
                 * add backpack items to slots in container so that they are synchronized to client side inventory/container
                 * --will be used to validate trades on client-side
                 */
                addSlotToContainer(new SlotItemHandler(storage, i, 100000, 100000));
            }
        }
    }

    @Override
    public void sendInitData() {
        if (tradeList != null) {
            CompoundTag packetTag = new CompoundTag();
            packetTag.put("tradeData", tradeList.serializeNBT());
            sendDataToClient(packetTag);
        }
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains("tradeData")) {
            tradeList = new POTradeList();
            tradeList.deserializeNBT(tag.getCompound("tradeData"));
        } else if (tag.contains("doTrade")) {
            tradeList.performTrade(player, storage, tag.getInt("doTrade"));
        }
        refreshGui();
    }

    @Override
    public void onContainerClosed(Player player) {
        this.entity.closeTrade();
        super.onContainerClosed(player);
    }

    public void doTrade(int tradeIndex) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("doTrade", tradeIndex);
        sendDataToServer(tag);
    }
}
