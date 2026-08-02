package net.shadowmage.ancientwarfare.structure.gamedata;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;

public class StructureEntry {
    public String name;
    private int value;
    public final StructureBB bb;
    private BlockPos protectionFlagPos = BlockPos.ZERO;
    private boolean hasProtectionFlag = false;
    private boolean isConquered = false;
    private boolean preventNaturalHostileSpawns = false;
    private int cx;
    private int cz;
    private StructureMap structureMap = null;

    public void setProtectionFlagPos(BlockPos protectionFlagPos) {
        this.protectionFlagPos = protectionFlagPos;
        hasProtectionFlag = true;
        markStructureMapDirty();
    }

    public void setStructureMap(StructureMap structureMap) {
        this.structureMap = structureMap;
    }

    public void setConquered() {
        isConquered = true;
        preventNaturalHostileSpawns = false;
        markStructureMapDirty();
    }

    private void markStructureMapDirty() {
        if (structureMap != null) {
            structureMap.markDirty();
        }
    }

    public StructureEntry(int x, int y, int z, Direction face, StructureTemplate template) {
        name = template.name;
        bb = new StructureBB(new BlockPos(x, y, z), face, template.getSize(), template.getOffset());
        cx = x >> 4;
        cz = z >> 4;
        value = template.getValidationSettings().getClusterValue();
        preventNaturalHostileSpawns = template.getValidationSettings().shouldPreventHostileSpawns();
    }

    public StructureEntry(StructureBB bb, String name, int value, int cx, int cz) {
        this.name = name;
        this.bb = bb;
        this.value = value;
        this.cx = cx;
        this.cz = cz;
    }

    public StructureEntry() {
        bb = new StructureBB(BlockPos.ZERO, BlockPos.ZERO);
    }//NBT constructor

    public void writeToNBT(CompoundTag tag) {
        tag.putString("name", name);
        tag.putInt("value", value);
        tag.putIntArray("bb", new int[]{bb.min.getX(), bb.min.getY(), bb.min.getZ(), bb.max.getX(), bb.max.getY(), bb.max.getZ()});
        tag.putLong("protectionFlagPos", protectionFlagPos.asLong());
        tag.putInt("cx", cx);
        tag.putInt("cz", cz);
        tag.putBoolean("preventHostile", preventNaturalHostileSpawns);
    }

    public void readFromNBT(CompoundTag tag) {
        name = tag.getString("name");
        value = tag.getInt("value");
        int[] datas = tag.getIntArray("bb");
        if (datas.length >= 6) {
            bb.min = new BlockPos(datas[0], datas[1], datas[2]);
            bb.max = new BlockPos(datas[3], datas[4], datas[5]);
        }
        protectionFlagPos = BlockPos.of(tag.getLong("protectionFlagPos"));
        cx = tag.getInt("cx");
        cz = tag.getInt("cz");
        preventNaturalHostileSpawns = tag.getBoolean("preventHostile");
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public StructureBB getBB() {
        return bb;
    }

    public BlockPos getProtectionFlagPos() {
        return protectionFlagPos;
    }

    public int getChunkZ() {
        return cz;
    }

    public int getChunkX() {
        return cx;
    }

    public boolean shouldPreventNaturalHostileSpawns() {
        return preventNaturalHostileSpawns;
    }

    public boolean hasProtectionFlag() {
        return hasProtectionFlag;
    }

    public boolean getConquered() {
        return isConquered;
    }
}
