package net.shadowmage.ancientwarfare.npc.datafixes;

import net.minecraft.nbt.CompoundTag;
import net.shadowmage.ancientwarfare.core.datafixes.ILegacyDataFixer;

public class FoodBundleDataFixer implements ILegacyDataFixer {
    @Override
    public int getFixVersion() {
        return 7;
    }

    private static final String COMPONENT_NAME = "ancientwarfare:component";

    @Override
    public CompoundTag fixTagCompound(CompoundTag compound) {
        if (COMPONENT_NAME.equals(compound.getString("id")) && compound.getShort("Damage") == 100) {
            compound.putString("id", "ancientwarfarenpc:food_bundle");
            compound.putShort("Damage", (short) 0);
        }
        return compound;
    }
}
