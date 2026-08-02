package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.shadowmage.ancientwarfare.structure.api.IStructureBuilder;

import static net.minecraft.world.level.block.VineBlock.*;

public class TemplateRuleVine extends TemplateRuleVanillaBlocks {

    public static final String PLUGIN_NAME = "vine";

    private BooleanProperty[] SIDE_PROPERTIES = new BooleanProperty[]{NORTH, EAST, SOUTH, WEST};

    public TemplateRuleVine() {
        super();
    }

    public TemplateRuleVine(Level world, BlockPos pos, BlockState state, int turns) {
        super(world, pos, state, turns);
        this.state = rotateSides(this.state, turns);
    }

    private BlockState rotateSides(BlockState state, int turns) {
        BlockState modifiedState = state;
        for (int i = 0; i < 4; i++) {
            modifiedState = modifiedState.setValue(SIDE_PROPERTIES[(i + turns) % 4], state.getValue(SIDE_PROPERTIES[i]));
        }
        return modifiedState;
    }

    @Override
    public boolean shouldReuseRule(Level world, BlockState state, int turns, BlockPos pos) {
        return state.getBlock() == this.state.getBlock() && rotateSides(state, turns).getValues().equals(this.state.getValues());
    }

    @Override
    public void handlePlacement(Level world, int turns, BlockPos pos, IStructureBuilder builder) {
        builder.placeBlock(pos, getState(turns), buildPass);
    }

    @Override
    public BlockState getState(int turns) {
        return rotateSides(state, turns);
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }
}
