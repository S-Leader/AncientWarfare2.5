package net.shadowmage.ancientwarfare.automation.render;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueBase;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelProperty;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;
import net.shadowmage.ancientwarfare.core.render.property.CoreProperties;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public abstract class BaseTorqueRenderer<T extends TileTorqueBase> extends AnimatedBlockRenderer {
    protected BaseTorqueRenderer(String modelPath) {
        super(modelPath);
    }

    @Override
    protected Collection<CCModel> applyModelTransforms(Collection<CCModel> modelGroups, Direction face, LegacyModelState state) {
        Set<CCModel> transformedGroups = Sets.newHashSet();

        Direction frontFacing = state.getValue(CoreProperties.UNLISTED_FACING);

        if (state.getValue(AutomationProperties.DYNAMIC)) {
            ImmutableMap<LegacyModelProperty<?>, Optional<?>> properties = state.getUnlistedProperties();
            float[] rotations = new float[6];
            for (Direction facing : Direction.values()) {
                if (properties.containsKey(AutomationProperties.ROTATIONS[facing.get3DDataValue()])) {
                    rotations[facing.get3DDataValue()] = state.getValue(AutomationProperties.ROTATIONS[facing.get3DDataValue()]);
                }
            }
            transformMovingParts(transformedGroups, frontFacing, rotations, state);
        } else {
            transformedGroups.addAll(rotateFacing(modelGroups, frontFacing));
        }

        return transformedGroups;
    }

    @Override
    protected void renderItemModels(CCRenderState ccrs, ItemStack stack) {
        super.renderItemModels(ccrs, stack);
        Set<CCModel> movingParts = Sets.newHashSet();
        transformMovingParts(movingParts, Direction.NORTH, new float[6], null);

        movingParts.forEach(m -> m.render(ccrs, getIconTransform(stack)));
    }

    protected abstract void transformMovingParts(Collection<CCModel> transformedGroups, Direction frontFacing, float[] rotations,
                                                 @Nullable LegacyModelState state);

    @Override
    public LegacyModelState handleState(LegacyModelState state, BlockGetter access, BlockPos pos) {
        Direction facing = Direction.NORTH;
        Optional<TileTorqueBase> tileentity = WorldTools.getTile(access, pos, TileTorqueBase.class)
                .filter(tile -> !tile.isRemoved());

        if (tileentity.isPresent()) {
            TileTorqueBase torquePart = tileentity.get();
            facing = torquePart.getPrimaryFacing();
        }

        LegacyModelState updatedState = state.setValue(CoreProperties.UNLISTED_FACING, facing);
        updatedState = updatedState.setValue(AutomationProperties.DYNAMIC, false);
        for (Direction f : Direction.values()) {
            updatedState = updatedState.setValue(AutomationProperties.ROTATIONS[f.get3DDataValue()], 0f);
        }

        if (tileentity.isPresent()) {
            //noinspection unchecked
            updatedState = handleAdditionalProperties(updatedState, (T) tileentity.get());
        }

        return updatedState;
    }

    protected LegacyModelState handleAdditionalProperties(LegacyModelState state, T tileEntity) {
        return state;
    }
}
