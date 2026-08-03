package net.shadowmage.ancientwarfare.structure.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.tile.SpawnerSettings;
import net.shadowmage.ancientwarfare.structure.tile.TileAdvancedSpawner;

public class SpawnerHelper {
    private SpawnerHelper() {
    }

    public static void createSpawner(IRespawnData respawnData, Level world) {
        if (!(world instanceof ServerLevel serverLevel) || !respawnData.canRespawn()) {
            return;
        }

        BlockPos respawnPos = respawnData.getRespawnPos();

        // Never synchronously request a chunk from an entity removal or chunk-unload
        // path. The deferred listener will retry after this chunk is loaded normally.
        if (!serverLevel.hasChunkAt(respawnPos) || !serverLevel.isEmptyBlock(respawnPos)) {
            return;
        }

        serverLevel.setBlock(respawnPos, AWStructureBlocks.ADVANCED_SPAWNER.defaultBlockState(), 3);
        WorldTools.getTile(serverLevel, respawnPos, TileAdvancedSpawner.class).ifPresent(te -> {
            SpawnerSettings settings = new SpawnerSettings();
            settings.readFromNBT(respawnData.getSpawnerSettings());
            // If the entity spawned only a fraction of time before it despawned,
            // disable the broken entry by replacing it with a zombie.
            if (serverLevel.getGameTime() - respawnData.getSpawnTime() < 10
                    && !settings.getSpawnGroups().isEmpty()
                    && !settings.getSpawnGroups().get(0).getEntitiesToSpawn().isEmpty()) {
                settings.getSpawnGroups().get(0).getEntitiesToSpawn().get(0)
                        .setEntityToSpawn(new ResourceLocation("minecraft", "zombie"));
            }
            te.setSettings(settings);
        });
    }
}
