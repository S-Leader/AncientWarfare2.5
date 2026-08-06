// Persistent per-level data is supplied through the modern SavedData-backed AWGameData service.

package net.shadowmage.ancientwarfare.structure.gamedata;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.compat.WorldSavedData;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.Zone;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.structure.network.PacketStructureEntry;
import net.shadowmage.ancientwarfare.structure.util.ConquerHelper;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class StructureMap extends WorldSavedData {
    private static final Cache<Zone, Set<StructureEntry>> CHUNK_STRUCTURE_ENTRIES = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();

    private StructureDimensionMap map;

    public StructureMap(String name) {
        super(name);
        map = new StructureDimensionMap();
    }

    @Override
    public void readFromNBT(CompoundTag nbttagcompound) {
        CompoundTag mapTag = nbttagcompound.getCompound("map");
        map.readFromNBT(this, mapTag);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbttagcompound) {
        CompoundTag mapTag = new CompoundTag();
        map.writeToNBT(mapTag);
        nbttagcompound.put("map", mapTag);
        return nbttagcompound;
    }

    public boolean shouldPreventSpawnAtPos(Level world, BlockPos pos) {
        boolean preventSpawn = false;
        for (StructureEntry entry : getStructuresInChunk(world, pos)) {
            if (entry.getBB().contains(pos) && entry.shouldPreventNaturalHostileSpawns() && !entry.getConquered()) {
                if (entry.hasProtectionFlag() || ConquerHelper.checkBBNotConquered(world, entry.getBB())) {
                    return true;
                } else {
                    entry.setConquered();
                }
            }
        }
        return preventSpawn;
    }

    private Set<StructureEntry> getStructuresInChunk(Level world, BlockPos pos) {
        Set<StructureEntry> structures;
        ChunkPos chunkPos = new ChunkPos(pos);
        BlockPos min = new BlockPos(chunkPos.x * 16, 1, chunkPos.z * 16);
        BlockPos max = new BlockPos(chunkPos.x * 16 + 15, 255, chunkPos.z * 16 + 15);
        Zone chunkZone = new Zone(min, max);
        try {
            structures = CHUNK_STRUCTURE_ENTRIES.get(chunkZone, () -> getStructuresIn(world, chunkZone));
        } catch (ExecutionException e) {
            AncientWarfareNPC.LOG.error("Error getting structure entries in chunk for hostile entity check: ", e);
            return new HashSet<>();
        }
        return structures;
    }

    public Collection<StructureEntry> getEntriesNear(Level world, int worldX, int worldZ, int chunkRadius, boolean expandBySize, Collection<StructureEntry> list) {
        int cx = worldX >> 4;
        int cz = worldZ >> 4;
        return map.getEntriesNear(dimensionKey(world), cx, cz, chunkRadius, expandBySize, list);
    }

    private Set<StructureEntry> getStructuresIn(Level world, Zone zone) {
        Set<StructureEntry> ret = new HashSet<>();
        for (StructureEntry structure : getEntriesNear(world, zone.min.getX(), zone.min.getZ(), 1, true, new ArrayList<>())) {
            if (structure.getBB().intersects(zone)) {
                ret.add(structure);
            }
        }
        return ret;
    }

    public Optional<StructureEntry> getStructureAt(Level world, BlockPos pos) {
        for (StructureEntry structure : getEntriesNear(world, pos.getX(), pos.getZ(), 1, true, new ArrayList<>())) {
            if (structure.getBB().contains(pos)) {
                return Optional.of(structure);
            }
        }
        return Optional.empty();
    }

    public Optional<StructureEntry> getStructureAt(Level world, int chunkX, int chunkZ) {
        return map.getEntryAt(dimensionKey(world), chunkX, chunkZ);
    }

    public void setGeneratedAt(Level world, int worldX, int worldZ, StructureEntry entry, boolean unique) {
        entry.setStructureMap(this);
        int cx = worldX >> 4;
        int cz = worldZ >> 4;
        String dimension = dimensionKey(world);
        setGeneratedAt(dimension, cx, cz, entry, unique);
    }

    public void setGeneratedAt(String dimension, int cx, int cz, StructureEntry entry, boolean unique) {
        setGeneratedAt(dimension, cx, cz, entry, unique, true);
    }

    public void setGeneratedAt(String dimension, int cx, int cz, StructureEntry entry, boolean unique, boolean sync) {
        entry.setStructureMap(this);
        map.setGeneratedAt(dimension, cx, cz, entry, unique);
        CHUNK_STRUCTURE_ENTRIES.invalidateAll();
        markDirty();
        if (sync) {
            NetworkHandler.sendToAllPlayers(new PacketStructureEntry(dimension, cx, cz, entry));
        }
    }

    /**
     * Removes a provisional natural-worldgen reservation only when the entry at
     * the coordinate is still the exact entry supplied by the failed ticket.
     * This prevents an old failure callback from deleting a newer structure that
     * reused the same anchor chunk.
     */
    public boolean removeGeneratedAt(Level world, int worldX, int worldZ,
                                     StructureEntry expectedEntry, boolean unique) {
        String dimension = dimensionKey(world);
        int cx = worldX >> 4;
        int cz = worldZ >> 4;
        if (!map.removeGeneratedAt(dimension, cx, cz, expectedEntry, unique)) {
            return false;
        }

        CHUNK_STRUCTURE_ENTRIES.invalidateAll();
        markDirty();
        return true;
    }

    private static String dimensionKey(Level world) {
        return world.dimension().location().toString();
    }

    public boolean isGeneratedUnique(String name) {
        return this.map.generatedUniques.contains(name);
    }

    private static class StructureDimensionMap {
        private final Map<String, StructureWorldMap> mapsByDimension = new HashMap<>();
        private final Set<String> generatedUniques = new HashSet<>();

        private Collection<StructureEntry> getEntriesNear(String dimension, int chunkX, int chunkZ,
                                                          int chunkRadius, boolean expandBySize,
                                                          Collection<StructureEntry> list) {
            StructureWorldMap worldMap = mapsByDimension.get(dimension);
            return worldMap == null
                    ? Collections.emptyList()
                    : worldMap.getEntriesNear(chunkX, chunkZ, chunkRadius, expandBySize, list);
        }

        private Optional<StructureEntry> getEntryAt(String dimension, int chunkX, int chunkZ) {
            StructureWorldMap worldMap = mapsByDimension.get(dimension);
            return worldMap == null ? Optional.empty() : worldMap.getEntryAt(chunkX, chunkZ);
        }

        private void setGeneratedAt(String dimension, int chunkX, int chunkZ, StructureEntry entry, boolean unique) {
            mapsByDimension.computeIfAbsent(dimension, ignored -> new StructureWorldMap())
                    .setGeneratedAt(chunkX, chunkZ, entry);
            if (unique) {
                generatedUniques.add(entry.name);
            }
        }

        private boolean removeGeneratedAt(String dimension, int chunkX, int chunkZ,
                                          StructureEntry expectedEntry, boolean unique) {
            StructureWorldMap worldMap = mapsByDimension.get(dimension);
            if (worldMap == null || !worldMap.removeGeneratedAt(chunkX, chunkZ, expectedEntry)) {
                return false;
            }

            if (unique) {
                generatedUniques.remove(expectedEntry.name);
            }
            if (worldMap.isEmpty()) {
                mapsByDimension.remove(dimension);
            }
            return true;
        }

        public void readFromNBT(StructureMap structureMap, CompoundTag tag) {
            mapsByDimension.clear();
            generatedUniques.clear();
            ListTag uniquesList = tag.getList("uniques", Constants.NBT.TAG_STRING);
            ListTag dimensionList = tag.getList("dimensions", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < dimensionList.size(); i++) {
                CompoundTag dimensionTag = dimensionList.getCompound(i);
                String dimension = dimensionTag.contains("dim_id", Constants.NBT.TAG_STRING)
                        ? dimensionTag.getString("dim_id")
                        : legacyDimensionKey(dimensionTag.getInt("dim"));
                StructureWorldMap worldMap = mapsByDimension.computeIfAbsent(dimension,
                        ignored -> new StructureWorldMap());
                worldMap.readFromNBT(structureMap, dimensionTag.getCompound("data"));
            }

            for (int i = 0; i < uniquesList.size(); i++) {
                generatedUniques.add(uniquesList.getString(i));
            }
        }

        private static String legacyDimensionKey(int dimension) {
            return switch (dimension) {
                case 0 -> "minecraft:overworld";
                case -1 -> "minecraft:the_nether";
                case 1 -> "minecraft:the_end";
                default -> "ancientwarfare:legacy_dimension_" + dimension;
            };
        }

        public void writeToNBT(CompoundTag tag) {
            ListTag dimensionsList = new ListTag();
            ListTag uniquesList = new ListTag();
            for (Map.Entry<String, StructureWorldMap> dimension : mapsByDimension.entrySet()) {
                CompoundTag dimensionTag = new CompoundTag();
                CompoundTag dimensionData = new CompoundTag();
                dimensionTag.putString("dim_id", dimension.getKey());
                dimension.getValue().writeToNBT(dimensionData);
                dimensionTag.put("data", dimensionData);
                dimensionsList.add(dimensionTag);
            }

            for (String name : generatedUniques) {
                uniquesList.add(StringTag.valueOf(name));
            }
            tag.put("dimensions", dimensionsList);
            tag.put("uniques", uniquesList);
        }
    }//end structure dimension map

    private static class StructureWorldMap {

        private HashMap<Integer, HashMap<Integer, StructureEntry>> worldMap = new HashMap<>();
        private int largestGeneratedX;
        private int largestGeneratedZ;

        public Collection<StructureEntry> getEntriesNear(int chunkX, int chunkZ, int chunkRadius, boolean expandBySize, Collection<StructureEntry> list) {
            StructureEntry entry;
            int crx = chunkRadius;
            int crz = chunkRadius;
            if (expandBySize) {
                crx += largestGeneratedX / 16;
                crz += largestGeneratedZ / 16;
            }
            for (int x = chunkX - crx; x <= chunkX + crx; x++) {
                if (worldMap.containsKey(x)) {
                    for (int z = chunkZ - crz; z <= chunkZ + crz; z++) {
                        entry = worldMap.get(x).get(z);
                        if (entry != null) {
                            list.add(entry);
                        }
                    }
                }
            }
            return list;
        }

        public void setGeneratedAt(int chunkX, int chunkZ, StructureEntry entry) {
            if (!this.worldMap.containsKey(chunkX)) {
                this.worldMap.put(chunkX, new HashMap<>());
            }
            this.worldMap.get(chunkX).put(chunkZ, entry);
            int x = entry.bb.getXSize();
            int z = entry.bb.getZSize();
            if (x > largestGeneratedX) {
                largestGeneratedX = x;
            }
            if (z > largestGeneratedZ) {
                largestGeneratedZ = z;
            }
        }

        public boolean removeGeneratedAt(int chunkX, int chunkZ, StructureEntry expectedEntry) {
            HashMap<Integer, StructureEntry> zEntries = worldMap.get(chunkX);
            if (zEntries == null || zEntries.get(chunkZ) != expectedEntry) {
                return false;
            }

            zEntries.remove(chunkZ);
            if (zEntries.isEmpty()) {
                worldMap.remove(chunkX);
            }
            recalculateLargestGeneratedSize();
            return true;
        }

        public boolean isEmpty() {
            return worldMap.isEmpty();
        }

        private void recalculateLargestGeneratedSize() {
            largestGeneratedX = 0;
            largestGeneratedZ = 0;
            for (HashMap<Integer, StructureEntry> zEntries : worldMap.values()) {
                for (StructureEntry entry : zEntries.values()) {
                    largestGeneratedX = Math.max(largestGeneratedX, entry.bb.getXSize());
                    largestGeneratedZ = Math.max(largestGeneratedZ, entry.bb.getZSize());
                }
            }
        }

        public Optional<StructureEntry> getEntryAt(int chunkX, int chunkZ) {
            if (!worldMap.containsKey(chunkX)) {
                return Optional.empty();
            }
            return Optional.ofNullable(worldMap.get(chunkX).get(chunkZ));
        }

        public void readFromNBT(StructureMap structureMap, CompoundTag nbttagcompound) {
            ListTag entryList = nbttagcompound.getList("entries", Constants.NBT.TAG_COMPOUND);
            StructureEntry entry;
            CompoundTag entryTag;
            int x;
            int z;
            for (int i = 0; i < entryList.size(); i++) {
                entryTag = entryList.getCompound(i);
                x = entryTag.getInt("x");
                z = entryTag.getInt("z");
                entry = new StructureEntry();
                entry.setStructureMap(structureMap);
                entry.readFromNBT(entryTag);
                if (!this.worldMap.containsKey(x)) {
                    this.worldMap.put(x, new HashMap<>());
                }
                this.worldMap.get(x).put(z, entry);
            }
            this.largestGeneratedX = nbttagcompound.getInt("largestX");
            this.largestGeneratedZ = nbttagcompound.getInt("largestZ");
        }

        public void writeToNBT(CompoundTag nbttagcompound) {
            ListTag entryList = new ListTag();
            CompoundTag entryTag;
            for (Map.Entry<Integer, HashMap<Integer, StructureEntry>> x : worldMap.entrySet()) {
                for (Map.Entry<Integer, StructureEntry> z : x.getValue().entrySet()) {
                    entryTag = new CompoundTag();
                    entryTag.putInt("x", x.getKey());
                    entryTag.putInt("z", z.getKey());
                    z.getValue().writeToNBT(entryTag);
                    entryList.add(entryTag);
                }
            }
            nbttagcompound.putInt("largestX", largestGeneratedX);
            nbttagcompound.putInt("largestZ", largestGeneratedZ);
            nbttagcompound.put("entries", entryList);
        }
    }//end structure X Map

}
