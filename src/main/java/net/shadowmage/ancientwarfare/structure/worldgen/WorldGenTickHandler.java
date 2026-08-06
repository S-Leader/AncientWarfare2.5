package net.shadowmage.ancientwarfare.structure.worldgen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBuilder;
import net.shadowmage.ancientwarfare.structure.town.WorldTownGenerator;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class WorldGenTickHandler {
    private static final int MAX_BLOCKS_TO_GEN_PER_TICK = 10000;
    private static final int MAX_TOWN_TEMPLATE_POSITIONS_PER_TICK = 10000;
    private static final int MAX_TOWN_TEMPLATE_POSITIONS_PER_STEP = 1000;
    private static final int MAX_STANDALONE_STRUCTURE_WAIT_TICKS = 1200;

    public static final WorldGenTickHandler INSTANCE = new WorldGenTickHandler();

    private final List<ChunkGenerationTicket> newChunkChecks;
    private final List<ChunkGenerationTicket> chunkChecks;
    private final List<ChunkGenerationTicket> newWorldGenTickets;
    private final List<ChunkGenerationTicket> newTownGenTickets;
    private final List<ChunkGenerationTicket> chunksToGen;
    private final List<ChunkGenerationTicket> townChunksToGen;

    /*
     * Normal standalone structures retain their original queue and their original
     * all-at-once StructureBuilder#instantConstruction behaviour.
     */
    private final List<StructureTicket> newStructureGenTickets;
    private final List<StructureTicket> structuresToGen;

    /*
     * Town pieces use a separate, ordered queue. This prevents a town piece which
     * is waiting for chunks, or a large town template being built over several
     * ticks, from blocking unrelated standalone world-generation structures.
     */
    private final List<TownQueueTicket> newTownStructureGenTickets;
    private final List<TownQueueTicket> townStructuresToGen;

    private WorldGenTickHandler() {
        newChunkChecks = new ArrayList<>();
        chunkChecks = new ArrayList<>();
        newWorldGenTickets = new ArrayList<>();
        newTownGenTickets = new ArrayList<>();
        newStructureGenTickets = new ArrayList<>();
        newTownStructureGenTickets = new ArrayList<>();
        chunksToGen = new ArrayList<>();
        townChunksToGen = new ArrayList<>();
        structuresToGen = new ArrayList<>();
        townStructuresToGen = new ArrayList<>();
    }

    /** Clears every transient queue between integrated/dedicated server sessions. */
    public void reset() {
        newChunkChecks.clear();
        chunkChecks.clear();
        newWorldGenTickets.clear();
        newTownGenTickets.clear();
        chunksToGen.clear();
        townChunksToGen.clear();
        newStructureGenTickets.clear();
        structuresToGen.clear();
        newTownStructureGenTickets.clear();
        townStructuresToGen.clear();
    }

    /**
     * Entry point from ChunkEvent.Load. Territory lookup loads neighboring chunks,
     * which deadlocks the 1.20 chunk pipeline if done inside the load event itself —
     * so only the coordinates are queued here and the check runs on the next tick.
     */
    public void addChunkForDeferredCheck(Level world, int chunkX, int chunkZ) {
        newChunkChecks.add(new ChunkGenerationTicket(world, chunkX, chunkZ));
    }

    void addChunkForGeneration(Level world, int chunkX, int chunkZ) {
        newWorldGenTickets.add(new ChunkGenerationTicket(world, chunkX, chunkZ));
    }

    public void addChunkForTownGeneration(Level world, int chunkX, int chunkZ) {
        newTownGenTickets.add(new ChunkGenerationTicket(world, chunkX, chunkZ));
    }

    /**
     * Queues an ordinary, non-town structure. Do not use this for town walls,
     * houses, cosmetics or lamps.
     */
    public void addStructureForGeneration(StructureBuilder builder) {
        addStructureForGeneration(builder, null, null);
    }

    /**
     * Queues an ordinary structure with completion hooks. Natural worldgen uses
     * these hooks to commit statistics on success and roll back its provisional
     * structure-map/territory reservation if construction fails or times out.
     */
    public void addStructureForGeneration(StructureBuilder builder,
                                          @Nullable Runnable onSuccess,
                                          @Nullable Runnable onFailure) {
        newStructureGenTickets.add(new StructureGenerationTicket(builder, onSuccess, onFailure));
    }

    /**
     * Adds a callback to the ordinary structure queue.
     */
    public void addStructureGenCallback(StructureTicket ticket) {
        newStructureGenTickets.add(ticket);
    }

    /**
     * Queues a town-owned structure piece. Town pieces are built incrementally
     * and stay ordered relative to town phase callbacks.
     */
    public void addTownStructureForGeneration(StructureBuilder builder) {
        newTownStructureGenTickets.add(new TownStructureGenerationTicket(builder));
    }

    /**
     * Adds a phase barrier to the town queue. For example, roads are generated
     * only after every wall ticket before this callback has completed.
     */
    public void addTownStructureGenCallback(StructureTicket ticket) {
        newTownStructureGenTickets.add(new TownCallbackTicket(ticket));
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent evt) {
        if (evt.phase == TickEvent.Phase.END) {
            runSafely("deferred chunk checks", this::runChunkChecks);
            runSafely("standalone structure selection", this::genChunks);
            runSafely("standalone structure construction", this::genStructures);
            runSafely("town structure construction", this::genTownStructures);
            runSafely("town selection", this::genTowns);
        }
    }


    private void runSafely(String phase, Runnable task) {
        try {
            task.run();
        } catch (Exception exception) {
            AncientWarfareStructure.LOG.error(
                    "Ancient Warfare worldgen phase '{}' failed; later phases and future ticks will continue",
                    phase, exception);
        }
    }

    public void finalTick() {
        while (!chunkChecks.isEmpty() || !newChunkChecks.isEmpty()) {
            runChunkChecks();
        }
        while (!chunksToGen.isEmpty() || !newWorldGenTickets.isEmpty()) {
            genChunks();
        }

        if (!newStructureGenTickets.isEmpty()) {
            structuresToGen.addAll(newStructureGenTickets);
            newStructureGenTickets.clear();
        }
        int remainingStructures = structuresToGen.size();
        while (remainingStructures-- > 0 && !structuresToGen.isEmpty()) {
            StructureTicket ticket = structuresToGen.remove(0);
            try {
                if (ticket.isReady()) {
                    ticket.call();
                } else if (ticket instanceof StructureGenerationTicket generationTicket) {
                    generationTicket.fail("server stopped before required chunks became available", null);
                }
            } catch (Exception exception) {
                if (ticket instanceof StructureGenerationTicket generationTicket) {
                    generationTicket.fail("exception during final worldgen flush", exception);
                } else {
                    AncientWarfareStructure.LOG.error(
                            "Discarding failed structure callback during final worldgen flush", exception);
                }
            }
        }

        if (!newTownStructureGenTickets.isEmpty()) {
            townStructuresToGen.addAll(newTownStructureGenTickets);
            newTownStructureGenTickets.clear();
        }
        genTownStructures();

        while (!townChunksToGen.isEmpty() || !newTownGenTickets.isEmpty()) {
            genTowns();
        }
    }

    private void runChunkChecks() {
        if (!newChunkChecks.isEmpty()) {
            chunkChecks.addAll(newChunkChecks);
            newChunkChecks.clear();
        }

        while (!chunkChecks.isEmpty()) {
            ChunkGenerationTicket ticket = chunkChecks.remove(0);
            try {
                Level world = ticket.getWorld();
                if (world != null) {
                    WorldStructureGenerator.INSTANCE.queueChunkForGeneration(ticket.chunkX, ticket.chunkZ, world);
                    WorldTownGenerator.INSTANCE.queueChunkForGeneration(ticket.chunkX, ticket.chunkZ, world);
                }
            } catch (Exception exception) {
                AncientWarfareStructure.LOG.error(
                        "Skipping failed deferred worldgen check for chunk [{}, {}]",
                        ticket.chunkX, ticket.chunkZ, exception);
            }
        }
    }

    private void genChunks() {
        if (!newWorldGenTickets.isEmpty()) {
            chunksToGen.addAll(newWorldGenTickets);
            newWorldGenTickets.clear();
        }

        while (!chunksToGen.isEmpty()) {
            ChunkGenerationTicket ticket = chunksToGen.remove(0);
            try {
                Level world = ticket.getWorld();
                if (world != null) {
                    WorldStructureGenerator.INSTANCE.generateAt(ticket.chunkX, ticket.chunkZ, world);
                }
            } catch (Exception exception) {
                AncientWarfareStructure.LOG.error(
                        "Skipping failed standalone structure-generation attempt for chunk [{}, {}]",
                        ticket.chunkX, ticket.chunkZ, exception);
            }
        }
    }

    private void genTowns() {
        if (!newTownGenTickets.isEmpty()) {
            townChunksToGen.addAll(newTownGenTickets);
            newTownGenTickets.clear();
        }

        int countGenerated = 0;
        while (!townChunksToGen.isEmpty() && countGenerated < 20) {
            ChunkGenerationTicket ticket = townChunksToGen.remove(0);
            try {
                Level world = ticket.getWorld();
                if (world != null) {
                    WorldTownGenerator.INSTANCE.attemptGeneration(world, ticket.chunkX * 16, ticket.chunkZ * 16);
                }
            } catch (Exception exception) {
                AncientWarfareStructure.LOG.error(
                        "Skipping failed town-generation attempt for chunk [{}, {}]",
                        ticket.chunkX, ticket.chunkZ, exception);
            }
            countGenerated++;
        }
    }

    /**
     * Original queue for standalone structures. It intentionally keeps the old
     * whole-template behaviour so this town fix cannot change ordinary ruins,
     * castles, dungeons or other world-generation buildings.
     */
    private void genStructures() {
        if (!newStructureGenTickets.isEmpty()) {
            structuresToGen.addAll(newStructureGenTickets);
            newStructureGenTickets.clear();
        }

        int totalBlocks = 0;
        int ticketsToInspect = structuresToGen.size();

        /*
         * Never leave an unready or broken ticket pinned at index zero. Each
         * ticket present at the beginning of this tick is inspected at most once.
         * Unready tickets rotate to the tail, so one missing chunk cannot stop all
         * later Ancient Warfare ruins and small buildings from spawning.
         */
        while (ticketsToInspect-- > 0
                && !structuresToGen.isEmpty()
                && totalBlocks < MAX_BLOCKS_TO_GEN_PER_TICK) {
            StructureTicket structureTicket = structuresToGen.remove(0);

            try {
                if (!structureTicket.isReady()) {
                    if (structureTicket instanceof StructureGenerationTicket generationTicket
                            && generationTicket.recordWaitTick()) {
                        generationTicket.fail("required chunks did not become available within "
                                + MAX_STANDALONE_STRUCTURE_WAIT_TICKS + " ticks", null);
                    } else {
                        structuresToGen.add(structureTicket);
                    }
                    continue;
                }

                totalBlocks += Math.max(1, structureTicket.getBlocksToGenerate());
                structureTicket.call();
            } catch (Exception exception) {
                if (structureTicket instanceof StructureGenerationTicket generationTicket) {
                    generationTicket.fail("exception while preparing or building the structure", exception);
                } else {
                    AncientWarfareStructure.LOG.error(
                            "Discarding failed standalone structure callback ticket", exception);
                }
            }
        }
    }

    /**
     * Ordered, incremental town-only queue. A town phase callback is removed only
     * after all town builders before it are finalized. Nothing in this method can
     * block or reorder the ordinary structure queue above.
     */
    private void genTownStructures() {
        int processedPositions = 0;
        while (!townStructuresToGen.isEmpty()
                && processedPositions < MAX_TOWN_TEMPLATE_POSITIONS_PER_TICK) {
            TownQueueTicket ticket = townStructuresToGen.get(0);
            if (!ticket.isReady()) {
                // Loading is bounded by the ticket. Keep town order and resume on
                // the next server tick instead of spinning on an unloaded chunk.
                break;
            }

            try {
                ticket.call();
            } catch (Exception exception) {
                ticket.markFailed();
                AncientWarfareStructure.LOG.error("Aborting failed town-generation ticket", exception);
            }

            processedPositions += Math.max(1, ticket.getProcessedPositions());
            if (ticket.isComplete()) {
                townStructuresToGen.remove(0);
            }
        }

        if (!newTownStructureGenTickets.isEmpty()) {
            townStructuresToGen.addAll(newTownStructureGenTickets);
            newTownStructureGenTickets.clear();
        }
    }

    private static class ChunkGenerationTicket {
        private final ResourceKey<Level> world;
        private final int chunkX;
        private final int chunkZ;

        private ChunkGenerationTicket(Level world, int x, int z) {
            this.world = world.dimension();
            chunkX = x;
            chunkZ = z;
        }

        @Nullable
        public ServerLevel getWorld() {
            return ServerLifecycleHooks.getCurrentServer() == null
                    ? null
                    : ServerLifecycleHooks.getCurrentServer().getLevel(world);
        }
    }

    /*
     * Base structure ticket class. Changed to a callback mechanism to allow
     * anonymous callback classes to inform town generation when the previous
     * pass is complete.
     */
    public interface StructureTicket {
        void call();

        int getBlocksToGenerate();

        default boolean isReady() {
            return true;
        }
    }

    /** Ordinary standalone structure ticket; retains all-at-once construction. */
    private static final class StructureGenerationTicket implements StructureTicket {
        private final StructureBuilder builder;
        @Nullable
        private final Runnable onSuccess;
        @Nullable
        private final Runnable onFailure;
        private int waitTicks;
        private boolean completed;
        private boolean failed;

        private StructureGenerationTicket(StructureBuilder builder,
                                          @Nullable Runnable onSuccess,
                                          @Nullable Runnable onFailure) {
            this.builder = builder;
            this.onSuccess = onSuccess;
            this.onFailure = onFailure;
        }

        @Override
        public void call() {
            if (completed || failed) {
                return;
            }

            builder.instantConstruction();
            completed = true;
            if (onSuccess != null) {
                try {
                    onSuccess.run();
                } catch (Exception callbackException) {
                    AncientWarfareStructure.LOG.error(
                            "Structure was built, but its completion callback failed", callbackException);
                }
            }
        }

        @Override
        public boolean isReady() {
            return builder.ensureRequiredChunksLoaded(4);
        }

        @Override
        public int getBlocksToGenerate() {
            StructureBB bb = builder.getBoundingBox();
            return bb.getXSize() * bb.getZSize() * bb.getYSize();
        }

        private boolean recordWaitTick() {
            waitTicks++;
            return waitTicks >= MAX_STANDALONE_STRUCTURE_WAIT_TICKS;
        }

        private void fail(String reason, @Nullable Exception exception) {
            if (completed || failed) {
                return;
            }
            failed = true;

            String templateName;
            try {
                templateName = builder.getTemplate() == null
                        ? "<unknown>"
                        : builder.getTemplate().name;
            } catch (Exception ignored) {
                templateName = "<unreadable>";
            }

            if (exception == null) {
                AncientWarfareStructure.LOG.error(
                        "Discarding failed standalone structure {}: {}", templateName, reason);
            } else {
                AncientWarfareStructure.LOG.error(
                        "Discarding failed standalone structure {}: {}", templateName, reason, exception);
            }

            if (onFailure != null) {
                try {
                    onFailure.run();
                } catch (Exception rollbackException) {
                    AncientWarfareStructure.LOG.error(
                            "Error rolling back failed standalone structure {}", templateName, rollbackException);
                }
            }
        }
    }

    private interface TownQueueTicket {
        void call();

        boolean isReady();

        boolean isComplete();

        int getProcessedPositions();

        void markFailed();
    }

    private static final class TownStructureGenerationTicket implements TownQueueTicket {
        private final StructureBuilder builder;
        private boolean failed;
        private int processedPositions = 1;

        private TownStructureGenerationTicket(StructureBuilder builder) {
            this.builder = builder;
        }

        @Override
        public void call() {
            if (!failed && !builder.isFinalized()) {
                processedPositions = Math.max(1,
                        builder.buildSome(MAX_TOWN_TEMPLATE_POSITIONS_PER_STEP));
            }
        }

        @Override
        public boolean isReady() {
            return failed || builder.ensureRequiredChunksLoaded(4);
        }

        @Override
        public boolean isComplete() {
            return failed || builder.isFinalized();
        }

        @Override
        public int getProcessedPositions() {
            return processedPositions;
        }

        @Override
        public void markFailed() {
            failed = true;
            AncientWarfareStructure.LOG.error(
                    "Aborting failed town structure {}", builder.getTemplate().name);
        }
    }

    private static final class TownCallbackTicket implements TownQueueTicket {
        private final StructureTicket delegate;
        private boolean complete;
        private boolean failed;

        private TownCallbackTicket(StructureTicket delegate) {
            this.delegate = delegate;
        }

        @Override
        public void call() {
            if (!complete && !failed) {
                delegate.call();
                complete = true;
            }
        }

        @Override
        public boolean isReady() {
            return failed || delegate.isReady();
        }

        @Override
        public boolean isComplete() {
            return complete || failed;
        }

        @Override
        public int getProcessedPositions() {
            return Math.max(1, delegate.getBlocksToGenerate());
        }

        @Override
        public void markFailed() {
            failed = true;
        }
    }
}
