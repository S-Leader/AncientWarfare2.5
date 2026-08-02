package net.shadowmage.ancientwarfare.core.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Label extends GuiElement {

    private String text;
    private boolean renderCentered = false;
    private int color = 0xffffffff;
    private boolean renderShadow;

    public Label(int topLeftX, int topLeftY, String text) {
        super(topLeftX, topLeftY);
        setText(text);
    }

    public Label setRenderCentered() {
        this.renderCentered = true;
        return this;
    }

    public Label setColor(int color) {
        this.color = color;
        return this;
    }

    public Label setShadow(boolean renderShadow) {
        this.renderShadow = renderShadow;
        return this;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        if (visible) {
            if (renderCentered) {
                drawText(width / 2);
            } else {
                drawText(0);
            }
        }
    }

    private void drawText(int offset) {
        if (width < Minecraft.getInstance().font.width(text)) {
            drawSplitString(text, renderX - offset, renderY, width, 0xDDDDDD);
        } else {
            if (renderShadow) {
                drawStringWithShadow(text, renderX - offset, renderY, color);
            } else {
                drawString(text, renderX - offset, renderY, color);
            }
        }
    }

    public void setText(String text) {
        if (text == null)
            text = "";
        this.text = I18n.get(text);
        this.setTooltipIfFound(text);
        this.width = Minecraft.getInstance().font.width(this.text);
        this.height = 8;
    }

    public String getText() {
        return text;
    }

}
