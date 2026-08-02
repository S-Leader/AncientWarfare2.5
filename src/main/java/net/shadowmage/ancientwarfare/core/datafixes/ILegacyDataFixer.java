package net.shadowmage.ancientwarfare.core.datafixes;

import net.minecraft.nbt.CompoundTag;

/**
 * Keeps Ancient Warfare's pre-DFU NBT migrations available on modern Minecraft.
 * Callers choose the data category and apply the registered fixes before loading
 * legacy entity, block-entity, or item data.
 */
public interface ILegacyDataFixer {
    int getFixVersion();

    CompoundTag fixTagCompound(CompoundTag compound);
}
