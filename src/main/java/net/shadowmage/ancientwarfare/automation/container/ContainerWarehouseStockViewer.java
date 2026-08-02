package net.shadowmage.ancientwarfare.automation.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.TileWarehouseStockViewer;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.TileWarehouseStockViewer.WarehouseStockFilter;
import net.shadowmage.ancientwarfare.core.container.ContainerTileBase;
import net.shadowmage.ancientwarfare.core.util.NBTHelper;

import java.util.ArrayList;
import java.util.List;

public class ContainerWarehouseStockViewer extends ContainerTileBase<TileWarehouseStockViewer> {

    public final List<WarehouseStockFilter> filters = new ArrayList<>();

    public ContainerWarehouseStockViewer(Player player, int x, int y, int z) {
        super(player, x, y, z);
        filters.addAll(tileEntity.getFilters());
        tileEntity.addViewer(this);
        addPlayerSlots(88);//240-8-4-4*18
    }

    /*
     * should be called from the tile whenever its client-side filters change
     */
    public void onFiltersChanged() {
        refreshGui();
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains("filterList")) {
            tileEntity.setFilters(NBTHelper.deserializeListFrom(tag, "filterList", WarehouseStockFilter::new));
        }
        super.handlePacketData(tag);
    }

    public void sendFiltersToServer() {
        CompoundTag tag = new CompoundTag();
        NBTHelper.writeSerializablesTo(tag, "filterList", filters);
        sendDataToServer(tag);
    }

    @Override
    public void onContainerClosed(Player par1EntityPlayer) {
        tileEntity.removeViewer(this);
        super.onContainerClosed(par1EntityPlayer);
    }

}
