package net.shadowmage.ancientwarfare.automation.container;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.automation.tile.worksite.TileWorksiteFarm;
import net.shadowmage.ancientwarfare.core.container.ContainerTileBase;
import net.shadowmage.ancientwarfare.core.interfaces.IBoundedSite;
import net.shadowmage.ancientwarfare.core.util.BlockTools;

public class ContainerWorksiteBoundsAdjust extends ContainerTileBase {

    public BlockPos min, max;

    public ContainerWorksiteBoundsAdjust(Player player, int x, int y, int z) {
        super(player, x, y, z);
        if (tileEntity instanceof IBoundedSite) {
            min = getWorksite().getWorkBoundsMin();
            max = getWorksite().getWorkBoundsMax();
        } else
            throw new IllegalArgumentException("Couldn't find work site");
    }

    @Override
    public void sendInitData() {
        if (tileEntity instanceof TileWorksiteFarm) {
            TileWorksiteFarm twub = (TileWorksiteFarm) tileEntity;
            CompoundTag tag = new CompoundTag();
            tag.putByteArray("checkedMap", twub.getTargetMap());
            sendDataToGui(tag);
        }
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains("guiClosed")) {
            if (tag.contains("min") && tag.contains("max")) {
                BlockPos min = BlockPos.of(tag.getLong("min"));
                BlockPos max = BlockPos.of(tag.getLong("max"));
                getWorksite().setWorkBoundsMin(min);
                getWorksite().setWorkBoundsMax(max);
                getWorksite().onBoundsAdjusted();
                getWorksite().onPostBoundsAdjusted();
            }
            if (tag.contains("checkedMap") && tileEntity instanceof TileWorksiteFarm) {
                TileWorksiteFarm twub = (TileWorksiteFarm) tileEntity;
                byte[] map = tag.getByteArray("checkedMap");
                twub.setTargetBlocks(map);
            }
            BlockTools.notifyBlockUpdate(player.level(), getPos());
        }
    }

    public void onClose(boolean boundsAdjusted, boolean targetsAdjusted, byte[] checkedMap) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("guiClosed", true);
        if (boundsAdjusted) {
            tag.putLong("min", min.asLong());
            tag.putLong("max", max.asLong());
        }
        if (targetsAdjusted && tileEntity instanceof TileWorksiteFarm) {
            tag.putByteArray("checkedMap", checkedMap);
        }
        sendDataToServer(tag);
    }

    public BlockPos getPos() {
        return tileEntity.getBlockPos();
    }

    public int getX() {
        return tileEntity.getBlockPos().getX();
    }

    public int getY() {
        return tileEntity.getBlockPos().getY();
    }

    public int getZ() {
        return tileEntity.getBlockPos().getZ();
    }

    public IBoundedSite getWorksite() {
        return (IBoundedSite) tileEntity;
    }
}
