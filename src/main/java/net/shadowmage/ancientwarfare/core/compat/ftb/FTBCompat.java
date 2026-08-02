package net.shadowmage.ancientwarfare.core.compat.ftb;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.shadowmage.ancientwarfare.core.compat.ICompat;
import net.shadowmage.ancientwarfare.core.owner.TeamViewerRegistry;

/**
 * Forge 1.20.1 FTB Teams compatibility bootstrap.
 */
public final class FTBCompat implements ICompat {
    @Override
    public String getModId() {
        return FTBTeamsAPI.MOD_ID;
    }

    @Override
    public void init() {
        TeamViewerRegistry.registerTeamViewer(new FTBTeamViewer());
    }
}
