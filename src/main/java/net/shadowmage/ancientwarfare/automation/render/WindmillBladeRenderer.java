package net.shadowmage.ancientwarfare.automation.render;

import codechicken.lib.model.PerspectiveModelState;
import codechicken.lib.model.bakedmodels.ModelProperties.PerspectiveProperties;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.util.TransformUtils;
import codechicken.lib.vec.*;
import codechicken.lib.vec.uv.IconTransformation;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.shadowmage.ancientwarfare.automation.block.BlockWindmillBlade;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.multiblock.TileWindmillBlade;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;
import net.shadowmage.ancientwarfare.core.render.property.CoreProperties;
import net.shadowmage.ancientwarfare.core.util.Trig;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WindmillBladeRenderer extends AnimatedBlockRenderer {

    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(AncientWarfareCore.MOD_ID + ":automation/windmill_blade"), "normal");
    public static final WindmillBladeRenderer INSTANCE = new WindmillBladeRenderer();

    private static final PerspectiveProperties MODEL_PROPERTIES;

    static {
        com.mojang.math.Transformation thirdPerson = TransformUtils.create(0, 2.5f, 0, 75, 225, 0, 0.375f);
        ImmutableMap.Builder<ItemDisplayContext, com.mojang.math.Transformation> defaultBlockBuilder = ImmutableMap.builder();
        defaultBlockBuilder.put(ItemDisplayContext.GUI, TransformUtils.create(0, 0, 0, 30, 225, 90, 0.320f));
        defaultBlockBuilder.put(ItemDisplayContext.GROUND, TransformUtils.create(0, 3, 0, 0, 0, 0, 0.125f));
        defaultBlockBuilder.put(ItemDisplayContext.FIXED, TransformUtils.create(0, 0, 0, 0, 0, 0, 0.25f));
        defaultBlockBuilder.put(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, thirdPerson);
        defaultBlockBuilder.put(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, TransformUtils.flipLeft(thirdPerson));
        defaultBlockBuilder.put(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, TransformUtils.create(0, 0, 0, 0, 135, 0, 0.2f));
        defaultBlockBuilder.put(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, TransformUtils.create(0, 0, 0, 0, 135, 0, 0.2f));
        MODEL_PROPERTIES = PerspectiveProperties.DEFAULT_BLOCK.toBuilder().withTransforms(new PerspectiveModelState(defaultBlockBuilder.build())).build();
    }

    private final Collection<CCModel> bladeShaft;
    private final Collection<CCModel> windmillShaft;
    private final Collection<CCModel> blade;
    private final Collection<CCModel> bladeJoint;
    private final CCModel cube;
    public TextureAtlasSprite cubeSprite;
    private IconTransformation cubeIconTransform;

    private WindmillBladeRenderer() {
        super("automation/windmill_blade.obj");
        cube = new OBJParser(new ResourceLocation(AncientWarfareCore.MOD_ID, "models/block/automation/windmill_blade_cube.obj"))
                .quads().coordSystem(RedundantTransformation.INSTANCE).parse().values().iterator().next().backfacedCopy();
        bladeShaft = removeGroups(s -> s.startsWith("bladeShaft."));
        windmillShaft = transformModels(removeGroups(s -> s.startsWith("windmillShaft.")), new Translation(0d, 0.5d, 0d));
        blade = removeGroups(s -> s.startsWith("blade."));
        bladeJoint = removeGroups(s -> s.startsWith("bladeJoint."));
    }

    private Collection<CCModel> transformModels(Collection<CCModel> groups, Transformation transform) {
        return groups.stream().map(e -> e.copy().apply(transform)).collect(Collectors.toSet());
    }

    public void setCubeSprite(TextureAtlasSprite sprite) {
        this.cubeSprite = sprite;
        cubeIconTransform = new IconTransformation(this.cubeSprite);
    }

    @Override
    public List<BakedQuad> bakeQuads(@Nullable Direction face, LegacyModelState state) {
        if (state.getValue(BlockWindmillBlade.FORMED) && (!state.getValue(AutomationProperties.DYNAMIC) || !state.getValue(AutomationProperties.IS_CONTROL))) {
            return Collections.emptyList(); //formed blade doesn't have static rendering and only control gets rendered
        }

        return super.bakeQuads(face, state);
    }

    @Override
    protected void renderBlockModels(Collection<CCModel> modelGroups, CCRenderState ccrs, Direction face, LegacyModelState state) {
        if (!state.getValue(BlockWindmillBlade.FORMED)) {
            cube.render(ccrs, cubeIconTransform);
        } else {
            super.renderBlockModels(modelGroups, ccrs, face, state);
        }
    }

    @Override
    protected Collection<CCModel> applyModelTransforms(Collection<CCModel> modelGroups, Direction face, LegacyModelState state) {
        if (!state.getValue(BlockWindmillBlade.FORMED)) {
            return Collections.singleton(cube);
        }

        if (!state.getValue(AutomationProperties.DYNAMIC) || !state.getValue(AutomationProperties.IS_CONTROL)) {
            return Collections.emptySet();
        }

        Direction frontFacing = state.getValue(CoreProperties.UNLISTED_HORIZONTAL_FACING);
        int height = (state.getValue(AutomationProperties.HEIGHT) - 1) / 2;
        float rotation = state.getValue(AutomationProperties.ROTATION);

        return transformBlades(frontFacing, height, rotation);
    }

    @Override
    protected IconTransformation getIconTransform(LegacyModelState state) {
        return !state.getValue(BlockWindmillBlade.FORMED) ? cubeIconTransform : iconTransform;
    }

    @Override
    protected TextureAtlasSprite getQuadSprite(LegacyModelState state) {
        return !state.getValue(BlockWindmillBlade.FORMED) ? cubeSprite : sprite;
    }

    private Set<CCModel> transformBlades(Direction frontFacing, int height, float rotation) {
        Set<CCModel> transformedGroups = Sets.newHashSet();
        Vector3 center = new Vector3(8d / 16d, 8d / 16d, 8d / 16d);
        Transformation baseRotation = new Rotation(rotation, 0, 0, 1).at(center);
        transformedGroups.addAll(rotateModels(windmillShaft, frontFacing, baseRotation));

        for (int i = 0; i < 4; i++) {
            Transformation bladeTransform = new Rotation(((float) i) * Trig.PI / 2f, 0, 0, 1).at(center).with(baseRotation);
            if (i < 2) {
                transformedGroups.addAll(rotateModels(bladeShaft, frontFacing, bladeTransform));
            }
            for (int k = 1; k < height; k++) {
                transformedGroups.addAll(rotateModels(blade, frontFacing, bladeTransform));
                if (k == height - 1) {
                    transformedGroups.addAll(rotateModels(bladeJoint, frontFacing, bladeTransform));
                } else {
                    bladeTransform = new Translation(0, 1, 0).with(bladeTransform);
                }
            }
        }
        return transformedGroups;
    }

    @Override
    public LegacyModelState handleState(LegacyModelState state, BlockGetter access, BlockPos pos) {
        return WorldTools.getTile(access, pos, TileWindmillBlade.class).map(t -> getWindmillState(state, t)).orElse(state);
    }

    private LegacyModelState getWindmillState(LegacyModelState state, TileWindmillBlade blade) {
        return state.setValue(BlockWindmillBlade.FORMED, blade.isFormed())
                .setValue(AutomationProperties.IS_CONTROL, false)
                .setValue(AutomationProperties.HEIGHT, 0)
                .setValue(AutomationProperties.ROTATION, 0f)
                .setValue(CoreProperties.UNLISTED_HORIZONTAL_FACING, Direction.NORTH)
                .setValue(AutomationProperties.DYNAMIC, false);
    }

    @Override
    public PerspectiveProperties getModelProperties(ItemStack stack) {
        return MODEL_PROPERTIES;
    }

    @Override
    protected void renderItemModels(CCRenderState ccrs, ItemStack stack) {
        transformBlades(Direction.NORTH, 2, 0).forEach(m -> m.render(ccrs, iconTransform));
    }
}
