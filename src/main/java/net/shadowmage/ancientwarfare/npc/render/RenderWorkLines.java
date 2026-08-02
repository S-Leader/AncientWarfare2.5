package net.shadowmage.ancientwarfare.npc.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shadowmage.ancientwarfare.core.compat.client.GlStateManager;
import net.shadowmage.ancientwarfare.core.util.RenderTools;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.item.ItemOrders;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class RenderWorkLines {
    public static final RenderWorkLines INSTANCE = new RenderWorkLines();

    private final List<BlockPos> positionList;

    private RenderWorkLines() {
        positionList = new ArrayList<>();
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void renderLastEvent(RenderLevelStageEvent evt) {
        if (evt.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        boolean render = AWNPCStatics.renderWorkPoints.getBoolean();
        if (!render) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        Item item = stack.getItem();
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemOrders)) {
            stack = player.getOffhandItem();
            item = stack.getItem();
        }
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemOrders)) {
            return;
        }

        GlStateManager.color(1.f, 1.f, 1.f, 1.f);
        positionList.addAll(((ItemOrders) item).getPositionsForRender(stack));

        if (positionList.size() > 0) {
            renderListOfPoints(player, evt.getPartialTick());
            positionList.clear();
        }
    }

    private void renderListOfPoints(Player player, float partialTick) {
        BlockPos prev = null;
        int index = 1;
        for (BlockPos point : positionList) {
            AABB bb = new AABB(0, 0, 0, 1, 1, 1);
            bb = bb.move(point.getX(), point.getY(), point.getZ());
            bb = RenderTools.adjustBBForPlayerPos(bb, player, partialTick);
            RenderTools.drawOutlinedBoundingBox(bb, 1.f, 1.f, 1.f);
            renderTextAt(player, point.getX() + 0.5d, point.getY() + 1.5d, point.getZ() + 0.5d, String.valueOf(index), partialTick);
            if (prev != null) {
                renderLineBetween(player, point.getX() + 0.5d, point.getY() + 0.5d, point.getZ() + 0.5d, prev.getX() + 0.5d, prev.getY() + 0.5d, prev.getZ() + 0.5d, partialTick);
            }
            prev = point;
            index++;
        }
    }

    private void renderLineBetween(Player player, double x1, double y1, double z1, double x2, double y2, double z2, float partialTick) {
        double ox = RenderTools.getRenderOffsetX(player, partialTick);
        double oy = RenderTools.getRenderOffsetY(player, partialTick);
        double oz = RenderTools.getRenderOffsetZ(player, partialTick);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();
        //1.12 used GL_LINE_LOOP with two vertices, which draws the same single segment; color/untextured state is now per-vertex + shader driven
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(x1 - ox, y1 - oy, z1 - oz).color(1.f, 1.f, 1.f, 0.4F).endVertex();
        buffer.vertex(x2 - ox, y2 - oy, z2 - oz).color(1.f, 1.f, 1.f, 0.4F).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        GlStateManager.disableBlend();
    }

    private void renderTextAt(Player player, double x, double y, double z, String text, float partialTick) {
        double ox = RenderTools.getRenderOffsetX(player, partialTick);
        double oy = RenderTools.getRenderOffsetY(player, partialTick);
        double oz = RenderTools.getRenderOffsetZ(player, partialTick);
        x -= ox;
        y -= oy;
        z -= oz;
        float f = 1.6F;
        float f1 = 0.016666668F * f;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.rotate(-player.getYRot(), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(player.getXRot(), 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-f1, -f1, f1);
        GlStateManager.disableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Font font = Minecraft.getInstance().font;
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        font.drawInBatch(text, 0, 0, 0xffffffff, false, new org.joml.Matrix4f(), buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        buffer.endBatch();
        GlStateManager.popMatrix();
    }
}
