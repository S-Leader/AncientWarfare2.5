package net.shadowmage.ancientwarfare.structure.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.shadowmage.ancientwarfare.core.util.BlockTools;

public class ItemStructureSettings {
    private static final String STRUCT_DATA_TAG = "structData";
    private static final String BUILD_KEY_TAG = "buildKey";
    private BlockPos pos1 = BlockPos.ZERO;
    private BlockPos pos2 = BlockPos.ZERO;
    BlockPos key = BlockPos.ZERO;
    Direction buildFace;
    String name = "";

    private ItemStructureSettings() {

    }

    /*
     * @param stack to extract the info from
     */
    public static ItemStructureSettings getSettingsFor(ItemStack stack) {
        ItemStructureSettings settings = new ItemStructureSettings();
        CompoundTag tag;
        //noinspection ConstantConditions
        if (stack.hasTag() && stack.getTag().contains(STRUCT_DATA_TAG)) {
            tag = stack.getTag().getCompound(STRUCT_DATA_TAG);
        } else {
            tag = new CompoundTag();
        }
        if (tag.contains("pos1")) {
            settings.pos1 = BlockPos.of(tag.getLong("pos1"));
        }
        if (tag.contains("pos2")) {
            settings.pos2 = BlockPos.of(tag.getLong("pos2"));
        }
        if (tag.contains(BUILD_KEY_TAG)) {
            settings.key = BlockPos.of(tag.getCompound(BUILD_KEY_TAG).getLong("key"));
            settings.buildFace = Direction.values()[tag.getCompound(BUILD_KEY_TAG).getByte("face")];
        }
        if (tag.contains("name")) {
            settings.name = tag.getString("name");
        }

        return settings;
    }

    public static void setSettingsFor(ItemStack item, ItemStructureSettings settings) {
        CompoundTag tag = new CompoundTag();
        if (settings.hasPos1()) {
            tag.putLong("pos1", settings.getPos1().asLong());
        }
        if (settings.hasPos2()) {
            tag.putLong("pos2", settings.getPos2().asLong());
        }
        if (settings.hasBuildKey()) {
            CompoundTag buildKeyTag = new CompoundTag();
            buildKeyTag.putByte("face", (byte) settings.buildFace.ordinal());
            buildKeyTag.putLong("key", settings.key.asLong());
            tag.put(BUILD_KEY_TAG, buildKeyTag);
        }
        if (settings.hasName()) {
            tag.putString("name", settings.name);
        }
        item.getOrCreateTag().put(STRUCT_DATA_TAG, tag);
    }

    public void setPos1(BlockPos pos) {
        pos1 = pos;
    }

    public void setPos2(BlockPos pos) {
        pos2 = pos;
    }

    public void setBuildKey(BlockPos pos, Direction face) {
        key = pos;
        buildFace = face;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasPos1() {
        return pos1 != BlockPos.ZERO;
    }

    public boolean hasPos2() {
        return pos2 != BlockPos.ZERO;
    }

    public boolean hasBuildKey() {
        return key != BlockPos.ZERO;
    }

    public boolean hasName() {
        return !name.isEmpty();
    }

    public BlockPos buildKey() {
        return key;
    }

    public Direction face() {
        return buildFace;
    }

    public String name() {
        return name;
    }

    void clearSettings() {
        pos1 = BlockPos.ZERO;
        pos2 = BlockPos.ZERO;
        key = BlockPos.ZERO;
        name = "";
    }

    public BlockPos getPos1() {
        return pos1;
    }

    public BlockPos getPos2() {
        return pos2;
    }

    public BlockPos getMin() {
        return BlockTools.getMin(pos1, pos2);
    }

    public BlockPos getMax() {
        return BlockTools.getMax(pos1, pos2);
    }

    public AABB getBoundingBox() {
        return new AABB(getMin(), getMax());
    }
}
