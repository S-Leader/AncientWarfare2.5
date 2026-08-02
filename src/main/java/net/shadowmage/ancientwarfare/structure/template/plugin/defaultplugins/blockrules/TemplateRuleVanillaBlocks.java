package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.structure.api.IStructureBuilder;
import net.shadowmage.ancientwarfare.structure.api.TemplateRuleBlock;
import net.shadowmage.ancientwarfare.structure.registry.StructureBlockRegistry;

public class TemplateRuleVanillaBlocks extends TemplateRuleBlock {

    public static final String PLUGIN_NAME = "vanillaBlocks";
    protected int buildPass;

    /*
     * constructor for dynamic construction.  passed world and coords so that the rule can handle its own logic internally
     */
    public TemplateRuleVanillaBlocks(Level world, BlockPos pos, BlockState state, int turns) {
        super(state, turns);
        this.buildPass = StructureBlockRegistry.getBuildPass(state);
    }

    public TemplateRuleVanillaBlocks() {
        super();
    }

    @Override
    public void handlePlacement(Level world, int turns, BlockPos pos, IStructureBuilder builder) {
        builder.placeBlock(pos, BlockTools.rotateFacing(state, turns), buildPass);
    }

    @Override
    public boolean shouldReuseRule(Level world, BlockState state, int turns, BlockPos pos) {
        return state.getBlock() == this.state.getBlock() && BlockTools.rotateFacing(state, turns).getValues().equals(this.state.getValues());
    }

    @Override
    public boolean shouldPlaceOnBuildPass(Level world, int turns, BlockPos pos, int buildPass) {
        return buildPass == this.buildPass;
    }

    @Override
    public void parseRule(CompoundTag tag) {
        super.parseRule(tag);
        this.buildPass = StructureBlockRegistry.getBuildPass(state);
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }
}
