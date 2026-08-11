package net.shadowmage.ancientwarfare.automation.render;

import codechicken.lib.render.CCModel;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import net.minecraft.core.Direction;
import net.shadowmage.ancientwarfare.automation.block.BlockTorqueTransportSided;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueSidedCell;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.Collectors;

public class TorqueTransportSidedRenderer extends TorqueTieredRenderer<TileTorqueSidedCell> {

    protected Collection<CCModel>[] gearHeads = new Collection[6];

    protected TorqueTransportSidedRenderer() {
        super("automation/torque_transport.obj");
        gearHeads[0] = removeGroups(s -> s.startsWith("downShaft."));
        gearHeads[1] = removeGroups(s -> s.startsWith("upShaft."));
        gearHeads[2] = removeGroups(s -> s.startsWith("northShaft."));
        gearHeads[3] = removeGroups(s -> s.startsWith("southShaft."));
        gearHeads[4] = removeGroups(s -> s.startsWith("westShaft."));
        gearHeads[5] = removeGroups(s -> s.startsWith("eastShaft."));
    }

    @Override
    protected Transformation getBaseTransformation() {
        return new Translation(0d, 0.5d, 0d);
    }

    @Override
    protected void transformMovingParts(Collection<CCModel> transformedGroups, Direction frontFacing, float[] rotations, @Nullable LegacyModelState state) {
        for (Direction facing : Direction.values()) {
            if (state.getValue(BlockTorqueTransportSided.CONNECTIONS[facing.ordinal()])) {
                transformedGroups.addAll(rotateShaft(gearHeads[facing.ordinal()], facing, state.getValue(AutomationProperties.ROTATIONS[facing.ordinal()])));
            }
        }
    }

    private Collection<CCModel> rotateShaft(Collection<CCModel> groups, Direction facing, float rotation) {
        return groups.stream().map(m -> rotateShaftPart(m, facing, rotation)).collect(Collectors.toSet());
    }

    private CCModel rotateShaftPart(CCModel part, Direction facing, float rotation) {
        return part.copy().apply(new Rotation((facing.ordinal() % 2 == 0) ? rotation : -rotation, facing.getAxis() == Direction.Axis.X ? 1 : 0, facing.getAxis() == Direction.Axis.Y ? 1 : 0, facing.getAxis() == Direction.Axis.Z ? 1 : 0).at(Vector3.CENTER));
    }

    @Override
    protected LegacyModelState handleAdditionalProperties(LegacyModelState state, TileTorqueSidedCell cell) {
        state = super.handleAdditionalProperties(state, cell);

        boolean[] connections = cell.getConnections();
        for (Direction facing : Direction.values()) {
            int index = facing.ordinal();
            state = state.setValue(BlockTorqueTransportSided.CONNECTIONS[index],
                    index < connections.length && connections[index]);
        }

        return state;
    }

}
