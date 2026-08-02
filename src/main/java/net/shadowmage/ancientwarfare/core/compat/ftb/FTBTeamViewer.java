package net.shadowmage.ancientwarfare.core.compat.ftb;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.owner.ITeamViewer;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * FTB Teams 1.20.1 implementation of Ancient Warfare's neutral team API.
 *
 * <p>FTB Lib's old Universe/ForgePlayer/ForgeTeam API no longer exists. Team
 * membership is now queried through {@link FTBTeamsAPI} and {@link TeamManager}.</p>
 */
public final class FTBTeamViewer implements ITeamViewer {
    @Override
    public boolean areTeamMates(Level level, UUID player1, UUID player2, String playerName1, String playerName2) {
        if (player1.equals(player2)) {
            return true;
        }
        TeamManager manager = getManager();
        return manager != null && manager.arePlayersInSameTeam(player1, player2);
    }

    @Override
    public boolean areFriendly(Level level, UUID player1, @Nullable UUID player2, String playerName1, String playerName2) {
        // FTB Teams 1.20.1 has party/team membership but no equivalent of the
        // removed ForgeTeam#isAlly API. Same-team membership is the exact safe
        // replacement; scoreboard alliances remain covered by ScoreboardTeamViewer.
        return player2 != null && areTeamMates(level, player1, player2, playerName1, playerName2);
    }

    @Override
    public Set<ResourceLocation> getPlayerTeamNames(Level level, UUID playerId, String playerName) {
        TeamManager manager = getManager();
        if (manager == null) {
            return Collections.emptySet();
        }

        return manager.getTeamForPlayerID(playerId)
                .map(Team::getTeamId)
                .map(id -> new ResourceLocation(FTBTeamsAPI.MOD_ID, id.toString()))
                .map(Collections::singleton)
                .orElseGet(Collections::emptySet);
    }

    private static @Nullable TeamManager getManager() {
        FTBTeamsAPI.API api = FTBTeamsAPI.api();
        return api.isManagerLoaded() ? api.getManager() : null;
    }

    @Override
    public boolean needsRegularMembershipRecheck() {
        return false;
    }

    @Override
    public String getName() {
        return FTBTeamsAPI.MOD_ID;
    }
}
