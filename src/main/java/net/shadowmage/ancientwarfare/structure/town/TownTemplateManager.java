package net.shadowmage.ancientwarfare.structure.town;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.worldgen.Territory;

import java.util.*;
import java.util.stream.Collectors;

public class TownTemplateManager {

    public static final TownTemplateManager INSTANCE = new TownTemplateManager();

    private final HashMap<String, TownTemplate> templates;
    private final List<TownTemplate> searchCache;

    private TownTemplateManager() {
        templates = new HashMap<>();
        searchCache = new ArrayList<>();
    }

    public void loadTemplate(TownTemplate template) {
        templates.put(template.getTownTypeName(), template);
    }

    public Optional<TownTemplate> getTemplate(String name) {
        return Optional.ofNullable(templates.get(name));
    }

    public Collection<TownTemplate> getTemplates() {
        return templates.values();
    }

    List<TownTemplate> getTemplatesValidAtPosition(Level world, int x, int z) {
        ResourceLocation biomeId = world.getBiome(new BlockPos(x, world.getSeaLevel(), z))
                .unwrapKey().map(key -> key.location()).orElse(null);
        if (biomeId == null) {
            AncientWarfareStructure.LOG.debug("Unable to resolve the biome registry key while validating town generation at {}, {}", x, z);
            return Collections.emptyList();
        }
        String biomeName = biomeId.toString();
        int dimension = getLegacyDimensionId(world);
        return templates.values().stream().filter(t -> isDimensionValid(dimension, t) && isBiomeValid(biomeName, t)).collect(Collectors.toList());
    }

    private int getLegacyDimensionId(Level world) {
        if (world.dimension() == Level.NETHER) {
            return -1;
        }
        if (world.dimension() == Level.END) {
            return 1;
        }
        return 0;
    }

    Optional<TownTemplate> selectTemplateFittingArea(Level world, TownBoundingArea area, List<TownTemplate> templates, Territory territory) {
        TownTemplate selection = null;
        int width = area.getChunkWidth();
        int length = area.getChunkLength();

        int min = Math.min(width, length);
        int templateMinimumSize;

        int totalWeight = 0;
        for (TownTemplate t : templates) {
            templateMinimumSize = t.getMinSize();
            if (min >= templateMinimumSize && isCorrectTerritory(territory.getTerritoryName(), t) && territory.getRemainingClusterValue() > t.getClusterValue()) {
                searchCache.add(t);
                totalWeight += t.getSelectionWeight();
            }
        }
        if (!searchCache.isEmpty() && totalWeight > 0) {
            totalWeight = world.getRandom().nextInt(totalWeight);
            for (TownTemplate t : searchCache) {
                totalWeight -= t.getSelectionWeight();
                if (totalWeight < 0) {
                    selection = t;
                    break;
                }
            }
        }
        searchCache.clear();
        return Optional.ofNullable(selection);
    }

    private boolean isCorrectTerritory(String territoryName, TownTemplate t) {
        return t.getTerritoryName().equals(territoryName) || t.getTerritoryName().isEmpty();
    }

    private boolean isBiomeValid(String biome, TownTemplate t) {
        boolean contains = t.getBiomeList().contains(biome);
        boolean wl = t.isBiomeWhiteList();
        return (wl && contains) || (!wl && !contains);
    }

    private boolean isDimensionValid(int dimension, TownTemplate t) {
        return t.getDimensionList().contains(dimension) == t.isDimensionWhiteList();
    }

    public void removeAll() {
        templates.clear();
    }
}
