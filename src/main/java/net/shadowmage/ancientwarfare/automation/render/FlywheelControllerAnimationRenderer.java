package net.shadowmage.ancientwarfare.automation.render;

import net.minecraft.core.Direction;
import net.shadowmage.ancientwarfare.automation.block.BlockFlywheelController;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileFlywheelController;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;

public class FlywheelControllerAnimationRenderer extends TorqueAnimationRenderer<TileFlywheelController> {
    public FlywheelControllerAnimationRenderer() {
        super(FlywheelControllerRenderer.INSTANCE);
    }

    @Override
    protected LegacyModelState handleState(TileFlywheelController te, float partialTicks, LegacyModelState state) {
        state = super.handleState(te, partialTicks, state);

        state = state.setValue(BlockFlywheelController.FLYWHEEL_ROTATION, te.getFlywheelRotation(partialTicks));
        Direction d = te.getPrimaryFacing();
        ITorque.ITorqueTile inputNeighbor = te.getTorqueCache()[d.getOpposite().ordinal()];
        state = state.setValue(AutomationProperties.USE_INPUT, inputNeighbor != null && inputNeighbor.canOutputTorque(d) && inputNeighbor.useOutputRotation(d.getOpposite()));
        state = state.setValue(AutomationProperties.INPUT_ROTATION, inputNeighbor != null ? inputNeighbor.getClientOutputRotation(d, partialTicks) : 0);

        return state;
    }
}
