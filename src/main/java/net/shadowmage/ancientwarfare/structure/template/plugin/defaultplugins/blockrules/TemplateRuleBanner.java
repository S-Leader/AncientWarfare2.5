package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class TemplateRuleBanner extends TemplateRuleBlockTile {
    public static final String PLUGIN_NAME = "banner";

    public TemplateRuleBanner(Level world, BlockPos pos, BlockState state, int turns) {
        super(world, pos, state, turns);
    }

    public TemplateRuleBanner() {
        super();
    }

    @Override
    protected Optional<ItemStack> getStack() {
        ItemStack stack = new ItemStack(state.getBlock().asItem());
        BlockItem.setBlockEntityData(stack, BlockEntityType.BANNER, tag.copy());
        return Optional.of(stack);
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public BlockState getState(int turns) {
        BlockState rotatedState = super.getState(turns);
        return rotatedState.getBlock() instanceof BannerBlock
                ? rotatedState.rotate(Rotation.values()[turns % 4])
                : rotatedState;
    }
}
