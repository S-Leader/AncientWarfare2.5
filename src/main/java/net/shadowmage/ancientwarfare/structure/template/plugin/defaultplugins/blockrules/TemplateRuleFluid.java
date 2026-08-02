package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TemplateRuleFluid extends TemplateRuleVanillaBlocks {
    public static final String PLUGIN_NAME = "fluid";

    public TemplateRuleFluid(Level world, BlockPos pos, BlockState state, int turns) {
        super(world, pos, state, turns);
    }

    public TemplateRuleFluid() {
        super();
    }

    @Override
    public boolean placeInSurvival() {
        return true;
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }
}
