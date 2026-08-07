package net.shadowmage.ancientwarfare.structure.town;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.structure.config.AWStructureStatics;
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

        int largestRelevantTown = templates.stream().mapToInt(TownTemplate::getMaxSize).max().orElse(0);
        int smallestRelevantTown = templates.stream().mapToInt(TownTemplate::getMinSize).min().orElse(1);
        TownPlacementValidator.findGenerationPosition(world, blockX, blockZ, largestRelevantTown, smallestRelevantTown).ifPresent(area ->
                selectTerritoryTemplate(world, blockX, blockZ, templates, area));
    }

    private void selectTerritoryTemplate(Level world, int blockX, int blockZ, List<TownTemplate> templates, TownBoundingArea area) {
        TerritoryManager.getTerritory(blockX >> 4, blockZ >> 4, world).ifPresent(territory ->
                selectTemplateAndShrinkToMax(world, templates, area, territory));
    }

    private void selectTemplateAndShrinkToMax(Level world, List<TownTemplate> templates, TownBoundingArea area, Territory territory) {
        TownTemplateManager.INSTANCE.selectTemplateFittingArea(world, area, templates, territory).ifPresent(
                template -> {
                    if (area.getChunkWidth() > template.getMaxSize()) { // shrink width to the configured inclusive chunk count
                        area.chunkMaxX = area.chunkMinX + template.getMaxSize() - 1;
                    }
                    if (area.getChunkLength() > template.getMaxSize()) { // shrink length to the configured inclusive chunk count
                        area.chunkMaxZ = area.chunkMinZ + template.getMaxSize() - 1;
                    }
                    if (world instanceof ServerLevel serverLevel) {
                        PersistentTownGenerationManager.INSTANCE.queue(serverLevel, area, template, territory);
                    }
                }
        );
    }

    public void generate(Level world, TownBoundingArea area, TownTemplate template) {
        if (!(world instanceof ServerLevel serverLevel)) {
            return;
        }
        TerritoryManager.getTerritory(area.getCenterX() >> 4, area.getCenterZ() >> 4, world)
                .ifPresent(territory -> PersistentTownGenerationManager.INSTANCE.queue(
                        serverLevel, area, template, territory));
    }

}
