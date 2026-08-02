package net.shadowmage.ancientwarfare.automation.render;

import codechicken.lib.render.CCModel;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileWindmillController;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;

import java.util.Collection;

public class WindmillGeneratorRenderer extends BaseTorqueRenderer<TileWindmillController> {

    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(AncientWarfareCore.MOD_ID + ":automation/windmill_generator"), "normal");
    public static final WindmillGeneratorRenderer INSTANCE = new WindmillGeneratorRenderer();

    private final Collection<CCModel> outputGear;

    private WindmillGeneratorRenderer() {
        super("automation/windmill_generator.obj");
        outputGear = removeGroups(s -> s.startsWith("base.outputGear."));
    }

    @Override
    protected void transformMovingParts(Collection<CCModel> transformedGroups, Direction frontFacing, float[] rotations, LegacyModelState state) {
        float outR = rotations[frontFacing.get3DDataValue()];
        transformedGroups.addAll(rotateModels(outputGear, frontFacing, new Rotation(outR, 0, 0, 1).at(new Vector3(8d / 16d, 8d / 16d, 8d / 16d))));
    }
}