package net.shadowmage.ancientwarfare.core.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.compat.client.GlStateManager;

@OnlyIn(Dist.CLIENT)
public class Line extends GuiElement {

    int lineWidth;
    int color;
    int x2, y2;

    /*
     * @param color RGBA color
     */
    public Line(int topLeftX, int topLeftY, int x2, int y2, int lineWidth, int color) {
        super(topLeftX, topLeftY, x2 - topLeftX, y2 - topLeftY);
        this.color = color;
        this.x2 = x2 - topLeftX;
        this.y2 = y2 - topLeftY;
        this.lineWidth = lineWidth;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();

        GlStateManager.glLineWidth(lineWidth * getScaleFactor());
        setColor();
        //a 2-vertex GL_LINE_LOOP is a single segment; shader color set above tints the white vertices
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(renderX, renderY, 0).color(1.f, 1.f, 1.f, 1.f).endVertex();
        buffer.vertex(renderX + x2, renderY + y2, 0).color(1.f, 1.f, 1.f, 1.f).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        GlStateManager.color(1.f, 1.f, 1.f, 1.f);

        GlStateManager.enableTexture2D();
    }

    private void setColor() {
        float r, g, b, a;
        r = (color & 0xff000000) >> 24;
        g = (color & 0x00ff0000) >> 16;
        b = (color & 0x0000ff00) >> 8;
        a = (color & 0x000000ff) >> 0;
        r = (float) r / 255.f;
        g = (float) g / 255.f;
        b = (float) b / 255.f;
        a = (float) a / 255.f;
        GlStateManager.color(r, g, b, a);
    }

    private int getScaleFactor() {
        Minecraft mc = Minecraft.getInstance();
        int scaledWidth = mc.getWindow().getWidth();
        int scaledHeight = mc.getWindow().getHeight();
        int scaleFactor = 1;
        int guiScale = mc.options.guiScale().get();
        if (guiScale == 0) {
            guiScale = 1000;
        }
        while (scaleFactor < guiScale && scaledWidth / (scaleFactor + 1) >= 320 && scaledHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }
        return scaleFactor;
    }

}
