package net.shadowmage.ancientwarfare.structure.datafixes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.shadowmage.ancientwarfare.core.datafixes.ILegacyDataFixer;
import net.shadowmage.ancientwarfare.structure.block.BlockWoodenCoffin;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.datafixes.FixResult;
import net.shadowmage.ancientwarfare.structure.template.datafixes.fixers.TileRuleDataFixer;
import net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules.TemplateRuleCoffin;

public class WoodenCoffinFixer extends TileRuleDataFixer implements ILegacyDataFixer {

    private static final String OLD_COFFIN_REG_NAME = "ancientwarfarestructure:coffin";

    public WoodenCoffinFixer() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void missingMappings(MissingMappingsEvent event) {
        for (MissingMappingsEvent.Mapping<Block> entry :
                event.getMappings(ForgeRegistries.Keys.BLOCKS, "ancientwarfarestructure")) {
            if (entry.getKey().getPath().equals("coffin")) {
                entry.remap(AWStructureBlocks.WOODEN_COFFIN);
            }
        }
        for (MissingMappingsEvent.Mapping<Item> entry :
                event.getMappings(ForgeRegistries.Keys.ITEMS, "ancientwarfarestructure")) {
            if (entry.getKey().getPath().equals("coffin")) {
                entry.ignore();
            }
        }
    }

    @Override
    public int getFixVersion() {
        return 11;
    }

    @Override
    public CompoundTag fixTagCompound(CompoundTag compound) {
        String oldId = compound.getString("id");
        if (oldId.equals(OLD_COFFIN_REG_NAME)) {
            compound.putString("id", "ancientwarfarestructure:wooden_coffin");
            compound.putString("variant", mapToNewVariant(compound.getInt("variant")));
        }
        return compound;
    }

    @Override
    public CompoundTag fixRuleCompoundTag(CompoundTag compound) {
        return fixTagCompound(compound);
    }

    @Override
    protected String getFixerName() {
        return "WoodenCoffinFixer";
    }

    @Override
    protected FixResult<String> fixJSONData(String data, CompoundTag tag) {
        if (tag.contains("blockState")) {
            CompoundTag blockStateTag = tag.getCompound("blockState");
            if (blockStateTag.getString("blockName").equals(OLD_COFFIN_REG_NAME)) {
                //noinspection ConstantConditions
                blockStateTag.putString("blockName", ForgeRegistries.BLOCKS.getKey(AWStructureBlocks.WOODEN_COFFIN).toString());
            }
        }
        return super.fixJSONData(data, tag);
    }

    private String mapToNewVariant(int variant) {
        switch (variant) {
            case 2:
                return BlockWoodenCoffin.Variant.BIRCH.getName();
            case 3:
                return BlockWoodenCoffin.Variant.SPRUCE.getName();
            case 4:
                return BlockWoodenCoffin.Variant.JUNGLE.getName();
            case 5:
                return BlockWoodenCoffin.Variant.ACACIA.getName();
            case 6:
                return BlockWoodenCoffin.Variant.DARK_OAK.getName();
            case 1:
            default:
                return BlockWoodenCoffin.Variant.OAK.getName();
        }
    }

    private static final StructureTemplate.Version VERSION = new StructureTemplate.Version(2, 11);

    @Override
    public StructureTemplate.Version getVersion() {
        return VERSION;
    }

    @Override
    public boolean isForRule(String ruleName) {
        return ruleName.equals(TemplateRuleCoffin.PLUGIN_NAME);
    }
}
