package net.shadowmage.ancientwarfare.automation.tile;

import net.minecraft.world.level.ChunkPos;
import net.shadowmage.ancientwarfare.automation.chunkloader.AWChunkLoader;
import net.shadowmage.ancientwarfare.core.interfaces.IChunkLoaderTile;
import net.shadowmage.ancientwarfare.core.tile.TileUpdatable;

import java.util.HashSet;
import java.util.Set;

public class TileChunkLoaderSimple extends TileUpdatable implements IChunkLoaderTile {
    private final Set<ChunkPos> forcedChunks = new HashSet<>();

    public TileChunkLoaderSimple() {
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            refreshForcedChunks();
        }
    }

    @Override
    public void invalidate() {
        releaseForcedChunks();
        super.invalidate();
    }

    @Override
    public void refreshForcedChunks() {
        if (level == null || level.isClientSide) {
            return;
        }

        Set<ChunkPos> wanted = getChunksToForce();
        for (ChunkPos existing : Set.copyOf(forcedChunks)) {
            if (!wanted.contains(existing)) {
                AWChunkLoader.INSTANCE.unforce(level, worldPosition, existing);
                forcedChunks.remove(existing);
            }
        }
        for (ChunkPos chunk : wanted) {
            if (forcedChunks.add(chunk)) {
                AWChunkLoader.INSTANCE.force(level, worldPosition, chunk);
            }
        }
    }

    @Override
    public void releaseForcedChunks() {
        if (level != null && !level.isClientSide) {
            AWChunkLoader.INSTANCE.unforceAll(level, worldPosition, forcedChunks);
        }
        forcedChunks.clear();
    }

    /**
     * Legacy call site retained for block placement.
     */
    public void setupInitialTicket() {
        refreshForcedChunks();
    }

    protected Set<ChunkPos> getChunksToForce() {
        Set<ChunkPos> chunks = new HashSet<>();
        ChunkPos center = new ChunkPos(worldPosition);
        for (int x = center.x - 1; x <= center.x + 1; x++) {
            for (int z = center.z - 1; z <= center.z + 1; z++) {
                chunks.add(new ChunkPos(x, z));
            }
        }
        return chunks;
    }
}
