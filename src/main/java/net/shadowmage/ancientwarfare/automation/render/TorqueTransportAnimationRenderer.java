package net.shadowmage.ancientwarfare.automation.render;

import net.minecraft.core.Direction;
import net.shadowmage.ancientwarfare.automation.block.BlockTorqueTransportSided;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueSidedCell;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;

public class TorqueTransportAnimationRenderer extends TorqueAnimationRenderer<TileTorqueSidedCell> {
    public TorqueTransportAnimationRenderer(AnimatedBlockRenderer bakery) {
        super(bakery);
    }

    @Override
    protected LegacyModelState handleState(TileTorqueSidedCell transportCell, float partialTicks, LegacyModelState state) {
        state = super.handleState(transportCell, partialTicks, state);

        ITorque.ITorqueTile[] neighbors = transportCell.getTorqueCache();
        boolean[] connections = transportCell.getConnections();
        for (Direction facing : Direction.values()) {
            state = state.setValue(BlockTorqueTransportSided.CONNECTIONS[facing.ordinal()], connections[facing.ordinal()]);

            if (connections[facing.ordinal()]) {
                if (!transportCell.canOutputTorque(facing) && neighbors[facing.ordinal()] != null && neighbors[facing.ordinal()].useOutputRotation(null)) {
                    float r = -neighbors[facing.ordinal()].getClientOutputRotation(facing.getOpposite(), partialTicks);

                    state = state.setValue(AutomationProperties.ROTATIONS[facing.ordinal()], r);
                }
            }
        }

        return state;
    }
}
