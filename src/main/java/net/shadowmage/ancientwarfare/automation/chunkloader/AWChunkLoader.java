package net.shadowmage.ancientwarfare.automation.chunkloader;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.shadowmage.ancientwarfare.automation.AncientWarfareAutomation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Forge 1.20.1 chunk-loading bridge.
 *
 * <p>The old Ticket API was removed. Persistent tickets are now keyed directly
 * by the owning block position, so the block entity position replaces the old
 * ticket object and its custom NBT payload.</p>
 */
public final class AWChunkLoader {
    public static final AWChunkLoader INSTANCE = new AWChunkLoader();
    private final Map<ChunkTicketKey, Boolean> pendingChanges = new LinkedHashMap<>();

    private AWChunkLoader() {
    }

    public boolean force(Level level, BlockPos owner, ChunkPos chunk) {
        return setForced(level, owner, chunk, true);
    }

    public boolean unforce(Level level, BlockPos owner, ChunkPos chunk) {
        return setForced(level, owner, chunk, false);
    }

    public void forceAll(Level level, BlockPos owner, Collection<ChunkPos> chunks) {
        for (ChunkPos chunk : chunks) {
            force(level, owner, chunk);
        }
    }

    public void unforceAll(Level level, BlockPos owner, Collection<ChunkPos> chunks) {
        for (ChunkPos chunk : chunks) {
            unforce(level, owner, chunk);
        }
    }

    private boolean setForced(Level level, BlockPos owner, ChunkPos chunk, boolean add) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        // BlockEntity#onLoad may run while DistanceManager is iterating its own
        // ticket map. Calling ForgeChunkManager there mutates that same map and can
        // throw ConcurrentModificationException. Coalesce the requested final
        // state and apply it at the beginning of a later server tick.
        synchronized (pendingChanges) {
            pendingChanges.put(new ChunkTicketKey(serverLevel.dimension(), owner.immutable(), chunk.toLong()), add);
        }
        return true;
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        /*
         * Apply ticket mutations at the beginning of the server tick, before any
         * ServerLevel starts its chunk-distance update. Applying them at END can
         * still re-enter chunk loading while Minecraft is draining chunk tasks,
         * which is exactly where DistanceManager iterates its ticket collections.
         */
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        Map<ChunkTicketKey, Boolean> changes;
        synchronized (pendingChanges) {
            if (pendingChanges.isEmpty()) {
                return;
            }
            changes = new LinkedHashMap<>(pendingChanges);
            pendingChanges.clear();
        }
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        changes.forEach((key, add) -> {
            ServerLevel level = server.getLevel(key.dimension());
            if (level != null) {
                ChunkPos chunk = new ChunkPos(key.chunkPos());
                ForgeChunkManager.forceChunk(level, AncientWarfareAutomation.MOD_ID,
                        key.owner(), chunk.x, chunk.z, add, true);
            }
        });
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        synchronized (pendingChanges) {
            pendingChanges.clear();
        }
    }

    private record ChunkTicketKey(ResourceKey<Level> dimension, BlockPos owner, long chunkPos) {
    }
}
