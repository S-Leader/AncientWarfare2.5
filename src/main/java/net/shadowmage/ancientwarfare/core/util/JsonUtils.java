package net.shadowmage.ancientwarfare.core.util;

import com.google.gson.*;
import net.minecraft.util.GsonHelper;

import java.io.Reader;

/**
 * Source-compatible facade for the 1.12 JsonUtils API.
 */
public final class JsonUtils {
    private JsonUtils() {
    }

    private static JsonElement element(JsonElement parent, String name) {
        if (name == null || name.isEmpty()) return parent;
        if (!parent.isJsonObject() || !parent.getAsJsonObject().has(name))
            throw new JsonSyntaxException("Missing " + name);
        return parent.getAsJsonObject().get(name);
    }

    public static boolean hasField(JsonObject object, String name) {
        return object.has(name);
    }

    public static boolean isJsonPrimitive(JsonObject object, String name) {
        return object.has(name) && object.get(name).isJsonPrimitive();
    }

    public static String getString(JsonElement parent, String name) {
        return GsonHelper.convertToString(element(parent, name), name);
    }

    public static int getInt(JsonElement parent, String name) {
        return GsonHelper.convertToInt(element(parent, name), name);
    }

    public static int getInt(JsonObject parent, String name, int fallback) {
        return parent.has(name) ? getInt(parent, name) : fallback;
    }

    public static float getFloat(JsonElement parent, String name) {
        return GsonHelper.convertToFloat(element(parent, name), name);
    }

    public static boolean getBoolean(JsonElement parent, String name) {
        return GsonHelper.convertToBoolean(element(parent, name), name);
    }

    public static boolean getBoolean(JsonObject parent, String name, boolean fallback) {
        return parent.has(name) ? getBoolean(parent, name) : fallback;
    }

    public static JsonObject getJsonObject(JsonElement parent, String name) {
        return GsonHelper.convertToJsonObject(element(parent, name), name);
    }

    public static JsonArray getJsonArray(JsonElement parent, String name) {
        return GsonHelper.convertToJsonArray(element(parent, name), name);
    }

    public static <T> T fromJson(Gson gson, Reader reader, Class<T> type) {
        return GsonHelper.fromJson(gson, reader, type);
    }
}
