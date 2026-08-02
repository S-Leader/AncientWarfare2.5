package net.shadowmage.ancientwarfare.core.gui.manual.elements;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;
import net.shadowmage.ancientwarfare.core.compat.client.GlStateManager;
import net.shadowmage.ancientwarfare.core.gui.manual.GuiManual;
import net.shadowmage.ancientwarfare.core.gui.manual.IElementWrapperCreator;
import net.shadowmage.ancientwarfare.core.manual.ImageElement;
import net.shadowmage.ancientwarfare.core.util.RenderTools;
import net.shadowmage.ancientwarfare.core.util.TextureUtils;

import java.util.List;

public class ImageElementWrapper extends BaseElementWrapper<ImageElement> {
    private static final int BOTTOM_PADDING = 4;
    private final ResourceLocation texture;

    public ImageElementWrapper(GuiManual gui, int topLeftY, int width, int height, ImageElement element) {
        super(gui, 0, topLeftY, width, height, element);

        texture = TextureUtils.getTextureLocation("registry/manual/" + getElement().getPath().replaceFirst("^/", ""));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        super.render(mouseX, mouseY, partialTick);
        RenderSystem.setShaderTexture(0, texture);
        int textureWidth = getElement().getWidth();
        int textureHeight = getElement().getHeight();
        float scale = getScale(width, textureWidth);
        int scaledWidth = (int) (scale * textureWidth);
        int padding = Math.min(0, (width - scaledWidth) / 2);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        //scaled draw of the full texture region (0,0 - textureWidth,textureHeight), replacing Gui.drawScaledCustomSizeModalRect
        RenderTools.renderTexturedQuad(renderX + padding, renderY, renderX + padding + scaledWidth, renderY + (int) (textureHeight * scale),
                0.0F, 0.0F, 1.0F, 1.0F);
    }

    private static float getScale(int width, int textureWidth) {
        return width < textureWidth ? (float) width / textureWidth : 1.0f;
    }

    public static class Creator implements IElementWrapperCreator<ImageElement> {
        @Override
        public List<BaseElementWrapper<ImageElement>> construct(GuiManual gui, int topLeftY, int width, int remainingPageHeight, int emptyPageHeight, ImageElement element) {
            int scaledHeight = (int) (getScale(width, element.getWidth()) * element.getHeight());
            return ImmutableList.of(new ImageElementWrapper(gui, remainingPageHeight < scaledHeight ? 0 : topLeftY, width, scaledHeight + BOTTOM_PADDING, element));
        }
    }
}
