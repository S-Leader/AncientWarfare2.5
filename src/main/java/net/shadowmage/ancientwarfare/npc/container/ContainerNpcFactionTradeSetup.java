package net.shadowmage.ancientwarfare.npc.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.npc.entity.faction.NpcFactionTrader;
import net.shadowmage.ancientwarfare.npc.registry.FactionTradeListRegistry;
import net.shadowmage.ancientwarfare.npc.registry.FactionTradeListTemplate;
import net.shadowmage.ancientwarfare.npc.registry.FactionTradeTemplate;
import net.shadowmage.ancientwarfare.npc.trade.FactionTrade;
import net.shadowmage.ancientwarfare.npc.trade.FactionTradeList;

import java.util.ArrayList;
import java.util.List;

public class ContainerNpcFactionTradeSetup extends ContainerNpcBase<NpcFactionTrader> {

    public FactionTradeList tradeList;
    public boolean tradesChanged = false;

    public ContainerNpcFactionTradeSetup(Player player, int x, int y, int z) {
        super(player, x);
        this.tradeList = entity.getTradeList();
        this.entity.startTrade(player);

        addPlayerSlots();
    }

    @Override
    public void sendInitData() {
        tradeList.updateTradesForView();
        CompoundTag packetTag = new CompoundTag();
        packetTag.put("tradeData", tradeList.serializeNBT());
        sendDataToClient(packetTag);
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains("tradeData")) {
            tradeList.deserializeNBT(tag.getCompound("tradeData"));
        }
        refreshGui();
    }

    @Override
    public void onContainerClosed(Player p_75134_1_) {
        this.entity.closeTrade();
        super.onContainerClosed(p_75134_1_);
    }

    public void onGuiClosed() {
        if (player.level().isClientSide && tradesChanged) {
            tradeList.removeEmptyTrades();
            sendTradeListToServer();
        }
    }

    private void sendTradeListToServer() {
        CompoundTag packetTag = new CompoundTag();
        packetTag.put("tradeData", tradeList.serializeNBT());
        sendDataToServer(packetTag);
    }

    public void setTradeList(FactionTradeList tradeList) {
        this.tradeList = tradeList;
        sendTradeListToServer();
    }

    public void saveTradeTemplate(String templateName, boolean factionSpecific) {
        List<FactionTradeTemplate> trades = new ArrayList<>();
        for (FactionTrade trade : tradeList) {
            trades.add(FactionTradeTemplate.fromTrade(trade));
        }

        FactionTradeListTemplate list = new FactionTradeListTemplate(templateName, trades);

        if (factionSpecific) {
            FactionTradeListRegistry.saveFactionTradeList(list, entity.getFaction());
        } else {
            FactionTradeListRegistry.saveTradeList(list);
        }
    }
}
