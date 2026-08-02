package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.api.IStructureBuilder;
import net.shadowmage.ancientwarfare.structure.api.TemplateRuleBlock;

public class TemplateRuleTorqueMultiblock extends TemplateRuleBlock {
    public static final String PLUGIN_NAME = "awTorqueMulti";
    private CompoundTag tag;

    public TemplateRuleTorqueMultiblock(Level world, BlockPos pos, BlockState state, int turns) {
        super(state, turns);
        WorldTools.getTile(world, pos).ifPresent(t -> this.tag = t.saveWithFullMetadata());
    }

    public TemplateRuleTorqueMultiblock() {
        super();
    }

    @Override
    public boolean shouldReuseRule(Level world, BlockState state, int turns, BlockPos pos) {
        return false;
    }

    @Override
    public void handlePlacement(Level world, int turns, BlockPos pos, IStructureBuilder builder) {
        if (world.setBlock(pos, state, 3)) {
            WorldTools.getTile(world, pos).ifPresent(t -> {
                t.load(tag.copy());
                t.setChanged();
            });
            BlockTools.notifyBlockUpdate(world, pos);
            state.getBlock().setPlacedBy(world, pos, state, null, ItemStack.EMPTY);
        }
    }

    @Override
    public void parseRule(CompoundTag tag) {
        super.parseRule(tag);
        this.tag = tag.getCompound("teData");
    }

    @Override
    public void writeRuleData(CompoundTag tag) {
        super.writeRuleData(tag);
        tag.put("teData", this.tag);
    }

    @Override
    public boolean shouldPlaceOnBuildPass(Level world, int turns, BlockPos pos, int buildPass) {
        return buildPass == 0;
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }
}
