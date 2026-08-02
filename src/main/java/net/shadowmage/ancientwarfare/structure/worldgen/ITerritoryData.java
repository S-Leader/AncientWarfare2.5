package net.shadowmage.ancientwarfare.structure.worldgen;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Optional;

public interface ITerritoryData extends INBTSerializable<CompoundTag> {
    int getNextTerritoryId(String territoryName);

    Optional<Territory> getTerritory(long chunkPosValue);

    boolean isOwned(long chunkPos);

    void setOwned(long chunkPos, String territoryId, String territoryName);

    boolean isDifferentTerritory(String territoryId, int x, int z);
}
