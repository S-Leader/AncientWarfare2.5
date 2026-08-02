package net.shadowmage.ancientwarfare.structure.container;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.tile.EntityStatueInfo;
import net.shadowmage.ancientwarfare.structure.tile.TileStatue;

import java.util.Optional;

public class ContainerStatue extends ContainerBase {
    private TileStatue statue;

    public ContainerStatue(Player player, int x, int y, int z) {
        super(player);
        Optional<TileStatue> te = WorldTools.getTile(player.level(), new BlockPos(x, y, z), TileStatue.class);
        if (!te.isPresent()) {
            throw new IllegalArgumentException("Statue not found");
        }
        statue = te.get();
    }

    public EntityStatueInfo getStatueInfo() {
        return statue.getEntityStatueInfo();
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        statue.getEntityStatueInfo().deserializeNBT(tag);
        statue.setChanged();
    }

    public void updateServer() {
        sendDataToServer(statue.getEntityStatueInfo().serializeNBT(new CompoundTag()));
    }
}
