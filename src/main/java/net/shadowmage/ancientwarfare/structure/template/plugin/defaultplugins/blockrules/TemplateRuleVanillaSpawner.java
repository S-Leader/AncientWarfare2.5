package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.util.Constants;

import java.util.stream.StreamSupport;

import static net.shadowmage.ancientwarfare.npc.event.EventHandler.NO_SPAWN_PREVENTION_TAG;

public class TemplateRuleVanillaSpawner extends TemplateRuleBlockTile<SpawnerBlockEntity> {
    public static final String PLUGIN_NAME = "vanillaSpawner";
    private static final String SPAWN_DATA_TAG = "SpawnData";
    private static final String TAGS_TAG = "Tags";

    public TemplateRuleVanillaSpawner(Level world, BlockPos pos, BlockState state, int turns) {
        super(world, pos, state, turns);
        if (tag.contains(SPAWN_DATA_TAG)) {
            CompoundTag compound = tag.getCompound(SPAWN_DATA_TAG);
            compound.put(TAGS_TAG, getTags(compound));
        }
        if (tag.contains("SpawnPotentials")) {
            ListTag spawnPotentials = tag.getList("SpawnPotentials", Constants.NBT.TAG_COMPOUND);
            for (Tag tag : spawnPotentials) {
                CompoundTag compound = (CompoundTag) tag;
                compound.put(TAGS_TAG, getTags(compound));
            }
        }
    }

    private ListTag getTags(CompoundTag compound) {
        ListTag tags = compound.contains(TAGS_TAG) ? compound.getList(TAGS_TAG, Constants.NBT.TAG_STRING) : new ListTag();
        if (!hasNoSpawnPreventionTag(tags)) {
            tags.add(StringTag.valueOf(NO_SPAWN_PREVENTION_TAG));
        }
        return tags;
    }

    private boolean hasNoSpawnPreventionTag(ListTag tags) {
        return StreamSupport.stream(tags.spliterator(), false).anyMatch(n -> ((StringTag) n).getAsString().equals(NO_SPAWN_PREVENTION_TAG));
    }

    public TemplateRuleVanillaSpawner() {
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }
}
