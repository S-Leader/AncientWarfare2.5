package net.shadowmage.ancientwarfare.automation.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.shadowmage.ancientwarfare.core.block.BlockBase;
import net.shadowmage.ancientwarfare.core.render.model.LegacyBakery;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;

import java.util.List;

/**
 * Modern BER replacement for Forge's removed FastTESR path.
 */
public abstract class BaseAnimationRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    private final LegacyBakery bakery;

    protected BaseAnimationRenderer(LegacyBakery bakery) {
        this.bakery = bakery;
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
                       int packedLight, int packedOverlay) {
        // Chunk rebuild/removal can leave a BER entry alive for one frame. Torque
        // renderers query neighbouring block entities, so rendering that stale entry
        // can crash while the junction is being mined.
        if (blockEntity.isRemoved() || blockEntity.getLevel() == null
                || blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos()) != blockEntity) {
            return;
        }

        /*
         * Blocks using ENTITYBLOCK_ANIMATED have no chunk/breaking-overlay model.
         * Render their non-moving shell here before the animated parts. This is
         * currently used by the torque junction to avoid a native CCL/Rubidium
         * crash as soon as the breaking overlay is requested.
         */
        if (blockEntity.getBlockState().getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED
                && blockEntity.getBlockState().getBlock() instanceof BlockBase block) {
            LegacyModelState staticState = block.getLegacyModelState(
                    blockEntity.getBlockState(), blockEntity.getLevel(), blockEntity.getBlockPos());
            renderState(blockEntity, staticState, poseStack, buffers, packedLight, packedOverlay);
        }

        LegacyModelState state = handleState(blockEntity, partialTick, LegacyModelState.of(blockEntity.getBlockState()));
        renderState(blockEntity, state, poseStack, buffers, packedLight, packedOverlay);
    }

    private void renderState(T blockEntity, LegacyModelState state, PoseStack poseStack,
                             MultiBufferSource buffers, int packedLight, int packedOverlay) {
        List<BakedQuad> normalQuads = bakery.bakeQuads((Direction) null, state);
        if (!normalQuads.isEmpty()) {
            renderQuads(blockEntity, normalQuads, poseStack, buffers.getBuffer(RenderType.solid()),
                    packedLight, packedOverlay);
            return;
        }

        renderQuads(blockEntity, bakery.bakeLayerFace(null, RenderType.solid(), state), poseStack,
                buffers.getBuffer(RenderType.solid()), packedLight, packedOverlay);
        renderQuads(blockEntity, bakery.bakeLayerFace(null, RenderType.cutout(), state), poseStack,
                buffers.getBuffer(RenderType.cutout()), packedLight, packedOverlay);
        renderQuads(blockEntity, bakery.bakeLayerFace(null, RenderType.translucent(), state), poseStack,
                buffers.getBuffer(RenderType.translucent()), packedLight, packedOverlay);
    }

    private static void renderQuads(BlockEntity blockEntity, List<BakedQuad> quads, PoseStack poseStack,
                                    VertexConsumer consumer, int packedLight, int packedOverlay) {
        for (BakedQuad quad : quads) {
            float shade = blockEntity.getLevel() == null
                    ? 1.0F
                    : blockEntity.getLevel().getShade(quad.getDirection(), quad.isShade());
            consumer.putBulkData(poseStack.last(), quad, shade, shade, shade, packedLight, packedOverlay);
        }
    }

    protected abstract LegacyModelState handleState(T blockEntity, float partialTick, LegacyModelState state);
}
