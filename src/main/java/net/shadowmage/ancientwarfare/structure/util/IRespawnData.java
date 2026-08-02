package net.shadowmage.ancientwarfare.structure.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public interface IRespawnData {
    boolean canRespawn();

    BlockPos getRespawnPos();

    CompoundTag getSpawnerSettings();

    long getSpawnTime();

    void setRespawnPos(BlockPos pos);

    void setSpawnerSettings(CompoundTag tag);

    void setSpawnTime(long time);
}
