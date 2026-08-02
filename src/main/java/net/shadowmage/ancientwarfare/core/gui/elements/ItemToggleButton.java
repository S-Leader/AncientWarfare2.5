package net.shadowmage.ancientwarfare.core.gui.elements;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.compat.client.ClientSoundHelper;
import net.shadowmage.ancientwarfare.core.compat.client.GlStateManager;
import net.shadowmage.ancientwarfare.core.gui.GuiContainerBase;
import net.shadowmage.ancientwarfare.core.gui.Listener;
import net.shadowmage.ancientwarfare.core.util.RenderTools;

public abstract class ItemToggleButton extends GuiElement {
    private final ItemStack item;
    private boolean pressed = false;
    private boolean toggled = false;

    public ItemToggleButton(int topLeftX, int topLeftY, ItemStack item) {
        this(topLeftX, topLeftY, item, true);
    }

    public ItemToggleButton(int topLeftX, int topLeftY, ItemStack item, boolean canClickUntoggle) {
        super(topLeftX, topLeftY, 20, 20);
        this.item = item;

        this.addNewListener(new Listener(Listener.MOUSE_UP) {
            @Override
            public boolean onEvent(GuiElement widget, GuiContainerBase.ActivationEvent evt) {
                if (pressed && enabled && visible && isMouseOverElement(evt.mx, evt.my)) {
                    ClientSoundHelper.playButtonClick();
                    onPressed(evt.mButton);
                }
                pressed = false;
                return true;
            }
        });
        this.addNewListener(new Listener(Listener.MOUSE_DOWN) {
            @Override
            public boolean onEvent(GuiElement widget, GuiContainerBase.ActivationEvent evt) {
                if (enabled && visible && isMouseOverElement(evt.mx, evt.my) && (canClickUntoggle || !toggled)) {
                    pressed = true;
                    toggled = !toggled;
                }
                return true;
            }
        });

    }

    protected abstract void onPressed(int mButton);

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        if (visible) {
            RenderSystem.setShaderTexture(0, widgetTexture2);
            int textureSize = 256;
            int startX = 0;
            int startY = 40;

            if (enabled) {
                if (isMouseOverElement(mouseX, mouseY)) {
                    startY = toggled ? 160 : 80;
                } else {
                    startY = toggled ? 120 : 40;
                }
            }
            int usedWidth = 256;
            int usedHeight = 40;
            RenderTools.renderQuarteredTexture(textureSize, textureSize, startX, startY, usedWidth, usedHeight, renderX, renderY, width, height);

            GlStateManager.enableRescaleNormal();
            GlStateManager.enableDepth();
            Lighting.setupFor3DItems();
            Minecraft mc = Minecraft.getInstance();
            GuiGraphics graphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
            graphics.renderItem(item, renderX + 2, renderY + 2);
            Lighting.setupForFlatItems();
            GlStateManager.disableRescaleNormal();
        }
    }

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }
}
