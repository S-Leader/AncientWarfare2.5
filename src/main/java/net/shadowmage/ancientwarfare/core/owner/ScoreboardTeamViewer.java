package net.shadowmage.ancientwarfare.core.owner;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Vanilla scoreboard-team adapter for 1.20.1.
 */
public final class ScoreboardTeamViewer implements ITeamViewer {
    @Override
    public boolean areTeamMates(Level level, UUID player1, UUID player2, String playerName1, String playerName2) {
        return player1.equals(player2) || isSameTeam(level, playerName1, playerName2);
    }

    @Override
    public boolean areFriendly(Level level, UUID player1, @Nullable UUID player2, String playerName1, String playerName2) {
        return playerName1.equals(playerName2)
                || player2 != null && player1.equals(player2)
                || isSameTeam(level, playerName1, playerName2);
    }

    @Override
    public Set<ResourceLocation> getPlayerTeamNames(Level level, UUID playerId, String playerName) {
        PlayerTeam team = level.getScoreboard().getPlayersTeam(playerName);
        return team == null
                ? Collections.emptySet()
                : Collections.singleton(new ResourceLocation("minecraft", toPath(team.getName())));
    }

    @Override
    public String getName() {
        return "minecraft";
    }

    private static boolean isSameTeam(Level level, String playerName1, String playerName2) {
        PlayerTeam first = level.getScoreboard().getPlayersTeam(playerName1);
        return first != null && first == level.getScoreboard().getPlayersTeam(playerName2);
    }

    private static String toPath(String name) {
        String path = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_");
        return path.isEmpty() ? "unnamed_team" : path;
    }
}
