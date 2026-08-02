package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.structure.api.IStructureBuilder;

import java.util.Collections;
import java.util.List;

public class TemplateRuleBlockDoors extends TemplateRuleVanillaBlocks {
    public static final String PLUGIN_NAME = "doors";

    public TemplateRuleBlockDoors(Level world, BlockPos pos, BlockState state, int turns) {
        super(world, pos, state, turns);
    }

    public TemplateRuleBlockDoors() {
        super();
    }

    @Override
    public List<ItemStack> getResources() {
        if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            return super.getResources();
        }
        return Collections.emptyList();
    }

    @Override
    public void handlePlacement(Level world, int turns, BlockPos pos, IStructureBuilder builder) {
        if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            BlockState rotatedState = BlockTools.rotateFacing(this.state, turns);
            builder.placeBlock(pos, rotatedState, buildPass);
            builder.placeBlock(pos.below(), rotatedState.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), buildPass);
        }
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }
}
