package net.shadowmage.ancientwarfare.automation.render;

import net.shadowmage.ancientwarfare.automation.block.BlockWindmillBlade;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.multiblock.TileWindmillBlade;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;
import net.shadowmage.ancientwarfare.core.render.property.CoreProperties;

public class WindmillBladeAnimationRenderer extends BaseAnimationRenderer<TileWindmillBlade> {

    public WindmillBladeAnimationRenderer() {
        super(WindmillBladeRenderer.INSTANCE);
    }

    @Override
    protected LegacyModelState handleState(TileWindmillBlade blade, float partialTicks, LegacyModelState state) {
        state = state.setValue(BlockWindmillBlade.FORMED, blade.isFormed());
        state = state.setValue(AutomationProperties.IS_CONTROL, blade.isControl());
        state = state.setValue(AutomationProperties.HEIGHT, blade.getWindmillSize());
        state = state.setValue(AutomationProperties.ROTATION, blade.getRotation(partialTicks));
        state = state.setValue(CoreProperties.UNLISTED_HORIZONTAL_FACING, blade.getDirection());
        state = state.setValue(AutomationProperties.DYNAMIC, true);

        return state;
    }
}
