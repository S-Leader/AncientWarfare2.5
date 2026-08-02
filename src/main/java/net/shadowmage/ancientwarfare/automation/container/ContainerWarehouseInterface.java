package net.shadowmage.ancientwarfare.automation.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.TileWarehouseInterface;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.WarehouseInterfaceFilter;
import net.shadowmage.ancientwarfare.core.compat.CapabilityItemHandler;
import net.shadowmage.ancientwarfare.core.container.ContainerTileBase;
import net.shadowmage.ancientwarfare.core.util.NBTHelper;

import java.util.ArrayList;
import java.util.List;

public class ContainerWarehouseInterface extends ContainerTileBase<TileWarehouseInterface> {

    public List<WarehouseInterfaceFilter> filters = new ArrayList<>();

    public ContainerWarehouseInterface(Player player, int x, int y, int z) {
        super(player, x, y, z);
        IItemHandler inventory = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElse(null);
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new SlotItemHandler(inventory, i, (i % 3) * 18 + 8 + 3 * 18, (i / 3) * 18 + 8 + 80 + 8));
        }
        tileEntity.addViewer(this);
        filters.addAll(tileEntity.getFilters());
        addPlayerSlots(8 + 8 + 3 * 18 + 80 + 8);
    }

    @Override
    public void onContainerClosed(Player par1EntityPlayer) {
        tileEntity.removeViewer(this);
        super.onContainerClosed(par1EntityPlayer);
    }

    @Override
    public void sendInitData() {
        sendDataToClient(getTagToSend());
    }

    public void sendFiltersToServer()//should be called whenever filters change, so that the base container/etc can be updated
    {
        sendDataToServer(getTagToSend());
    }

    public CompoundTag getTagToSend() {
        CompoundTag tag = new CompoundTag();
        NBTHelper.writeSerializablesTo(tag, "filterList", filters);
        return tag;
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains("filterList")) {
            List<WarehouseInterfaceFilter> filters = NBTHelper.deserializeListFrom(tag, "filterList", WarehouseInterfaceFilter::new);
            if (player.level().isClientSide) {
                this.filters = filters;
                refreshGui();
            } else {
                tileEntity.setFilters(filters);
            }
        }
    }

    public void onInterfaceFiltersChanged() {
        filters.clear();
        filters.addAll(tileEntity.getFilters());
        sendInitData();
    }

}
