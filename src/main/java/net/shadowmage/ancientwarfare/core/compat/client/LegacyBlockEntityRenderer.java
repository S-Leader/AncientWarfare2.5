package net.shadowmage.ancientwarfare.core.compat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Transitional adapter that lets an old TESR keep its legacy render body while
 * receiving Forge 1.20.1's pose stack and buffer source.
 *
 * <p>The coordinates passed to a modern BER are already local to the block, so
 * legacy x/y/z arguments are zero. Legacy GlStateManager calls are bridged to
 * the active PoseStack by LegacyRenderContext.</p>
 */
public abstract class LegacyBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    private PoseStack activePoseStack;
    private MultiBufferSource activeBufferSource;
    private int activePackedLight;
    private int activePackedOverlay;

    @Override
    public final void render(T blockEntity, float partialTick, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        PoseStack previousPose = activePoseStack;
        MultiBufferSource previousBuffers = activeBufferSource;
        int previousLight = activePackedLight;
        int previousOverlay = activePackedOverlay;
        activePoseStack = poseStack;
        activeBufferSource = bufferSource;
        activePackedLight = packedLight;
        activePackedOverlay = packedOverlay;
        try {
            LegacyRenderContext.run(poseStack,
                    () -> render(blockEntity, 0.0D, 0.0D, 0.0D, partialTick, -1, 1.0F));
        } finally {
            activePoseStack = previousPose;
            activeBufferSource = previousBuffers;
            activePackedLight = previousLight;
            activePackedOverlay = previousOverlay;
        }
    }

    public abstract void render(T blockEntity, double x, double y, double z,
                                float partialTick, int destroyStage, float alpha);

    protected final Font getFontRenderer() {
        return Minecraft.getInstance().font;
    }

    protected final PoseStack getActivePoseStack() {
        return activePoseStack;
    }

    protected final MultiBufferSource getActiveBufferSource() {
        return activeBufferSource;
    }

    protected final int getActivePackedLight() {
        return activePackedLight;
    }

    protected final int getActivePackedOverlay() {
        return activePackedOverlay;
    }

    protected final void bindTexture(ResourceLocation texture) {
        Minecraft.getInstance().getTextureManager().bindForSetup(texture);
    }

    /**
     * Old TESR name retained for subclasses that rendered outside normal range.
     */
    public boolean isGlobalRenderer(T blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return isGlobalRenderer(blockEntity);
    }
}
