package net.shadowmage.ancientwarfare.npc.trade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class FactionTrade extends Trade {
    private static final String REFILL_TIME_TAG = "refillTime";
    private int refillFrequency;
    private long refillTime = -1;
    private int maxAvailable;
    private int currentAvailable;
    private int minLevel;

    public FactionTrade() {
        refillFrequency = 20 * 60 * 5;//five minutes per item refilled
        maxAvailable = 1;
        currentAvailable = 1;
    }

    public boolean hasItems() {
        for (int i = 0; i < size(); i++) {
            if (!getInputStack(i).isEmpty() || !getOutputStack(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public int getRefillFrequency() {
        return refillFrequency;
    }

    public int getMaxAvailable() {
        return maxAvailable;
    }

    public int getCurrentAvailable() {
        return currentAvailable;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = Math.max(0, minLevel);
    }

    public boolean isAvailableAtLevel(int level) {
        return level >= minLevel;
    }

    public void setRefillFrequency(int refill) {
        refillFrequency = refill;
    }

    public void setMaxAvailable(int max) {
        maxAvailable = max;
        currentAvailable = max;
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        tag.putInt("refillFrequency", refillFrequency);
        tag.putLong(REFILL_TIME_TAG, refillTime);
        tag.putInt("maxAvailable", maxAvailable);
        tag.putInt("currentAvailable", currentAvailable);
        tag.putInt("minLevel", minLevel);
        return super.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        refillFrequency = tag.getInt("refillFrequency");
        refillTime = tag.contains(REFILL_TIME_TAG) ? tag.getLong(REFILL_TIME_TAG) : -1;
        maxAvailable = tag.getInt("maxAvailable");
        currentAvailable = tag.getInt("currentAvailable");
        setMinLevel(tag.getInt("minLevel"));
        super.readFromNBT(tag);
    }

    public void updateTrade(long totalWorldTime) {
        if (refillTime == -1) {
            refillTime = totalWorldTime + refillFrequency;
        }

        if (refillFrequency > 0 && refillTime > 0 && refillTime <= totalWorldTime)//update per freq period
        {
            long timeDiff = totalWorldTime - refillTime;
            while (currentAvailable < maxAvailable && timeDiff >= 0) {
                timeDiff -= refillFrequency;
                currentAvailable++;
            }
            refillTime = currentAvailable < maxAvailable ? totalWorldTime + refillFrequency : 0;
        } else if (refillFrequency == 0)//full refill automatically if frequency==0
        {
            currentAvailable = maxAvailable;
        }//dont refill if frequency<0
    }

    @Override
    public boolean performTrade(Player player, @Nullable IItemHandler storage) {
        return currentAvailable > 0 && super.performTrade(player, null);
    }

    public boolean performTrade(Player player, @Nullable IItemHandler storage, int traderLevel) {
        return isAvailableAtLevel(traderLevel) && performTrade(player, storage);
    }

    @Override
    protected void doTrade(Player player, @Nullable IItemHandler storage) {
        if (refillFrequency != 0) {
            currentAvailable--;
            refillTime = player.level().getGameTime() + refillFrequency;
        }//0 denotes instant restock, no reason to decrease qty if it will just be instantly restocked when GUI is opened next
        super.doTrade(player, storage);
    }

    public long getRefillTime() {
        return refillTime;
    }
}
