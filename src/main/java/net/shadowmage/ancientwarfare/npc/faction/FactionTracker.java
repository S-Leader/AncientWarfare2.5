package net.shadowmage.ancientwarfare.npc.faction;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.npc.gamedata.FactionData;
import net.shadowmage.ancientwarfare.npc.gamedata.TeamData;

import java.util.UUID;
import java.util.function.ToIntFunction;

public class FactionTracker {
    public static final FactionTracker INSTANCE = new FactionTracker();

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent evt) {
        onPlayerLogin(evt.getEntity());
    }

    private void onPlayerLogin(Player player) {
        FactionData data = AWGameData.INSTANCE.getData(player.level(), FactionData.class);
        data.onPlayerLogin(player);

        TeamData teamData = AWGameData.INSTANCE.getData(player.level(), TeamData.class);
        teamData.checkAndUpdatePlayerTeamMemberships(player);
    }

    public void adjustStandingFor(Level world, String playerName, String factionName, int adjustment) {
        if (world.isClientSide) {
            throw new IllegalArgumentException("Cannot adjust standing on client world!");
        }
        FactionData data = AWGameData.INSTANCE.getData(world, FactionData.class);
        TeamData teamData = AWGameData.INSTANCE.getData(world, TeamData.class);
        teamData.adjustStanding(world, data, playerName, factionName, adjustment);
    }

    public void setStandingFor(Level world, String playerName, String factionName, int standing) {
        if (world.isClientSide) {
            throw new IllegalArgumentException("Cannot set standing on client world!");
        }
        FactionData data = AWGameData.INSTANCE.getData(world, FactionData.class);
        data.setStandingFor(playerName, factionName, standing);
    }

    public boolean isHostileToPlayer(Level world, UUID playerUUID, String playerName, String factionName) {
        return getStandingFor(world, playerUUID, playerName, factionName) < 0;
    }

    private int getStandingFor(Level world, UUID playerUUID, String playerName, String factionName) {
        return getStandingFor(world, playerName, factionName, teamData -> teamData.getWorstStandingFor(world, playerUUID, playerName, factionName, world.getGameTime()));
    }

    public int getStandingFor(Level world, String playerName, String factionName) {
        return getStandingFor(world, playerName, factionName, teamData -> teamData.getWorstStandingFor(playerName, factionName, world.getGameTime()));
    }

    private int getStandingFor(Level world, String playerName, String factionName, ToIntFunction<TeamData> getTeamStanding) {
        FactionData data = AWGameData.INSTANCE.getData(world, FactionData.class);

        int teamStanding = getTeamStanding.applyAsInt(AWGameData.INSTANCE.getData(world, TeamData.class));
        int playerStanding = data.getStandingFor(playerName, factionName);
        return playerStanding < teamStanding ? playerStanding : teamStanding;
    }
}
