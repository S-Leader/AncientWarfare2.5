package net.shadowmage.ancientwarfare.core.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.interfaces.IEntityPacketHandler;

import java.io.IOException;

public class PacketEntity extends PacketBase {
    private int entityId;
    public CompoundTag packetData = new CompoundTag();

    public PacketEntity() {
    }

    public PacketEntity(Entity e) {
        this.entityId = e.getId();
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        data.writeInt(entityId);
        if (packetData != null) {
            try (ByteBufOutputStream outputStream = new ByteBufOutputStream(data)) {
                NbtIo.writeCompressed(packetData, outputStream);
            } catch (IOException e) {
                AncientWarfareCore.LOG.error("Error writing entity packet data: ", e);
            }
        }
    }

    @Override
    protected void readFromStream(ByteBuf data) {
        entityId = data.readInt();
        try (ByteBufInputStream inputStream = new ByteBufInputStream(data)) {
            packetData = NbtIo.readCompressed(inputStream);
        } catch (IOException e) {
            AncientWarfareCore.LOG.error("Error reading entity packet data: ", e);
        }
    }

    @Override
    protected void execute(Player player) {
        Entity e = player.level().getEntity(entityId);
        if (e instanceof IEntityPacketHandler) {
            ((IEntityPacketHandler) e).handlePacketData(packetData);
        }
    }

}
