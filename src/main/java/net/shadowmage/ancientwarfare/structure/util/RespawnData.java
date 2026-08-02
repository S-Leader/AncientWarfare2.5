package net.shadowmage.ancientwarfare.structure.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public class RespawnData implements IRespawnData {
    private BlockPos respawnPos = BlockPos.ZERO;
    private CompoundTag spawnerSettings = new CompoundTag();
    private boolean canRespawn = false;
    private long spawnTime = 0;

    @Override
    public boolean canRespawn() {
        return canRespawn;
    }

    @Override
    public BlockPos getRespawnPos() {
        return respawnPos;
    }

    @Override
    public CompoundTag getSpawnerSettings() {
        return spawnerSettings;
    }

    @Override
    public long getSpawnTime() {
        return spawnTime;
    }

    @Override
    public void setRespawnPos(BlockPos pos) {
        respawnPos = pos;
        canRespawn = !respawnPos.equals(BlockPos.ZERO);
    }

    @Override
    public void setSpawnerSettings(CompoundTag tag) {
        spawnerSettings = tag;
    }

    @Override
    public void setSpawnTime(long time) {
        spawnTime = time;
    }
}
