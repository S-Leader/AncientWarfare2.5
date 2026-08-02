package net.shadowmage.ancientwarfare.npc.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.entity.faction.NpcFactionTrader;
import net.shadowmage.ancientwarfare.npc.faction.FactionTracker;
import net.shadowmage.ancientwarfare.npc.registry.FactionRegistry;
import net.shadowmage.ancientwarfare.npc.registry.StandingChanges;
import net.shadowmage.ancientwarfare.npc.trade.FactionTradeList;

public class ContainerNpcFactionTradeView extends ContainerNpcBase<NpcFactionTrader> {
    private static final String DO_TRADE_TAG = "doTrade";
    private static final String TRADE_DATA_TAG = "tradeData";
    private static final String TRADER_LEVEL_TAG = "traderLevel";
    public final FactionTradeList tradeList;
    private int traderLevel;

    @SuppressWarnings("unused") //used in reflection
    public ContainerNpcFactionTradeView(Player player, int x, int y, int z) {
        super(player, x);
        tradeList = entity.getTradeList();
        traderLevel = entity.getLevelingStats().getLevel();
        entity.startTrade(player);

        addPlayerSlots();
    }

    @Override
    public void sendInitData() {
        tradeList.updateTradesForView();
        CompoundTag packetTag = new CompoundTag();
        packetTag.put(TRADE_DATA_TAG, tradeList.serializeNBT());
        packetTag.putInt(TRADER_LEVEL_TAG, traderLevel);
        sendDataToClient(packetTag);
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains(TRADE_DATA_TAG)) {
            tradeList.deserializeNBT(tag.getCompound(TRADE_DATA_TAG));
        }
        if (tag.contains(TRADER_LEVEL_TAG)) {
            traderLevel = tag.getInt(TRADER_LEVEL_TAG);
        }
        if (tag.contains(DO_TRADE_TAG) && tradeList.performTrade(player, tag.getInt(DO_TRADE_TAG), entity.getLevelingStats().getLevel())) {
            FactionTracker.INSTANCE.adjustStandingFor(entity.level(), player.getName().getString(), entity.getFaction(), FactionRegistry.getFaction(entity.getFaction()).getStandingSettings().getStandingChange(StandingChanges.TRADE));
            entity.addExperience(AWNPCStatics.npcXpFromTrade);
            traderLevel = entity.getLevelingStats().getLevel();
            CompoundTag update = new CompoundTag();
            update.put(TRADE_DATA_TAG, tradeList.serializeNBT());
            update.putInt(TRADER_LEVEL_TAG, traderLevel);
            sendDataToClient(update);
        }
        refreshGui();
    }

    @Override
    public void onContainerClosed(Player player) {
        entity.closeTrade();
        super.onContainerClosed(player);
    }

    public void doTrade(int tradeNum) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(DO_TRADE_TAG, tradeNum);
        sendDataToServer(tag);
    }

    public Level getWorld() {
        return player.level();
    }

    public int getTraderLevel() {
        return traderLevel;
    }
}
