package net.shadowmage.ancientwarfare.structure.template.datafixes.fixers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.template.datafixes.FixResult;

import static net.shadowmage.ancientwarfare.structure.api.TemplateRule.JSON_PREFIX;

public abstract class TileRuleDataFixer extends RuleDataFixerBase {
    private static final String TE_DATA_TAG = "teData";

    @Override
    protected FixResult<String> fixData(String ruleName, String data) {
        CompoundTag tag;
        try {
            tag = TagParser.parseTag(data.substring(JSON_PREFIX.length()));
        } catch (CommandSyntaxException e) {
            AncientWarfareStructure.LOG.error("Error getting nbt from json string: ", e);
            return new FixResult.NotModified<>(data);
        }

        return fixJSONData(data, tag);
    }

    protected FixResult<String> fixJSONData(String data, CompoundTag tag) {
        if (tag.contains(TE_DATA_TAG)) {
            tag.put(TE_DATA_TAG, fixRuleCompoundTag(tag.getCompound(TE_DATA_TAG)));
            return new FixResult.Modified<>(JSON_PREFIX + tag.toString(), getFixerName());
        }
        return new FixResult.NotModified<>(data);
    }

    protected abstract String getFixerName();

    protected abstract CompoundTag fixRuleCompoundTag(CompoundTag compoundTag);
}
