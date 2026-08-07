package net.shadowmage.ancientwarfare.structure.town;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.gamedata.StructureEntry;
import net.shadowmage.ancientwarfare.structure.gamedata.StructureMap;
import net.shadowmage.ancientwarfare.structure.gamedata.TownEntry;
import net.shadowmage.ancientwarfare.structure.gamedata.TownMap;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBuilder;
import net.shadowmage.ancientwarfare.structure.worldgen.Territory;
import net.shadowmage.ancientwarfare.structure.worldgen.TerritoryManager;
import net.shadowmage.ancientwarfare.structure.worldgen.WorldStructureGenerator;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * Persistent phased town builder inspired by the modern 1.20.1 handoff port,
 * while retaining AW2's original TownGenerator layout decisions.
 */
public final class PersistentTownGenerationManager {
    private static final int PREPARE_COLUMNS_PER_TICK = 96;
    private static final int ROAD_BLOCKS_PER_TICK = 192;

    public static final PersistentTownGenerationManager INSTANCE = new PersistentTownGenerationManager();

    private final Map<ServerLevel, CachedPlan> planCache = new WeakHashMap<>();

    private PersistentTownGenerationManager() {
    }

    /** Clears only in-memory deterministic plan caches; persisted tasks stay in SavedData. */
    public void resetTransientState() {
        planCache.clear();
    }

    /**
     * Reserve the town immediately, then persist the actual construction task.
     * Reservations prevent normal AWS and later towns from overlapping a town
     * that is only partially built. Failed tasks roll these reservations back.
     */
    public boolean queue(ServerLevel level, TownBoundingArea area, TownTemplate template, Territory territory) {
        TownGenerationData data = AWGameData.INSTANCE.getPerWorldData(level, TownGenerationData.class);
        if (data.contains(area.getCenterX(), area.getCenterZ())) {
            return false;
        }

        StructureBB bb = reservationBounds(area);
        StructureMap structureMap = AWGameData.INSTANCE.getPerWorldData(level, StructureMap.class);
        TownMap townMap = AWGameData.INSTANCE.getPerWorldData(level, TownMap.class);
        StructureEntry structureEntry = new StructureEntry(bb, template.getTownTypeName(), template.getClusterValue(),
                area.getCenterX() >> 4, area.getCenterZ() >> 4);

        boolean structureReserved = false;
        boolean townReserved = false;
        boolean clusterReserved = false;
        try {
            structureMap.setGeneratedAt(level, area.getCenterX(), area.getCenterZ(), structureEntry, false);
            structureReserved = true;
            townMap.setGenerated(new TownEntry(bb, template.shouldPreventNaturalHostileSpawns()));
            townReserved = true;
            territory.addClusterValue(template.getClusterValue());
            clusterReserved = true;

            TownGenerationData.Task task = new TownGenerationData.Task(template.getTownTypeName(),
                    area.getChunkMinX(), area.getChunkMinZ(), area.getChunkMaxX(), area.getChunkMaxZ(),
                    area.getMinY(), area.getMaxY(), template.getClusterValue());
            task.clusterReserved = true;
            data.add(task);
            AncientWarfareStructure.LOG.info("Queued persistent town {} at {}, {}",
                    template.getTownTypeName(), area.getCenterX(), area.getCenterZ());
            return true;
        } catch (Exception exception) {
            AncientWarfareStructure.LOG.error("Unable to reserve persistent town {} at {}, {}",
                    template.getTownTypeName(), area.getCenterX(), area.getCenterZ(), exception);
            if (clusterReserved) {
                territory.removeClusterValue(template.getClusterValue());
            }
            if (townReserved) {
                townMap.removeGenerated(bb);
            }
            if (structureReserved) {
                structureMap.removeGeneratedAt(level, area.getCenterX(), area.getCenterZ(),
                        template.getTownTypeName(), bb, false);
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
        TownGenerationData data = AWGameData.INSTANCE.getPerWorldData(level, TownGenerationData.class);
        cleanupFailed(level, data);

        TownGenerationData.Task task = data.active();
        if (task == null) {
            planCache.remove(level);
            return;
        }

        TownTemplate template = TownTemplateManager.INSTANCE.getTemplate(task.templateName).orElse(null);
        if (template == null) {
            fail(data, task, "town template is no longer loaded", null);
            return;
        }

        CachedPlan cached;
        try {
            cached = cachedPlan(level, task, template);
        } catch (Exception exception) {
            fail(data, task, "failed to rebuild deterministic 1.12 town plan", exception);
            return;
        }

        try {
            switch (task.phase) {
                case PREPARE -> prepare(data, task, cached.generator);
                case WALLS -> structures(level, data, task, cached.plan.walls(), TownGenerationData.Phase.ROADS, false);
                case ROADS -> roads(data, task, cached.generator, cached.plan);
                case BUILDINGS -> structures(level, data, task, cached.plan.buildings(), TownGenerationData.Phase.LAMPS, false);
                case LAMPS -> structures(level, data, task, cached.plan.lamps(), TownGenerationData.Phase.FINALIZE, true);
                case FINALIZE -> finalizeTown(level, data, task, cached.generator);
                default -> {
                }
            }
        } catch (Exception exception) {
            fail(data, task, "exception in persistent town phase " + task.phase, exception);
        }
    }

    private CachedPlan cachedPlan(ServerLevel level, TownGenerationData.Task task, TownTemplate template) {
        String key = task.templateName + "@" + task.chunkMinX + "," + task.chunkMinZ + ","
                + task.chunkMaxX + "," + task.chunkMaxZ + "," + task.minY + "," + task.maxY;
        CachedPlan cached = planCache.get(level);
        if (cached != null && cached.key.equals(key)) {
            return cached;
        }
        TownGenerator generator = new TownGenerator(level, task.area(), template);
        TownGenerationPlan plan = generator.createPlan();
        cached = new CachedPlan(key, generator, plan);
        planCache.put(level, cached);
        return cached;
    }

    private void prepare(TownGenerationData data, TownGenerationData.Task task, TownGenerator generator) {
        StructureBB bounds = generator.exteriorBounds;
        int width = bounds.getXSize();
        int length = bounds.getZSize();
        int total = width * length;
        int end = Math.min(total, task.progress + PREPARE_COLUMNS_PER_TICK);
        int targetY = bounds.min.getY() - 1;

        var replacementId = generator.template.getBiomeReplacement().orElse(null);
        var replacementBiome = replacementId == null ? null : ForgeRegistries.BIOMES.getValue(replacementId);

        for (int index = task.progress; index < end; index++) {
            int localX = index % width;
            int localZ = index / width;
            int x = bounds.min.getX() + localX;
            int z = bounds.min.getZ() + localZ;
            prepareColumn(generator.world, x, z, targetY);
            if (replacementBiome != null
                    && (x == bounds.min.getX() || Math.floorMod(x, 4) == 0)
                    && (z == bounds.min.getZ() || Math.floorMod(z, 4) == 0)) {
                WorldTools.changeBiome(generator.world, new BlockPos(x, targetY, z), replacementBiome);
            }
            task.progress = index + 1;
        }

        if (task.progress >= total) {
            task.phase = TownGenerationData.Phase.WALLS;
            task.progress = 0;
        }
        data.markDirty();
    }

    private void prepareColumn(net.minecraft.world.level.Level level, int x, int z, int targetY) {
        int minBuild = level.getMinBuildHeight();
        int clearTop = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
        TerrainSample sample = terrainBelowFluid(level, x, clearTop, z);

        BlockState top = sample.state;
        if (top.isAir() || !top.getFluidState().isEmpty()) {
            top = Blocks.GRASS_BLOCK.defaultBlockState();
        }
        BlockState fill = sample.y > minBuild
                ? level.getBlockState(new BlockPos(x, sample.y - 1, z))
                : Blocks.DIRT.defaultBlockState();
        if (fill.isAir() || !fill.getFluidState().isEmpty()) {
            fill = top.is(Blocks.SAND) || top.is(Blocks.RED_SAND) ? top : Blocks.DIRT.defaultBlockState();
        }

        for (int y = clearTop; y > targetY; y--) {
            level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        for (int y = Math.min(sample.y + 1, targetY); y < targetY; y++) {
            level.setBlock(new BlockPos(x, y, z), fill, Block.UPDATE_CLIENTS);
        }

        // Fill caves/shallow water directly below the town floor, but stop as soon
        // as a real supporting block is found. The percentile terrain validator
        // keeps this bounded on normal modern terrain.
        for (int y = targetY - 1; y >= minBuild; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && state.getFluidState().isEmpty()) {
                break;
            }
            level.setBlock(pos, fill, Block.UPDATE_CLIENTS);
        }
        level.setBlock(new BlockPos(x, targetY, z), top, Block.UPDATE_CLIENTS);
    }

    private TerrainSample terrainBelowFluid(net.minecraft.world.level.Level level, int x, int startY, int z) {
        for (int y = startY; y >= level.getMinBuildHeight(); y--) {
            BlockState state = level.getBlockState(new BlockPos(x, y, z));
            if (!state.isAir() && state.getFluidState().isEmpty()) {
                return new TerrainSample(state, y);
            }
        }
        return new TerrainSample(Blocks.GRASS_BLOCK.defaultBlockState(), level.getMinBuildHeight());
    }


    private void roads(TownGenerationData data, TownGenerationData.Task task,
                       TownGenerator generator, TownGenerationPlan plan) {
        var roads = plan.roads();
        int end = Math.min(roads.size(), task.progress + ROAD_BLOCKS_PER_TICK);
        for (int i = task.progress; i < end; i++) {
            generator.placeRoadBlock(roads.get(i));
            task.progress = i + 1;
        }
        if (task.progress >= roads.size()) {
            task.phase = TownGenerationData.Phase.BUILDINGS;
            task.progress = 0;
        }
        data.markDirty();
    }

    /** Places one complete original AWS template per tick, loading only its exact intersecting chunks. */
    private void structures(ServerLevel level, TownGenerationData data, TownGenerationData.Task task,
                            java.util.List<TownGenerationPlan.PlannedStructure> placements,
                            TownGenerationData.Phase next, boolean lamp) {
        if (task.progress < placements.size()) {
            TownGenerationPlan.PlannedStructure placement = placements.get(task.progress);
            try {
                Optional<StructureBuilder> builderResult = placement.createBuilder(level);
                if (builderResult.isEmpty()) {
                    AncientWarfareStructure.LOG.error(
                            "Skipping missing town sub-template {} in town {} at {}, {}",
                            placement.templateName(), task.templateName, task.centerX(), task.centerZ());
                    task.progress++;
                    data.markDirty();
                    return;
                }
                StructureBuilder builder = builderResult.get();

                // This code runs from ServerTickEvent, not ChunkEvent.Load, so exact
                // synchronous loading is safe and cannot pin an unrelated global queue.
                if (!builder.ensureRequiredChunksLoaded(Integer.MAX_VALUE)) {
                    return;
                }

                if (lamp && !lampAreaClear(level, builder)) {
                    task.progress++;
                    data.markDirty();
                    return;
                }

                builder.instantConstruction();
            } catch (Exception placementException) {
                // A single corrupt/unsupported AWS piece must never poison the whole
                // town state machine. Log it, consume that placement, and continue.
                AncientWarfareStructure.LOG.error(
                        "Skipping failed town sub-template {} in town {} at {}, {}",
                        placement.templateName(), task.templateName, task.centerX(), task.centerZ(),
                        placementException);
            }
            task.progress++;
            data.markDirty();
        }

        if (task.progress >= placements.size()) {
            task.phase = next;
            task.progress = 0;
            data.markDirty();
        }
    }

    private boolean lampAreaClear(ServerLevel level, StructureBuilder builder) {
        StructureBB bb = builder.getBoundingBox();
        int yOffset = builder.getTemplate().getOffset().getY();
        for (BlockPos pos : BlockPos.betweenClosed(bb.min.offset(0, yOffset, 0), bb.max)) {
            if (!level.isEmptyBlock(pos)) {
                return false;
            }
        }
        return true;
    }

    private void finalizeTown(ServerLevel level, TownGenerationData data,
                              TownGenerationData.Task task, TownGenerator generator) {
        if (task.progress == 0) {
            WorldStructureGenerator.sprinkleSnow(level, generator.maximalBounds, 0);
            task.progress = 1;
            data.markDirty();
            return;
        }

        generator.generateVillagers();
        task.phase = TownGenerationData.Phase.COMPLETE;
        data.markDirty();
        AncientWarfareStructure.LOG.info("Completed persistent town {} at {}, {}",
                task.templateName, task.centerX(), task.centerZ());
        data.remove(task);
        planCache.remove(level);
    }

    private void fail(TownGenerationData data, TownGenerationData.Task task,
                      String reason, Exception exception) {
        task.phase = TownGenerationData.Phase.FAILED;
        task.progress = 0;
        data.markDirty();
        if (exception == null) {
            AncientWarfareStructure.LOG.error("Persistent town {} failed at {}, {}: {}",
                    task.templateName, task.centerX(), task.centerZ(), reason);
        } else {
            AncientWarfareStructure.LOG.error("Persistent town {} failed at {}, {}: {}",
                    task.templateName, task.centerX(), task.centerZ(), reason, exception);
        }
    }

    private void cleanupFailed(ServerLevel level, TownGenerationData data) {
        for (TownGenerationData.Task task : new ArrayList<>(data.tasks())) {
            if (task.phase != TownGenerationData.Phase.FAILED) {
                continue;
            }
            StructureBB bb = reservationBounds(task.area());
            try {
                AWGameData.INSTANCE.getPerWorldData(level, TownMap.class).removeGenerated(bb);
                AWGameData.INSTANCE.getPerWorldData(level, StructureMap.class).removeGeneratedAt(
                        level, task.centerX(), task.centerZ(), task.templateName, bb, false);
                if (task.clusterReserved) {
                    TerritoryManager.getTerritory(task.centerX() >> 4, task.centerZ() >> 4, level)
                            .ifPresent(territory -> territory.removeClusterValue(task.clusterValue));
                    task.clusterReserved = false;
                }
                data.remove(task);
                planCache.remove(level);
                AncientWarfareStructure.LOG.warn("Rolled back failed persistent town {} at {}, {}",
                        task.templateName, task.centerX(), task.centerZ());
            } catch (Exception rollbackException) {
                AncientWarfareStructure.LOG.error("Failed to roll back persistent town {} at {}, {}; will retry",
                        task.templateName, task.centerX(), task.centerZ(), rollbackException);
                data.markDirty();
            }
        }
    }

    private static StructureBB reservationBounds(TownBoundingArea area) {
        return new StructureBB(
                new BlockPos(area.getBlockMinX(), area.getMinY(), area.getBlockMinZ()),
                new BlockPos(area.getBlockMaxX(), area.getMaxY(), area.getBlockMaxZ()));
    }

    private record TerrainSample(BlockState state, int y) {
    }

    private record CachedPlan(String key, TownGenerator generator, TownGenerationPlan plan) {
    }
}
