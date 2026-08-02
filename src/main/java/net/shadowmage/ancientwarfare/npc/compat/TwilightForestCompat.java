package net.shadowmage.ancientwarfare.npc.compat;

import net.minecraft.resources.ResourceLocation;
import net.shadowmage.ancientwarfare.core.compat.ICompat;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

/**
 * Twilight Forest no longer exposes a configurable numeric dimension id.
 */
public class TwilightForestCompat implements ICompat {
    private static final ResourceLocation TWILIGHT_FOREST = new ResourceLocation("twilightforest", "twilight_forest");

    @Override
    public String getModId() {
        return "twilightforest";
    }

    @Override
    public void init() {
        WorldTools.registerDimensionDaytimeLogic(TWILIGHT_FOREST,
                level -> level.getDayTime() % 24000L < 12000L);
    }
}
