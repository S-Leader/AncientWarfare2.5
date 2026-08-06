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
        newStructureGenTickets.add(new StructureGenerationTicket(builder));
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
            runChunkChecks();
            genChunks();
            genStructures();
            genTownStructures();
            genTowns();
        }
    }

    public void finalTick() {
        while (!chunkChecks.isEmpty() || !newChunkChecks.isEmpty()) {
            runChunkChecks();
        }
        while (!chunksToGen.isEmpty()) {
            genChunks();
        }

        if (!newStructureGenTickets.isEmpty()) {
            structuresToGen.addAll(newStructureGenTickets);
            newStructureGenTickets.clear();
        }
        int remainingStructures = structuresToGen.size();
        while (remainingStructures-- > 0 && !structuresToGen.isEmpty()) {
            StructureTicket ticket = structuresToGen.remove(0);
            if (ticket.isReady()) {
                ticket.call();
            }
        }

        if (!newTownStructureGenTickets.isEmpty()) {
            townStructuresToGen.addAll(newTownStructureGenTickets);
            newTownStructureGenTickets.clear();
        }
        genTownStructures();

        while (!townChunksToGen.isEmpty()) {
            genTowns();
        }
    }

    private void runChunkChecks() {
        while (!chunkChecks.isEmpty()) {
            ChunkGenerationTicket ticket = chunkChecks.remove(0);
            Level world = ticket.getWorld();
            if (world != null) {
                WorldStructureGenerator.INSTANCE.queueChunkForGeneration(ticket.chunkX, ticket.chunkZ, world);
                WorldTownGenerator.INSTANCE.queueChunkForGeneration(ticket.chunkX, ticket.chunkZ, world);
            }
        }
        if (!newChunkChecks.isEmpty()) {
            chunkChecks.addAll(newChunkChecks);
            newChunkChecks.clear();
        }
    }

    private void genChunks() {
        while (!chunksToGen.isEmpty()) {
            ChunkGenerationTicket ticket = chunksToGen.remove(0);
            Level world = ticket.getWorld();
            if (world != null) {
                WorldStructureGenerator.INSTANCE.generateAt(ticket.chunkX, ticket.chunkZ, world);
            }
        }
        if (!newWorldGenTickets.isEmpty()) {
            chunksToGen.addAll(newWorldGenTickets);
            newWorldGenTickets.clear();
        }
    }

    private void genTowns() {
        int countGenerated = 0;
        while (!townChunksToGen.isEmpty() && countGenerated < 20) {
            ChunkGenerationTicket ticket = townChunksToGen.remove(0);
            Level world = ticket.getWorld();
            if (world != null) {
                WorldTownGenerator.INSTANCE.attemptGeneration(world, ticket.chunkX * 16, ticket.chunkZ * 16);
            }
            countGenerated++;
        }
        if (!newTownGenTickets.isEmpty()) {
            townChunksToGen.addAll(newTownGenTickets);
            newTownGenTickets.clear();
        }
    }

    /**
     * Original queue for standalone structures. It intentionally keeps the old
     * whole-template behaviour so this town fix cannot change ordinary ruins,
     * castles, dungeons or other world-generation buildings.
     */
    private void genStructures() {
        int totalBlocks = 0;
        while (!structuresToGen.isEmpty() && totalBlocks < MAX_BLOCKS_TO_GEN_PER_TICK) {
            StructureTicket structureTicket = structuresToGen.get(0);
            if (!structureTicket.isReady()) {
                break;
            }
            structuresToGen.remove(0);
            totalBlocks += structureTicket.getBlocksToGenerate();
            structureTicket.call();
        }
        if (!newStructureGenTickets.isEmpty()) {
            structuresToGen.addAll(newStructureGenTickets);
            newStructureGenTickets.clear();
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

    /** Ordinary standalone structure ticket; retains the original behaviour. */
    private static final class StructureGenerationTicket implements StructureTicket {
        private final StructureBuilder builder;

        private StructureGenerationTicket(StructureBuilder builder) {
            this.builder = builder;
        }

        @Override
        public void call() {
            try {
                builder.instantConstruction();
            } catch (Exception exception) {
                AncientWarfareStructure.LOG.error(
                        "Error building structure {}: ", builder.getTemplate().name, exception);
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
