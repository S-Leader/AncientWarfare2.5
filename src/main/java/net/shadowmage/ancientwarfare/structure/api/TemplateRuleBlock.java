package net.shadowmage.ancientwarfare.structure.api;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.NBTHelper;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.registry.StructureBlockRegistry;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;

public abstract class TemplateRuleBlock extends TemplateRule {
    protected BlockState state;
    private ItemStack cachedStack = null;
    private boolean placeInSurvival = false;

    public TemplateRuleBlock(BlockState state, int turns) {
        this.state = BlockTools.rotateFacing(state, turns);
    }

    public TemplateRuleBlock() {
    }

    public abstract boolean shouldReuseRule(Level world, BlockState state, int turns, BlockPos pos);

    @Override
    public List<ItemStack> getResources() {
        if (state.getBlock() == Blocks.AIR) {
            return Collections.emptyList();
        }

        ItemStack stack = getCachedStack();
        if (!stack.isEmpty()) {
            return Collections.singletonList(stack);
        }

        return Collections.emptyList();
    }

    private ItemStack getCachedStack() {
        cacheStack();
        return cachedStack;
    }

    private void cacheStack() {
        if (cachedStack == null) {
            Optional<ItemStack> stack = getStack();
            placeInSurvival = stack.isPresent();
            cachedStack = stack.orElse(ItemStack.EMPTY);
        }
    }

    protected Optional<ItemStack> getStack() {
        return StructureBlockRegistry.getItemStackFrom(state);
    }

    @Override
    public ItemStack getRemainingStack() {
        return StructureBlockRegistry.getRemainingStackFrom(state);
    }

    @Override
    public boolean placeInSurvival() {
        cacheStack();
        return state.getBlock() != Blocks.AIR && placeInSurvival;
    }

    @Override
    protected String getRuleType() {
        return "rule";
    }

    @Override
    public void parseRule(CompoundTag tag) {
        try {
            state = NBTHelper.getBlockState(tag.getCompound("blockState"));
        } catch (MissingResourceException e) {
            AncientWarfareStructure.LOG.warn("Unable to find blockstate while parsing structure template thus replacing it with air - {}.", e.getMessage());
            state = Blocks.AIR.defaultBlockState();
        }
    }

    @Override
    public void writeRuleData(CompoundTag tag) {
        tag.put("blockState", NBTHelper.getBlockStateTag(state));
    }

    @OnlyIn(Dist.CLIENT)
    public void renderRule(int turns, BlockPos pos, BlockGetter blockAccess, MultiBufferSource bufferSource) {
        // Compatibility overload for external callers. The structure preview itself
        // uses the overload that receives the active level-render PoseStack.
        PoseStack poseStack = new PoseStack();
        renderRule(turns, pos, blockAccess, poseStack, bufferSource);
    }

    @OnlyIn(Dist.CLIENT)
    public void renderRule(int turns, BlockPos pos, BlockGetter blockAccess, PoseStack poseStack,
                           MultiBufferSource bufferSource) {
        BlockState previewState = getState(turns);
        if (previewState.isAir()) {
            return;
        }

        PreviewBlockAndTintGetter previewLevel = new PreviewBlockAndTintGetter(blockAccess);
        var dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(previewState);
        long seed = previewState.getSeed(pos);
        RandomSource random = RandomSource.create(seed);
        ModelData modelData = model.getModelData(previewLevel, pos, previewState, ModelData.EMPTY);

        poseStack.pushPose();
        try {
            /*
             * Vanilla's chunk builder translates the PoseStack to the block before
             * calling renderBatched. Do the same here. The parent PoseStack already
             * contains the camera translation supplied by RenderLevelStageEvent.
             * This is the important part the previous port was missing.
             */
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

            for (RenderType renderType : model.getRenderTypes(previewState, random, modelData)) {
                // Models may consume RandomSource while selecting quads. Reset it for
                // every layer so multi-layer models get deterministic matching quads.
                random.setSeed(seed);
                VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
                dispatcher.renderBatched(previewState, pos, previewLevel, poseStack, vertexConsumer,
                        true, random, modelData, renderType);
            }
        } finally {
            poseStack.popPose();
        }
    }

    /**
     * Adapts the template's plain {@link BlockGetter} to the {@link BlockAndTintGetter} required by the
     * 1.20 block renderer; block data comes from the template while light/tint/shade come from the client level.
     */
    @OnlyIn(Dist.CLIENT)
    private static final class PreviewBlockAndTintGetter implements BlockAndTintGetter {
        private final BlockGetter delegate;

        private PreviewBlockAndTintGetter(BlockGetter delegate) {
            this.delegate = delegate;
        }

        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            return delegate.getBlockEntity(pos);
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return delegate.getBlockState(pos);
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return delegate.getFluidState(pos);
        }

        @Override
        public int getHeight() {
            return delegate.getHeight();
        }

        @Override
        public int getMinBuildHeight() {
            return delegate.getMinBuildHeight();
        }

        @Override
        public float getShade(Direction direction, boolean shade) {
            Level level = Minecraft.getInstance().level;
            return level == null ? 1.0F : level.getShade(direction, shade);
        }

        @Override
        public LevelLightEngine getLightEngine() {
            Level level = Minecraft.getInstance().level;
            if (level == null) {
                throw new IllegalStateException("Cannot render a structure preview without a client level");
            }
            return level.getLightEngine();
        }

        @Override
        public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
            Level level = Minecraft.getInstance().level;
            return level == null ? -1 : level.getBlockTint(pos, colorResolver);
        }
    }

    public BlockState getState(int turns) {
        return BlockTools.rotateFacing(state, turns);
    }

    @Nullable
    public BlockEntity getTileEntity(int turns) {
        return null;
    }

    @SuppressWarnings("squid:S1172")
    public boolean isDynamicallyRendered(int turns) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public void renderRuleDynamic(int turns, BlockPos pos) {
        PoseStack poseStack = new PoseStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        renderRuleDynamic(turns, pos, poseStack, bufferSource);
        bufferSource.endBatch();
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void renderRuleDynamic(int turns, BlockPos pos, PoseStack poseStack, MultiBufferSource bufferSource) {
        BlockEntity blockEntity = getTileEntity(turns);
        if (blockEntity == null) {
            return;
        }
        var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);
        if (renderer == null) {
            return;
        }

        poseStack.pushPose();
        try {
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            renderer.render(blockEntity, 0.0F, poseStack, bufferSource,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        } finally {
            poseStack.popPose();
        }
    }
}
