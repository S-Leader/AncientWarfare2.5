package net.shadowmage.ancientwarfare.structure.template.datafixes.fixers;

import com.google.common.collect.ImmutableSet;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.datafixes.FixResult;

import java.util.Set;

import static net.shadowmage.ancientwarfare.structure.api.TemplateRule.JSON_PREFIX;

public class EntityEquipmentFixer extends RuleDataFixerBase {
    private static final StructureTemplate.Version VERSION = new StructureTemplate.Version(2, 8);

    private static final Set<String> APPLICABLE_TO_RULES = ImmutableSet.of("AWNpc", "vanillaLogicEntity");

    @Override
    public StructureTemplate.Version getVersion() {
        return VERSION;
    }

    @Override
    public boolean isForRule(String ruleName) {
        return APPLICABLE_TO_RULES.contains(ruleName);
    }

    @Override
    protected FixResult<String> fixData(String ruleName, String data) {
        CompoundTag tag;
        try {
            tag = TagParser.parseTag(data.substring(JSON_PREFIX.length()));
        } catch (CommandSyntaxException e) {
            AncientWarfareStructure.LOG.error("Error getting nbt from json string: ", e);
            return new FixResult.NotModified<>(data);
        }

        if (!tag.contains("equipmentData")) {
            return new FixResult.NotModified<>(data);
        }

        ListTag equipmentList = tag.getCompound("equipmentData").getList("equipmentContents", Constants.NBT.TAG_COMPOUND);

        ListTag handItems = new ListTag();
        ListTag armorItems = new ListTag();
        for (int i = 0; i < equipmentList.size(); i++) {
            CompoundTag stack = equipmentList.getCompound(i);
            stack.remove("slot");
            if (i < 2) {
                handItems.add(stack);
            } else {
                armorItems.add(stack);
            }
        }
        CompoundTag entityData = tag.getCompound("entityData");
        entityData.put("HandItems", handItems);
        entityData.put("ArmorItems", armorItems);
        tag.put("entityData", entityData);

        return new FixResult.Modified<>(JSON_PREFIX + tag.toString(), "EntityEquipmentFixer");
    }
}
