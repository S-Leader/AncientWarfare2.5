package net.shadowmage.ancientwarfare.automation.render;

import net.shadowmage.ancientwarfare.automation.tile.torque.multiblock.TileFlywheelStorage;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;

import static net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties.*;

public class FlywheelStorageAnimationRenderer extends BaseAnimationRenderer<TileFlywheelStorage> {

    public FlywheelStorageAnimationRenderer() {
        super(FlywheelStorageRenderer.INSTANCE);
    }

    @Override
    protected LegacyModelState handleState(TileFlywheelStorage te, float partialTicks, LegacyModelState state) {
        state = state.setValue(DYNAMIC, true);
        state = state.setValue(IS_CONTROL, te.isControl);
        state = state.setValue(WIDTH, te.setWidth);
        state = state.setValue(HEIGHT, te.setHeight);
        state = state.setValue(ROTATION, (float) te.rotation);

        return state;
    }
}
