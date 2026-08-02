package net.shadowmage.ancientwarfare.core.gui.elements;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.shadowmage.ancientwarfare.core.compat.client.GlStateManager;
import net.shadowmage.ancientwarfare.core.gui.GuiContainerBase.ActivationEvent;
import net.shadowmage.ancientwarfare.core.gui.Listener;
import net.shadowmage.ancientwarfare.core.interfaces.ITooltipRenderer;
import net.shadowmage.ancientwarfare.core.util.RenderTools;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class ItemSlot extends GuiElement {
    private ItemStack item = ItemStack.EMPTY;
    protected ITooltipRenderer render;
    protected boolean highlightOnMouseOver = true;
    protected boolean renderItemQuantity = true;
    protected boolean renderSlotBackground = true;
    protected boolean renderLabel = false;
    private Consumer<ItemStack> quantityChangeListener;

    public ItemSlot(int topLeftX, int topLeftY, ItemStack item, ITooltipRenderer render) {
        super(topLeftX - 1, topLeftY - 1, 18, 18);
        this.item = item;
        this.render = render;

        Listener listener = new Listener(Listener.MOUSE_DOWN) {
            @Override
            public boolean onEvent(GuiElement widget, ActivationEvent evt) {
                if (widget.isMouseOverElement(evt.mx, evt.my)) {
                    if (adjustQuantity(evt.mButton == 1)) {
                        return true;
                    }
                    ItemStack stack = Minecraft.getInstance().player.containerMenu.getCarried();
                    onSlotClicked(stack, evt.mButton == 1);
                }
                return true;
            }
        };
        addNewListener(listener);
    }

    public ItemSlot setQuantityAdjustable(Consumer<ItemStack> changeListener) {
        quantityChangeListener = changeListener;
        return this;
    }

    private boolean adjustQuantity(boolean decrease) {
        if (quantityChangeListener == null || item.isEmpty()) {
            return false;
        }
        int amount = Screen.hasShiftDown() ? 32 : Screen.hasControlDown() ? 1 : 0;
        if (amount == 0) {
            return false;
        }

        long adjusted = decrease ? (long) item.getCount() - amount : (long) item.getCount() + amount;
        item.setCount((int) Math.max(1L, Math.min(Integer.MAX_VALUE, adjusted)));
        quantityChangeListener.accept(item.copy());
        return true;
    }

    public void setRenderLabel(boolean val) {
        this.renderLabel = val;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public ItemSlot setRenderTooltip(boolean val) {
        this.renderTooltip = val;
        return this;
    }

    public ItemSlot setRenderItemQuantity(boolean val) {
        this.renderItemQuantity = val;
        return this;
    }

    public ItemSlot setHighlightOnMouseOver(boolean val) {
        this.highlightOnMouseOver = val;
        return this;
    }

    public ItemSlot setRenderSlotBackground(boolean val) {
        this.renderSlotBackground = val;
        return this;
    }

    public ItemStack getStack() {
        return item;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        if (visible) {
            Minecraft mc = Minecraft.getInstance();
            if (renderSlotBackground) {
                RenderSystem.setShaderTexture(0, widgetTexture1);
                RenderTools.renderQuarteredTexture(256, 256, 152, 120, 18, 18, renderX, renderY, width, height);
            }

            if (!this.item.isEmpty()) {
                //z-offset previously applied through itemRender.zLevel is handled internally by GuiGraphics#renderItem
                Font font = IClientItemExtensions.of(item.getItem()).getFont(item, IClientItemExtensions.FontContext.ITEM_COUNT);
                if (font == null) {
                    font = mc.font;
                }

                GuiGraphics graphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
                GlStateManager.enableRescaleNormal();
                Lighting.setupFor3DItems();
                graphics.renderItem(item, renderX + 1, renderY + 1);
                if (renderItemQuantity && item.getCount() > 1) {
                    graphics.renderItemDecorations(font, item, renderX + 1, renderY + 1, "");
                    renderStackSize(renderX + 1, renderY + 1, item.getCount(), font);
                }
                Lighting.setupForFlatItems();
                GlStateManager.disableRescaleNormal();
                if (renderLabel) {
                    int x = renderX + 18;
                    int y = renderY + 3;
                    GlStateManager.color(1.f, 1.f, 1.f, 1.f);
                    drawString(font, item.getDisplayName().getString(), x, y, 0xffffffff, true);
                }
            }

            if (isMouseOverElement(mouseX, mouseY)) {
                if (highlightOnMouseOver) {
                    // Matches AbstractContainerScreen's vanilla slot highlight (0x80FFFFFF).
                    GlStateManager.color(1.f, 1.f, 1.f, 0.5f);

                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GlStateManager.disableLighting();
                    GlStateManager.disableTexture2D();
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(0, 0, 200);
                    //shader color set above (with alpha) tints the white vertices, matching the fixed-function current-color behavior
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);
                    BufferBuilder buffer = Tesselator.getInstance().getBuilder();
                    buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                    buffer.vertex(renderX + 1, renderY + 1, 0).color(255, 255, 255, 128).endVertex();
                    buffer.vertex(renderX + 1, renderY + 1 + (height - 2), 0).color(255, 255, 255, 128).endVertex();
                    buffer.vertex(renderX + 1 + (width - 2), renderY + 1 + (height - 2), 0).color(255, 255, 255, 128).endVertex();
                    buffer.vertex(renderX + 1 + (width - 2), renderY + 1, 0).color(255, 255, 255, 128).endVertex();
                    BufferUploader.drawWithShader(buffer.end());
                    GlStateManager.popMatrix();
                    GlStateManager.color(1.f, 1.f, 1.f, 1.f);
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                }
                if (renderTooltip && this.render != null) {
                    if (this.tooltip != null) {
                        this.render.handleElementTooltipRender(tooltip, mouseX, mouseY);
                    } else if (!this.item.isEmpty()) {
                        this.render.handleItemStackTooltipRender(item, mouseX, mouseY);
                    }
                }
            }
            GlStateManager.color(1, 1, 1, 1);
        }
    }

    public void renderStackSize(int renderX, int renderY, int stackSize, Font fr) {
        GlStateManager.pushMatrix();
        float ox = renderX + 16, oy = renderY + 8;
        GlStateManager.translate(ox + 0.5f, oy + 0.5f, 0);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();

        String s1 = String.valueOf(stackSize);

        float w = fr.width(s1);
        float scale = stackSize > 99 ? 0.5f : 1.f;
        int oy1 = stackSize > 99 ? 6 : 0;

        GlStateManager.scale(scale, scale, scale);

        drawString(fr, s1, -(int) w, oy1, 16777215, true);

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    public void onSlotClicked(ItemStack stack, boolean rightClicked) {

    }

}
