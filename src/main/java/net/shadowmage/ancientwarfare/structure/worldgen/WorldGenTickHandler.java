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
    public static final WorldGenTickHandler INSTANCE = new WorldGenTickHandler();
    private final List<ChunkGenerationTicket> newChunkChecks;
    private final List<ChunkGenerationTicket> chunkChecks;
    private final List<ChunkGenerationTicket> newWorldGenTickets;
    private final List<ChunkGenerationTicket> newTownGenTickets;
    private final List<ChunkGenerationTicket> chunksToGen;
    private final List<ChunkGenerationTicket> townChunksToGen;
    private final List<StructureTicket> newStructureGenTickets;
    private final List<StructureTicket> structuresToGen;

    private WorldGenTickHandler() {
        newChunkChecks = new ArrayList<>();
        chunkChecks = new ArrayList<>();
        newWorldGenTickets = new ArrayList<>();
        newTownGenTickets = new ArrayList<>();
        newStructureGenTickets = new ArrayList<>();
        chunksToGen = new ArrayList<>();
        townChunksToGen = new ArrayList<>();
        structuresToGen = new ArrayList<>();
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

    public void addStructureForGeneration(StructureBuilder builder) {
        newStructureGenTickets.add(new StructureGenerationTicket(builder));
    }

    public void addStructureGenCallback(StructureTicket tk) {
        newStructureGenTickets.add(tk);
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent evt) {
        if (evt.phase == TickEvent.Phase.END) {
            runChunkChecks();
            genChunks();
            genStructures();
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
        while (!townChunksToGen.isEmpty()) {
            genTowns();
        }
    }

    private void runChunkChecks() {
        while (!chunkChecks.isEmpty()) {
            ChunkGenerationTicket tk = chunkChecks.remove(0);
            Level world = tk.getWorld();
            if (world != null) {
                WorldStructureGenerator.INSTANCE.queueChunkForGeneration(tk.chunkX, tk.chunkZ, world);
                WorldTownGenerator.INSTANCE.queueChunkForGeneration(tk.chunkX, tk.chunkZ, world);
            }
        }
        if (!newChunkChecks.isEmpty()) {
            chunkChecks.addAll(newChunkChecks);
            newChunkChecks.clear();
        }
    }

    private void genChunks() {
        while (!chunksToGen.isEmpty()) {
            ChunkGenerationTicket tk = chunksToGen.remove(0);
            Level world = tk.getWorld();
            if (world != null) {
                WorldStructureGenerator.INSTANCE.generateAt(tk.chunkX, tk.chunkZ, world);
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
            ChunkGenerationTicket tk = townChunksToGen.remove(0);
            Level world = tk.getWorld();
            if (world != null) {
                WorldTownGenerator.INSTANCE.attemptGeneration(world, tk.chunkX * 16, tk.chunkZ * 16);
            }
            countGenerated++;
        }
        if (!newTownGenTickets.isEmpty()) {
            townChunksToGen.addAll(newTownGenTickets);
            newTownGenTickets.clear();
        }
    }

    private void genStructures() {
        int totalBlocks = 0;
        /*
         * Only inspect the tickets that existed at the beginning of this pass.
         * Unready cross-chunk structures are appended for a later tick instead of
         * being removed and immediately retried in a tight loop.
         */
        int ticketsToInspect = structuresToGen.size();
        while (ticketsToInspect-- > 0 && !structuresToGen.isEmpty()
                && totalBlocks < MAX_BLOCKS_TO_GEN_PER_TICK) {
            StructureTicket structureTicket = structuresToGen.remove(0);
            if (!structureTicket.isReady()) {
                structuresToGen.add(structureTicket);
                continue;
            }
            totalBlocks += structureTicket.getBlocksToGenerate();
            structureTicket.call();
        }
        if (!newStructureGenTickets.isEmpty()) {
            structuresToGen.addAll(newStructureGenTickets);
            newStructureGenTickets.clear();
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
            return ServerLifecycleHooks.getCurrentServer() == null ? null : ServerLifecycleHooks.getCurrentServer().getLevel(world);
        }
    }

    /*
     * Base structure ticket class.  Changed to a callback mechanism to allow anonymous callback classes,
     * to inform town-gen of when first / second pass structures are finished being generated; to allow
     * the road to generate after walls, etc
     *
     * @author Shadowmage
     */
    public interface StructureTicket {
        void call();

        int getBlocksToGenerate();

        default boolean isReady() {
            return true;
        }
    }

    private static final class StructureGenerationTicket implements StructureTicket {
        private final StructureBuilder builder;

        private StructureGenerationTicket(StructureBuilder builder) {
            this.builder = builder;
        }

        @Override
        public void call() {
            try {
                builder.instantConstruction();
            } catch (Exception ex) {
                AncientWarfareStructure.LOG.error("Error building structure {}: ", builder.getTemplate().name, ex);
            }
        }

        @Override
        public boolean isReady() {
            return builder.areRequiredChunksLoaded();
        }

        @Override
        public int getBlocksToGenerate() {
            StructureBB bb = builder.getBoundingBox();
            return bb.getXSize() * bb.getZSize() * bb.getYSize();
        }
    }
}
