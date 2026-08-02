package net.shadowmage.ancientwarfare.core.gamedata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.shadowmage.ancientwarfare.core.compat.WorldSavedData;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Creates the original AW2 saved-data classes through the 1.20.1 data storage.
 */
public final class AWGameData {
    public static final AWGameData INSTANCE = new AWGameData();

    /**
     * Client levels need a non-persistent mirror for structure/network caches.
     */
    private final Map<Level, Map<Class<?>, WorldSavedData>> clientData = new WeakHashMap<>();

    private AWGameData() {
    }

    public <T extends WorldSavedData> T getData(Level level, Class<T> type) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return getClientData(level, type);
        }
        return initData(serverLevel.getServer().overworld().getDataStorage(), type);
    }

    public <T extends WorldSavedData> T getPerWorldData(Level level, Class<T> type) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return getClientData(level, type);
        }
        return initData(serverLevel.getDataStorage(), type);
    }

    @SuppressWarnings("unchecked")
    private synchronized <T extends WorldSavedData> T getClientData(Level level, Class<T> type) {
        Map<Class<?>, WorldSavedData> byType = clientData.computeIfAbsent(level, ignored -> new HashMap<>());
        return (T) byType.computeIfAbsent(type, ignored -> construct(type, "AW" + type.getSimpleName()));
    }

    private <T extends WorldSavedData> T initData(DimensionDataStorage storage, Class<T> type) {
        String name = "AW" + type.getSimpleName();
        return storage.computeIfAbsent(
                tag -> load(type, name, tag),
                () -> construct(type, name),
                name);
    }

    private static <T extends WorldSavedData> T load(Class<T> type, String name, CompoundTag tag) {
        T data = construct(type, name);
        data.readFromNBT(tag);
        return data;
    }

    private static <T extends WorldSavedData> T construct(Class<T> type, String name) {
        try {
            Constructor<T> named = type.getConstructor(String.class);
            return named.newInstance(name);
        } catch (ReflectiveOperationException namedFailure) {
            try {
                Constructor<T> empty = type.getConstructor();
                return empty.newInstance();
            } catch (ReflectiveOperationException emptyFailure) {
                throw new IllegalStateException("Error creating saved data " + type.getName(), namedFailure);
            }
        }
    }
}
