package net.shadowmage.ancientwarfare.structure.gamedata;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.compat.WorldSavedData;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.Trig;
import net.shadowmage.ancientwarfare.core.util.Zone;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;
import net.shadowmage.ancientwarfare.structure.util.ConquerHelper;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

// Persistent per-level data is supplied through the modern SavedData-backed AWGameData service.
public class TownMap extends WorldSavedData {
    private static final Cache<TownChunkCacheKey, Set<TownEntry>> CHUNK_TOWN_ENTRIES = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    private Set<TownEntry> townEntries = new HashSet<>();

    public TownMap(String name) {
        super(name);
    }

    public void setGenerated(TownEntry townEntry) {
        townEntry.setTownMap(this);
        townEntries.add(townEntry);
        CHUNK_TOWN_ENTRIES.invalidateAll();
        markDirty();
    }

    /** Removes a provisional town reservation after a failed persistent build. */
    public boolean removeGenerated(StructureBB expected) {
        boolean removed = townEntries.removeIf(entry -> sameBounds(entry.getBB(), expected));
        if (removed) {
            CHUNK_TOWN_ENTRIES.invalidateAll();
            markDirty();
        }
        return removed;
    }

    private static boolean sameBounds(StructureBB a, StructureBB b) {
        return a.min.equals(b.min) && a.max.equals(b.max);
    }

    public boolean shouldPreventSpawnAtPos(Level world, BlockPos pos) {
        for (TownEntry entry : getTownsInChunk(world, pos)) {
            if (entry.getBB().contains(pos) && entry.shouldPreventNaturalHostileSpawns() && !entry.getConquered()) {
                if (ConquerHelper.checkBBNotConquered(world, entry.getBB())) {
                    return true;
                } else {
                    entry.setConquered();
                }
            }
        }
        return false;
    }

    private Set<TownEntry> getTownsInChunk(Level world, BlockPos pos) {
        Set<TownEntry> towns;
        ChunkPos chunkPos = new ChunkPos(pos);
        BlockPos min = new BlockPos(chunkPos.x * 16, world.getMinBuildHeight(), chunkPos.z * 16);
        BlockPos max = new BlockPos(chunkPos.x * 16 + 15, world.getMaxBuildHeight() - 1, chunkPos.z * 16 + 15);
        Zone chunkZone = new Zone(min, max);
        try {
            TownChunkCacheKey cacheKey = new TownChunkCacheKey(this, chunkPos.x, chunkPos.z);
            towns = CHUNK_TOWN_ENTRIES.get(cacheKey, () -> getTownsIn(chunkZone));
        } catch (ExecutionException e) {
            AncientWarfareNPC.LOG.error("Error getting structure entries in chunk for hostile entity check: ", e);
            return new HashSet<>();
        }
        return towns;
    }

    private static final class TownChunkCacheKey {
        private final TownMap townMap;
        private final int chunkX;
        private final int chunkZ;

        private TownChunkCacheKey(TownMap townMap, int chunkX, int chunkZ) {
            this.townMap = townMap;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            return obj instanceof TownChunkCacheKey other
                    && townMap == other.townMap
                    && chunkX == other.chunkX
                    && chunkZ == other.chunkZ;
        }

        @Override
        public int hashCode() {
            return 31 * (31 * System.identityHashCode(townMap) + chunkX) + chunkZ;
        }
    }

    private Set<TownEntry> getTownsIn(Zone zone) {
        Set<TownEntry> ret = new HashSet<>();
        for (TownEntry townEntry : townEntries) {
            if (townEntry.getBB().intersects(zone)) {
                ret.add(townEntry);
            }
        }
        return ret;
    }

    /*
     * return the distance of the closest found town or defaultVal if no town was found closer
     */
    public float getClosestTown(int bx, int bz, float defaultVal) {
        float distance = defaultVal;
        for (TownEntry townEntry : townEntries) {
            StructureBB bb = townEntry.getBB();
            float d = Trig.getDistance(bx, 0, bz, bb.getCenterX(), 0, bb.getCenterZ());
            if (d < distance) {
                distance = d;
            }
        }
        return distance;
    }

    public boolean intersectsWithTown(StructureBB bb) {
        for (TownEntry townEntry : townEntries) {
            if (townEntry.getBB().intersects(bb)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        CHUNK_TOWN_ENTRIES.invalidateAll();
        townEntries.clear();
        legacyDeserialization(tag);

        ListTag list = tag.getList("townEntries", Constants.NBT.TAG_COMPOUND);
        for (Tag nbt : list) {
            townEntries.add(TownEntry.deserializeNBT((CompoundTag) nbt).setTownMap(this));
        }
    }

    // Kept as an explicit one-way migration for worlds saved with the pre-TownEntry format.
    private void legacyDeserialization(CompoundTag tag) {
        ListTag list = tag.getList("boundingBoxes", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            StructureBB bb = new StructureBB(BlockPos.ZERO, BlockPos.ZERO);
            bb.deserializeNBT(list.getCompound(i));
            townEntries.add(new TownEntry(bb, false).setTownMap(this));
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        ListTag list = new ListTag();
        for (TownEntry townEntry : townEntries) {
            list.add(townEntry.serializeNBT());
        }
        tag.put("townEntries", list);
        return tag;
    }
}
