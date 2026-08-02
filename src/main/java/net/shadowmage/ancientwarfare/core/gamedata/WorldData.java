package net.shadowmage.ancientwarfare.core.gamedata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.compat.WorldSavedData;
import net.shadowmage.ancientwarfare.core.util.NBTHelper;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WorldData extends WorldSavedData {
    CompoundTag dataTag = new CompoundTag();

    private Set<UUID> playersGivenManual = new HashSet<>();

    /*
     * reflection constructor for mc-vanilla code
     */
    public WorldData(String par) {
        super(par);
    }

    public final boolean get(String key) {
        return dataTag.getBoolean(key);
    }

    public final void set(String name, boolean val) {
        dataTag.putBoolean(name, val);
        markDirty();
    }

    public final void addPlayerThatWasGivenManual(Player player) {
        playersGivenManual.add(player.getUUID());
        markDirty();
    }

    public boolean wasPlayerGivenManual(Player player) {
        return playersGivenManual.contains(player.getUUID());
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        this.dataTag = tag.getCompound("AWWorldData");
        playersGivenManual = NBTHelper.getUniqueIdSet(dataTag.get("playersGivenManual"));
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        dataTag.put("playersGivenManual", NBTHelper.getNBTUniqueIdList(playersGivenManual));
        tag.put("AWWorldData", this.dataTag);
        return tag;
    }

}
