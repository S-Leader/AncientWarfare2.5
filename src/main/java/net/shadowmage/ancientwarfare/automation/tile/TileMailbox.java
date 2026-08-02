package net.shadowmage.ancientwarfare.automation.tile;

import com.google.common.collect.Lists;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.shadowmage.ancientwarfare.automation.gamedata.MailboxData;
import net.shadowmage.ancientwarfare.automation.gamedata.MailboxData.DeliverableItem;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.IRotatableTile;
import net.shadowmage.ancientwarfare.core.compat.CapabilityItemHandler;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.core.interfaces.ITickable;
import net.shadowmage.ancientwarfare.core.render.property.CoreProperties;
import net.shadowmage.ancientwarfare.core.tile.IBlockBreakHandler;
import net.shadowmage.ancientwarfare.core.tile.TileOwned;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TileMailbox extends TileOwned implements IRotatableTile, ITickable, IBlockBreakHandler {

    private boolean autoExport;
    private boolean privateBox;

    public ItemStackHandler sendInventory = new ItemStackHandler(18);
    public List<Direction> sendSides = Lists.newArrayList();
    public ItemStackHandler receivedInventory = new ItemStackHandler(18);
    public List<Direction> receivedSides = Lists.newArrayList();

    private final LazyOptional<IItemHandler> sendInventoryCap = LazyOptional.of(() -> sendInventory);
    private final LazyOptional<IItemHandler> receivedInventoryCap = LazyOptional.of(() -> receivedInventory);

    private String mailboxName;
    private String destinationName;

    public TileMailbox() {
        sendSides.add(Direction.UP);
        receivedSides.add(Direction.DOWN);
    }

    @Override
    public void update() {
        if (!hasWorld() || world.isClientSide) {
            return;
        }
        if (mailboxName != null)//try to receive mail
        {
            MailboxData data = AWGameData.INSTANCE.getData(world, MailboxData.class);

            List<DeliverableItem> items = new ArrayList<>();
            data.getDeliverableItems(privateBox ? getOwner().getName() : null, mailboxName, items, world, pos.getX(), pos.getY(), pos.getZ());
            data.addMailboxReceiver(privateBox ? getOwner().getName() : null, mailboxName, this);

            if (destinationName != null)//try to send mail
            {
                trySendItems(data);
            }
        }
        if (autoExport) {
            exportReceivedItems();
        }
    }

    private void exportReceivedItems() {
        for (Direction side : receivedSides) {
            BlockEntity adjacent = world.getBlockEntity(pos.relative(side));
            if (adjacent == null) {
                continue;
            }
            adjacent.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite())
                    .ifPresent(handler -> InventoryTools.transferItems(receivedInventory, handler, stack -> true, 64));
        }
    }

    private void trySendItems(MailboxData data) {
        ItemStack item;
        String owner = privateBox ? getOwner().getName() : null;
        int dim = getLegacyDimensionId(world);
        for (int slot = 0; slot < sendInventory.getSlots(); slot++) {
            item = sendInventory.getStackInSlot(slot);
            if (!item.isEmpty()) {
                data.addDeliverableItem(owner, destinationName, sendInventory.extractItem(slot, item.getCount(), false), dim, pos);
                break;
            }
        }
    }

    public String getMailboxName() {
        return mailboxName;
    }

    public String getTargetName() {
        return destinationName;
    }

    public void setMailboxName(String name) {
        if (world.isClientSide) {
            return;
        }
        mailboxName = name;
        markDirty();
    }

    public void setTargetName(String name) {
        if (world.isClientSide) {
            return;
        }
        destinationName = name;
        markDirty();
    }

    public boolean isAutoExport() {
        return autoExport;
    }

    public boolean isPrivateBox() {
        return privateBox;
    }

    public void setAutoExport(boolean val) {
        if (autoExport != val) {
            autoExport = val;
            markDirty();
        }
    }

    public void setPrivateBox(boolean val) {
        if (world.isClientSide) {
            return;
        }
        if (val != privateBox) {
            mailboxName = null;
            destinationName = null;
            markDirty();
        }
        privateBox = val;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        if (tag.contains("targetName")) {
            destinationName = tag.getString("targetName");
        }
        if (tag.contains("mailboxName")) {
            mailboxName = tag.getString("mailboxName");
        }
        if (tag.contains("autoExport")) {
            autoExport = tag.getBoolean("autoExport");
        }
        if (tag.contains("privateBox")) {
            privateBox = tag.getBoolean("privateBox");
        }
        if (tag.contains("sendInventory")) {
            sendInventory.deserializeNBT(tag.getCompound("sendInventory"));
        }
        if (tag.contains("receivedInventory")) {
            receivedInventory.deserializeNBT(tag.getCompound("receivedInventory"));
        }
        if (tag.contains("sendSides")) {
            int[] sides = tag.getIntArray("sendSides");
            sendSides = Arrays.stream(sides).mapToObj(o -> Direction.values()[o]).collect(Collectors.toList());
        }
        if (tag.contains("receivedSides")) {
            int[] sides = tag.getIntArray("receivedSides");
            receivedSides = Arrays.stream(sides).mapToObj(o -> Direction.values()[o]).collect(Collectors.toList());
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        if (destinationName != null) {
            tag.putString("targetName", destinationName);
        }
        if (mailboxName != null) {
            tag.putString("mailboxName", mailboxName);
        }
        tag.put("sendInventory", sendInventory.serializeNBT());
        tag.put("receivedInventory", receivedInventory.serializeNBT());
        tag.putIntArray("sendSides", sendSides.stream().mapToInt(Enum::ordinal).toArray());
        tag.putIntArray("receivedSides", receivedSides.stream().mapToInt(Enum::ordinal).toArray());
        tag.putBoolean("privateBox", privateBox);
        tag.putBoolean("autoExport", autoExport);

        return tag;
    }

    @Override
    public Direction getPrimaryFacing() {
        return world.getBlockState(pos).getValue(CoreProperties.FACING);
    }

    @Override
    public void setPrimaryFacing(Direction face) {
        world.setBlock(pos, world.getBlockState(pos).setValue(CoreProperties.FACING, face), 0);
    }

    private int getLegacyDimensionId(Level world) {
        if (world.dimension() == Level.NETHER) {
            return -1;
        }
        if (world.dimension() == Level.END) {
            return 1;
        }
        return 0;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (receivedSides.contains(facing)) {
                return receivedInventoryCap.cast();
            } else if (sendSides.contains(facing)) {
                return sendInventoryCap.cast();
            }
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        sendInventoryCap.invalidate();
        receivedInventoryCap.invalidate();
    }

    @Override
    public void onBlockBroken(BlockState state) {
        InventoryTools.dropItemsInWorld(world, sendInventory, pos);
        InventoryTools.dropItemsInWorld(world, receivedInventory, pos);
    }
}
