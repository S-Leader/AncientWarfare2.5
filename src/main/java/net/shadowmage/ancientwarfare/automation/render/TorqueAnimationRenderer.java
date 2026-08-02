package net.shadowmage.ancientwarfare.automation.render;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.Direction;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueBase;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelProperty;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;
import net.shadowmage.ancientwarfare.core.render.property.CoreProperties;

import java.util.Optional;

public class TorqueAnimationRenderer<T extends TileTorqueBase> extends BaseAnimationRenderer<T> {

    public TorqueAnimationRenderer(AnimatedBlockRenderer bakery) {
        super(bakery);
    }

    @Override
    protected LegacyModelState handleState(T te, float partialTicks, LegacyModelState state) {
        Direction facing = te.getPrimaryFacing();
        state = state.setValue(CoreProperties.UNLISTED_FACING, facing);
        ImmutableMap<LegacyModelProperty<?>, Optional<?>> properties = state.getUnlistedProperties();
        float[] rotations = new float[6];
        for (Direction f : Direction.values()) {
            if (properties.containsKey(AutomationProperties.ROTATIONS[f.get3DDataValue()])) {
                float rotation = te.getClientOutputRotation(f, partialTicks);
                rotations[f.get3DDataValue()] = rotation;
                state = state.setValue(AutomationProperties.ROTATIONS[f.get3DDataValue()], rotation);

            } else {
                state = state.setValue(AutomationProperties.ROTATIONS[f.get3DDataValue()], 0f);
            }
        }
        state = state.setValue(AutomationProperties.DYNAMIC, true);
        state = updateAdditionalProperties(state, te);
        return state;
    }

    protected LegacyModelState updateAdditionalProperties(LegacyModelState state, TileTorqueBase te) {
        return state;
    }
}
