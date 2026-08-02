package net.shadowmage.ancientwarfare.npc.gamedata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.compat.WorldSavedData;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.npc.faction.PlayerFactionEntry;
import net.shadowmage.ancientwarfare.npc.network.PacketFactionUpdate;

import java.util.HashMap;

public class FactionData extends WorldSavedData {
    private static final String FACTION_INIT_TAG = "factionInit";
    private static final String FACTION_UPDATE_TAG = "factionUpdate";

    private HashMap<String, PlayerFactionEntry> playerFactionEntries = new HashMap<>();

    public FactionData(String par1Str) {
        super(par1Str);
    }

    public void onPlayerLogin(Player player) {
        String name = player.getName().getString();
        if (!playerFactionEntries.containsKey(name)) {
            playerFactionEntries.put(name, new PlayerFactionEntry(name));
            markDirty();
        }
        sendFactionEntry(player);
    }

    public PlayerFactionEntry getEntryFor(String playerName) {
        return playerFactionEntries.get(playerName);
    }

    public int getStandingFor(String playerName, String faction) {
        if (playerFactionEntries.containsKey(playerName)) {
            return playerFactionEntries.get(playerName).getStandingFor(faction);
        }
        return 0;
    }

    public void adjustStandingFor(Level world, String playerName, String faction, int adjustment) {
        if (playerFactionEntries.containsKey(playerName)) {
            playerFactionEntries.get(playerName).adjustStandingFor(faction, adjustment);
            markDirty();
        }
        sendFactionUpdate(world, playerName, faction);
    }

    public void setStandingFor(String playerName, String faction, int setting) {
        if (playerFactionEntries.containsKey(playerName)) {
            playerFactionEntries.get(playerName).setStandingFor(faction, setting);
            markDirty();
        }
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        ListTag entryList = tag.getList("entryList", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < entryList.size(); i++) {
            CompoundTag entryTag = entryList.getCompound(i);
            readEntryFromNBT(entryTag);
        }
    }

    private void readEntryFromNBT(CompoundTag entryTag) {
        PlayerFactionEntry entry;
        entry = new PlayerFactionEntry(entryTag);
        playerFactionEntries.put(entry.playerName, entry);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        ListTag entryList = new ListTag();
        for (PlayerFactionEntry entry : this.playerFactionEntries.values()) {
            entryList.add(entry.writeToNBT(new CompoundTag()));
        }
        tag.put("entryList", entryList);

        return tag;
    }

    public void handlePacketData(Player player, CompoundTag tag) {
        if (tag.contains(FACTION_UPDATE_TAG)) {
            handleClientFactionUpdate(player, tag.getCompound(FACTION_UPDATE_TAG));
        }
        if (tag.contains(FACTION_INIT_TAG)) {
            handleClientFactionInit(tag.getCompound(FACTION_INIT_TAG));
        }
    }

    private void handleClientFactionUpdate(Player player, CompoundTag tag) {
        String faction = tag.getString("faction");
        int standing = tag.getInt("standing");
        setStandingFor(player.getName().getString(), faction, standing);
    }

    private void handleClientFactionInit(CompoundTag tag) {
        readEntryFromNBT(tag);
    }

    private void sendFactionEntry(Player player) {
        PlayerFactionEntry entry = getEntryFor(player.getName().getString());
        CompoundTag tag = new CompoundTag();
        CompoundTag initTag = entry.writeToNBT(new CompoundTag());
        tag.put(FACTION_INIT_TAG, initTag);
        PacketFactionUpdate pkt = new PacketFactionUpdate(tag);
        NetworkHandler.sendToPlayer((ServerPlayer) player, pkt);
    }

    private void sendFactionUpdate(Level world, String playerName, String factionName) {
        Player player = world.players().stream().filter(p -> p.getName().getString().equals(playerName)).findFirst().orElse(null);
        if (player instanceof ServerPlayer) {
            int standing = getStandingFor(playerName, factionName);
            CompoundTag tag = new CompoundTag();
            CompoundTag updateTag = new CompoundTag();
            updateTag.putString("faction", factionName);
            updateTag.putInt("standing", standing);
            tag.put(FACTION_UPDATE_TAG, updateTag);
            PacketFactionUpdate pkt = new PacketFactionUpdate(tag);
            NetworkHandler.sendToPlayer((ServerPlayer) player, pkt);
        }
    }
}
