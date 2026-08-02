package net.shadowmage.ancientwarfare.npc.faction;

import net.minecraft.nbt.CompoundTag;

public final class PlayerFactionEntry extends FactionEntry {
    public final String playerName;

    public PlayerFactionEntry(CompoundTag tag) {
        super(tag);
        playerName = tag.getString("playerName");
    }

    public PlayerFactionEntry(String playerName) {
        super();
        this.playerName = playerName;
    }

    @Override
    public final CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString("playerName", playerName);
        return tag;
    }
}
