package net.shadowmage.ancientwarfare.core.render.model;

import codechicken.lib.model.PerspectiveModelState;
import codechicken.lib.model.bakedmodels.ModelProperties.PerspectiveProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

final class LegacyBakedModel implements BakedModel {
    private final LegacyBakery bakery;
    private final Supplier<TextureAtlasSprite> particle;

    LegacyBakedModel(LegacyBakery bakery, Supplier<TextureAtlasSprite> particle) {
        this.bakery = bakery;
        this.particle = particle;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        List<BakedQuad> quads = state == null ? bakery.bakeItemQuads(side, ItemStack.EMPTY)
                : bakery.bakeQuads(side, LegacyModelState.of(state));
        return ensureValidSprites(quads);
    }

    BakedModel forItem(ItemStack stack) {
        return new ItemModel(this, stack.copy());
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random,
                                    ModelData data, @Nullable RenderType renderType) {
        if (state == null) {
            return List.of();
        }
        LegacyModelState legacyState = LegacyModelState.of(state, data);
        List<BakedQuad> quads = renderType == null
                ? bakery.bakeQuads(side, legacyState)
                : bakery.bakeLayerFace(side, renderType, legacyState);
        return ensureValidSprites(quads);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData tileData) {
        return bakery.handleState(LegacyModelState.of(state, tileData), level, pos).toModelData();
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
        return bakery.getRenderTypes(LegacyModelState.of(state, data));
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext context, PoseStack poseStack, boolean applyLeftHandTransform) {
        applyLegacyTransform(bakery, ItemStack.EMPTY, context, poseStack);
        return this;
    }


    private List<BakedQuad> ensureValidSprites(List<BakedQuad> quads) {
        if (quads.isEmpty()) {
            return quads;
        }
        TextureAtlasSprite fallback = particle.get();
        List<BakedQuad> safe = null;
        for (int i = 0; i < quads.size(); i++) {
            BakedQuad quad = quads.get(i);
            if (quad.getSprite() != null) {
                if (safe != null) {
                    safe.add(quad);
                }
                continue;
            }
            if (safe == null) {
                safe = new java.util.ArrayList<>(quads.size());
                safe.addAll(quads.subList(0, i));
            }
            safe.add(new BakedQuad(quad.getVertices().clone(), quad.getTintIndex(), quad.getDirection(),
                    fallback, quad.isShade(), quad.hasAmbientOcclusion()));
        }
        return safe == null ? quads : safe;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return particle.get();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    private static void applyLegacyTransform(LegacyBakery bakery, ItemStack stack,
                                             ItemDisplayContext context, PoseStack poseStack) {
        Object modelProperties = bakery.getModelProperties(stack);
        if (!(modelProperties instanceof PerspectiveProperties properties)) {
            return;
        }
        PerspectiveModelState modelState = properties.getTransforms();
        if (modelState == null) {
            return;
        }
        Transformation transform = modelState.getTransform(context);
        var translation = transform.getTranslation();
        poseStack.translate(translation.x(), translation.y(), translation.z());
        poseStack.mulPose(transform.getLeftRotation());
        var scale = transform.getScale();
        poseStack.scale(scale.x(), scale.y(), scale.z());
        poseStack.mulPose(transform.getRightRotation());
    }

    /**
     * Captures the actual metadata/NBT stack for legacy CCL item baking.
     */
    private static final class ItemModel extends BakedModelWrapper<LegacyBakedModel> {
        private final ItemStack stack;

        private ItemModel(LegacyBakedModel original, ItemStack stack) {
            super(original);
            this.stack = stack;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
            return originalModel.ensureValidSprites(originalModel.bakery.bakeItemQuads(side, stack));
        }

        @Override
        public ItemOverrides getOverrides() {
            return ItemOverrides.EMPTY;
        }

        @Override
        public BakedModel applyTransform(ItemDisplayContext context, PoseStack poseStack,
                                         boolean applyLeftHandTransform) {
            applyLegacyTransform(originalModel.bakery, stack, context, poseStack);
            return this;
        }
    }
}
