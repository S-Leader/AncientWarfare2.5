package net.shadowmage.ancientwarfare.core.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;

import java.io.IOException;

public class PacketHelper {
    private PacketHelper() {
    }

    public static void writeNBTTag(ByteBuf data, CompoundTag tag) {
        try (ByteBufOutputStream outputStream = new ByteBufOutputStream(data)) {
            NbtIo.writeCompressed(tag, outputStream);
        } catch (IOException e) {
            AncientWarfareCore.LOG.error("Error writing tag to buffer:\n", e);
        }
    }

    public static CompoundTag readNBTTag(ByteBuf data) {
        try (ByteBufInputStream inputStream = new ByteBufInputStream(data)) {
            return NbtIo.readCompressed(inputStream);
        } catch (IOException e) {
            AncientWarfareStructure.LOG.error("Error reading tag from buffer:\n", e);
            return new CompoundTag();
        }
    }
}
