package net.shadowmage.ancientwarfare.npc.datafixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.CompoundTag;
import net.shadowmage.ancientwarfare.core.datafixes.ILegacyDataFixer;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;

import java.util.Map;
import java.util.Set;

import static net.shadowmage.ancientwarfare.core.entity.AWEntityRegistry.*;

public class FactionExpansionEntityFixer implements ILegacyDataFixer {
    private static final String FACTION_NAME_TAG = "factionName";
    private final Set<String> ids = ImmutableSet.of(
            AncientWarfareNPC.MOD_ID + ":" + NPC_FACTION_ARCHER,
            AncientWarfareNPC.MOD_ID + ":" + NPC_FACTION_ARCHER_ELITE,
            AncientWarfareNPC.MOD_ID + ":" + NPC_FACTION_BARD,
            AncientWarfareNPC.MOD_ID + ":" + NPC_FACTION_CAVALRY,
            AncientWarfareNPC.MOD_ID + ":" + NPC_FACTION_CIVILIAN_FEMALE,
            AncientWarfareNPC.MOD_ID + ":" + NPC_FACTION_CIVILIAN_MALE,
            AncientWarfareNPC.MOD_ID + ":" + NPC_FACTION_COMMANDER,
            AncientWarfareNPC.MOD_ID + ":" + NPC_FACTION_LEADER_ELITE,
            AncientWarfareNPC.MOD_ID + ":" + NPC_FACTION_MOUNTED_ARCHER,
            AncientWarfareNPC.MOD_ID + ":" + NPC_FACTION_PRIEST,
            AncientWarfareNPC.MOD_ID + ":" + NPC_FACTION_SOLDIER,
            AncientWarfareNPC.MOD_ID + ":" + NPC_FACTION_SOLDIER_ELITE,
            AncientWarfareNPC.MOD_ID + ":" + NPC_FACTION_TRADER
    );

    public static final Map<String, String> RENAMES = new ImmutableMap.Builder<String, String>()
            .put("custom_3", "empire")
            .put("viking", "norska")
            .put("desert", "sarkonid")
            .put("native", "kong")
            .put("bandit", "brigand")
            .build();

    @Override
    public int getFixVersion() {
        return 5;
    }

    @Override
    public CompoundTag fixTagCompound(CompoundTag compound) {
        String id = compound.getString("id");

        if (ids.contains(id) && compound.contains(FACTION_NAME_TAG) && RENAMES.containsKey(compound.getString(FACTION_NAME_TAG))) {
            compound.putString(FACTION_NAME_TAG, RENAMES.get(compound.getString(FACTION_NAME_TAG)));
        }
        return compound;
    }
}
