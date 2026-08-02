package net.shadowmage.ancientwarfare.core.render;

import codechicken.lib.render.CCModel;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import com.google.common.collect.Sets;
import net.minecraft.core.Direction;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;
import net.shadowmage.ancientwarfare.core.render.property.CoreProperties;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class RotatableBlockRenderer extends BaseBakery {

    protected RotatableBlockRenderer(String modelPath) {
        super(modelPath);
    }

    @Override
    protected Collection<CCModel> applyModelTransforms(Collection<CCModel> modelGroups, Direction face, LegacyModelState state) {
        Set<CCModel> transformedGroups = Sets.newHashSet();

        for (CCModel group : modelGroups) {
            transformedGroups.add(rotateFacing(group.copy(), getFacing(state)));
        }

        return transformedGroups;
    }

    protected Direction getFacing(LegacyModelState state) {
        return state.getValue(CoreProperties.UNLISTED_HORIZONTAL_FACING);
    }

    protected Collection<CCModel> rotateFacing(Collection<CCModel> groups, Direction frontFacing) {
        return groups.stream().map(e -> rotateFacing(e.copy(), frontFacing)).collect(Collectors.toSet());
    }

    protected CCModel rotateFacing(CCModel group, Direction frontFacing) {
        double angle = Math.PI / 2d * (frontFacing.getAxis() == Direction.Axis.Y ? frontFacing.getStepY() : -((frontFacing.get2DDataValue() + 2) & 3));

        return group.apply(new Rotation(angle, frontFacing.getAxis() == Direction.Axis.Y ? 1 : 0, frontFacing.getAxis() != Direction.Axis.Y ? 1 : 0, 0).at(Vector3.CENTER));
    }
}
