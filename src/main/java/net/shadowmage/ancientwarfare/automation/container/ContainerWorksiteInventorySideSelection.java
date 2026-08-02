package net.shadowmage.ancientwarfare.automation.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.automation.tile.worksite.TileWorksiteBoundedInventory;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.RelativeSide;
import net.shadowmage.ancientwarfare.core.container.ContainerTileBase;

import java.util.HashMap;

public class ContainerWorksiteInventorySideSelection extends ContainerTileBase<TileWorksiteBoundedInventory> {
    public final HashMap<RelativeSide, RelativeSide> sideMap = new HashMap<>();
    private static final String MACHINE_SIDE_KEY = "machineSide";
    private static final String INVENTORY_SIDE_KEY = "inventorySide";
    private static final String ACCESS_CHANGE_KEY = "accessChange";

    public ContainerWorksiteInventorySideSelection(Player player, int x, int y, int z) {
        super(player, x, y, z);

        for (RelativeSide rSide : tileEntity.getInventorySideMappings().keySet()) {
            sideMap.put(rSide, tileEntity.getInventorySideMappings().get(rSide));
        }
    }

    @Override
    public void sendInitData() {
        sendAccessMap();
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        handleAccessMapTag(tag);
        if (tag.contains("closeGUI")) {
            tileEntity.onBlockClicked(player, null);//hack to open the worksites GUI
        }
        refreshGui();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        synchAccessMap();
    }

    private void handleAccessMapTag(CompoundTag tag) {
        if (tag.contains("accessMap")) {
            CompoundTag accessTag = tag.getCompound("accessMap");
            int[] rMap = accessTag.getIntArray("rMap");
            int[] rMap2 = accessTag.getIntArray("iMap");
            RelativeSide rSide;
            RelativeSide iSide;
            for (int i = 0; i < rMap.length && i < rMap2.length; i++) {
                rSide = RelativeSide.values()[rMap[i]];
                iSide = RelativeSide.values()[rMap2[i]];
                sideMap.put(rSide, iSide);
            }
        } else if (tag.contains(ACCESS_CHANGE_KEY)) {
            CompoundTag slotTag = tag.getCompound(ACCESS_CHANGE_KEY);
            RelativeSide machineSide = RelativeSide.values()[slotTag.getInt(MACHINE_SIDE_KEY)];
            RelativeSide inventorySide = RelativeSide.values()[slotTag.getInt(INVENTORY_SIDE_KEY)];
            sideMap.put(machineSide, inventorySide);
            if (!player.level().isClientSide) {
                tileEntity.setInventorySideMappings(machineSide, inventorySide);
            }
        }
    }

    private void sendAccessMap() {
        int l = sideMap.size();
        int rMap[] = new int[l];
        int iMap[] = new int[l];
        int index = 0;
        for (RelativeSide rSide : sideMap.keySet()) {
            rMap[index] = rSide.ordinal();
            iMap[index] = sideMap.get(rSide).ordinal();
            index++;
        }
        CompoundTag accessTag = new CompoundTag();
        accessTag.putIntArray("rMap", rMap);
        accessTag.putIntArray("iMap", iMap);
        CompoundTag tag = new CompoundTag();
        tag.put("accessMap", accessTag);
        sendDataToClient(tag);
    }

    private void synchAccessMap() {
        CompoundTag tag;
        CompoundTag slotTag;
        RelativeSide rSide2, rSide3;
        for (RelativeSide rSide : tileEntity.getInventorySideMappings().keySet()) {
            rSide2 = tileEntity.getInventorySideMappings().get(rSide);
            rSide3 = sideMap.get(rSide);
            if (rSide2 != rSide3) {
                sideMap.put(rSide, rSide2);

                tag = new CompoundTag();
                slotTag = new CompoundTag();
                slotTag.putInt(MACHINE_SIDE_KEY, rSide.ordinal());
                slotTag.putInt(INVENTORY_SIDE_KEY, rSide2.ordinal());
                tag.put(ACCESS_CHANGE_KEY, slotTag);
                sendDataToClient(tag);
            }
        }
    }

    public void sendSlotChange(RelativeSide base, RelativeSide access) {
        CompoundTag tag;
        CompoundTag slotTag;
        tag = new CompoundTag();
        slotTag = new CompoundTag();
        slotTag.putInt(MACHINE_SIDE_KEY, base.ordinal());
        slotTag.putInt(INVENTORY_SIDE_KEY, access.ordinal());
        tag.put(ACCESS_CHANGE_KEY, slotTag);
        sendDataToServer(tag);
    }

    public void close() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("closeGUI", true);
        sendDataToServer(tag);
    }
}
