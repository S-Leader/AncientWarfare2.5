package net.shadowmage.ancientwarfare.core.gui.elements;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.shadowmage.ancientwarfare.core.compat.client.GlStateManager;
import net.shadowmage.ancientwarfare.core.gui.GuiContainerBase;
import net.shadowmage.ancientwarfare.core.interfaces.ITooltipRenderer;
import net.shadowmage.ancientwarfare.core.util.RenderTools;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class CompositeItemSlots extends CompositeScrolled {
    private List<ItemSlot> itemSlots = new ArrayList<>();
    ITooltipRenderer render;

    public CompositeItemSlots(GuiContainerBase gui, int topLeftX, int topLeftY, int width, int height, ITooltipRenderer render) {
        super(gui, topLeftX, topLeftY, width, height);
        this.render = render;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        if (!isMouseOverElement(mouseX, mouseY)) {
            mouseX = Integer.MIN_VALUE;
            mouseY = Integer.MIN_VALUE;
        }
        RenderSystem.setShaderTexture(0, backgroundTextureLocation);
        //render background before setting viewport so that it is not cropped
        RenderTools.renderQuarteredTexture(256, 256, 0, 0, 256, 240, renderX, renderY, width, height);
        setViewport();
        for (GuiElement element : this.elements) {
            if (element.renderY > renderY + height || element.renderY + element.height < renderY
                    || element.renderX > renderX + width || element.renderX + element.width < renderX) {
                continue;
            }

            if (element instanceof ItemSlot) {
                itemSlots.add((ItemSlot) element);
            } else {
                element.render(mouseX, mouseY, partialTick);
            }
        }

        Minecraft mc = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, widgetTexture1);
        GuiGraphics graphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());

        //needs texture enabled
        //lighting, full color and alpha, depth-test disabled
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.color(1.f, 1.f, 1.f, 1.f);
        for (ItemSlot slot : itemSlots) {
            renderSlotBackground(slot);
        }

        //needs texture, lighting, color, alpha, and depth-test enabled
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        Lighting.setupFor3DItems();
        //z-offset previously applied through itemRender.zLevel is handled internally by GuiGraphics#renderItem
        for (ItemSlot slot : itemSlots) {
            renderItemStack(graphics, slot);
        }
        Lighting.setupForFlatItems();

        //needs texture(fonts), color, alpha enabled
        //needs lighting, depth-test disabled
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();
        for (ItemSlot slot : itemSlots) {
            renderOverlay(graphics, slot);
        }

        //needs texture disabled (draw white quad @ alpha for highlight)
        for (ItemSlot slot : itemSlots) {
            renderSlotHighlight(slot, mouseX, mouseY);
        }
        //reset renderabled and render-viewport
        itemSlots.clear();
        popViewport();

        //scroll-bar rendering, needs lighting/depth test disabled, texture enabled
        GlStateManager.enableTexture2D();
        scrollbar.render(mouseX, mouseY, partialTick);
    }

    private void renderSlotBackground(ItemSlot slot) {
        if (slot.visible && slot.renderSlotBackground) {
            RenderTools.renderQuarteredTexture(256, 256, 152, 120, 18, 18, slot.renderX, slot.renderY, slot.width, slot.height);
        }
    }

    private void renderSlotHighlight(ItemSlot slot, int mouseX, int mouseY) {
        ItemStack stack = slot.getStack();
        if (stack.isEmpty() || !slot.visible) {
            return;
        }
        if (slot.highlightOnMouseOver && slot.isMouseOverElement(mouseX, mouseY)) {
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.disableTexture2D();
            GlStateManager.color(1.f, 1.f, 1.f, 0.5f);

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            //shader color set above (with alpha) tints the white vertices, matching the fixed-function current-color behavior
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buffer.vertex(slot.renderX, slot.renderY, 0).color(1.f, 1.f, 1.f, 1.f).endVertex();
            buffer.vertex(slot.renderX, slot.renderY + slot.height, 0).color(1.f, 1.f, 1.f, 1.f).endVertex();
            buffer.vertex(slot.renderX + slot.width, slot.renderY + slot.height, 0).color(1.f, 1.f, 1.f, 1.f).endVertex();
            buffer.vertex(slot.renderX + slot.width, slot.renderY, 0).color(1.f, 1.f, 1.f, 1.f).endVertex();
            BufferUploader.drawWithShader(buffer.end());
            GlStateManager.disableBlend();
            if (slot.renderTooltip && !slot.getStack().isEmpty() && render != null) {
                if (slot.tooltip != null) {
                    this.render.handleElementTooltipRender(slot.tooltip, mouseX, mouseY);
                } else {
                    this.render.handleItemStackTooltipRender(slot.getStack(), mouseX, mouseY);
                }
            }
            GlStateManager.color(1.f, 1.f, 1.f, 1.f);
            GlStateManager.enableDepth();
        }
    }

    private void renderOverlay(GuiGraphics graphics, ItemSlot slot) {
        ItemStack stack = slot.getStack();
        if (stack.isEmpty() || !slot.visible) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Font font = IClientItemExtensions.of(stack.getItem()).getFont(stack, IClientItemExtensions.FontContext.ITEM_COUNT);
        if (font == null) {
            font = mc.font;
        }
        graphics.renderItemDecorations(font, stack, slot.renderX + 1, slot.renderY + 1, "");
        if (slot.renderItemQuantity && slot.getStack().getCount() > 1) {
            slot.renderStackSize(slot.renderX + 1, slot.renderY + 1, stack.getCount(), font);
        }
    }

    private void renderItemStack(GuiGraphics graphics, ItemSlot slot) {
        ItemStack stack = slot.getStack();
        if (stack.isEmpty() || !slot.visible) {
            return;
        }
        graphics.renderItem(stack, slot.renderX + 1, slot.renderY + 1);
    }

}
