package net.shadowmage.ancientwarfare.structure.api;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public abstract class TemplateRuleEntityBase extends TemplateRule {

    private BlockPos pos;

    /*
     * Called by reflection
     */
    public TemplateRuleEntityBase() {
    }

    @Override
    public void writeRuleData(CompoundTag tag) {
        tag.putLong("position", pos.asLong());
    }

    @Override
    public void parseRule(CompoundTag tag) {
        pos = BlockPos.of(tag.getLong("position"));
    }

    @Override
    protected String getRuleType() {
        return "entity";
    }

    public final void setPosition(BlockPos pos) {
        this.pos = pos;
    }

    public final BlockPos getPosition() {
        return pos;
    }

    @Override
    public ItemStack getRemainingStack() {
        return ItemStack.EMPTY;
    }
}
