package net.shadowmage.ancientwarfare.automation.render;

import codechicken.lib.render.CCModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;

import javax.annotation.Nullable;
import java.util.Collection;

public class TorqueJunctionRenderer extends TorqueTransportSidedRenderer {
    public static final ModelResourceLocation LIGHT_MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(AncientWarfareCore.MOD_ID + ":automation/torque_junction"), "light");
    public static final ModelResourceLocation MEDIUM_MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(AncientWarfareCore.MOD_ID + ":automation/torque_junction"), "medium");
    public static final ModelResourceLocation HEAVY_MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(AncientWarfareCore.MOD_ID + ":automation/torque_junction"), "heavy");

    public static final TorqueJunctionRenderer INSTANCE = new TorqueJunctionRenderer();

    private TorqueJunctionRenderer() {
        super();
    }

    @Override
    protected void transformMovingParts(Collection<CCModel> transformedGroups, Direction frontFacing, float[] rotations, @Nullable LegacyModelState state) {
        if (state == null) {
            transformedGroups.addAll(gearHeads[Direction.NORTH.ordinal()]);
            transformedGroups.addAll(gearHeads[Direction.EAST.ordinal()]);
            transformedGroups.addAll(gearHeads[Direction.WEST.ordinal()]);
        } else {
            super.transformMovingParts(transformedGroups, frontFacing, rotations, state);
        }
    }
}
