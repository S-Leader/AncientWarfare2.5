package net.shadowmage.ancientwarfare.automation.render;

import codechicken.lib.render.CCModel;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileHandCrankedGenerator;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;

import java.util.Collection;

public class HandCrankedGeneratorRenderer extends BaseTorqueRenderer<TileHandCrankedGenerator> {

    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(AncientWarfareCore.MOD_ID + ":automation/hand_cranked_generator"), "normal");
    public static final HandCrankedGeneratorRenderer INSTANCE = new HandCrankedGeneratorRenderer();

    private final Collection<CCModel> outputGear;
    private final Collection<CCModel> inputGear;

    private HandCrankedGeneratorRenderer() {
        super("automation/hand_cranked_generator.obj");

        outputGear = removeGroups(s -> s.startsWith("base.outputGear."));
        inputGear = removeGroups(s -> s.startsWith("base.inputGear."));
    }

    @Override
    protected void transformMovingParts(Collection<CCModel> transformedGroups, Direction frontFacing, float[] rotations, LegacyModelState state) {
        float inR = rotations[Direction.UP.get3DDataValue()];
        float outR = rotations[frontFacing.get3DDataValue()];
        transformedGroups.addAll(rotateModels(outputGear, frontFacing, new Rotation(outR, 0, 0, 1).at(new Vector3(8d / 16d, 8d / 16d, 8d / 16d))));
        transformedGroups.addAll(rotateModels(inputGear, frontFacing, new Rotation(inR, 0, 1, 0).at(new Vector3(8d / 16d, 10.5d / 16d, 11.5d / 16d))));
    }
}