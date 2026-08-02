package net.shadowmage.ancientwarfare.npc.trade;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public final class POTradePoint {
    protected BlockPos position;
    protected int delay;
    protected boolean shouldUpkeep;//if the npc should refill upkeep at this stop

    public BlockPos getPosition() {
        return position;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void setShouldUpkeep(boolean val) {
        this.shouldUpkeep = val;
    }

    public boolean shouldUpkeep() {
        return shouldUpkeep;
    }

    public void readFromNBT(CompoundTag tag) {
        position = BlockPos.of(tag.getLong("pos"));
        delay = tag.getInt("delay");
        shouldUpkeep = tag.getBoolean("upkeep");
    }

    public CompoundTag writeToNBT(CompoundTag tag) {
        tag.putLong("pos", position.asLong());
        tag.putInt("delay", delay);
        tag.putBoolean("upkeep", shouldUpkeep);
        return tag;
    }

}
