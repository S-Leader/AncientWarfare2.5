package net.shadowmage.ancientwarfare.core.datafixes;

import net.minecraft.nbt.CompoundTag;

public class VehicleOwnerFixer implements ILegacyDataFixer {
    @Override
    public int getFixVersion() {
        return 1;
    }

    @Override
    public CompoundTag fixTagCompound(CompoundTag compound) {
        if (compound.getString("id").equals("ancientwarfarevehicle:vehicle")) {
            compound.putLong("ownerIdLeast", compound.getLong("ownerUuidLeast"));
            compound.putLong("ownerIdMost", compound.getLong("ownerUuidMost"));
        }
        return compound;
    }
}
