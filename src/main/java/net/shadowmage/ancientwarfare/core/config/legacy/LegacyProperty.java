package net.shadowmage.ancientwarfare.core.config.legacy;

import java.util.function.Consumer;

/**
 * Typed view over one migrated legacy configuration value.
 */
public final class LegacyProperty {
    private static final String LIST_SEPARATOR = "\u001f";
    private String value;
    private final Consumer<String> writer;

    LegacyProperty(String value) {
        this(value, ignored -> {
        });
    }

    LegacyProperty(String value, Consumer<String> writer) {
        this.value = value == null ? "" : value;
        this.writer = writer;
    }

    public boolean getBoolean() {
        return getBoolean(false);
    }

    public boolean getBoolean(boolean fallback) {
        return "true".equalsIgnoreCase(value) || (!"false".equalsIgnoreCase(value) && fallback);
    }

    public int getInt() {
        return getInt(0);
    }

    public int getInt(int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    public double getDouble() {
        return getDouble(0.0D);
    }

    public double getDouble(double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    public String getString() {
        return value;
    }

    public String get() {
        return value;
    }

    public void set(String newValue) {
        value = newValue;
        writer.accept(newValue);
    }

    public String[] getStringList() {
        return value.isEmpty() ? new String[0] : value.split(LIST_SEPARATOR, -1);
    }

    static String encode(String[] values) {
        return String.join(LIST_SEPARATOR, values);
    }
}
