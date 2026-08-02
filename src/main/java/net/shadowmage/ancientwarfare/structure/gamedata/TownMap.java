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
    private static final Cache<Zone, Set<TownEntry>> CHUNK_TOWN_ENTRIES = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();

    private Set<TownEntry> townEntries = new HashSet<>();

    public TownMap(String name) {
        super(name);
    }

    public void setGenerated(TownEntry townEntry) {
        townEntry.setTownMap(this);
        townEntries.add(townEntry);
        markDirty();
    }

    public boolean shouldPreventSpawnAtPos(Level world, BlockPos pos) {
        for (TownEntry entry : getTownsInChunk(pos)) {
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

    private Set<TownEntry> getTownsInChunk(BlockPos pos) {
        Set<TownEntry> towns;
        ChunkPos chunkPos = new ChunkPos(pos);
        BlockPos min = new BlockPos(chunkPos.x * 16, 1, chunkPos.z * 16);
        BlockPos max = new BlockPos(chunkPos.x * 16 + 15, 255, chunkPos.z * 16 + 15);
        Zone chunkZone = new Zone(min, max);
        try {
            towns = CHUNK_TOWN_ENTRIES.get(chunkZone, () -> getTownsIn(chunkZone));
        } catch (ExecutionException e) {
            AncientWarfareNPC.LOG.error("Error getting structure entries in chunk for hostile entity check: ", e);
            return new HashSet<>();
        }
        return towns;
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
