package net.shadowmage.ancientwarfare.structure.gamedata;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;

public class TownEntry {
    private StructureBB bb;
    private boolean preventNaturalHostileSpawns;
    private TownMap townMap;
    private boolean isConquered = false;

    public TownEntry(StructureBB bb, boolean preventNaturalHostileSpawns) {
        this.bb = bb;
        this.preventNaturalHostileSpawns = preventNaturalHostileSpawns;
    }

    public TownEntry setTownMap(TownMap townMap) {
        this.townMap = townMap;
        return this;
    }

    public void setConquered() {
        isConquered = true;
        preventNaturalHostileSpawns = false;
        if (townMap != null) {
            townMap.markDirty();
        }
    }

    public static TownEntry deserializeNBT(CompoundTag tag) {
        StructureBB bb = new StructureBB(BlockPos.ZERO, BlockPos.ZERO);
        bb.deserializeNBT(tag.getCompound("bb"));
        return new TownEntry(bb, tag.getBoolean("preventNaturalHostileSpawns"));
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("bb", bb.serializeNBT());
        tag.putBoolean("preventNaturalHostileSpawns", preventNaturalHostileSpawns);
        return tag;
    }

    public boolean shouldPreventNaturalHostileSpawns() {
        return preventNaturalHostileSpawns;
    }

    public StructureBB getBB() {
        return bb;
    }

    public boolean getConquered() {
        return isConquered;
    }
}
