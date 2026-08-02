package net.shadowmage.ancientwarfare.core.util;

import net.minecraftforge.fml.ModList;

public class CompatUtils {
    private CompatUtils() {
    }

    public static boolean areModsLoaded(String[] mods) {
        for (String mod : mods) {
            if (!mod.isEmpty() && !ModList.get().isLoaded(mod)) {
                return false;
            }
        }
        return true;
    }
}
