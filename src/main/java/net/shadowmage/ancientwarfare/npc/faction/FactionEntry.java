package net.shadowmage.ancientwarfare.npc.faction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.registry.FactionRegistry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FactionEntry implements Iterable<Map.Entry<String, Integer>> {
    private final HashMap<String, Integer> factionStandings = new HashMap<>();

    public FactionEntry(CompoundTag tag) {
        this();
        readFromNBT(tag);
    }

    public FactionEntry() {
        for (String name : FactionRegistry.getFactionNames()) {
            setStandingFor(name, AncientWarfareNPC.statics.getPlayerDefaultStanding(name), false);
        }
    }

    public int getStandingFor(String factionName) {
        if (factionStandings.containsKey(factionName)) {
            return factionStandings.get(factionName);
        }
        return 0;
    }

    private void setStandingFor(String factionName, int standing, boolean checkIfFixed) {
        if (checkIfFixed && !FactionRegistry.getFaction(factionName).getStandingSettings().canPlayerStandingChange()) {
            return;
        }
        factionStandings.put(factionName, standing);
    }

    public void setStandingFor(String factionName, int standing) {
        setStandingFor(factionName, standing, true);
    }

    public void adjustStandingFor(String factionName, int adjustment) {
        if (factionStandings.containsKey(factionName)) {
            setStandingFor(factionName, getStandingFor(factionName) + adjustment);
        } else {
            System.out.println("Invalid Faction name!");
        }
    }

    public final void readFromNBT(CompoundTag tag) {
        ListTag entryList = tag.getList("entryList", Constants.NBT.TAG_COMPOUND);
        CompoundTag entryTag;
        String name;
        for (int i = 0; i < entryList.size(); i++) {
            entryTag = entryList.getCompound(i);
            name = entryTag.getString("name");
            setStandingFor(name, entryTag.getInt("standing"));
        }
    }

    public CompoundTag writeToNBT(CompoundTag tag) {
        ListTag entryList = new ListTag();
        for (Map.Entry<String, Integer> entry : factionStandings.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("name", entry.getKey());
            entryTag.putInt("standing", entry.getValue());
            entryList.add(entryTag);
        }
        tag.put("entryList", entryList);
        return tag;
    }

    @Override
    public Iterator<Map.Entry<String, Integer>> iterator() {
        return factionStandings.entrySet().iterator();
    }
}
