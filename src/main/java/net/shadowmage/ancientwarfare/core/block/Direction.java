package net.shadowmage.ancientwarfare.core.block;

import javax.annotation.Nullable;

public final class Direction {
    private static final String TRANSLATION_KEY_PREFIX = "guistrings.inventory.direction.";

    private Direction() {
    }

    public static String getTranslationKey(@Nullable net.minecraft.core.Direction facing) {
        return TRANSLATION_KEY_PREFIX + (facing == null ? "unknown" : facing.getName());
    }
}
