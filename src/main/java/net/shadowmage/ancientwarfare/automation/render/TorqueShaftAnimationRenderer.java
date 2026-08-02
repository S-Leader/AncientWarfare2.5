package net.shadowmage.ancientwarfare.automation.render;

import net.minecraft.core.Direction;
import net.shadowmage.ancientwarfare.automation.block.BlockTorqueTransportShaft;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueShaft;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;
import net.shadowmage.ancientwarfare.core.render.property.CoreProperties;

public class TorqueShaftAnimationRenderer extends TorqueAnimationRenderer<TileTorqueShaft> {

    public TorqueShaftAnimationRenderer() {
        super(TorqueShaftRenderer.INSTANCE);
    }

    @Override
    protected LegacyModelState handleState(TileTorqueShaft shaft, float partialTicks, LegacyModelState state) {
        state = super.handleState(shaft, partialTicks, state);
        state = state.setValue(BlockTorqueTransportShaft.HAS_NEXT, shaft.next() != null);
        state = state.setValue(BlockTorqueTransportShaft.HAS_PREVIOUS, shaft.prev() != null);

        Direction facing = state.getValue(CoreProperties.UNLISTED_FACING);

        ITorque.ITorqueTile itt = shaft.getTorqueCache()[facing.getOpposite().ordinal()];
        state = state.setValue(AutomationProperties.USE_INPUT, itt != null && itt.canOutputTorque(facing) && itt.useOutputRotation(null));
        state = state.setValue(AutomationProperties.INPUT_ROTATION, itt != null ? itt.getClientOutputRotation(facing, partialTicks) : 0);

        return state;
    }
}
