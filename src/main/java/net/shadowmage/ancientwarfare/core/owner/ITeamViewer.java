package net.shadowmage.ancientwarfare.core.owner;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

public interface ITeamViewer {
    boolean areTeamMates(Level world, UUID player1, UUID player2, String playerName1, String playerName2);

    boolean areFriendly(Level world, UUID player1, @Nullable UUID player2, String playerName1, String playerName2);

    Set<ResourceLocation> getPlayerTeamNames(Level world, UUID playerId, String playerName);

    default boolean needsRegularMembershipRecheck() {
        return true;
    }

    String getName();
}
