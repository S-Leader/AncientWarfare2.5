package net.shadowmage.ancientwarfare.core.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.compat.client.GlStateManager;

@OnlyIn(Dist.CLIENT)
public class Rectangle extends GuiElement {

    private final int color, hoverColor;

    public Rectangle(int topLeftX, int topLeftY, int width, int height, int color, int hoverColor) {
        super(topLeftX, topLeftY, width, height);
        this.color = color;
        this.hoverColor = hoverColor;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        setColor(getColor(mouseX, mouseY));
        //shader color set above tints the white vertices, matching the fixed-function current-color behavior
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(renderX, renderY, 0).color(1.f, 1.f, 1.f, 1.f).endVertex();
        buffer.vertex(renderX, renderY + height, 0).color(1.f, 1.f, 1.f, 1.f).endVertex();
        buffer.vertex(renderX + width, renderY + height, 0).color(1.f, 1.f, 1.f, 1.f).endVertex();
        buffer.vertex(renderX + width, renderY, 0).color(1.f, 1.f, 1.f, 1.f).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        GlStateManager.color(1.f, 1.f, 1.f, 1.f);
    }

    protected int getColor(int mouseX, int mouseY) {
        return isMouseOverElement(mouseX, mouseY) ? hoverColor : color;
    }

    private void setColor(int color) {
        float r, g, b, a;
        r = (color >> 24) & 255;
        g = (color >> 16) & 255;
        b = (color >> 8) & 255;
        a = (color >> 0) & 255;
        r = (float) r / 255.f;
        g = (float) g / 255.f;
        b = (float) b / 255.f;
        a = (float) a / 255.f;
        GlStateManager.color(r, g, b, a);
    }

}
