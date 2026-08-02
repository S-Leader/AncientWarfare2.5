package net.shadowmage.ancientwarfare.structure.datafixes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.shadowmage.ancientwarfare.core.datafixes.ILegacyDataFixer;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.datafixes.fixers.TileRuleDataFixer;
import net.shadowmage.ancientwarfare.structure.tile.LootSettings;

public class TileLootFixer extends TileRuleDataFixer implements ILegacyDataFixer {
    @Override
    public int getFixVersion() {
        return 8;
    }

    @Override
    public CompoundTag fixTagCompound(CompoundTag compound) {
        String id = compound.getString("id");

        if (id.equals("ancientwarfarestructure:loot_basket") || id.equals("ancientwarfarestructure:advanced_loot_chest_tile")) {
            String lootTableName = compound.getString("LootTable");
            int lootRolls = compound.getInt("lootRolls");

            LootSettings lootSettings = new LootSettings();
            lootSettings.setHasLoot(true);
            lootSettings.setLootRolls(lootRolls);
            lootSettings.setLootTableName(new ResourceLocation(lootTableName));

            compound.put("lootSettings", lootSettings.serializeNBT());
        }
        return compound;
    }

    @Override
    public CompoundTag fixRuleCompoundTag(CompoundTag compound) {
        return fixTagCompound(compound);
    }

    private static final StructureTemplate.Version VERSION = new StructureTemplate.Version(2, 9);

    @Override
    public StructureTemplate.Version getVersion() {
        return VERSION;
    }

    @Override
    public boolean isForRule(String ruleName) {
        return ruleName.equals("blockTile");
    }

    @Override
    protected String getFixerName() {
        return "TileLootFixer";
    }
}
