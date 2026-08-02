package net.shadowmage.ancientwarfare.core.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.core.gui.elements.GuiElement;
import net.shadowmage.ancientwarfare.core.gui.elements.Tooltip;
import net.shadowmage.ancientwarfare.core.interfaces.IContainerGuiCallback;
import net.shadowmage.ancientwarfare.core.interfaces.ITooltipRenderer;
import net.shadowmage.ancientwarfare.core.interfaces.IWidgetSelection;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Modern container-screen host with compatibility hooks for Ancient Warfare's
 * existing widget hierarchy. Subclasses can keep their old init/draw methods
 * while input and lifecycle are driven by the 1.20 screen API.
 */
public abstract class GuiContainerBase<T extends ContainerBase> extends AbstractContainerScreen<T>
        implements IContainerGuiCallback, ITooltipRenderer, IWidgetSelection {
    private static final LinkedList<Viewport> VIEWPORT_STACK = new LinkedList<>();

    protected final T inventorySlots;
    protected final Minecraft mc = Minecraft.getInstance();
    protected int xSize;
    protected int ySize;
    protected int guiLeft;
    protected int guiTop;
    protected final Player player;

    private final List<GuiElement> elements = new ArrayList<>();
    private GuiElement selectedWidget;
    protected boolean shouldCloseOnVanillaKeys = true;
    private boolean initDone;
    private boolean shouldUpdate;
    private float partialRenderTick;
    private Tooltip elementTooltip;
    private int elementTooltipX;
    private int elementTooltipY;
    private ItemStack tooltipStack = ItemStack.EMPTY;
    private int tooltipX;
    private int tooltipY;
    private ResourceLocation backgroundTexture;
    private GuiGraphics currentGraphics;

    protected GuiContainerBase(ContainerBase container, int width, int height) {
        super((T) container, container.player.getInventory(), Component.empty());
        this.inventorySlots = (T) container;
        this.player = container.player;
        this.xSize = width;
        this.ySize = height;
        this.imageWidth = width;
        this.imageHeight = height;
        this.backgroundTexture = GuiElement.backgroundTextureLocation;
        container.setGui(this);
    }

    public GuiContainerBase(ContainerBase container) {
        this(container, 256, 240);
    }

    public final void setTexture(String name) {
        backgroundTexture = new ResourceLocation(name);
    }

    public final T getContainer() {
        return inventorySlots;
    }

    @Override
    public void handleItemStackTooltipRender(ItemStack itemStack, int mouseX, int mouseY) {
        tooltipStack = itemStack;
        tooltipX = mouseX;
        tooltipY = mouseY;
    }

    @Override
    public void handleElementTooltipRender(Tooltip tooltip, int mouseX, int mouseY) {
        elementTooltip = tooltip;
        elementTooltipX = mouseX;
        elementTooltipY = mouseY;
    }

    protected void clearElements() {
        if (selectedWidget != null && elements.contains(selectedWidget)) selectedWidget = null;
        elements.clear();
    }

    protected void addGuiElement(GuiElement element) {
        elements.add(element);
    }

    protected void removeGuiElement(GuiElement element) {
        elements.remove(element);
    }

    @Override
    protected final void init() {
        initGui();
    }

    /**
     * Legacy initialization hook retained for all existing screens.
     */
    public void initGui() {
        super.init();
        imageWidth = xSize;
        imageHeight = ySize;
        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;
        guiLeft = leftPos;
        guiTop = topPos;
        if (!initDone) {
            initElements();
            initDone = true;
        }
        setupElements();
        for (GuiElement element : elements) element.updateGuiPosition(guiLeft, guiTop);
    }

    @Override
    public final void containerTick() {
        updateScreen();
    }

    public void updateScreen() {
        if (shouldUpdate) {
            inventorySlots.setGui(this);
            initGui();
            shouldUpdate = false;
        }
    }

    @Override
    public final void onClose() {
        onGuiClosed();
        super.onClose();
    }

    public void onGuiClosed() {
    }

    protected final void closeGui() {
        if (onGuiCloseRequested() && minecraft != null) {
            // Follow AbstractContainerScreen's vanilla close path. Calling
            // setScreen(null) directly removes the pixels but never sends the
            // serverbound container-close packet, leaving the previous menu active
            // and causing later NPC/worksite interactions to be discarded.
            onClose();
        }
    }

    protected boolean onGuiCloseRequested() {
        return true;
    }

    @Override
    public final void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        currentGraphics = graphics;
        try {
            drawScreen(mouseX, mouseY, partialTick);
        } finally {
            currentGraphics = null;
        }
    }

    /**
     * Legacy render hook. Existing subclasses overriding drawScreen continue to work.
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (currentGraphics == null) return;
        renderBackground(currentGraphics);
        partialRenderTick = partialTicks;
        super.render(currentGraphics, mouseX, mouseY, partialTicks);
        renderTooltip(currentGraphics, mouseX, mouseY);
        if (!tooltipStack.isEmpty()) {
            currentGraphics.renderTooltip(font, tooltipStack, tooltipX, tooltipY);
            tooltipStack = ItemStack.EMPTY;
        }
        if (elementTooltip != null) {
            elementTooltip.renderTooltip(elementTooltipX, elementTooltipY, partialRenderTick);
            elementTooltip = null;
        }
    }

    @Override
    protected final void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        currentGraphics = graphics;
        drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    }

    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        if (currentGraphics == null) return;
        if (backgroundTexture != null) {
            RenderSystem.setShaderTexture(0, backgroundTexture);
            currentGraphics.blit(backgroundTexture, guiLeft, guiTop, xSize, ySize, 0, 0, 256, 240, 256, 256);
        }
        for (Slot slot : inventorySlots.slots) {
            currentGraphics.blit(GuiElement.widgetTexture1, slot.x - 1 + guiLeft, slot.y - 1 + guiTop, 152, 120, 18, 18);
        }
    }

    @Override
    protected final void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        currentGraphics = graphics;
        drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        if (currentGraphics == null) return;
        currentGraphics.pose().pushPose();
        currentGraphics.pose().translate(-guiLeft, -guiTop, 0);
        long time = System.currentTimeMillis();
        for (GuiElement element : elements) {
            element.render(mouseX, mouseY, partialRenderTick);
            element.postRender(mouseX, mouseY, partialRenderTick, time, this);
        }
        currentGraphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        dispatchMouse(Listener.MOUSE_DOWN, button, true, mouseX, mouseY, 0);
        return selectedWidget != null || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dispatchMouse(Listener.MOUSE_UP, button, false, mouseX, mouseY, 0);
        return selectedWidget != null || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        dispatchMouse(Listener.MOUSE_WHEEL, -1, false, mouseX, mouseY, (int) Math.signum(delta));
        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        dispatchMouse(Listener.MOUSE_MOVED, -1, false, mouseX, mouseY, 0);
        super.mouseMoved(mouseX, mouseY);
    }

    private void dispatchMouse(int type, int button, boolean state, double x, double y, int wheel) {
        ActivationEvent event = new ActivationEvent(type, button, state, (int) x, (int) y, wheel);
        for (GuiElement element : elements) element.handleMouseInput(event);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        ActivationEvent event = new ActivationEvent(Listener.KEY_DOWN, key, '\0', true);
        for (GuiElement element : elements) element.handleKeyboardInput(event);
        if (selectedWidget != null) return true;
        if (key == GLFW.GLFW_KEY_ESCAPE || minecraft.options.keyInventory.matches(key, scanCode)) {
            if (shouldCloseOnVanillaKeys) closeGui();
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int key, int scanCode, int modifiers) {
        ActivationEvent event = new ActivationEvent(Listener.KEY_UP, key, '\0', false);
        for (GuiElement element : elements) element.handleKeyboardInput(event);
        return selectedWidget != null || super.keyReleased(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        ActivationEvent event = new ActivationEvent(Listener.KEY_DOWN, 0, character, true);
        for (GuiElement element : elements) element.handleKeyboardInput(event);
        return selectedWidget != null || super.charTyped(character, modifiers);
    }

    protected void keyTyped(char character, int key) {
        keyPressed(key, 0, 0);
        if (character != '\0') charTyped(character, 0);
    }

    @Override
    public void refreshGui() {
        shouldUpdate = true;
    }

    public abstract void initElements();

    public abstract void setupElements();

    @Override
    public void handlePacketData(CompoundTag data) {
    }

    protected void renderToolTip(ItemStack stack, int x, int y) {
        handleItemStackTooltipRender(stack, x, y);
    }

    @Override
    public void onWidgetSelected(GuiElement element) {
        selectedWidget = element;
        element.setSelected(true);
    }

    @Override
    public void onWidgetDeselected(GuiElement element) {
        if (selectedWidget == element) selectedWidget = null;
        element.setSelected(false);
    }

    public void onCompositeCleared(List<GuiElement> composite) {
        if (composite.contains(selectedWidget)) selectedWidget = null;
    }

    public static final class ActivationEvent {
        public final int type;
        public int key;
        public int mButton;
        public boolean state;
        public char ch;
        public int mx;
        public int my;
        public int mw;

        private ActivationEvent(int type, int button, boolean state, int mx, int my, int mw) {
            this.type = type;
            this.mButton = button;
            this.state = state;
            this.mx = mx;
            this.my = my;
            this.mw = mw;
        }

        private ActivationEvent(int type, int key, char character, boolean state) {
            this.type = type;
            this.key = key;
            this.ch = character;
            this.state = state;
        }
    }

    public static void pushViewport(int x, int y, int width, int height) {
        Viewport parent = VIEWPORT_STACK.peek();
        if (parent != null) {
            int right = Math.min(x + width, parent.x + parent.width);
            int bottom = Math.min(y + height, parent.y + parent.height);
            x = Math.max(x, parent.x);
            y = Math.max(y, parent.y);
            width = Math.max(0, right - x);
            height = Math.max(0, bottom - y);
        }
        applyViewport(x, y, width, height);
        VIEWPORT_STACK.push(new Viewport(x, y, width, height));
    }

    public static void popViewport() {
        VIEWPORT_STACK.poll();
        Viewport viewport = VIEWPORT_STACK.peek();
        if (viewport == null) RenderSystem.disableScissor();
        else applyViewport(viewport.x, viewport.y, viewport.width, viewport.height);
    }

    private static void applyViewport(int x, int y, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        double scale = minecraft.getWindow().getGuiScale();
        int sx = (int) Math.floor(x * scale);
        int sy = minecraft.getWindow().getHeight() - (int) Math.ceil((y + height) * scale);
        RenderSystem.enableScissor(sx, sy, (int) Math.ceil(width * scale), (int) Math.ceil(height * scale));
    }

    private record Viewport(int x, int y, int width, int height) {
    }
}
