package net.shadowmage.ancientwarfare.automation.render;

import net.minecraft.core.Direction;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueBase;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;
import net.shadowmage.ancientwarfare.core.render.property.CoreProperties;


public class TorqueAnimationRenderer<T extends TileTorqueBase> extends BaseAnimationRenderer<T> {

    public TorqueAnimationRenderer(AnimatedBlockRenderer bakery) {
        super(bakery);
    }

    @Override
    protected LegacyModelState handleState(T te, float partialTicks, LegacyModelState state) {
        Direction facing = te.getPrimaryFacing();
        state = state.setValue(CoreProperties.UNLISTED_FACING, facing);
        /*
         * Forge 1.12 IExtendedBlockState pre-populated every unlisted property, so
         * the old renderer could use containsKey(ROTATIONS[i]) as a capability
         * test. LegacyModelState.of(BlockState) intentionally starts empty in the
         * 1.20 port, therefore that check made every animated torque angle fall
         * through to 0 every frame. Every torque renderer registers all six
         * ROTATIONS properties, so populate them unconditionally from the tile.
         */
        for (Direction f : Direction.values()) {
            float rotation = te.getClientOutputRotation(f, partialTicks);
            state = state.setValue(AutomationProperties.ROTATIONS[f.get3DDataValue()], rotation);
        }
        state = state.setValue(AutomationProperties.DYNAMIC, true);
        state = updateAdditionalProperties(state, te);
        return state;
    }

    protected LegacyModelState updateAdditionalProperties(LegacyModelState state, TileTorqueBase te) {
        return state;
    }
}
