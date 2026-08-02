package net.shadowmage.ancientwarfare.structure.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.shadowmage.ancientwarfare.core.network.PacketBase;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplateManager;

import java.io.IOException;

public class PacketStructure extends PacketBase {
    public CompoundTag packetData = new CompoundTag();

    @Override
    protected void writeToStream(ByteBuf data) {
        if (packetData != null) {
            try (ByteBufOutputStream outputStream = new ByteBufOutputStream(data)) {
                NbtIo.writeCompressed(packetData, outputStream);
            } catch (IOException e) {
                AncientWarfareStructure.LOG.error("Error writing structure template packet: ", e);
            }
        }
    }

    @Override
    protected void readFromStream(ByteBuf data) {
        try (ByteBufInputStream inputStream = new ByteBufInputStream(data)) {
            packetData = NbtIo.readCompressed(inputStream);
        } catch (IOException e) {
            AncientWarfareStructure.LOG.error("Error reading structure template packet: ", e);
        }
    }

    @Override
    protected void execute() {
        StructureTemplateManager.onTemplateData(packetData);
    }
}
