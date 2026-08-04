package net.shadowmage.ancientwarfare.automation.render;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.lighting.LightModel;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.vec.*;
import codechicken.lib.vec.uv.IconTransformation;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;
import net.shadowmage.ancientwarfare.automation.block.TorqueTier;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.multiblock.TileFlywheelStorage;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.render.model.LegacyBakery;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties.IS_CONTROL;
import static net.shadowmage.ancientwarfare.core.render.BaseBakery.finalizeBakedQuads;

@OnlyIn(Dist.CLIENT)
public class FlywheelStorageRenderer implements LegacyBakery, ITESRRenderer {
    private static final String FLYWHEEL_STORAGE_REGISTRY_PATH = ":automation/flywheel_storage";
    public static final ModelResourceLocation LIGHT_MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(AncientWarfareCore.MOD_ID + FLYWHEEL_STORAGE_REGISTRY_PATH), "small_light");
    public static final ModelResourceLocation MEDIUM_MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(AncientWarfareCore.MOD_ID + FLYWHEEL_STORAGE_REGISTRY_PATH), "small_medium");
    public static final ModelResourceLocation HEAVY_MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(AncientWarfareCore.MOD_ID + FLYWHEEL_STORAGE_REGISTRY_PATH), "small_heavy");

    public static final FlywheelStorageRenderer INSTANCE = new FlywheelStorageRenderer();

    private Collection<CCModel> spindleSmall;
    private Collection<CCModel> upperShroudSmall;
    private Collection<CCModel> lowerShroudSmall;
    private Collection<CCModel> flywheelExtensionSmall;
    private Collection<CCModel> lowerWindowSmall;
    private Collection<CCModel> upperWindowSmall;
    private Collection<CCModel> caseBarsSmall;
    private Collection<CCModel> spindleLarge;
    private Collection<CCModel> upperShroudLarge;
    private Collection<CCModel> lowerShroudLarge;
    private Collection<CCModel> flywheelExtensionLarge;
    private Collection<CCModel> lowerWindowLarge;
    private Collection<CCModel> upperWindowLarge;
    private Collection<CCModel> caseBarsLarge;

    private Map<Pair<Boolean, TorqueTier>, IconTransformation> iconTransformations = Maps.newHashMap();
    private Map<Pair<Boolean, TorqueTier>, TextureAtlasSprite> sprites = Maps.newHashMap();

    public void setSprite(boolean large, TorqueTier tier, TextureAtlasSprite sprite) {
        sprites.put(new ImmutablePair<>(large, tier), sprite);
        iconTransformations.put(new ImmutablePair<>(large, tier), new IconTransformation(sprite));
    }

    public TextureAtlasSprite getSprite(TorqueTier tier) {
        return sprites.get(new ImmutablePair<>(false, tier));
    }

    private IconTransformation getIconTransformation(boolean large, TorqueTier tier) {
        return iconTransformations.get(new ImmutablePair<>(large, tier));
    }

    private FlywheelStorageRenderer() {
        Map<String, CCModel> smallModel = loadModel("flywheel_small.obj");
        flywheelExtensionSmall = removeGroups(smallModel, s -> s.startsWith("spindle.flywheelExtension."));
        spindleSmall = removeGroups(smallModel, s -> s.startsWith("spindle."));
        upperShroudSmall = removeGroups(smallModel, s -> s.startsWith("shroudUpper."));
        lowerShroudSmall = removeGroups(smallModel, s -> s.startsWith("shroudLower."));
        lowerWindowSmall = removeGroups(smallModel, s -> s.startsWith("windowLower."));
        upperWindowSmall = removeGroups(smallModel, s -> s.startsWith("windowUpper."));
        caseBarsSmall = removeGroups(smallModel, s -> s.startsWith("caseBars."));

        Map<String, CCModel> largeModel = loadModel("flywheel_large.obj");
        flywheelExtensionLarge = removeGroups(largeModel, s -> s.startsWith("spindle.flywheelExtension."));
        spindleLarge = removeGroups(largeModel, s -> s.startsWith("spindle."));
        upperShroudLarge = removeGroups(largeModel, s -> s.startsWith("shroudUpper."));
        lowerShroudLarge = removeGroups(largeModel, s -> s.startsWith("shroudLower."));
        lowerWindowLarge = removeGroups(largeModel, s -> s.startsWith("windowLower."));
        upperWindowLarge = removeGroups(largeModel, s -> s.startsWith("windowUpper."));
        caseBarsLarge = removeGroups(largeModel, s -> s.startsWith("caseBars."));
    }

    private Map<String, CCModel> loadModel(String modelName) {
        Map<String, CCModel> ret = new OBJParser(new ResourceLocation(AncientWarfareCore.MOD_ID, "models/block/automation/" + modelName))
                .quads().coordSystem(RedundantTransformation.INSTANCE).parse();

        for (Map.Entry<String, CCModel> group : ret.entrySet()) {
            group.setValue(group.getValue().backfacedCopy().computeNormals());
        }

        return ret;
    }

    private Collection<CCModel> removeGroups(Map<String, CCModel> objGroups, Function<String, Boolean> filter) {
        Set<CCModel> ret = Sets.newHashSet();

        Iterator<Map.Entry<String, CCModel>> iterator = objGroups.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, CCModel> entry = iterator.next();

            if (filter.apply(entry.getKey())) {
                ret.add(entry.getValue());
                iterator.remove();
            }
        }

        return ret;
    }

    private Collection<CCModel> transformModels(Collection<CCModel> groups, Transformation transform) {
        return groups.stream().map(e -> e.copy().apply(transform)).collect(Collectors.toSet());
    }

    @Override
    public List<BakedQuad> bakeItemQuads(@Nullable Direction face, ItemStack stack) {
        if (face != null) {
            return Collections.emptyList();
        }
        List<BakedQuad> bakedQuads = new ArrayList<>();
        QuadBakingVertexConsumer buffer = new QuadBakingVertexConsumer(bakedQuads::add);

        TorqueTier tier = TorqueTier.fromItemStack(stack);
        TextureAtlasSprite quadSprite = sprites.get(new ImmutablePair<>(false, tier));
        if (quadSprite != null) {
            buffer.setSprite(quadSprite);
        }
        buffer.setTintIndex(-1);
        buffer.setShade(true);
        buffer.setHasAmbientOcclusion(true);
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.bind(buffer, DefaultVertexFormat.BLOCK);

        HashSet<CCModel> transformedModels = Sets.newHashSet();
        transformedModels.addAll(getTransformedModels(RenderType.solid(), false, true, 0f, 1));
        transformedModels.addAll(getTransformedModels(RenderType.solid(), false, false, 0f, 1));
        transformedModels.addAll(getTransformedModels(RenderType.translucent(), false, false, 0f, 1));
        renderModels(transformedModels, ccrs, false, tier);

        return finalizeBakedQuads(bakedQuads, quadSprite);
    }

    @Override
    public List<BakedQuad> bakeLayerFace(@Nullable Direction face, RenderType layer, LegacyModelState state) {
        if (face != null || !state.getValue(AutomationProperties.IS_CONTROL)) {
            return Collections.emptyList();
        }

        List<BakedQuad> bakedQuads = new ArrayList<>();
        QuadBakingVertexConsumer buffer = new QuadBakingVertexConsumer(bakedQuads::add);

        boolean largeModel = state.getValue(AutomationProperties.WIDTH) > 1;
        TorqueTier tier = state.getValue(AutomationProperties.TIER);
        TextureAtlasSprite quadSprite = sprites.get(new ImmutablePair<>(largeModel, tier));
        if (quadSprite != null) {
            buffer.setSprite(quadSprite);
        }
        buffer.setTintIndex(-1);
        buffer.setShade(true);
        buffer.setHasAmbientOcclusion(true);
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.bind(buffer, DefaultVertexFormat.BLOCK);

        renderModels(getTransformedModels(layer, state), ccrs, largeModel, tier);

        List<BakedQuad> finalized = finalizeBakedQuads(bakedQuads, quadSprite);
        return layer == RenderType.translucent() ? forceAlpha(finalized, 0.25F) : finalized;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(LegacyModelState state) {
        return ChunkRenderTypeSet.of(RenderType.solid(), RenderType.translucent());
    }

    private Set<CCModel> getTransformedModels(RenderType layer, LegacyModelState state) {
        if (!state.getValue(IS_CONTROL)) {
            return Collections.emptySet();
        }

        boolean largeModel = state.getValue(AutomationProperties.WIDTH) > 1;
        boolean displayDynamicParts = state.getValue(AutomationProperties.DYNAMIC);
        float rotation = state.getValue(AutomationProperties.ROTATION);
        int height = state.getValue(AutomationProperties.HEIGHT);

        return getTransformedModels(layer, largeModel, displayDynamicParts, rotation, height);
    }

    private Set<CCModel> getTransformedModels(RenderType layer, boolean largeModel, boolean displayDynamicParts, float rotation, int height) {
        Set<CCModel> transformedGroups = Sets.newHashSet();
        Collection<CCModel> spindle = largeModel ? spindleLarge : spindleSmall;
        Collection<CCModel> flywheelExtension = largeModel ? flywheelExtensionLarge : flywheelExtensionSmall;
        Collection<CCModel> caseBars = largeModel ? caseBarsLarge : caseBarsSmall;
        Collection<CCModel> lowerWindow = largeModel ? lowerWindowLarge : lowerWindowSmall;
        Collection<CCModel> upperShroud = largeModel ? upperShroudLarge : upperShroudSmall;
        Collection<CCModel> lowerShroud = largeModel ? lowerShroudLarge : lowerShroudSmall;
        Collection<CCModel> upperWindow = largeModel ? upperWindowLarge : upperWindowSmall;

        if (displayDynamicParts) {
            if (layer == RenderType.solid()) {
                Transformation rotationTransform = new Rotation(rotation, 0, 1, 0).at(Vector3.CENTER);
                for (int i = 0; i < height; i++) {
                    Translation translation = new Translation(0, i, 0);
                    transformedGroups.addAll(transformModels(spindle, translation.with(rotationTransform)));
                    if (i < height - 1) {
                        transformedGroups.addAll(transformModels(flywheelExtension, translation.with(rotationTransform)));//at every level less than highest
                    }
                }
            }
        } else {
            for (int i = 0; i < height; i++) {
                Translation translation = new Translation(0, i, 0);
                if (layer == RenderType.solid()) {
                    transformedGroups.addAll(transformModels(caseBars, translation));
                    if (i == height - 1) {
                        transformedGroups.addAll(transformModels(upperShroud, translation));//at highest level
                    }
                    if (i == 0) {
                        transformedGroups.addAll(transformModels(lowerShroud, translation));//at ground level
                    }
                } else {
                    transformedGroups.addAll(transformModels(lowerWindow, translation));
                    if (i < height - 1) {
                        transformedGroups.addAll(transformModels(upperWindow, translation));
                    }
                }
            }
        }

        return transformedGroups;
    }

    private static List<BakedQuad> forceAlpha(List<BakedQuad> quads, float alpha) {
        int packedAlpha = Math.max(0, Math.min(255, Math.round(alpha * 255.0F))) << 24;
        List<BakedQuad> translucent = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            int[] vertices = quad.getVertices().clone();
            for (int vertex = 0; vertex < 4; vertex++) {
                int colorIndex = vertex * IQuadTransformer.STRIDE + IQuadTransformer.COLOR;
                vertices[colorIndex] = (vertices[colorIndex] & 0x00FFFFFF) | packedAlpha;
            }
            translucent.add(new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(),
                    quad.getSprite(), quad.isShade(), quad.hasAmbientOcclusion()));
        }
        return translucent;
    }

    @Override
    public LegacyModelState handleState(LegacyModelState state, BlockGetter access, BlockPos pos) {
        Optional<TileFlywheelStorage> te = WorldTools.getTile(access, pos, TileFlywheelStorage.class);
        boolean isControl = true;
        int height = 1;
        int width = 1;

        if (te.isPresent()) {
            TileFlywheelStorage storage = te.get();

            isControl = storage.isControl;
            width = storage.setWidth;
            height = storage.setHeight;
        }

        LegacyModelState updatedState = state.setValue(AutomationProperties.DYNAMIC, false);
        updatedState = updatedState.setValue(AutomationProperties.IS_CONTROL, isControl);
        updatedState = updatedState.setValue(AutomationProperties.HEIGHT, height);
        updatedState = updatedState.setValue(AutomationProperties.WIDTH, width);
        updatedState = updatedState.setValue(AutomationProperties.ROTATION, 0f);

        return updatedState;
    }

    private void renderModels(Collection<CCModel> modelGroups, CCRenderState ccrs, boolean large, TorqueTier tier) {
        for (CCModel group : modelGroups) {
            group.render(ccrs, getIconTransformation(large, tier));
        }
    }

    @Override
    public void renderTransformedBlockModels(CCRenderState ccrs, LegacyModelState state) {
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);

        boolean largeModel = state.getValue(AutomationProperties.WIDTH) > 1;
        TorqueTier tier = state.getValue(AutomationProperties.TIER);

        Set<CCModel> modelGroups = getTransformedModels(RenderType.solid(), state);

        for (CCModel group : modelGroups) {
            group.render(ccrs, LightModel.standardLightModel, getIconTransformation(largeModel, tier));
        }
    }
}
