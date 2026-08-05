package net.shadowmage.ancientwarfare.structure.event;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.shadowmage.ancientwarfare.npc.entity.faction.NpcFaction;
import net.shadowmage.ancientwarfare.structure.util.CapabilityRespawnData;
import net.shadowmage.ancientwarfare.structure.util.IRespawnData;
import net.shadowmage.ancientwarfare.structure.util.RespawnData;
import net.shadowmage.ancientwarfare.structure.util.SpawnerHelper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Replaces the removed IWorldEventListener entity-removal callback.
 *
 * <p>EntityLeaveLevelEvent can be fired while DistanceManager is iterating its
 * chunk update set. Reading or writing a block from that callback may synchronously
 * request a chunk and re-enter DistanceManager, causing a ConcurrentModificationException.
 * Respawn data is therefore copied here and processed from the next server tick.</p>
 */
public final class OneShotEntityDespawnListener {
    private static final int MAX_PENDING_CHECKS_PER_TICK = 64;
    private static final int MAX_SPAWNERS_PER_TICK = 8;

    public static final OneShotEntityDespawnListener INSTANCE = new OneShotEntityDespawnListener();

    private final ArrayDeque<PendingRespawn> pendingRespawns = new ArrayDeque<>();
    private final Set<PendingRespawnKey> pendingKeys = new HashSet<>();
    private boolean serverStopping;

    private OneShotEntityDespawnListener() {
    }

    @SubscribeEvent
    public void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()
                || !(event.getEntity() instanceof LivingEntity living)
                || living instanceof NpcFaction
                || living.getRemovalReason() != Entity.RemovalReason.DISCARDED
                || living.getHealth() <= 0.0F) {
            return;
        }

        ResourceLocation typeId = ForgeRegistries.ENTITY_TYPES.getKey(living.getType());
        if (typeId != null && "iceandfire".equals(typeId.getNamespace())) {
            return;
        }

        CapabilityRespawnData.get(living)
                .filter(IRespawnData::canRespawn)
                .ifPresent(data -> queueRespawn(data, event.getLevel()));
    }

    /**
     * A killed entity must consume its one-shot-spawner association permanently.
     * Otherwise its later removal can be mistaken for a distance despawn and the
     * spawner is recreated after the player has legitimately defeated the mob.
     */
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        CapabilityRespawnData.get(event.getEntity())
                .ifPresent(data -> data.setRespawnPos(net.minecraft.core.BlockPos.ZERO));
    }

    /**
     * Takes a detached snapshot while the entity capability is still valid.
     * Mob#checkDespawn discards the entity synchronously, and the capability is
     * invalidated from EntityLeaveLevelEvent before checkDespawn returns.
     */
    public RespawnData snapshotRespawnData(Entity entity) {
        return CapabilityRespawnData.get(entity)
                .filter(IRespawnData::canRespawn)
                .map(this::copyRespawnData)
                .orElse(null);
    }

    /**
     * Queues a detached copy of the respawn data without touching the world.
     * This method is safe to call from entity removal/despawn callbacks.
     */
    public void queueRespawn(IRespawnData data, Level level) {
        if (serverStopping || level.isClientSide() || !(level instanceof ServerLevel) || !data.canRespawn()) {
            return;
        }

        RespawnData snapshot = copyRespawnData(data);
        PendingRespawn pending = new PendingRespawn(level.dimension(), snapshot);
        if (pendingKeys.add(pending.key())) {
            pendingRespawns.addLast(pending);
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || pendingRespawns.isEmpty()) {
            return;
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        int checksRemaining = Math.min(MAX_PENDING_CHECKS_PER_TICK, pendingRespawns.size());
        int spawnersRemaining = MAX_SPAWNERS_PER_TICK;
        List<ReadyRespawn> readyRespawns = new ArrayList<>();

        while (checksRemaining-- > 0 && spawnersRemaining > 0 && !pendingRespawns.isEmpty()) {
            PendingRespawn pending = pendingRespawns.removeFirst();
            ServerLevel level = server.getLevel(pending.dimension());

            if (level == null) {
                pendingKeys.remove(pending.key());
                continue;
            }

            // hasChunkAt only checks the loaded chunk map. It must not synchronously
            // load the chunk while DistanceManager may still be processing updates.
            if (!level.hasChunkAt(pending.data().getRespawnPos())) {
                pendingRespawns.addLast(pending);
                continue;
            }

            readyRespawns.add(new ReadyRespawn(level, pending));
            spawnersRemaining--;
        }

        // Execute world writes only after the queue iteration has finished, so any
        // callbacks caused by block placement cannot mutate the queue being iterated.
        // A failed placement is queued again instead of silently losing the NPC.
        for (ReadyRespawn ready : readyRespawns) {
            PendingRespawn pending = ready.pending();
            if (SpawnerHelper.createSpawner(pending.data(), ready.level())) {
                pendingKeys.remove(pending.key());
            } else {
                pendingRespawns.addLast(pending);
            }
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        serverStopping = false;
        pendingRespawns.clear();
        pendingKeys.clear();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        // Never touch chunks while ServerChunkCache/DistanceManager is closing.
        serverStopping = true;
        pendingRespawns.clear();
        pendingKeys.clear();
    }

    private RespawnData copyRespawnData(IRespawnData data) {
        RespawnData snapshot = new RespawnData();
        snapshot.setRespawnPos(data.getRespawnPos().immutable());
        snapshot.setSpawnerSettings(data.getSpawnerSettings().copy());
        snapshot.setSpawnTime(data.getSpawnTime());
        return snapshot;
    }

    private record PendingRespawn(ResourceKey<Level> dimension, RespawnData data) {
        private PendingRespawnKey key() {
            return new PendingRespawnKey(dimension, data.getRespawnPos().asLong());
        }
    }

    private record PendingRespawnKey(ResourceKey<Level> dimension, long blockPos) {
        private PendingRespawnKey {
            Objects.requireNonNull(dimension, "dimension");
        }
    }

    private record ReadyRespawn(ServerLevel level, PendingRespawn pending) {
    }
}
