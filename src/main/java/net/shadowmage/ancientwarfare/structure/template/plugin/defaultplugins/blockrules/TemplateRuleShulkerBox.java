package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class TemplateRuleShulkerBox extends TemplateRuleBlockInventory {
    public static final String PLUGIN_NAME = "shulkerBox";

    public TemplateRuleShulkerBox(Level world, BlockPos pos, BlockState state, int turns) {
        super(world, pos, state, turns);
    }

    public TemplateRuleShulkerBox() {
        super();
    }

    @Override
    protected Optional<ItemStack> getStack() {
        //in 1.20 each shulker box color is its own block/item, so the item itself already encodes the 1.12 damage/color value
        return Optional.of(new ItemStack(state.getBlock().asItem()));
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }
}
