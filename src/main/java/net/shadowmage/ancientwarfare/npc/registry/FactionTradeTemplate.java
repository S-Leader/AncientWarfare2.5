package net.shadowmage.ancientwarfare.npc.registry;

import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.npc.trade.FactionTrade;

import java.util.List;

public class FactionTradeTemplate {
    private final List<ItemStack> input;

    private final List<ItemStack> output;
    private final int refillFrequency;
    private final int maxTrades;
    private final int minLevel;

    public FactionTradeTemplate(List<ItemStack> input, List<ItemStack> output, int refillFrequency, int maxTrades) {
        this(input, output, refillFrequency, maxTrades, 0);
    }

    public FactionTradeTemplate(List<ItemStack> input, List<ItemStack> output, int refillFrequency, int maxTrades, int minLevel) {
        this.input = input;
        this.output = output;
        this.refillFrequency = refillFrequency;
        this.maxTrades = maxTrades;
        this.minLevel = Math.max(0, minLevel);
    }

    public List<ItemStack> getInput() {
        return input;
    }

    public List<ItemStack> getOutput() {
        return output;
    }

    public int getRefillFrequency() {
        return refillFrequency;
    }

    public int getMaxTrades() {
        return maxTrades;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public FactionTrade toTrade() {
        FactionTrade trade = new FactionTrade();
        trade.setMaxAvailable(maxTrades);
        trade.setRefillFrequency(refillFrequency);
        trade.setMinLevel(minLevel);
        int slot = 0;
        for (ItemStack stack : input) {
            trade.setInputStack(slot++, stack);
        }
        slot = 0;
        for (ItemStack stack : output) {
            trade.setOutputStack(slot++, stack);
        }
        return trade;
    }

    public static FactionTradeTemplate fromTrade(FactionTrade trade) {
        return new FactionTradeTemplate(trade.getInput(), trade.getOutput(), trade.getRefillFrequency(), trade.getMaxAvailable(), trade.getMinLevel());
    }
}
