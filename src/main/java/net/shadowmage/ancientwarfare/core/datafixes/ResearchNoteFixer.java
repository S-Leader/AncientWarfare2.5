package net.shadowmage.ancientwarfare.core.datafixes;

import net.minecraft.nbt.CompoundTag;

public class ResearchNoteFixer implements ILegacyDataFixer {
    private static final String RESEARCH_PREFIX = "research.";
    private static final String RESEARCH_NAME_TAG = "researchName";

    @Override
    public int getFixVersion() {
        return 4;
    }

    @Override
    public CompoundTag fixTagCompound(CompoundTag compound) {
        String id = compound.getString("id");
        if (id.equals("ancientwarfare:research_note")) {
            CompoundTag tag = compound.getCompound("tag");
            if (tag.getString(RESEARCH_NAME_TAG).startsWith(RESEARCH_PREFIX)) {
                tag.putString(RESEARCH_NAME_TAG, tag.getString(RESEARCH_NAME_TAG).substring(RESEARCH_PREFIX.length()));
            }
        }
        return compound;
    }
}
