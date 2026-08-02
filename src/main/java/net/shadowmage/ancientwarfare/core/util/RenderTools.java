package net.shadowmage.ancientwarfare.core.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.compat.client.LegacyRenderContext;

import javax.annotation.Nullable;
import java.awt.*;

@OnlyIn(Dist.CLIENT)
public final class RenderTools {
    private RenderTools() {
    }

    /**
     * Modern renderers pass packed light per vertex, so this method is retained as a compatibility hook.
     */
    public static void setFullColorLightmap() {
    }

    public static void renderQuarteredTexture(int textureWidth, int textureHeight, int texStartX, int texStartY,
                                              int texUsedWidth, int texUsedHeight, int renderStartX, int renderStartY, int renderWidth, int renderHeight) {
        float perX = 1F / textureWidth;
        float perY = 1F / textureHeight;
        float minU = texStartX * perX;
        float minV = texStartY * perY;
        float maxU = (texStartX + texUsedWidth) * perX;
        float maxV = (texStartY + texUsedHeight) * perY;
        float halfU = renderWidth * 0.5F * perX;
        float halfV = renderHeight * 0.5F * perY;
        float halfW = renderWidth * 0.5F;
        float halfH = renderHeight * 0.5F;
        renderTexturedQuad(renderStartX, renderStartY, renderStartX + halfW, renderStartY + halfH, minU, minV, minU + halfU, minV + halfV);
        renderTexturedQuad(renderStartX + halfW, renderStartY, renderStartX + renderWidth, renderStartY + halfH, maxU - halfU, minV, maxU, minV + halfV);
        renderTexturedQuad(renderStartX, renderStartY + halfH, renderStartX + halfW, renderStartY + renderHeight, minU, maxV - halfV, minU + halfU, maxV);
        renderTexturedQuad(renderStartX + halfW, renderStartY + halfH, renderStartX + renderWidth, renderStartY + renderHeight, maxU - halfU, maxV - halfV, maxU, maxV);
    }

    public static void renderTexturedQuad(float x1, float y1, float x2, float y2, float u1, float v1, float u2, float v2) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(x1, y2, 0).uv(u1, v2).endVertex();
        buffer.vertex(x2, y2, 0).uv(u2, v2).endVertex();
        buffer.vertex(x2, y1, 0).uv(u2, v1).endVertex();
        buffer.vertex(x1, y1, 0).uv(u1, v1).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }

    public static void renderColoredQuad(int x, int y, int width, int height, float red, float green, float blue) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        vertex(buffer, x, y + height, 0, red, green, blue, 1);
        vertex(buffer, x + width, y + height, 0, red, green, blue, 1);
        vertex(buffer, x + width, y, 0, red, green, blue, 1);
        vertex(buffer, x, y, 0, red, green, blue, 1);
        BufferUploader.drawWithShader(buffer.end());
    }

    public static void drawOutlinedBoundingBox2(AABB box, float red, float green, float blue, float width) {
        drawOutlinedBoundingBox(box, red, green, blue, false);
    }

    /**
     * Renders orientation axes using the current model-view transform.
     */
    public static void renderOrientationPoints(float colorMultiplier) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        line(buffer, 0, 0, 0, 1, 0, 0, colorMultiplier, 0, 0);
        line(buffer, 0, 0, 0, 0, 1, 0, 0, colorMultiplier, 0);
        line(buffer, 0, 0, 0, 0, 0, 1, 0, 0, colorMultiplier);
        BufferUploader.drawWithShader(buffer.end());
    }

    public static void drawTopSideOverlay(AABB box, Color color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        float red = color.getRed() / 255F;
        float green = color.getGreen() / 255F;
        float blue = color.getBlue() / 255F;
        float alpha = color.getAlpha() / 255F;
        float y = (float) box.maxY + 0.01F;
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        vertex(buffer, box.minX, y, box.maxZ, red, green, blue, alpha);
        vertex(buffer, box.maxX, y, box.maxZ, red, green, blue, alpha);
        vertex(buffer, box.maxX, y, box.minZ, red, green, blue, alpha);
        vertex(buffer, box.minX, y, box.minZ, red, green, blue, alpha);
        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.disableBlend();
    }

    public static void drawOutlinedBoundingBox(AABB box, Color color) {
        drawOutlinedBoundingBox(box, color, false);
    }

    public static void drawOutlinedBoundingBox(AABB box, Color color, boolean disableDepth) {
        drawOutlinedBoundingBox(box, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, disableDepth);
    }

    public static void drawOutlinedBoundingBox(AABB box, float red, float green, float blue) {
        drawOutlinedBoundingBox(box, red, green, blue, false);
    }

    private static void drawOutlinedBoundingBox(AABB box, float red, float green, float blue, boolean disableDepth) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (disableDepth) RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        outline(buffer, box, red, green, blue);
        BufferUploader.drawWithShader(buffer.end());
        if (disableDepth) RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static AABB adjustBBForPlayerPos(AABB box, Player player, float partialTick) {
        return box.move(-getRenderOffsetX(player, partialTick), -getRenderOffsetY(player, partialTick), -getRenderOffsetZ(player, partialTick));
    }

    public static double getRenderOffsetX(Player player, float partialTick) {
        return player.xOld + (player.getX() - player.xOld) * partialTick;
    }

    public static double getRenderOffsetY(Player player, float partialTick) {
        return player.yOld + (player.getY() - player.yOld) * partialTick;
    }

    public static double getRenderOffsetZ(Player player, float partialTick) {
        return player.zOld + (player.getZ() - player.zOld) * partialTick;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void renderTESR(@Nullable BlockEntity blockEntity, BlockPos pos) {
        if (blockEntity == null) return;
        BlockEntityRenderer renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);
        if (renderer == null) return;
        PoseStack poseStack = new PoseStack();
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        renderer.render(blockEntity, 0, poseStack, buffers, LightTexture.FULL_BRIGHT, 0);
        buffers.endBatch();
    }

    private static void vertex(BufferBuilder buffer, double x, double y, double z, float red, float green, float blue, float alpha) {
        PoseStack pose = LegacyRenderContext.pose();
        if (pose != null) {
            buffer.vertex(pose.last().pose(), (float) x, (float) y, (float) z)
                    .color(red, green, blue, alpha).endVertex();
        } else {
            buffer.vertex(x, y, z).color(red, green, blue, alpha).endVertex();
        }
    }

    private static void line(BufferBuilder buffer, double x1, double y1, double z1, double x2, double y2, double z2, float red, float green, float blue) {
        vertex(buffer, x1, y1, z1, red, green, blue, 1);
        vertex(buffer, x2, y2, z2, red, green, blue, 1);
    }

    private static void outline(BufferBuilder buffer, AABB b, float r, float g, float blue) {
        line(buffer, b.minX, b.minY, b.minZ, b.maxX, b.minY, b.minZ, r, g, blue);
        line(buffer, b.maxX, b.minY, b.minZ, b.maxX, b.minY, b.maxZ, r, g, blue);
        line(buffer, b.maxX, b.minY, b.maxZ, b.minX, b.minY, b.maxZ, r, g, blue);
        line(buffer, b.minX, b.minY, b.maxZ, b.minX, b.minY, b.minZ, r, g, blue);
        line(buffer, b.minX, b.maxY, b.minZ, b.maxX, b.maxY, b.minZ, r, g, blue);
        line(buffer, b.maxX, b.maxY, b.minZ, b.maxX, b.maxY, b.maxZ, r, g, blue);
        line(buffer, b.maxX, b.maxY, b.maxZ, b.minX, b.maxY, b.maxZ, r, g, blue);
        line(buffer, b.minX, b.maxY, b.maxZ, b.minX, b.maxY, b.minZ, r, g, blue);
        line(buffer, b.minX, b.minY, b.minZ, b.minX, b.maxY, b.minZ, r, g, blue);
        line(buffer, b.maxX, b.minY, b.minZ, b.maxX, b.maxY, b.minZ, r, g, blue);
        line(buffer, b.maxX, b.minY, b.maxZ, b.maxX, b.maxY, b.maxZ, r, g, blue);
        line(buffer, b.minX, b.minY, b.maxZ, b.minX, b.maxY, b.maxZ, r, g, blue);
    }
}
