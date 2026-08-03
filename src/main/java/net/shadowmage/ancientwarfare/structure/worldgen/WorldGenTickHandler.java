package net.shadowmage.ancientwarfare.structure.worldgen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBuilder;
import net.shadowmage.ancientwarfare.structure.town.WorldTownGenerator;

import javax.annotation.Nullable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Delayed structure-generation scheduler.
 *
 * <p>All world and chunk access is performed from {@link TickEvent.Phase#START}.
 * Running legacy generation from the server-tick END phase can synchronously load
 * neighbouring chunks while Minecraft is draining chunk tasks and iterating the
 * DistanceManager ticket map, which causes a re-entrant
 * {@link java.util.ConcurrentModificationException}.</p>
 */
public final class WorldGenTickHandler {
    private static final int MAX_BLOCKS_TO_GEN_PER_TICK = 10000;
    private static final int MAX_CHUNK_CHECKS_PER_TICK = 32;
    private static final int MAX_WORLD_GEN_CHUNKS_PER_TICK = 1;
    private static final int MAX_TOWN_GEN_CHUNKS_PER_TICK = 1;

    public static final WorldGenTickHandler INSTANCE = new WorldGenTickHandler();

    private final Queue<ChunkGenerationTicket> chunkChecks = new ConcurrentLinkedQueue<>();
    private final Queue<ChunkGenerationTicket> chunksToGen = new ConcurrentLinkedQueue<>();
    private final Queue<ChunkGenerationTicket> townChunksToGen = new ConcurrentLinkedQueue<>();
    private final Queue<StructureTicket> structuresToGen = new ConcurrentLinkedQueue<>();
    private StructureTicket activeStructureTicket;

    private volatile boolean acceptingTickets = true;

    private WorldGenTickHandler() {
    }

    /** Reset state when a new integrated/dedicated server starts in the same JVM. */
    public void start() {
        clearQueues();
        acceptingTickets = true;
    }

    /**
     * Stop accepting work and discard unfinished generation.
     *
     * <p>Do not finish queued structures from ServerStoppingEvent. At that point
     * ServerChunkCache is already closing and any chunk lookup can mutate the
     * DistanceManager while it is being iterated.</p>
     */
    public void shutdown() {
        acceptingTickets = false;
        clearQueues();
    }

    private void clearQueues() {
        chunkChecks.clear();
        chunksToGen.clear();
        townChunksToGen.clear();

        if (activeStructureTicket != null) {
            activeStructureTicket.cancel(false);
            activeStructureTicket = null;
        }
        StructureTicket queued;
        while ((queued = structuresToGen.poll()) != null) {
            queued.cancel(false);
        }
    }

    /**
     * Entry point from ChunkEvent.Load. Only coordinates are queued here; no
     * territory lookup or neighbouring chunk access is allowed from the load event.
     */
    public void addChunkForDeferredCheck(Level world, int chunkX, int chunkZ) {
        if (canQueue(world)) {
            chunkChecks.offer(new ChunkGenerationTicket(world, chunkX, chunkZ));
        }
    }

    void addChunkForGeneration(Level world, int chunkX, int chunkZ) {
        if (canQueue(world)) {
            chunksToGen.offer(new ChunkGenerationTicket(world, chunkX, chunkZ));
        }
    }

    public void addChunkForTownGeneration(Level world, int chunkX, int chunkZ) {
        if (canQueue(world)) {
            townChunksToGen.offer(new ChunkGenerationTicket(world, chunkX, chunkZ));
        }
    }

    public void addStructureForGeneration(StructureBuilder builder) {
        if (acceptingTickets && builder != null) {
            structuresToGen.offer(new StructureGenerationTicket(builder));
        }
    }

    public void addStructureGenCallback(StructureTicket ticket) {
        if (acceptingTickets && ticket != null) {
            structuresToGen.offer(ticket);
        }
    }

    private boolean canQueue(Level world) {
        return acceptingTickets && world != null && !world.isClientSide();
    }

    boolean isAcceptingTickets() {
        return acceptingTickets;
    }

    @SuppressWarnings("unused")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !acceptingTickets) {
            return;
        }

        // START is deliberately used: no generation code may run from END.
        runChunkChecks();
        genChunks();
        genStructures();
        genTowns();
    }

    private void runChunkChecks() {
        int checks = Math.min(chunkChecks.size(), MAX_CHUNK_CHECKS_PER_TICK);
        while (checks-- > 0 && acceptingTickets) {
            ChunkGenerationTicket ticket = chunkChecks.poll();
            if (ticket == null) {
                break;
            }
            Level world = ticket.getWorld();
            if (world != null) {
                WorldStructureGenerator.INSTANCE.queueChunkForGeneration(ticket.chunkX, ticket.chunkZ, world);
                WorldTownGenerator.INSTANCE.queueChunkForGeneration(ticket.chunkX, ticket.chunkZ, world);
            }
        }
    }

    private void genChunks() {
        /*
         * Do not keep selecting more potentially huge templates while one
         * validation/build chain is still active. This bounds both temporary
         * chunk tickets and queued multi-chunk work.
         */
        if (hasPendingStructureWork()) {
            return;
        }
        int chunks = Math.min(chunksToGen.size(), MAX_WORLD_GEN_CHUNKS_PER_TICK);
        while (chunks-- > 0 && acceptingTickets) {
            ChunkGenerationTicket ticket = chunksToGen.poll();
            if (ticket == null) {
                break;
            }
            Level world = ticket.getWorld();
            if (world != null) {
                WorldStructureGenerator.INSTANCE.generateAt(ticket.chunkX, ticket.chunkZ, world);
            }
        }
    }

    private void genTowns() {
        if (hasPendingStructureWork()) {
            return;
        }
        int towns = Math.min(townChunksToGen.size(), MAX_TOWN_GEN_CHUNKS_PER_TICK);
        while (towns-- > 0 && acceptingTickets) {
            ChunkGenerationTicket ticket = townChunksToGen.poll();
            if (ticket == null) {
                break;
            }
            Level world = ticket.getWorld();
            if (world != null) {
                WorldTownGenerator.INSTANCE.attemptGeneration(world, ticket.chunkX * 16, ticket.chunkZ * 16);
            }
        }
    }

    private boolean hasPendingStructureWork() {
        return activeStructureTicket != null || !structuresToGen.isEmpty();
    }

    private void genStructures() {
        int totalBlocks = 0;

        /*
         * Keep one active ticket until it completes. Town generation places
         * barrier callbacks after walls/houses; rotating an unfinished builder
         * to the queue tail would allow those callbacks to run too early.
         */
        while (acceptingTickets && totalBlocks < MAX_BLOCKS_TO_GEN_PER_TICK) {
            if (activeStructureTicket == null) {
                activeStructureTicket = structuresToGen.poll();
            }
            if (activeStructureTicket == null) {
                return;
            }
            if (!activeStructureTicket.isReady()) {
                return;
            }

            int budget = MAX_BLOCKS_TO_GEN_PER_TICK - totalBlocks;
            try {
                int processed = activeStructureTicket.process(Math.max(1, budget));
                totalBlocks += Math.max(1, processed);
                if (activeStructureTicket.isComplete()) {
                    activeStructureTicket = null;
                } else {
                    // The current builder keeps its cursor and resumes next tick.
                    return;
                }
            } catch (Throwable throwable) {
                AncientWarfareStructure.LOG.error("Error processing delayed structure generation", throwable);
                activeStructureTicket.cancel(true);
                activeStructureTicket = null;
            }
        }
    }

    private static class ChunkGenerationTicket {
        private final ResourceKey<Level> world;
        private final int chunkX;
        private final int chunkZ;

        private ChunkGenerationTicket(Level world, int x, int z) {
            this.world = world.dimension();
            this.chunkX = x;
            this.chunkZ = z;
        }

        @Nullable
        public ServerLevel getWorld() {
            var server = ServerLifecycleHooks.getCurrentServer();
            return server == null ? null : server.getLevel(world);
        }
    }

    /**
     * A queued generation step. Callback tickets complete in one call; real
     * structure builders override process/isComplete and are spread over ticks.
     */
    public interface StructureTicket {
        void call();

        int getBlocksToGenerate();

        default boolean isReady() {
            return true;
        }

        default int process(int maxBlocks) {
            call();
            return Math.min(Math.max(1, getBlocksToGenerate()), Math.max(1, maxBlocks));
        }

        default boolean isComplete() {
            return true;
        }

        default void cancel(boolean releaseChunkTickets) {
        }
    }

    private static final class StructureGenerationTicket implements StructureTicket {
        private final StructureBuilder builder;

        private StructureGenerationTicket(StructureBuilder builder) {
            this.builder = builder;
        }

        @Override
        public void call() {
            builder.instantConstruction();
        }

        @Override
        public int process(int maxBlocks) {
            return builder.buildNext(Math.max(1, maxBlocks));
        }

        @Override
        public boolean isReady() {
            return builder.prepareRequiredChunks(2);
        }

        @Override
        public boolean isComplete() {
            return builder.isConstructionComplete();
        }

        @Override
        public int getBlocksToGenerate() {
            StructureBB bb = builder.getBoundingBox();
            long volume = (long) bb.getXSize() * bb.getZSize() * bb.getYSize();
            return (int) Math.min(Integer.MAX_VALUE, Math.max(1L, volume));
        }

        @Override
        public void cancel(boolean releaseChunkTickets) {
            builder.cancelConstruction(releaseChunkTickets);
        }
    }
}
