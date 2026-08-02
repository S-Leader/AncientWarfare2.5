package net.shadowmage.ancientwarfare.core.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.util.RenderTools;

@OnlyIn(Dist.CLIENT)
public class TexturedRectangle extends GuiElement {

    ResourceLocation texture;
    private float u1;
    private float v1;
    private float u2;
    private float v2;

    public TexturedRectangle(int topLeftX, int topLeftY, int width, int height, ResourceLocation texture, int tx, int ty, int u, int v, int uw, int vh) {
        super(topLeftX, topLeftY, width, height);

        float perX = 1.f / ((float) tx);
        float perY = 1.f / ((float) ty);
        u1 = ((float) u) * perX;
        v1 = ((float) v) * perY;
        u2 = (float) (u + uw) * perX;
        v2 = (float) (v + vh) * perY;
        this.texture = texture;
    }

    public void setTexture(ResourceLocation texture) {
        this.texture = texture;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        if (visible && texture != null) {
            RenderSystem.setShaderTexture(0, texture);
            RenderTools.renderTexturedQuad(renderX, renderY, renderX + width, renderY + height, u1, v1, u2, v2);
        }
    }

}
