package net.shadowmage.ancientwarfare.structure.worldgen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.NBTHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TerritoryData implements ITerritoryData {
    private Map<String, Territory> territories = new HashMap<>();
    private Map<Long, Territory> chunkOwnership = new HashMap<>();
    private Map<String, Integer> territoryIds = new HashMap<>();

    @Override
    public int getNextTerritoryId(String territoryName) {
        int id = territoryIds.getOrDefault(territoryName, 0);
        territoryIds.put(territoryName, ++id);
        return id;
    }

    @Override
    public Optional<Territory> getTerritory(long chunkPosValue) {
        return Optional.ofNullable(chunkOwnership.get(chunkPosValue));
    }

    @Override
    public boolean isOwned(long chunkPos) {
        return chunkOwnership.containsKey(chunkPos);
    }

    @Override
    public void setOwned(long chunkPos, String territoryId, String territoryName) {
        Territory territory = territories.getOrDefault(territoryId, new Territory(territoryId, territoryName));
        territory.addChunk(chunkPos);
        territories.put(territoryId, territory);
        chunkOwnership.put(chunkPos, territory);
    }

    @Override
    public boolean isDifferentTerritory(String territoryId, int x, int z) {
        long chunkPosValue = ChunkPos.asLong(x, z);
        return !chunkOwnership.containsKey(chunkPosValue) || !chunkOwnership.get(chunkPosValue).getTerritoryId().equals(territoryId);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag ret = new CompoundTag();
        ret.put("territories", NBTHelper.getTagList(territories.values(), Territory::serializeNBT));
        ret.put("territoryIds", NBTHelper.mapToCompoundList(territoryIds, (t, k) -> t.putString("territoryName", k), (t, v) -> t.putInt("id", v)));
        return ret;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        territories.clear();
        territoryIds.clear();
        chunkOwnership.clear();

        Set<Territory> territorySet = NBTHelper.getSet(nbt.getList("territories", Constants.NBT.TAG_COMPOUND), n -> {
            Territory territory = new Territory();
            territory.deserializeNBT((CompoundTag) n);
            return territory;
        });

        for (Territory t : territorySet) {
            territories.put(t.getTerritoryId(), t);
            for (long chunkPos : t.getChunkPositions()) {
                chunkOwnership.put(chunkPos, t);
            }
        }
        territoryIds = NBTHelper.getMap(nbt.getList("territoryIds", Constants.NBT.TAG_COMPOUND), n -> n.getString("territoryName"), n -> n.getInt("id"));
    }
}
