package net.shadowmage.ancientwarfare.structure.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.NBTHelper;
import net.shadowmage.ancientwarfare.structure.config.AWStructureStatics;
import net.shadowmage.ancientwarfare.structure.registry.TerritorySettingRegistry;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Territory implements INBTSerializable<CompoundTag> {
    private String territoryId;
    private String territoryName;
    private int totalClusterValue = 0;
    private Set<Long> chunkPositions = new HashSet<>();
    private BlockPos territoryCenter = BlockPos.ZERO;
    private Set<BlockPos> chunkCenters = new HashSet<>();

    Territory() {
    }

    Territory(String territoryId, String territoryName) {
        this.territoryId = territoryId;
        this.territoryName = territoryName;
    }

    String getTerritoryId() {
        return territoryId;
    }

    public void addClusterValue(int value) {
        totalClusterValue = Math.max(0, totalClusterValue + value);
    }

    public void removeClusterValue(int value) {
        totalClusterValue = Math.max(0, totalClusterValue - Math.max(0, value));
    }

    void addChunk(long chunkPos) {
        chunkPositions.add(chunkPos);
        chunkCenters.add(getChunkCenter(chunkPos));
        territoryCenter = TerritoryManager.getTerritoryCenter(chunkCenters);
    }

    private BlockPos getChunkCenter(long chunkPos) {
        return TerritoryManager.getChunkCenterPos((int) (chunkPos & 4294967295L), (int) (chunkPos >> 32 & 4294967295L));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Territory territory = (Territory) o;
        return territoryId.equals(territory.territoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(territoryId);
    }

    public String getTerritoryName() {
        return territoryName;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag ret = new CompoundTag();
        ret.putString("territoryId", territoryId);
        ret.putString("territoryName", territoryName);
        ret.putInt("totalClusterValue", totalClusterValue);
        ret.put("chunkPositions", NBTHelper.getTagList(chunkPositions, LongTag::valueOf));
        return ret;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        territoryId = nbt.getString("territoryId");
        territoryName = nbt.getString("territoryName");
        totalClusterValue = nbt.getInt("totalClusterValue");
        chunkPositions = NBTHelper.getSet(nbt.getList("chunkPositions", Constants.NBT.TAG_LONG), n -> ((LongTag) n).getAsLong());
        chunkCenters.clear();
        chunkPositions.forEach(cp -> chunkCenters.add(getChunkCenter(cp)));
        territoryCenter = TerritoryManager.getTerritoryCenter(chunkCenters);
    }

    Set<Long> getChunkPositions() {
        return chunkPositions;
    }

    public BlockPos getTerritoryCenter() {
        return territoryCenter;
    }

    public int getRemainingClusterValue() {
        return (int) (TerritorySettingRegistry.getTerritorySettings(territoryName).getPerChunkClusterValueMultiplier() * AWStructureStatics.chunkClusterValue * chunkPositions.size()) - totalClusterValue;
    }
}
