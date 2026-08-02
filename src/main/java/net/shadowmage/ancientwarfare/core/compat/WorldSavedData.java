package net.shadowmage.ancientwarfare.core.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * AW2 compatibility base for the 1.12 WorldSavedData contract. The actual
 * persistence backend is Minecraft 1.20.1 SavedData/DimensionDataStorage.
 * <p>
 * Lives in an AW package: keeping it under net.minecraft.* made the mod and
 * the minecraft module export the same package, which JPMS rejects at launch.
 */
public abstract class WorldSavedData extends SavedData {
    private final String name;

    protected WorldSavedData(String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    public abstract void readFromNBT(CompoundTag tag);

    public abstract CompoundTag writeToNBT(CompoundTag tag);

    @Override
    public final CompoundTag save(CompoundTag tag) {
        return writeToNBT(tag);
    }

    public final void markDirty() {
        setDirty();
    }
}
