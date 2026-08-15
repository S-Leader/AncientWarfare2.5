package net.shadowmage.ancientwarfare.structure.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.gamedata.StructureEntry;
import net.shadowmage.ancientwarfare.structure.gamedata.StructureMap;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplateManager;
import net.shadowmage.ancientwarfare.structure.template.build.PhasedIslandStructureBuilder;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;
import net.shadowmage.ancientwarfare.structure.worldgen.stats.WorldGenStatistics;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Phased large-island generator based on the handoff repository's
 * LargeIslandWorldGenerator. Only ISLAND templates use this path.
 */
public final class PersistentIslandGenerationManager {
    // 64 seabed columns is normally only a few hundred block writes per tick.
    private static final int UNDERFILL_COLUMNS_PER_TICK = 64;
    // Biomes are quart-resolution; one 4x4 sample covers a horizontal biome cell.
    private static final int BIOME_QUART_COLUMNS_PER_TICK = 64;

    public static final PersistentIslandGenerationManager INSTANCE = new PersistentIslandGenerationManager();

    private final Map<ServerLevel, CachedBuild> builderCache = new WeakHashMap<>();

    private PersistentIslandGenerationManager() {
    }

    public void resetTransientState() {
        builderCache.clear();
    }

    public boolean queue(ServerLevel level, BlockPos pos, Direction face, StructureTemplate template,
                         StructureMap map, Territory territory, long generationStart) {
        IslandGenerationData data = AWGameData.INSTANCE.getPerWorldData(level, IslandGenerationData.class);
        if (data.active() != null) {
            return false;
        }

        StructureEntry entry = new StructureEntry(pos.getX(), pos.getY(), pos.getZ(), face, template);
        boolean structureReserved = false;
        boolean clusterReserved = false;
        try {
            map.setGeneratedAt(level, pos.getX(), pos.getZ(), entry, template.getValidationSettings().isUnique());
            structureReserved = true;
            territory.addClusterValue(template.getValidationSettings().getClusterValue());
            clusterReserved = true;

            IslandGenerationData.Task task = new IslandGenerationData.Task(
                    template.name, pos.getX(), pos.getY(), pos.getZ(), face.get2DDataValue(),
                    template.getValidationSettings().getClusterValue(), template.getValidationSettings().isUnique());
            task.structureReserved = true;
            task.clusterReserved = true;
            data.setActive(task);
            AncientWarfareStructure.LOG.info(
                    "Queued phased island {} at {}, {}, {} (selection took {} ms)",
                    template.name, pos.getX(), pos.getY(), pos.getZ(), System.currentTimeMillis() - generationStart);
            return true;
        } catch (Exception exception) {
            AncientWarfareStructure.LOG.error("Unable to reserve phased island {} at {}", template.name, pos, exception);
            if (clusterReserved) {
                territory.removeClusterValue(template.getValidationSettings().getClusterValue());
            }
            if (structureReserved) {
                map.removeGeneratedAt(level, pos.getX(), pos.getZ(), entry, template.getValidationSettings().isUnique());
            }
            return false;
        }
    }

    public void tickAllLevels() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        for (ServerLevel level : server.getAllLevels()) {
            tick(level);
        }
    }

    private void tick(ServerLevel level) {
        IslandGenerationData data = AWGameData.INSTANCE.getPerWorldData(level, IslandGenerationData.class);
        IslandGenerationData.Task task = data.active();
        if (task == null) {
            builderCache.remove(level);
            return;
        }
        if (task.phase == IslandGenerationData.Phase.FAILED) {
            rollback(level, data, task);
            return;
        }

        StructureTemplate template = StructureTemplateManager.getTemplate(task.templateName).orElse(null);
        if (template == null) {
            fail(data, task, "template disappeared while island was being built", null);
            return;
        }

        Direction face = Direction.from2DDataValue(task.facing);
        BlockPos origin = new BlockPos(task.x, task.y, task.z);
        StructureBB bb = new StructureBB(origin, face, template);

        try {
            switch (task.phase) {
                case UNDERFILL -> underfill(level, data, task, template, bb);
                case BUILD -> build(level, data, task, template, origin, face, bb);
                case BIOME -> replaceBiome(level, data, task, template, bb);
                case FINALIZE -> finish(level, data, task, template, origin, face);
                default -> { }
            }
        } catch (Exception exception) {
            fail(data, task, "exception during phased island generation", exception);
        }
    }

    private void underfill(ServerLevel level, IslandGenerationData data, IslandGenerationData.Task task,
                           StructureTemplate template, StructureBB bb) {
        int width = bb.getXSize();
        int depth = bb.getZSize();
        int total = width * depth;
        int end = Math.min(total, task.progress + UNDERFILL_COLUMNS_PER_TICK);

        for (int index = task.progress; index < end; index++) {
            int x = bb.min.getX() + index % width;
            int z = bb.min.getZ() + index / width;
            if (!ensureBlockLoaded(level, x, z)) {
                return;
            }
            underfillColumn(level, x, z, bb.min.getY());
            task.progress = index + 1;
        }

        if (task.progress >= total) {
            task.phase = IslandGenerationData.Phase.BUILD;
            task.progress = 0;
            builderCache.remove(level);
        }
        data.markDirty();
    }

    private void underfillColumn(ServerLevel level, int x, int z, int islandMinY) {
        int topFilledY = WorldStructureGenerator.getTargetY(level, x, z, true);
        if (topFilledY >= islandMinY) {
            return;
        }
        BlockState fill = level.getBlockState(new BlockPos(x, topFilledY, z));
        if (fill.isAir() || !fill.getFluidState().isEmpty()) {
            fill = Blocks.DIRT.defaultBlockState();
        }
        // Block flag 3 on every underfill block was one of the main lag sources:
        // every water replacement recursively notified neighbours/fluids. These
        // blocks are internal terrain support, so client sync without neighbour
        // propagation is sufficient while the island is assembled.
        for (int y = topFilledY; y < islandMinY; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.getBlockState(pos).equals(fill)) {
                level.setBlock(pos, fill, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }

    private void build(ServerLevel level, IslandGenerationData data, IslandGenerationData.Task task,
                       StructureTemplate template, BlockPos origin, Direction face, StructureBB bb) {
        int minChunkX = SectionPos.blockToSectionCoord(bb.min.getX());
        int maxChunkX = SectionPos.blockToSectionCoord(bb.max.getX());
        int minChunkZ = SectionPos.blockToSectionCoord(bb.min.getZ());
        int maxChunkZ = SectionPos.blockToSectionCoord(bb.max.getZ());
        int chunkWidth = maxChunkX - minChunkX + 1;
        int chunkDepth = maxChunkZ - minChunkZ + 1;
        int chunkCount = chunkWidth * chunkDepth;
        int passes = 4; // legacy StructureBuilder priorities 0..3
        int totalSteps = chunkCount * passes;

        CachedBuild cached = builderCache.get(level);
        if (cached == null || !cached.matches(task)) {
            // A JVM restart loses StructureBuilder's temporary rail/redstone/BE
            // finalization maps. Re-run BUILD from step zero; already placed blocks
            // are simply overwritten in small slices, avoiding corrupted finalization.
            if (task.progress != 0) {
                task.progress = 0;
                data.markDirty();
            }
            cached = new CachedBuild(task.templateName, task.x, task.y, task.z,
                    new PhasedIslandStructureBuilder(level, template, face, origin));
            builderCache.put(level, cached);
        }

        if (task.progress >= totalSteps) {
            task.phase = IslandGenerationData.Phase.BIOME;
            task.progress = 0;
            data.markDirty();
            return;
        }

        int pass = task.progress / chunkCount;
        int chunkIndex = task.progress % chunkCount;
        ChunkPos chunk = new ChunkPos(minChunkX + chunkIndex % chunkWidth, minChunkZ + chunkIndex / chunkWidth);
        if (!ensureChunkLoaded(level, chunk.x, chunk.z)) {
            return;
        }

        cached.builder.buildChunkPass(chunk, pass);
        task.progress++;
        data.markDirty();
    }

    private void replaceBiome(ServerLevel level, IslandGenerationData data, IslandGenerationData.Task task,
                              StructureTemplate template, StructureBB bb) {
        ResourceLocation replacementId = template.getValidationSettings()
                .getPropertyValue(net.shadowmage.ancientwarfare.structure.template.build.validation.properties.StructureValidationProperties.BIOME_REPLACEMENT);
        var replacement = ForgeRegistries.BIOMES.getValue(replacementId);
        if (replacement == null) {
            task.phase = IslandGenerationData.Phase.FINALIZE;
            task.progress = 0;
            data.markDirty();
            return;
        }

        int width = (bb.getXSize() + 3) / 4;
        int depth = (bb.getZSize() + 3) / 4;
        int total = width * depth;
        int end = Math.min(total, task.progress + BIOME_QUART_COLUMNS_PER_TICK);
        for (int index = task.progress; index < end; index++) {
            int x = bb.min.getX() + (index % width) * 4;
            int z = bb.min.getZ() + (index / width) * 4;
            if (!ensureBlockLoaded(level, x, z)) {
                return;
            }
            int y = Math.max(level.getMinBuildHeight() + 1,
                    level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1);
            BlockState state = level.getBlockState(new BlockPos(x, y, z));
            if (!state.isAir() && LegacyMaterial.of(state) != LegacyMaterial.WATER) {
                WorldTools.changeBiome(level, new BlockPos(x, y, z), replacement);
            }
            task.progress = index + 1;
        }
        if (task.progress >= total) {
            task.phase = IslandGenerationData.Phase.FINALIZE;
            task.progress = 0;
        }
        data.markDirty();
    }

    private void finish(ServerLevel level, IslandGenerationData data, IslandGenerationData.Task task,
                        StructureTemplate template, BlockPos origin, Direction face) {
        CachedBuild cached = builderCache.get(level);
        if (cached == null || !cached.matches(task)) {
            // If the server was stopped exactly between BUILD and FINALIZE, rebuild
            // the chunk passes from zero so finalization maps are reconstructed.
            task.phase = IslandGenerationData.Phase.BUILD;
            task.progress = 0;
            data.markDirty();
            return;
        }
        cached.builder.finishIslandBlocks();
        template.getValidationSettings().postGeneration(level, origin, cached.builder.getBoundingBox(), template);

        task.phase = IslandGenerationData.Phase.COMPLETE;
        data.setActive(null);
        builderCache.remove(level);
        WorldGenStatistics.addStructureGeneratedInfo(template.name, level, origin);
        AncientWarfareStructure.LOG.info("Completed phased island {} at {}, {}, {}",
                template.name, origin.getX(), origin.getY(), origin.getZ());
    }

    private boolean ensureBlockLoaded(ServerLevel level, int blockX, int blockZ) {
        return ensureChunkLoaded(level,
                SectionPos.blockToSectionCoord(blockX),
                SectionPos.blockToSectionCoord(blockZ));
    }

    private boolean ensureChunkLoaded(ServerLevel level, int chunkX, int chunkZ) {
        if (!level.hasChunk(chunkX, chunkZ)) {
            // Exactly one current slice is synchronously requested. The old path
            // loaded every chunk under a 300+ block island in the same tick.
            level.getChunk(chunkX, chunkZ);
        }
        return level.hasChunk(chunkX, chunkZ);
    }

    private void fail(IslandGenerationData data, IslandGenerationData.Task task, String reason, Exception exception) {
        task.phase = IslandGenerationData.Phase.FAILED;
        task.progress = 0;
        data.markDirty();
        if (exception == null) {
            AncientWarfareStructure.LOG.error("Phased island {} failed: {}", task.templateName, reason);
        } else {
            AncientWarfareStructure.LOG.error("Phased island {} failed: {}", task.templateName, reason, exception);
        }
    }

    private void rollback(ServerLevel level, IslandGenerationData data, IslandGenerationData.Task task) {
        StructureTemplate template = StructureTemplateManager.getTemplate(task.templateName).orElse(null);
        if (template == null) {
            data.setActive(null);
            builderCache.remove(level);
            return;
        }
        Direction face = Direction.from2DDataValue(task.facing);
        BlockPos origin = new BlockPos(task.x, task.y, task.z);
        StructureBB bb = new StructureBB(origin, face, template);
        try {
            if (task.clusterReserved) {
                TerritoryManager.getTerritory(task.x >> 4, task.z >> 4, level)
                        .ifPresent(t -> t.removeClusterValue(task.clusterValue));
                task.clusterReserved = false;
            }
            if (task.structureReserved) {
                AWGameData.INSTANCE.getPerWorldData(level, StructureMap.class).removeGeneratedAt(
                        level, task.x, task.z, task.templateName, bb, task.unique);
                task.structureReserved = false;
            }
            data.setActive(null);
            builderCache.remove(level);
        } catch (Exception exception) {
            AncientWarfareStructure.LOG.error("Unable to roll back failed phased island {}; will retry",
                    task.templateName, exception);
            data.markDirty();
        }
    }

    private record CachedBuild(String templateName, int x, int y, int z,
                               PhasedIslandStructureBuilder builder) {
        boolean matches(IslandGenerationData.Task task) {
            return templateName.equals(task.templateName) && x == task.x && y == task.y && z == task.z;
        }
    }
}
