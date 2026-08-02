package net.shadowmage.ancientwarfare.structure.template.datafixes.fixers;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.util.LegacyBlockState;
import net.shadowmage.ancientwarfare.core.util.NBTHelper;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.datafixes.FixResult;

import java.util.Map;

import static net.shadowmage.ancientwarfare.structure.api.TemplateRule.JSON_PREFIX;

public class BlockMetaToBlockStateFixer extends RuleDataFixerBase {
    private static final StructureTemplate.Version VERSION = new StructureTemplate.Version(2, 5);

    @SuppressWarnings("squid:S1192")
    private static final Map<String, String> ruleBlockNameMapping = new ImmutableMap.Builder<String, String>()
            .put("modBlockDefault", "blockName")
            .put("awTorqueMulti", "blockId")
            .put("rotatable", "blockId")
            .put("inventory", "blockName")
            .put("vanillaSign", "blockName")
            .put("vanillaSkull", "blockName")
            .put("blockTile", "blockName")
            .put("vanillaFlowerPot", "blockName")
            .put("doors", "blockName")
            .put("vanillaBlocks", "blockName")
            .build();

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    protected FixResult<String> fixData(String ruleName, String data) {
        CompoundTag tag;
        try {
            tag = TagParser.parseTag(data.substring(JSON_PREFIX.length()));
        } catch (CommandSyntaxException e) {
            AncientWarfareStructure.LOG.error("Error getting nbt from json string: ", e);
            return new FixResult.NotModified<>(data);
        }

        String blockName = tag.getString(ruleBlockNameMapping.get(ruleName));
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
        if (block == null) {
            AncientWarfareStructure.LOG.warn("block {} cannot be found in registry replacing with air", blockName);
            tag.put("blockState", NBTHelper.getBlockStateTag(Blocks.AIR.defaultBlockState()));
        } else {
            //noinspection deprecation
            tag.put("blockState", NBTHelper.getBlockStateTag(LegacyBlockState.fromMeta(block, tag.getInt("meta"))));
        }

        return new FixResult.Modified<>(JSON_PREFIX + tag.toString(), "BlockMetaToBlockStateFixer");
    }

    @Override
    public StructureTemplate.Version getVersion() {
        return VERSION;
    }

    @Override
    public boolean isForRule(String ruleName) {
        return ruleBlockNameMapping.containsKey(ruleName);
    }
}
