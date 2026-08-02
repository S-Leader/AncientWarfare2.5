package net.shadowmage.ancientwarfare.structure.worldgen;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Forge 1.20.1 replacement for the removed IWorldGenerator callback.
 * Only newly-created server chunks are submitted to the existing delayed
 * structure/town queues; loading an old save does not reroll generation.
 */
public final class WorldGenerationEventHandler {
    public static final WorldGenerationEventHandler INSTANCE = new WorldGenerationEventHandler();

    private WorldGenerationEventHandler() {
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        ChunkPos chunk = event.getChunk().getPos();
        //Territory lookup loads other chunks; doing that inside ChunkEvent.Load deadlocks
        //the 1.20 chunk pipeline. Queue coordinates only; the check runs next server tick.
        WorldGenTickHandler.INSTANCE.addChunkForDeferredCheck(level, chunk.x, chunk.z);
    }
}
