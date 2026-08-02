package net.shadowmage.ancientwarfare.core.config.legacy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * File-backed bridge for AW2's 1.12 configuration calls.
 */
public final class LegacyConfiguration {
    private final Path path;
    private final Properties values = new Properties();
    private boolean changed;

    public LegacyConfiguration(java.io.File file) {
        path = file.toPath();
        if (Files.isRegularFile(path)) {
            try (InputStream input = Files.newInputStream(path)) {
                values.load(input);
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to read Ancient Warfare config " + path, exception);
            }
        }
    }

    public void addCustomCategoryComment(String category, String comment) {
    }

    public LegacyProperty get(String c, String n, boolean d) {
        return value(c, n, Boolean.toString(d));
    }

    public LegacyProperty get(String c, String n, boolean d, String comment) {
        return get(c, n, d);
    }

    public LegacyProperty get(String c, String n, int d) {
        return value(c, n, Integer.toString(d));
    }

    public LegacyProperty get(String c, String n, int d, String comment) {
        return get(c, n, d);
    }

    public LegacyProperty get(String c, String n, double d) {
        return value(c, n, Double.toString(d));
    }

    public LegacyProperty get(String c, String n, double d, String comment) {
        return get(c, n, d);
    }

    public LegacyProperty get(String c, String n, String d) {
        return value(c, n, d);
    }

    public LegacyProperty get(String c, String n, String d, String comment) {
        return get(c, n, d);
    }

    public LegacyProperty get(String c, String n, String[] d) {
        return value(c, n, LegacyProperty.encode(d));
    }

    public LegacyProperty get(String c, String n, String[] d, String comment) {
        return get(c, n, d);
    }

    public boolean getBoolean(String n, String c, boolean d, String comment) {
        return get(c, n, d).getBoolean(d);
    }

    public int getInt(String n, String c, int d, int min, int max, String comment) {
        return Math.max(min, Math.min(max, get(c, n, d).getInt(d)));
    }

    public float getFloat(String n, String c, float d, float min, float max, String comment) {
        return (float) Math.max(min, Math.min(max, get(c, n, (double) d).getDouble(d)));
    }

    public String[] getStringList(String n, String c, String[] d, String comment) {
        return get(c, n, d).getStringList();
    }

    public LegacyConfigCategory getCategory(String category) {
        LegacyConfigCategory result = new LegacyConfigCategory();
        String prefix = category + ".";
        values.stringPropertyNames().stream().filter(key -> key.startsWith(prefix)).sorted().forEach(key -> result.put(key.substring(prefix.length()), property(key)));
        return result;
    }

    public boolean hasChanged() {
        return changed;
    }

    public void save() {
        if (!changed && Files.isRegularFile(path)) return;
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            try (OutputStream output = Files.newOutputStream(path)) {
                values.store(output, "Ancient Warfare 2 migrated configuration");
            }
            changed = false;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save Ancient Warfare config " + path, exception);
        }
    }

    private LegacyProperty value(String category, String name, String fallback) {
        String key = category + "." + name;
        if (!values.containsKey(key)) {
            values.setProperty(key, fallback);
            changed = true;
        }
        return property(key);
    }

    private LegacyProperty property(String key) {
        return new LegacyProperty(values.getProperty(key), newValue -> {
            values.setProperty(key, newValue);
            changed = true;
        });
    }
}
