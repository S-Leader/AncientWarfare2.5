package net.shadowmage.ancientwarfare.npc.datafixes;

import net.minecraft.nbt.CompoundTag;
import net.shadowmage.ancientwarfare.core.datafixes.ILegacyDataFixer;
import net.shadowmage.ancientwarfare.core.util.RegistryTools;
import net.shadowmage.ancientwarfare.npc.init.AWNPCItems;

import static net.shadowmage.ancientwarfare.npc.datafixes.FactionExpansionEntityFixer.RENAMES;

public class FactionExpansionItemFixer implements ILegacyDataFixer {
    private static final String FACTION_TAG = "faction";

    @Override
    public int getFixVersion() {
        return 5;
    }

    @Override
    public CompoundTag fixTagCompound(CompoundTag compound) {
        String id = compound.getString("id");

        //noinspection ConstantConditions
        if (id.equals(RegistryTools.getRegistryName(AWNPCItems.NPC_SPAWNER).toString())) {
            CompoundTag tag = compound.getCompound("tag");
            if (tag.contains(FACTION_TAG) && RENAMES.containsKey(tag.getString(FACTION_TAG))) {
                tag.putString(FACTION_TAG, RENAMES.get(tag.getString(FACTION_TAG)));
            }
        }

        return compound;
    }
}
