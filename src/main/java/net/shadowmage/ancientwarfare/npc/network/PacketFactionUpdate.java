package net.shadowmage.ancientwarfare.npc.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.core.network.PacketBase;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.gamedata.FactionData;

import java.io.IOException;

public class PacketFactionUpdate extends PacketBase {
    private CompoundTag packetData;

    public PacketFactionUpdate(CompoundTag tag) {
        this.packetData = tag;
    }

    public PacketFactionUpdate() {
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        if (packetData != null) {
            try (ByteBufOutputStream outputStream = new ByteBufOutputStream(data)) {
                NbtIo.writeCompressed(packetData, outputStream);
            } catch (IOException e) {
                AncientWarfareNPC.LOG.error("Error writing faction update packet data: ", e);
            }
        }
    }

    @Override
    protected void readFromStream(ByteBuf data) {
        try (ByteBufInputStream inputStream = new ByteBufInputStream(data)) {
            packetData = NbtIo.readCompressed(inputStream);
        } catch (IOException e) {
            AncientWarfareNPC.LOG.error("Error reading faction update packet data: ", e);
        }
    }

    @Override
    protected void execute(Player player) {
        if (packetData != null) {
            AWGameData.INSTANCE.getData(player.level(), FactionData.class).handlePacketData(player, packetData);
        }
    }
}
