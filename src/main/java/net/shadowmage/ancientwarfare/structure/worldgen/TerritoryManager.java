package net.shadowmage.ancientwarfare.structure.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.structure.config.AWStructureStatics;
import net.shadowmage.ancientwarfare.structure.registry.TerritorySettingRegistry;
import net.shadowmage.ancientwarfare.structure.template.WorldGenStructureManager;
import net.shadowmage.ancientwarfare.structure.util.CollectionUtils;
import net.shadowmage.ancientwarfare.structure.worldgen.stats.WorldGenStatistics;

import java.util.*;

public class TerritoryManager {
    private static final HashMap<ResourceLocation, List<String>> territoryNamesByBiome = new HashMap<>();
    private static final HashMap<String, Set<ResourceLocation>> biomesByTerritoryNames = new HashMap<>();

    private TerritoryManager() {
    }

    public static Optional<Territory> getTerritory(int chunkX, int chunkZ, Level world) {
        long chunkPosValue = ChunkPos.asLong(chunkX, chunkZ);
        Optional<ITerritoryData> territoryData = CapabilityTerritoryData.get(world);
        if (territoryData.isEmpty()) {
            return Optional.empty();
        }

        ITerritoryData data = territoryData.get();
        if (!data.isOwned(chunkPosValue)) {
            new TerritoryGenerator(world).generateTerritory(chunkX, chunkZ, chunkPosValue, data);
        }

        return data.getTerritory(chunkPosValue);
    }

    static BlockPos getChunkCenterPos(int chunkX, int chunkZ) {
        int x = chunkX * 16 + 8;
        int z = chunkZ * 16 + 8;

        return new BlockPos(x, 1, z);
    }

    private static ResourceLocation getBiomeAtChunkCenter(Level world, int chunkX, int chunkZ) {
        BlockPos center = getChunkCenterPos(chunkX, chunkZ);
        // ServerLevel#getBiome loads the requested chunk. Territory expansion
        // examines many neighbours, so using it here recursively created thousands
        // of chunk tickets. Sample the generator's uncached noise biome instead.
        var biome = world instanceof ServerLevel serverLevel
                ? serverLevel.getUncachedNoiseBiome(QuartPos.fromBlock(center.getX()),
                QuartPos.fromBlock(center.getY()), QuartPos.fromBlock(center.getZ()))
                : world.getBiome(center);
        return biome.unwrapKey().map(key -> key.location()).orElse(null);
    }

    public static void clearTerritoryCache() {
        territoryNamesByBiome.clear();
        biomesByTerritoryNames.clear();
    }

    public static Set<String> getTerritoryNames() {
        return biomesByTerritoryNames.keySet();
    }

    public static Optional<Set<ResourceLocation>> getTerritoryBiomes(String territoryName) {
        return Optional.ofNullable(biomesByTerritoryNames.get(territoryName));
    }

    private static int getTerritoryWeight(String territoryName) {
        return (int) ((WorldGenStructureManager.INSTANCE.getTerritoryTemplates(territoryName).map(Set::size).orElse(0)
                + (!territoryName.equals(WorldGenStructureManager.GENERIC_TERRITORY_NAME) ? WorldGenStructureManager.INSTANCE.getTerritoryTemplates(WorldGenStructureManager.GENERIC_TERRITORY_NAME).map(Set::size).orElse(0) : 0))
                * TerritorySettingRegistry.getTerritorySettings(territoryName).getTerritoryWeightMultiplier());
    }

    @SuppressWarnings("java:S3518")
    public static float getTerritoryChanceInBiome(String territoryName, String biomeName) {
        ResourceLocation biome = new ResourceLocation(biomeName);
        if (!territoryNamesByBiome.containsKey(biome)) {
            return 0;
        }
        List<String> territoryNames = territoryNamesByBiome.get(biome);
        if (!territoryNames.contains(territoryName)) {
            return 0;
        }
        int weight = getTerritoryWeight(territoryName);

        float totalWeight = 0;

        for (String name : territoryNames) {
            totalWeight += getTerritoryWeight(name);
        }

        return weight / totalWeight;
    }

    public static void addTerritoryInBiome(String territoryName, String biomeName) {
        // Logic for disabling certain territories belonging to certain factions:
        if (AWStructureStatics.factionBlacklist.contains(territoryName.trim())) {
            WorldGenDetailedLogHelper.log("[AW2t]: Faction disabled in config; skipping territory for faction \"" + territoryName + "\"");
            return;
        }

        ResourceLocation biome = new ResourceLocation(biomeName);
        List<String> territoryNames = territoryNamesByBiome.getOrDefault(biome, new ArrayList<>());
        if (!territoryNames.contains(territoryName)) {
            territoryNames.add(territoryName);
            territoryNamesByBiome.put(biome, territoryNames);
        }

        Set<ResourceLocation> biomes = biomesByTerritoryNames.getOrDefault(territoryName, new HashSet<>());
        biomes.add(biome);
        biomesByTerritoryNames.put(territoryName, biomes);
    }

    private static class TerritoryGenerator {
        private Map<Long, ProcessedChunk> chunksToProcess = new HashMap<>();
        private Map<Long, BlockPos> includedChunks = new HashMap<>();
        private Set<Long> visitedChunks = new HashSet<>();
        private Level world;
        private double maxTerritoryCenterDistanceSq = 0;

        private TerritoryGenerator(Level world) {
            this.world = world;
        }

        private void generateTerritory(int chunkX, int chunkZ, long chunkPosValue, ITerritoryData territoryData) {
            BlockPos firstPos = getChunkCenterPos(chunkX, chunkZ);
            ResourceLocation biome = getBiomeAtChunkCenter(world, chunkX, chunkZ);
            if (biome == null || !territoryNamesByBiome.containsKey(biome)) {
                return;
            }

            WorldGenDetailedLogHelper.log("Generating new territory at x {} z {} in biome \"{}\"", firstPos::getX, firstPos::getZ, biome::toString);

            List<String> territoryNames = territoryNamesByBiome.get(biome);

            String territoryName = getRandomTerritory(territoryNames);
            String territoryId = territoryName + territoryData.getNextTerritoryId(territoryName);
            maxTerritoryCenterDistanceSq = TerritorySettingRegistry.getMaxTerritoryCenterDistanceSq(territoryName);

            includedChunks.put(chunkPosValue, firstPos);

            chunksToProcess.put(chunkPosValue, new ProcessedChunk(chunkPosValue, firstPos, chunkX, chunkZ));
            getTerritoryBiomes(territoryName).ifPresent(biomes -> processChunks(biomes, territoryData));

            if (isTooFewChunksForNewTerritory()) {
                territoryId = getFirstDifferentTerritoryIdFromNeighbors(chunkX, chunkZ, territoryId, territoryData);
                WorldGenDetailedLogHelper.log("Too few chunks in new territory - connecting to existing territory");
            }

            String finalTerritoryId = territoryId;
            includedChunks.forEach((hash, pos) -> territoryData.setOwned(hash, finalTerritoryId, territoryName));
            WorldGenDetailedLogHelper.log("{} chunks included in territory \"{}\"", includedChunks::size, () -> finalTerritoryId);
            if (!isTooFewChunksForNewTerritory()) {
                //noinspection ConstantConditions
                WorldGenStatistics.addTerritoryInfo(territoryName, biome.toString(), includedChunks.size()
                        * TerritorySettingRegistry.getTerritorySettings(territoryName).getPerChunkClusterValueMultiplier() * AWStructureStatics.chunkClusterValue);
            }
        }

        private boolean isTooFewChunksForNewTerritory() {
            return includedChunks.size() < 3;
        }

        private BlockPos getTerritoryCenter() {
            return TerritoryManager.getTerritoryCenter(includedChunks.values());
        }

        private void addChunkToProcess(Set<ResourceLocation> biomes, BlockPos territoryCenterPos, ChunkPos chunkPos, ITerritoryData territoryData) {
            long chunkPosValue = ChunkPos.asLong(chunkPos.x, chunkPos.z);
            BlockPos centerPos = getChunkCenterPos(chunkPos.x, chunkPos.z);
            if (territoryData.isOwned(chunkPosValue) || visitedChunks.contains(chunkPosValue) || chunksToProcess.containsKey(chunkPosValue)) {
                return;
            }
            ResourceLocation biome = getBiomeAtChunkCenter(world, chunkPos.x, chunkPos.z);
            if (biome != null && biomes.contains(biome) && centerPos.distSqr(territoryCenterPos) < 1.1 * maxTerritoryCenterDistanceSq) {
                chunksToProcess.put(chunkPosValue, new ProcessedChunk(chunkPosValue, centerPos, chunkPos.x, chunkPos.z));
            }
        }

        private void includeChunk(ProcessedChunk processedChunk) {
            includedChunks.put(processedChunk.getChunkPosValue(), processedChunk.getCenterPos());
            visitedChunks.add(processedChunk.getChunkPosValue());
            WorldGenDetailedLogHelper.log("Chunk #{} {}, territory center: {}", () -> includedChunks.entrySet().size(), processedChunk::getCenterPos, this::getTerritoryCenter);
        }

        private void processChunks(Set<ResourceLocation> biomes, ITerritoryData territoryData) {
            while (!chunksToProcess.isEmpty()) {
                if (includedChunks.isEmpty() && chunksToProcess.size() == 1) {
                    ProcessedChunk processedChunk = chunksToProcess.values().iterator().next();
                    includeChunk(processedChunk);
                } else {
                    processClosestChunk(biomes, territoryData);
                }
            }
        }

        private void processClosestChunk(Set<ResourceLocation> biomes, ITerritoryData territoryData) {
            ProcessedChunk closestChunkToProcess = null;
            double closestDist = Double.MAX_VALUE;
            BlockPos territoryCenterPos = getTerritoryCenter();
            for (ProcessedChunk processedChunk : chunksToProcess.values()) {
                double dist = territoryCenterPos.distSqr(processedChunk.getCenterPos());
                if (dist < closestDist) {
                    closestDist = dist;
                    closestChunkToProcess = processedChunk;
                }
            }
            if (closestChunkToProcess != null) {
                if (closestDist < maxTerritoryCenterDistanceSq) {
                    includeChunk(closestChunkToProcess);

                    addChunkToProcess(biomes, territoryCenterPos, new ChunkPos(closestChunkToProcess.getChunkX() + 1, closestChunkToProcess.getChunkZ()), territoryData);
                    addChunkToProcess(biomes, territoryCenterPos, new ChunkPos(closestChunkToProcess.getChunkX() - 1, closestChunkToProcess.getChunkZ()), territoryData);
                    addChunkToProcess(biomes, territoryCenterPos, new ChunkPos(closestChunkToProcess.getChunkX(), closestChunkToProcess.getChunkZ() + 1), territoryData);
                    addChunkToProcess(biomes, territoryCenterPos, new ChunkPos(closestChunkToProcess.getChunkX(), closestChunkToProcess.getChunkZ() - 1), territoryData);
                }
                chunksToProcess.remove(closestChunkToProcess.getChunkPosValue());
            }
        }

        private Optional<String> getDifferentTerritoryId(int chunkX, int chunkZ, String territoryId, ITerritoryData territoryData) {
            Optional<Territory> territory = territoryData.getTerritory(ChunkPos.asLong(chunkX, chunkZ));
            if (territory.isPresent()) {
                String id = territory.get().getTerritoryId();
                if (!id.equals(territoryId)) {
                    return Optional.of(id);
                }
            }
            return Optional.empty();
        }

        private String getFirstDifferentTerritoryIdFromNeighbors(int chunkX, int chunkZ, String territoryId, ITerritoryData territoryData) {
            return getDifferentTerritoryId(chunkX + 1, chunkZ, territoryId, territoryData)
                    .orElse(getDifferentTerritoryId(chunkX - 1, chunkZ, territoryId, territoryData)
                            .orElse(getDifferentTerritoryId(chunkX, chunkZ + 1, territoryId, territoryData)
                                    .orElse(getDifferentTerritoryId(chunkX, chunkZ - 1, territoryId, territoryData).orElse(territoryId))
                            )
                    );
        }

        private String getRandomTerritory(List<String> names) {
            return CollectionUtils.getWeightedRandomElement(new Random(world.getRandom().nextLong()), names,
                            TerritoryManager::getTerritoryWeight, (totalWeight, selected) -> {
                                WorldGenDetailedLogHelper.log("Out of total of {} territories with weight total of {} territory \"{}\" with weight {} was selected",
                                        names::size, () -> totalWeight, () -> selected, () -> selected == null ? "" : TerritoryManager.getTerritoryWeight(selected));
                                WorldGenDetailedLogHelper.log("Following territories and weights were considered: \n{}",
                                        () -> {
                                            StringJoiner joiner = new StringJoiner(", ");
                                            names.forEach(name -> joiner.add(name + ":" + TerritoryManager.getTerritoryWeight(name)));
                                            return joiner.toString();
                                        });
                            })
                    .orElse(WorldGenStructureManager.GENERIC_TERRITORY_NAME);
        }
    }

    static BlockPos getTerritoryCenter(Collection<BlockPos> positions) {
        BlockPos territoryCenterPos;
        double averageX = 0;
        double averageZ = 0;
        for (BlockPos pos : positions) {
            averageX += (float) pos.getX() / positions.size();
            averageZ += (float) pos.getZ() / positions.size();
        }
        territoryCenterPos = BlockPos.containing(averageX, 1, averageZ);
        return territoryCenterPos;
    }

    private static class ProcessedChunk {
        private long chunkPosValue;
        private BlockPos centerPos;
        private int chunkX;
        private int chunkZ;

        private ProcessedChunk(long chunkPosValue, BlockPos centerPos, int chunkX, int chunkZ) {
            this.chunkPosValue = chunkPosValue;
            this.centerPos = centerPos;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        private long getChunkPosValue() {
            return chunkPosValue;
        }

        private BlockPos getCenterPos() {
            return centerPos;
        }

        private int getChunkX() {
            return chunkX;
        }

        private int getChunkZ() {
            return chunkZ;
        }
    }
}
