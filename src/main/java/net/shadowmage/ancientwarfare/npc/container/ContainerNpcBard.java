package net.shadowmage.ancientwarfare.npc.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.util.SongPlayData;
import net.shadowmage.ancientwarfare.npc.entity.NpcBard;

public class ContainerNpcBard extends ContainerNpcBase<NpcBard> {

    public final SongPlayData data;

    public ContainerNpcBard(Player player, int x, int y, int z) {
        super(player, x);
        data = entity.getSongs();
    }

    @Override
    public void sendInitData() {
        CompoundTag tag = new CompoundTag();
        tag.put("tuneData", data.writeToNBT(new CompoundTag()));
        sendDataToClient(tag);
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains("tuneData")) {
            data.readFromNBT(tag.getCompound("tuneData"));
        }
        refreshGui();
    }

    public void sendTuneDataToServer() {
        if (player.level().isClientSide)//handles sending new/updated/changed data back to server on GUI close.  the last GUI to close will be the one whose data 'sticks'
        {
            CompoundTag tag = new CompoundTag();
            tag.put("tuneData", data.writeToNBT(new CompoundTag()));
            sendDataToServer(tag);
        }
    }

}
