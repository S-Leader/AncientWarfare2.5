package net.shadowmage.ancientwarfare.structure.datafixes;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.shadowmage.ancientwarfare.core.datafixes.ILegacyDataFixer;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.datafixes.fixers.TileRuleDataFixer;

public class LootSettingsPotionRegistryNameFixer extends TileRuleDataFixer implements ILegacyDataFixer {
    private static final ImmutableSet LOOT_TILES = ImmutableSet.of(
            "ancientwarfarestructure:loot_basket",
            "ancientwarfarestructure:advanced_loot_chest_tile",
            "ancientwarfarestructure:coffin",
            "ancientwarfarestructure:urn_tile"
    );

    @Override
    public int getFixVersion() {
        return 10;
    }

    @Override
    public CompoundTag fixTagCompound(CompoundTag compound) {
        String id = compound.getString("id");

        if (LOOT_TILES.contains(id)) {
            CompoundTag lootNbt = compound.getCompound("lootSettings");
            ListTag effectList = lootNbt.getList("effects", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < effectList.size(); i++) {
                CompoundTag effectNbt = effectList.getCompound(i);
                int effectId = effectNbt.getByte("Id") & 0xFF;
                MobEffect effect = BuiltInRegistries.MOB_EFFECT.byId(effectId);
                ResourceLocation effectIdName = effect == null ? null : BuiltInRegistries.MOB_EFFECT.getKey(effect);
                if (effectIdName != null) {
                    effectNbt.putString("RegistryName", effectIdName.toString());
                }
                effectNbt.remove("Id");
            }
        }

        return compound;
    }

    @Override
    public CompoundTag fixRuleCompoundTag(CompoundTag compound) {
        return fixTagCompound(compound);
    }

    private static final StructureTemplate.Version VERSION = new StructureTemplate.Version(2, 10);

    @Override
    public StructureTemplate.Version getVersion() {
        return VERSION;
    }

    @Override
    public boolean isForRule(String ruleName) {
        return ruleName.equals("blockTile") || ruleName.equals("inventory") || ruleName.equals("coffin");
    }

    @Override
    protected String getFixerName() {
        return "LootSettingsPotionRegistryNameFixer";
    }
}
