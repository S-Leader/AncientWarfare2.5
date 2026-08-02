package net.shadowmage.ancientwarfare.structure.town;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.structure.config.AWStructureStatics;
import net.shadowmage.ancientwarfare.structure.gamedata.TownEntry;
import net.shadowmage.ancientwarfare.structure.gamedata.TownMap;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;
import net.shadowmage.ancientwarfare.structure.worldgen.Territory;
import net.shadowmage.ancientwarfare.structure.worldgen.TerritoryManager;
import net.shadowmage.ancientwarfare.structure.worldgen.WorldGenTickHandler;

import java.util.List;

public class WorldTownGenerator {

    public static final WorldTownGenerator INSTANCE = new WorldTownGenerator();

    private WorldTownGenerator() {
    }

    /**
     * Queues natural town generation for a newly-created chunk.
     */
    public void queueChunkForGeneration(int chunkX, int chunkZ, Level world) {
        BlockPos spawn = world.getSharedSpawnPos();
        BlockPos chunkOrigin = new BlockPos(chunkX * 16, spawn.getY(), chunkZ * 16);
        double distSq = spawn.distSqr(chunkOrigin);
        if (AWStructureStatics.withinProtectionRange(distSq)) {
            return;
        }
        if (world.getRandom().nextFloat() < AWStructureStatics.townGenerationChance) {
            WorldGenTickHandler.INSTANCE.addChunkForTownGeneration(world, chunkX, chunkZ);
        }
    }

    public void attemptGeneration(Level world, int blockX, int blockZ) {
        List<TownTemplate> templates = TownTemplateManager.INSTANCE.getTemplatesValidAtPosition(world, blockX, blockZ);
        if (templates.isEmpty()) {
            return;
        }

        TownPlacementValidator.findGenerationPosition(world, blockX, blockZ).ifPresent(area ->
                selectTerritoryTemplate(world, blockX, blockZ, templates, area));
    }

    private void selectTerritoryTemplate(Level world, int blockX, int blockZ, List<TownTemplate> templates, TownBoundingArea area) {
        TerritoryManager.getTerritory(blockX >> 4, blockZ >> 4, world).ifPresent(territory ->
                selectTemplateAndShrinkToMax(world, templates, area, territory));
    }

    private void selectTemplateAndShrinkToMax(Level world, List<TownTemplate> templates, TownBoundingArea area, Territory territory) {
        TownTemplateManager.INSTANCE.selectTemplateFittingArea(world, area, templates, territory).ifPresent(
                template -> {
                    if (area.getChunkWidth() - 1 > template.getMaxSize())//shrink width down to town max size
                    {
                        area.chunkMaxX = area.chunkMinX + template.getMaxSize();
                    }
                    if (area.getChunkLength() - 1 > template.getMaxSize())//shrink length down to town max size
                    {
                        area.chunkMaxZ = area.chunkMinZ + template.getMaxSize();
                    }
                    generate(world, area, template);
                    territory.addClusterValue(template.getClusterValue());
                }
        );
    }

    public void generate(Level world, TownBoundingArea area, TownTemplate template) {
        StructureBB bb = new StructureBB(new BlockPos(area.getBlockMinX(), area.getMinY(), area.getBlockMinZ()), new BlockPos(area.getBlockMaxX(), area.getMaxY(), area.getBlockMaxZ()));
        /*
         * add the town to generated town map, to eliminate towns generating too close to eachother
         */
        AWGameData.INSTANCE.getPerWorldData(world, TownMap.class).setGenerated(new TownEntry(bb, template.shouldPreventNaturalHostileSpawns()));

        /*
         * and finally initialize generation.  The townGenerator will do borders, walls, roads, and add any structures to the world-gen tick handler for generation.
         */
        new TownGenerator(world, area, template).generate();
    }

}
