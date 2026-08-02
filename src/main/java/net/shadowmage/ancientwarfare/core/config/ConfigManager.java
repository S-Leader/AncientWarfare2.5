package net.shadowmage.ancientwarfare.core.config;

import net.shadowmage.ancientwarfare.core.config.legacy.LegacyProperty;

import java.util.*;

/**
 * Small client-facing registry for values that still live in the migrated
 * legacy configuration files. Forge's old IConfigElement GUI was removed;
 * this registry keeps the same toggles available to the modern config screen.
 */
public final class ConfigManager {
    private static final List<BooleanEntry> BOOLEAN_ENTRIES = new ArrayList<>();
    private static final Set<LegacyProperty> REGISTERED_PROPERTIES =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private ConfigManager() {
    }

    public static synchronized void registerBoolean(
            String translationKey, LegacyProperty property, Runnable saveAction) {
        if (property == null || !REGISTERED_PROPERTIES.add(property)) {
            return;
        }
        BOOLEAN_ENTRIES.add(new BooleanEntry(translationKey, property,
                saveAction == null ? () -> {
                } : saveAction));
    }

    public static synchronized List<BooleanEntry> getBooleanEntries() {
        return List.copyOf(BOOLEAN_ENTRIES);
    }

    public record BooleanEntry(String translationKey, LegacyProperty property, Runnable saveAction) {
        public boolean get() {
            return property.getBoolean();
        }

        public void set(boolean value) {
            property.set(Boolean.toString(value));
            saveAction.run();
        }

        public void toggle() {
            set(!get());
        }
    }
}
