package net.shadowmage.ancientwarfare.structure.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.config.AWStructureStatics;
import net.shadowmage.ancientwarfare.structure.gamedata.StructureEntry;
import net.shadowmage.ancientwarfare.structure.gamedata.StructureMap;
import net.shadowmage.ancientwarfare.structure.gamedata.TownMap;
import net.shadowmage.ancientwarfare.structure.registry.TerritorySettingRegistry;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.WorldGenStructureManager;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBuilderWorldGen;
import net.shadowmage.ancientwarfare.structure.worldgen.stats.PlacementRejectionReason;
import net.shadowmage.ancientwarfare.structure.worldgen.stats.WorldGenStatistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;

public class WorldStructureGenerator {

    public static final WorldStructureGenerator INSTANCE = new WorldStructureGenerator();

    private static final int MAX_DISTANCE_WITHIN_CLUSTER = 150;

    private final Random rng;
    private boolean debugTerritoryBorders = false;

    private WorldStructureGenerator() {
        rng = new Random();
    }

    /**
     * Called once for newly-created chunks by {@link WorldGenerationEventHandler}.
     * The expensive structure validation and placement still runs through the
     * existing tick queue so chunk generation is not blocked by a full template build.
     */
    public void queueChunkForGeneration(int chunkX, int chunkZ, Level world) {
        TerritoryManager.getTerritory(chunkX, chunkZ, world).ifPresent(territory -> {
            if (debugTerritoryBorders) {
                generateTerritoryBorders(chunkX, chunkZ, world, territory.getTerritoryId());
            }
            BlockPos spawn = world.getSharedSpawnPos();
            BlockPos chunkOrigin = new BlockPos(chunkX * 16, spawn.getY(), chunkZ * 16);
            double distSq = spawn.distSqr(chunkOrigin);
            if (AWStructureStatics.withinProtectionRange(distSq)) {
                return;
            }
            if (rng.nextFloat() < (AWStructureStatics.randomGenerationChance
                    * TerritorySettingRegistry.getTerritorySettings(territory.getTerritoryName()).getStructureGenerationChanceMultiplier())) {
                WorldGenTickHandler.INSTANCE.addChunkForGeneration(world, chunkX, chunkZ);
            }
        });
    }

    void generateAt(int chunkX, int chunkZ, Level world) {
        long t1 = System.currentTimeMillis();
        long seed = (((long) chunkX) << 32) | (((long) chunkZ) & 0xffffffffL);
        rng.setSeed(seed);
        int x = chunkX * 16 + rng.nextInt(16);
        int z = chunkZ * 16 + rng.nextInt(16);
        int y = getTargetY(world, x, z, false) + 1;
        if (y <= 0) {
            return;
        }

        TerritoryManager.getTerritory(chunkX, chunkZ, world).ifPresent(territory -> {
            Direction face = Direction.from2DDataValue(rng.nextInt(4));
            world.getProfiler().push("AWTemplateSelection");
            Optional<StructureTemplate> t = WorldGenStructureManager.INSTANCE.selectTemplateForGeneration(world, rng, x, y, z, face, territory);
            world.getProfiler().pop();
            AncientWarfareStructure.LOG.debug("Template selection took: {} ms.", System.currentTimeMillis() - t1);
            if (!t.isPresent()) {
                return;
            }
            StructureTemplate template = t.get();
            StructureMap map = AWGameData.INSTANCE.getPerWorldData(world, StructureMap.class);

            world.getProfiler().push("AWTemplateGeneration");
            if (attemptStructureGenerationAt(world, new BlockPos(x, y, z), face, template, map)) {
                territory.addClusterValue(template.getValidationSettings().getClusterValue());
                AncientWarfareStructure.LOG.info("Generated structure: {} at {}, {}, {}, time: {}ms", template.name, x, y, z, System.currentTimeMillis() - t1);
            }
            world.getProfiler().pop();
        });
    }

    private void generateTerritoryBorders(int chunkX, int chunkZ, Level world, String territoryId) {
        ITerritoryData territoryData = CapabilityTerritoryData.get(world).orElse(null);
        if (territoryData == null) {
            return;
        }
        if (territoryData.isDifferentTerritory(territoryId, chunkX - 1, chunkZ)) {
            for (int z = chunkZ * 16 + 1; z < (chunkZ * 16 + 15); z++) {
                int x = chunkX * 16 + 1;
                world.setBlock(new BlockPos(x, WorldStructureGenerator.getTargetY(world, x, z, false), z), Blocks.WHITE_CONCRETE.defaultBlockState(), 2);
            }
        }

        if (territoryData.isDifferentTerritory(territoryId, chunkX + 1, chunkZ)) {
            for (int z = chunkZ * 16 + 1; z < (chunkZ * 16 + 15); z++) {
                int x = chunkX * 16 + 14;
                world.setBlock(new BlockPos(x, WorldStructureGenerator.getTargetY(world, x, z, false), z), Blocks.WHITE_CONCRETE.defaultBlockState(), 2);
            }
        }

        if (territoryData.isDifferentTerritory(territoryId, chunkX, chunkZ - 1)) {
            for (int x = chunkX * 16 + 1; x < (chunkX * 16 + 15); x++) {
                int z = chunkZ * 16 + 1;
                world.setBlock(new BlockPos(x, WorldStructureGenerator.getTargetY(world, x, z, false), z), Blocks.WHITE_CONCRETE.defaultBlockState(), 2);
            }
        }

        if (territoryData.isDifferentTerritory(territoryId, chunkX, chunkZ + 1)) {
            for (int x = chunkX * 16 + 1; x < (chunkX * 16 + 15); x++) {
                int z = chunkZ * 16 + 14;
                world.setBlock(new BlockPos(x, WorldStructureGenerator.getTargetY(world, x, z, false), z), Blocks.WHITE_CONCRETE.defaultBlockState(), 2);
            }
        }
    }


    public static int getTargetY(Level world, int x, int z, boolean skipWater) {
        return getTargetY(world, x, z, skipWater, world.getMaxBuildHeight() - 1);
    }

    public static int getTargetY(Level world, int x, int z, boolean skipWater, int startAtY) {
        if (world.dimension() == Level.NETHER) {
            // Keep the original Nether placement convention.
            return 31;
        }
        int minimumY = world.getMinBuildHeight();
        int maximumY = Math.min(startAtY, world.getMaxBuildHeight() - 1);
        for (int y = maximumY; y >= minimumY; y--) {
            BlockState state = world.getBlockState(new BlockPos(x, y, z));
            if (AWStructureStatics.isSkippable(state) || (skipWater && state.getFluidState().is(FluidTags.WATER))) {
                continue;
            }
            return y;
        }
        return -1;
    }

    public static void sprinkleSnow(Level world, StructureBB bb, int border) {
        BlockPos p1 = bb.min.offset(-border, 0, -border);
        BlockPos p2 = bb.max.offset(border, 0, border);
        for (int x = p1.getX(); x <= p2.getX(); x++) {
            for (int z = p1.getZ(); z <= p2.getZ(); z++) {
                BlockPos snowPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, world.getMinBuildHeight(), z));
                BlockPos supportPos = snowPos.below();
                if (snowPos.getY() <= p2.getY()
                        && snowPos.getY() > world.getMinBuildHeight()
                        && world.getBiome(snowPos).value().coldEnoughToSnow(snowPos)) {
                    BlockState support = world.getBlockState(supportPos);
                    BlockState snow = Blocks.SNOW.defaultBlockState();
                    if (!support.isAir() && support.isFaceSturdy(world, supportPos, Direction.UP) && snow.canSurvive(world, snowPos)) {
                        world.setBlock(snowPos, snow, 3);
                    }
                }
            }
        }
    }

    public static int getStepNumber(int x, int z, int minX, int maxX, int minZ, int maxZ) {
        int steps = 0;
        if (x < minX - 1) {
            steps += (minX - 1) - x;
        } else if (x > maxX + 1) {
            steps += x - (maxX + 1);
        }
        if (z < minZ - 1) {
            steps += (minZ - 1) - z;
        } else if (z > maxZ + 1) {
            steps += z - (maxZ + 1);
        }
        return steps;
    }

    private boolean attemptStructureGenerationAt(Level world, BlockPos pos, Direction face, StructureTemplate template, StructureMap map) {
        long t1 = System.currentTimeMillis();
        int prevY = pos.getY();
        StructureBB bb = new StructureBB(pos, face, template.getSize(), template.getOffset());
        int y = template.getValidationSettings().getAdjustedSpawnY(world, pos.getX(), pos.getY(), pos.getZ(), face, template, bb);
        pos = new BlockPos(pos.getX(), y, pos.getZ());
        bb.min = bb.min.above(y - prevY);
        bb.max = bb.max.above(y - prevY);
        int xs = bb.getXSize();
        int zs = bb.getZSize();
        int size = ((Math.max(xs, zs)) / 16) + 3;
        if (!checkOtherStructureCrossAndCloseness(world, pos, map, bb, size, template.getValidationSettings().getBorderSize())) {
            WorldGenStatistics.addStructurePlacementRejection(template.name, PlacementRejectionReason.STRUCTURE_BB_OVERLAP);
            WorldGenDetailedLogHelper.log("Structure \"{}\" failed placement, because its bounding box {} intersects an existing structure", () -> template.name, () -> bb);
            return false;
        }

        TownMap townMap = AWGameData.INSTANCE.getPerWorldData(world, TownMap.class);
        if (townMap.intersectsWithTown(bb)) {
            WorldGenStatistics.addStructurePlacementRejection(template.name, PlacementRejectionReason.TOWN_BB_OVERLAP);
            WorldGenDetailedLogHelper.log("Structure \"{}\" failed placement, because its bounding box {} intersects an existing town", () -> template.name, () -> bb);
            return false;
        }
        if (template.getValidationSettings().validatePlacement(world, pos.getX(), pos.getY(), pos.getZ(), face, template, bb)) {
            WorldGenStatistics.addStructureGeneratedInfo(template.name, world, pos);
            AncientWarfareStructure.LOG.debug("Validation took: {} ms", System.currentTimeMillis() - t1);
            generateStructureAt(world, pos, face, template, map);
            return true;
        }
        return false;
    }

    private boolean checkOtherStructureCrossAndCloseness(Level world, BlockPos pos, StructureMap map, StructureBB bb, int size, int borderSize) {
        Collection<StructureEntry> bbCheckList = map.getEntriesNear(world, pos.getX(), pos.getZ(), size, true, new ArrayList<>());
        double maxDistance = 0;
        StructureBB bbWithBorder = new StructureBB(bb.min, bb.max).expand(borderSize, 0, borderSize);
        for (StructureEntry entry : bbCheckList) {
            if (bbWithBorder.intersects(entry.getBB())) {
                return false;
            }
            double distance = bb.getDistanceTo(entry.getBB());
            if (distance < MAX_DISTANCE_WITHIN_CLUSTER && distance > maxDistance) {
                maxDistance = distance;
            }
        }
        return !(maxDistance > 30 && world.getRandom().nextFloat() * (MAX_DISTANCE_WITHIN_CLUSTER - maxDistance) > 30);
    }

    private void generateStructureAt(Level world, BlockPos pos, Direction face, StructureTemplate template, StructureMap map) {
        map.setGeneratedAt(world, pos.getX(), pos.getZ(), new StructureEntry(pos.getX(), pos.getY(), pos.getZ(), face, template), template.getValidationSettings().isUnique());
        WorldGenTickHandler.INSTANCE.addStructureForGeneration(new StructureBuilderWorldGen(world, template, face, pos));
    }

}
