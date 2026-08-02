package net.shadowmage.ancientwarfare.npc.compat.ebwizardry;

import com.google.gson.JsonObject;
import net.minecraftforge.common.MinecraftForge;
import net.shadowmage.ancientwarfare.core.compat.ICompat;
import net.shadowmage.ancientwarfare.core.util.JsonUtils;
import net.shadowmage.ancientwarfare.npc.registry.FactionNpcDefault;

/**
 * Electroblob's Wizardry Redux compatibility bootstrap for Forge 1.20.1.
 */
public final class EBWizardryCompat implements ICompat {
    private static final String SPELLS_DATA_KEY = "ebwizardry.spells";

    @Override
    public String getModId() {
        return WizardryReduxBridge.MOD_ID;
    }

    @Override
    public void init() {
        MinecraftForge.EVENT_BUS.register(FactionAllyDesignation.class);
    }

    public static FactionNpcDefault applyDefaults(FactionNpcDefault defaults, String subtype, JsonObject data) {
        if (!subtype.contains("spellcaster") || !data.has("spells")) {
            return defaults;
        }
        return defaults.setCompatibilityData(SPELLS_DATA_KEY, JsonUtils.getString(data, "spells"));
    }

    public static String getDefaultSpells(FactionNpcDefault defaults) {
        return defaults.getCompatibilityData(SPELLS_DATA_KEY);
    }
}
