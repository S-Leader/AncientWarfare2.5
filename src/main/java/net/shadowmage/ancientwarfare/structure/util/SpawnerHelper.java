package net.shadowmage.ancientwarfare.structure.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.tile.SpawnerSettings;
import net.shadowmage.ancientwarfare.structure.tile.TileAdvancedSpawner;

public class SpawnerHelper {
    private SpawnerHelper() {
    }

    public static boolean createSpawner(IRespawnData respawnData, Level world) {
        if (!(world instanceof ServerLevel serverLevel) || !respawnData.canRespawn()) {
            return false;
        }

        BlockPos originalPos = respawnData.getRespawnPos();

        // Never synchronously request a chunk from an entity removal or chunk-unload
        // path. The deferred listener will retry after this chunk is loaded normally.
        if (!serverLevel.hasChunkAt(originalPos)) {
            return false;
        }

        BlockState originalState = serverLevel.getBlockState(originalPos);

        // Idempotence: if another entity from the same spawn group has already
        // restored the one-shot spawner, this task is complete. Never search above
        // or below it and create a second stacked spawner.
        if (originalState.is(AWStructureBlocks.ADVANCED_SPAWNER)) {
            return true;
        }

        // The respawn position is now the original spawner block position. Only that
        // exact position is valid; vertical fallbacks were the direct cause of piles
        // of four or five advanced spawners.
        if (!originalState.isAir() && !originalState.canBeReplaced()) {
            return false;
        }

        BlockPos respawnPos = originalPos.immutable();
        if (!serverLevel.setBlock(respawnPos, AWStructureBlocks.ADVANCED_SPAWNER.defaultBlockState(), 3)) {
            return false;
        }

        TileAdvancedSpawner spawner = WorldTools.getTile(serverLevel, respawnPos, TileAdvancedSpawner.class).orElse(null);
        if (spawner == null) {
            serverLevel.removeBlock(respawnPos, false);
            return false;
        }

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
        spawner.setSettings(settings);
        return true;
    }

}
