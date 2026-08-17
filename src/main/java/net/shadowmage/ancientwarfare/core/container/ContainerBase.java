package net.shadowmage.ancientwarfare.core.container;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.interfaces.IContainerGuiCallback;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.network.PacketGui;

import java.util.function.Supplier;

/**
 * Shared menu base adapted to the 1.20.1 AbstractContainerMenu contract.
 * Existing subclasses keep their Player-only constructors; the registered native MenuType
 * factory supplies the correct menu type and window id through a short-lived context.
 */
public class ContainerBase extends AbstractContainerMenu {
    private record ConstructionContext(MenuType<?> menuType, int windowId) {
    }

    private static final ThreadLocal<ConstructionContext> CONSTRUCTION_CONTEXT = new ThreadLocal<>();

    public final Player player;
    /**
     * 1.12 name retained so all legacy menus share one migration bridge.
     */
    public final NonNullList<Slot> inventorySlots = slots;
    /**
     * The old menu kept a parallel snapshot list. Modern synchronization is
     * handled by AbstractContainerMenu, but legacy code still reads its size.
     */
    public final NonNullList<ItemStack> inventoryItemStacks = NonNullList.create();
    private IContainerGuiCallback gui;
    public int playerSlots;

    public ContainerBase(Player player) {
        super(currentMenuType(), currentWindowId());
        this.player = player;
    }

    /**
     * Creates an existing AW container through its actual registered MenuType.
     * The context only exists for the duration of the menu factory invocation.
     */
    public static <T extends ContainerBase> T createForMenu(
            MenuType<?> menuType, int windowId, Supplier<T> constructor) {
        ConstructionContext previous = CONSTRUCTION_CONTEXT.get();
        CONSTRUCTION_CONTEXT.set(new ConstructionContext(menuType, windowId));
        try {
            return constructor.get();
        } finally {
            if (previous == null) {
                CONSTRUCTION_CONTEXT.remove();
            } else {
                CONSTRUCTION_CONTEXT.set(previous);
            }
        }
    }

    private static MenuType<?> currentMenuType() {
        ConstructionContext context = CONSTRUCTION_CONTEXT.get();
        return context == null ? AWMenuTypes.CLIENT_ONLY.get() : context.menuType();
    }

    private static int currentWindowId() {
        ConstructionContext context = CONSTRUCTION_CONTEXT.get();
        return context == null ? 0 : context.windowId();
    }

    @Override
    protected Slot addSlot(Slot slot) {
        Slot added = super.addSlot(slot);
        inventoryItemStacks.add(ItemStack.EMPTY);
        return added;
    }

    protected Slot addSlotToContainer(Slot slot) {
        return addSlot(slot);
    }

    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        return moveItemStackTo(stack, startIndex, endIndex, reverseDirection);
    }

    /**
     * Legacy close hook; vanilla only calls removed(), so route it through here.
     */
    public void onContainerClosed(Player player) {
        super.removed(player);
    }

    @Override
    public void removed(Player player) {
        onContainerClosed(player);
    }

    /**
     * Modern server ticks call broadcastChanges(), while all migrated AW
     * containers still override the 1.12 detectAndSendChanges() hook. Route
     * the modern call through that hook so initial and periodic GUI data is
     * synchronized even when no slot has just changed.
     */
    @Override
    public void broadcastChanges() {
        detectAndSendChanges();
    }

    public void detectAndSendChanges() {
        super.broadcastChanges();
    }

    public final void setGui(IContainerGuiCallback gui) {
        this.gui = gui;
    }

    protected int addPlayerSlots(int tx, int ty, int gap) {
        int y;
        int x;
        int slotNum;
        int xPos;
        int yPos;
        IItemHandler playerInventory = new InvWrapper(player.getInventory());
        for (x = 0; x < 9; ++x) {
            xPos = tx + x * 18;
            yPos = ty + gap + 3 * 18;
            addSlot(new SlotItemHandler(playerInventory, x, xPos, yPos));
        }
        for (y = 0; y < 3; ++y) {
            for (x = 0; x < 9; ++x) {
                slotNum = y * 9 + x + 9;
                xPos = tx + x * 18;
                yPos = ty + y * 18;
                addSlot(new SlotItemHandler(playerInventory, slotNum, xPos, yPos));
            }
        }
        playerSlots = 36;
        return ty + (4 * 18) + gap;
    }

    protected int addPlayerSlots(int ty) {
        return addPlayerSlots(8, ty, 4);
    }

    protected int addPlayerSlots() {
        return addPlayerSlots(8, 240 - 4 - 8 - 4 * 18, 4);
    }

    protected final void sendDataToGui(CompoundTag data) {
        if (!player.level().isClientSide()) {
            PacketGui packet = new PacketGui();
            packet.setTag("gui", data);
            packet.setMenuTarget(containerId);
            NetworkHandler.sendToPlayer((ServerPlayer) player, packet);
        }
    }

    protected void sendDataToServer(String tagName, Tag tag) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put(tagName, tag);
        sendDataToServer(compoundTag);
    }

    protected void sendDataToServer(CompoundTag data) {
        if (player.level().isClientSide()) {
            PacketGui packet = new PacketGui();
            packet.setData(data);
            packet.setMenuTarget(containerId);
            NetworkHandler.sendToServer(packet);
        }
    }

    protected void sendDataToClient(CompoundTag data) {
        if (!player.level().isClientSide()) {
            PacketGui packet = new PacketGui();
            packet.setData(data);
            packet.setMenuTarget(containerId);
            NetworkHandler.sendToPlayer((ServerPlayer) player, packet);
        }
    }

    public final void onPacketData(CompoundTag data) {
        if (data.contains("gui")) {
            if (gui != null) {
                gui.handlePacketData(data.getCompound("gui"));
            }
        } else {
            handlePacketData(data);
        }
    }

    public void sendInitData() {
    }

    public void handlePacketData(CompoundTag tag) {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public void refreshGui() {
        if (gui != null) {
            gui.refreshGui();
        }
    }

    /**
     * Slot coordinates are final in 1.20.1. Legacy screens should hide slots
     * visually and reject clicks instead of moving them by 10,000 pixels.
     */
    public void removeSlots() {
    }

    public void addSlots() {
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotClickedIndex) {
        return ItemStack.EMPTY;
    }
}
