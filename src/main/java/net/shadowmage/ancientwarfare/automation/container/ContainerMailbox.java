package net.shadowmage.ancientwarfare.automation.container;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import net.shadowmage.ancientwarfare.automation.gamedata.MailboxData;
import net.shadowmage.ancientwarfare.automation.tile.TileMailbox;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.RelativeSide;
import net.shadowmage.ancientwarfare.core.container.ContainerTileBase;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContainerMailbox extends ContainerTileBase<TileMailbox> {
    private static final int RECEIVED_SLOT_START = 0;
    private static final int RECEIVED_SLOT_END = 18;
    private static final int SEND_SLOT_START = RECEIVED_SLOT_END;
    private static final int SEND_SLOT_END = SEND_SLOT_START + 18;
    private static final int PLAYER_SLOT_START = SEND_SLOT_END;

    public int guiHeight;

    /* Synchronized GUI state. These must be snapshots, not aliases of tile lists. */
    public String targetName;
    public String mailboxName;
    public boolean autoExport;
    public boolean privateBox;
    public List<String> publicBoxNames = new ArrayList<>();
    public List<String> privateBoxNames = new ArrayList<>();
    public List<Direction> sendSides = new ArrayList<>();
    public List<Direction> receivedSides = new ArrayList<>();

    public ContainerMailbox(Player player, int x, int y, int z) {
        super(player, x, y, z);

        for (int i = 0; i < 18; i++) {
            int xPos = (i % 9) * 18 + 8;
            int yPos = (i / 9) * 18 + 20;
            addSlot(new SlotItemHandler(tileEntity.receivedInventory, i, xPos, yPos) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }

        for (int i = 0; i < 18; i++) {
            int xPos = (i % 9) * 18 + 8;
            int yPos = (i / 9) * 18 + 68;
            addSlot(new SlotItemHandler(tileEntity.sendInventory, i, xPos, yPos));
        }

        int playerInventoryY = 8 + 12 + 12 + 4 * 18;
        guiHeight = addPlayerSlots(playerInventoryY + 12) + 8 + 24;

        copyTileStateToMenu();

        if (!player.level().isClientSide()) {
            refreshMailboxNameLists();
        }
    }

    private void copyTileStateToMenu() {
        privateBox = tileEntity.isPrivateBox();
        autoExport = tileEntity.isAutoExport();
        mailboxName = tileEntity.getMailboxName();
        targetName = tileEntity.getTargetName();
        sendSides = new ArrayList<>(tileEntity.sendSides);
        receivedSides = new ArrayList<>(tileEntity.receivedSides);
    }

    private void refreshMailboxNameLists() {
        MailboxData data = AWGameData.INSTANCE.getData(player.level(), MailboxData.class);
        publicBoxNames = new ArrayList<>(data.getPublicBoxNames());
        privateBoxNames = new ArrayList<>(data.getPrivateBoxNames(tileEntity.getOwner().getName()));
    }

    @Override
    public void sendInitData() {
        if (player.level().isClientSide()) {
            return;
        }

        copyTileStateToMenu();
        refreshMailboxNameLists();

        CompoundTag tag = new CompoundTag();
        tag.putIntArray("sendSides", encodeDirections(sendSides));
        tag.putIntArray("receivedSides", encodeDirections(receivedSides));

        if (mailboxName != null) {
            tag.putString("mailboxName", mailboxName);
        } else {
            tag.putBoolean("clearMailbox", true);
        }

        if (targetName != null) {
            tag.putString("targetName", targetName);
        } else {
            tag.putBoolean("clearTarget", true);
        }

        tag.putBoolean("privateBox", privateBox);
        tag.putBoolean("autoExport", autoExport);
        tag.put("publicBoxNames", writeStringList(publicBoxNames));
        tag.put("privateBoxNames", writeStringList(privateBoxNames));
        sendDataToClient(tag);
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        boolean changedTile = handleAccessChange(tag);

        if (tag.contains("sendSides", Tag.TAG_INT_ARRAY)) {
            List<Direction> decoded = decodeDirections(tag.getIntArray("sendSides"));
            replaceContents(tileEntity.sendSides, decoded);
            sendSides = new ArrayList<>(decoded);
            changedTile = true;
        }

        if (tag.contains("receivedSides", Tag.TAG_INT_ARRAY)) {
            List<Direction> decoded = decodeDirections(tag.getIntArray("receivedSides"));
            replaceContents(tileEntity.receivedSides, decoded);
            receivedSides = new ArrayList<>(decoded);
            changedTile = true;
        }

        if (tag.contains("autoExport", Tag.TAG_BYTE)) {
            autoExport = tag.getBoolean("autoExport");
            tileEntity.setAutoExport(autoExport);
            changedTile = true;
        }

        if (tag.contains("privateBox", Tag.TAG_BYTE)) {
            privateBox = tag.getBoolean("privateBox");
            tileEntity.setPrivateBox(privateBox);
            changedTile = true;
        }

        if (tag.contains("clearMailbox", Tag.TAG_BYTE)) {
            mailboxName = null;
            tileEntity.setMailboxName(null);
            changedTile = true;
        } else if (tag.contains("mailboxName", Tag.TAG_STRING)) {
            mailboxName = tag.getString("mailboxName");
            tileEntity.setMailboxName(mailboxName);
            changedTile = true;
        }

        if (tag.contains("clearTarget", Tag.TAG_BYTE)) {
            targetName = null;
            tileEntity.setTargetName(null);
            changedTile = true;
        } else if (tag.contains("targetName", Tag.TAG_STRING)) {
            targetName = tag.getString("targetName");
            tileEntity.setTargetName(targetName);
            changedTile = true;
        }

        if (tag.contains("publicBoxNames", Tag.TAG_LIST)) {
            publicBoxNames = readStringList(tag.getList("publicBoxNames", Tag.TAG_STRING));
        }

        if (tag.contains("privateBoxNames", Tag.TAG_LIST)) {
            privateBoxNames = readStringList(tag.getList("privateBoxNames", Tag.TAG_STRING));
        }

        if (!player.level().isClientSide()
                && (tag.contains("addMailbox", Tag.TAG_STRING)
                || tag.contains("deleteMailbox", Tag.TAG_STRING))) {
            MailboxData data = AWGameData.INSTANCE.getData(player.level(), MailboxData.class);
            String ownerName = tileEntity.isPrivateBox() ? tileEntity.getOwner().getName() : null;

            if (tag.contains("addMailbox", Tag.TAG_STRING)) {
                String name = tag.getString("addMailbox");
                if (!name.isEmpty()) {
                    data.addMailbox(ownerName, name);
                }
            }

            if (tag.contains("deleteMailbox", Tag.TAG_STRING)) {
                String name = tag.getString("deleteMailbox");
                if (!name.isEmpty()) {
                    data.deleteMailbox(ownerName, name);
                }
            }
        }

        if (changedTile && !player.level().isClientSide()) {
            tileEntity.setChanged();
        }

        refreshGui();
    }

    private boolean handleAccessChange(CompoundTag tag) {
        if (!tag.contains("accessChange", Tag.TAG_COMPOUND)) {
            return false;
        }

        CompoundTag slotTag = tag.getCompound("accessChange");
        RelativeSide base = getRelativeSide(slotTag.getInt("baseSide"));
        RelativeSide access = getRelativeSide(slotTag.getInt("accessSide"));
        if (base == null || access == null) {
            return false;
        }

        updateSides(base, access);
        return true;
    }

    public void updateSides(RelativeSide base, RelativeSide access) {
        Direction facing = RelativeSide.getMCSideToAccess(
                BlockRotationHandler.RotationType.FOUR_WAY,
                tileEntity.getPrimaryFacing(),
                base
        );
        if (facing == null) {
            return;
        }

        /* A side may belong to only one inventory. Remove it first to prevent duplicates. */
        tileEntity.sendSides.remove(facing);
        tileEntity.receivedSides.remove(facing);

        if (access == RelativeSide.TOP) {
            tileEntity.receivedSides.add(facing);
        } else if (access == RelativeSide.BOTTOM) {
            tileEntity.sendSides.add(facing);
        }

        sendSides = new ArrayList<>(tileEntity.sendSides);
        receivedSides = new ArrayList<>(tileEntity.receivedSides);

        if (!player.level().isClientSide()) {
            tileEntity.setChanged();
        }
    }

    public void sendSlotChange(RelativeSide base, RelativeSide access) {
        if (base == null || access == null) {
            return;
        }

        CompoundTag slotTag = new CompoundTag();
        slotTag.putInt("baseSide", base.ordinal());
        slotTag.putInt("accessSide", access.ordinal());

        CompoundTag tag = new CompoundTag();
        tag.put("accessChange", slotTag);
        sendDataToServer(tag);
    }

    /**
     * 1.20.1 calls broadcastChanges() each server tick. The old
     * detectAndSendChanges() name is no longer the menu synchronization hook.
     */
    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (player.level().isClientSide()) {
            return;
        }

        CompoundTag tag = new CompoundTag();

        List<Direction> currentSendSides = new ArrayList<>(tileEntity.sendSides);
        if (!sendSides.equals(currentSendSides)) {
            sendSides = currentSendSides;
            tag.putIntArray("sendSides", encodeDirections(sendSides));
        }

        List<Direction> currentReceivedSides = new ArrayList<>(tileEntity.receivedSides);
        if (!receivedSides.equals(currentReceivedSides)) {
            receivedSides = currentReceivedSides;
            tag.putIntArray("receivedSides", encodeDirections(receivedSides));
        }

        String currentMailboxName = tileEntity.getMailboxName();
        if (!Objects.equals(mailboxName, currentMailboxName)) {
            mailboxName = currentMailboxName;
            if (mailboxName == null) {
                tag.putBoolean("clearMailbox", true);
            } else {
                tag.putString("mailboxName", mailboxName);
            }
        }

        String currentTargetName = tileEntity.getTargetName();
        if (!Objects.equals(targetName, currentTargetName)) {
            targetName = currentTargetName;
            if (targetName == null) {
                tag.putBoolean("clearTarget", true);
            } else {
                tag.putString("targetName", targetName);
            }
        }

        boolean currentAutoExport = tileEntity.isAutoExport();
        if (autoExport != currentAutoExport) {
            autoExport = currentAutoExport;
            tag.putBoolean("autoExport", autoExport);
        }

        boolean currentPrivateBox = tileEntity.isPrivateBox();
        if (privateBox != currentPrivateBox) {
            privateBox = currentPrivateBox;
            tag.putBoolean("privateBox", privateBox);
        }

        MailboxData data = AWGameData.INSTANCE.getData(player.level(), MailboxData.class);

        List<String> currentPublicNames = new ArrayList<>(data.getPublicBoxNames());
        if (!publicBoxNames.equals(currentPublicNames)) {
            publicBoxNames = currentPublicNames;
            tag.put("publicBoxNames", writeStringList(publicBoxNames));
        }

        List<String> currentPrivateNames = new ArrayList<>(
                data.getPrivateBoxNames(tileEntity.getOwner().getName())
        );
        if (!privateBoxNames.equals(currentPrivateNames)) {
            privateBoxNames = currentPrivateNames;
            tag.put("privateBoxNames", writeStringList(privateBoxNames));
        }

        if (!tag.isEmpty()) {
            sendDataToClient(tag);
        }
    }

    public void handleNameAdd(String name) {
        if (name == null || name.isEmpty()) {
            return;
        }

        CompoundTag tag = new CompoundTag();
        tag.putString("addMailbox", name);
        sendDataToServer(tag);
    }

    public void handleNameDelete(String name) {
        if (name == null || name.isEmpty()) {
            return;
        }

        CompoundTag tag = new CompoundTag();
        tag.putString("deleteMailbox", name);
        sendDataToServer(tag);
    }

    public void handleNameSelection(String name) {
        CompoundTag tag = new CompoundTag();
        if (name == null) {
            tag.putBoolean("clearMailbox", true);
        } else {
            tag.putString("mailboxName", name);
        }
        mailboxName = name;
        sendDataToServer(tag);
    }

    public void handleTargetSelection(String name) {
        CompoundTag tag = new CompoundTag();
        if (name == null) {
            tag.putBoolean("clearTarget", true);
        } else {
            tag.putString("targetName", name);
        }
        targetName = name;
        sendDataToServer(tag);
    }

    public void handlePrivateBoxToggle(boolean newValue) {
        targetName = null;
        mailboxName = null;
        privateBox = newValue;

        CompoundTag tag = new CompoundTag();
        tag.putBoolean("privateBox", newValue);
        sendDataToServer(tag);
    }

    public void handleAutoExportToggle(boolean newValue) {
        autoExport = newValue;

        CompoundTag tag = new CompoundTag();
        tag.putBoolean("autoExport", newValue);
        sendDataToServer(tag);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (slotIndex < PLAYER_SLOT_START) {
            /* Mailbox inventories -> player inventory. */
            if (!moveItemStackTo(stack, PLAYER_SLOT_START, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            /* Player inventory -> send inventory only. */
            if (!moveItemStackTo(stack, SEND_SLOT_START, SEND_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return original;
    }

    private static int[] encodeDirections(List<Direction> directions) {
        return directions.stream().mapToInt(Enum::ordinal).toArray();
    }

    private static List<Direction> decodeDirections(int[] values) {
        List<Direction> directions = new ArrayList<>();
        Direction[] allDirections = Direction.values();

        for (int value : values) {
            if (value >= 0 && value < allDirections.length) {
                Direction direction = allDirections[value];
                if (!directions.contains(direction)) {
                    directions.add(direction);
                }
            }
        }
        return directions;
    }

    private static RelativeSide getRelativeSide(int ordinal) {
        RelativeSide[] values = RelativeSide.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : null;
    }

    private static ListTag writeStringList(List<String> values) {
        ListTag result = new ListTag();
        for (String value : values) {
            if (value != null) {
                result.add(StringTag.valueOf(value));
            }
        }
        return result;
    }

    private static List<String> readStringList(ListTag values) {
        List<String> result = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); i++) {
            result.add(values.getString(i));
        }
        return result;
    }

    private static <E> void replaceContents(List<E> destination, List<E> source) {
        destination.clear();
        destination.addAll(source);
    }
}