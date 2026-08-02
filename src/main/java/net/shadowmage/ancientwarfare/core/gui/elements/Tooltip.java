package net.shadowmage.ancientwarfare.core.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.compat.client.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class Tooltip {

    private List<GuiElement> children = new ArrayList<>();
    int width, height;

    /*
     * actual size of the tooltip is width+8, height+8 (it draws some borders around the internals)
     */
    public Tooltip(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void renderTooltip(int mouseX, int mouseY, float partialTick) {
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        if (mouseX + this.width > screenWidth) {
            mouseX -= 28 + this.width;
        }
        if (mouseY + this.height + 6 > screenHeight) {
            mouseY = screenHeight - this.height - 6;
        }

        // tooltip needs a right-offset from cursor
        mouseX += 12;

        pushViewport(mouseX - 4, mouseY - 4, this.width + 8, this.height + 8);

        for (GuiElement element : this.children) {
            element.updateGuiPosition(mouseX, mouseY);
        }

        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.disableRescaleNormal();
        drawBackground(mouseX, mouseY);

        for (GuiElement element : this.children) {
            element.render(-1000, -1000, partialTick);
        }
        disableViewport();
    }

    public final void addTooltipElement(GuiElement element) {
        if (this.width < element.width) {
            this.width = element.width + 2;
        }
        if (this.height < element.height) {
            this.height = element.height + 2;
        }
        this.children.add(element);
    }

    private void drawBackground(int mouseX, int mouseY) {
        int color1 = -267386864;
        this.drawGradientRect(mouseX - 3, mouseY - 4, mouseX + this.width + 3, mouseY - 3, color1, color1);
        this.drawGradientRect(mouseX - 3, mouseY + this.height + 3, mouseX + this.width + 3, mouseY + this.height + 4, color1, color1);
        this.drawGradientRect(mouseX - 3, mouseY - 3, mouseX + this.width + 3, mouseY + this.height + 3, color1, color1);
        this.drawGradientRect(mouseX - 4, mouseY - 3, mouseX - 3, mouseY + this.height + 3, color1, color1);
        this.drawGradientRect(mouseX + this.width + 3, mouseY - 3, mouseX + this.width + 4, mouseY + this.height + 3, color1, color1);
        int color2 = 1347420415;
        int color3 = (color2 & 16711422) >> 1 | color2 & -16777216;
        this.drawGradientRect(mouseX - 3, mouseY - 3 + 1, mouseX - 3 + 1, mouseY + this.height + 3 - 1, color2, color3);
        this.drawGradientRect(mouseX + this.width + 2, mouseY - 3 + 1, mouseX + this.width + 3, mouseY + this.height + 3 - 1, color2, color3);
        this.drawGradientRect(mouseX - 3, mouseY - 3, mouseX + this.width + 3, mouseY - 3 + 1, color2, color2);
        this.drawGradientRect(mouseX - 3, mouseY + this.height + 2, mouseX + this.width + 3, mouseY + this.height + 3, color3, color3);
    }

    private void drawGradientRect(int par1, int par2, int par3, int par4, int par5, int par6) {
        float f = (float) (par5 >> 24 & 255) / 255.0F;
        float f1 = (float) (par5 >> 16 & 255) / 255.0F;
        float f2 = (float) (par5 >> 8 & 255) / 255.0F;
        float f3 = (float) (par5 & 255) / 255.0F;
        float f4 = (float) (par6 >> 24 & 255) / 255.0F;
        float f5 = (float) (par6 >> 16 & 255) / 255.0F;
        float f6 = (float) (par6 >> 8 & 255) / 255.0F;
        float f7 = (float) (par6 & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(par3, par2, 0).color(f1, f2, f3, f).endVertex();
        bufferBuilder.vertex(par1, par2, 0).color(f1, f2, f3, f).endVertex();
        bufferBuilder.vertex(par1, par4, 0).color(f5, f6, f7, f4).endVertex();
        bufferBuilder.vertex(par3, par4, 0).color(f5, f6, f7, f4).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    private static void pushViewport(int x, int y, int w, int h) {
        int tlx, tly, brx, bry;
        tlx = x;
        tly = y;
        brx = x + w;
        bry = y + h;

        x = tlx;
        y = tly;
        w = brx - tlx;
        h = bry - tly;

        Minecraft mc = Minecraft.getInstance();
        double guiScale = mc.getWindow().getGuiScale();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) Math.floor(x * guiScale), mc.getWindow().getHeight() - (int) Math.ceil((y + h) * guiScale), (int) Math.ceil(w * guiScale), (int) Math.ceil(h * guiScale));
    }

    private static void disableViewport() {
        GL11.glScissor(0, 0, Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight());
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

}
