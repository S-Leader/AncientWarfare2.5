package net.shadowmage.ancientwarfare.core.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;

import java.io.IOException;

public class PacketGui extends PacketBase {
    private static final String MENU_TARGET = "awMenuTarget";
    private CompoundTag packetData;

    public PacketGui(CompoundTag packetData) {
        this.packetData = packetData;
    }

    public PacketGui() {
        packetData = new CompoundTag();
    }

    public void setMenuRequest(int id, int x, int y, int z) {
        packetData.putBoolean("requestMenu", true);
        packetData.putInt("id", id);
        packetData.putInt("x", x);
        packetData.putInt("y", y);
        packetData.putInt("z", z);
    }

    public void setTag(String key, CompoundTag tag) {
        packetData.put(key, tag);
    }

    public void setData(CompoundTag tag) {
        this.packetData = tag;
    }

    public void setMenuTarget(int menuId) {
        packetData.putInt(MENU_TARGET, menuId);
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        if (packetData != null) {
            try (ByteBufOutputStream outputStream = new ByteBufOutputStream(data)) {
                NbtIo.writeCompressed(packetData, outputStream);
            } catch (IOException e) {
                AncientWarfareCore.LOG.error("Error writing gui packet data: ", e);
            }
        }
    }

    @Override
    protected void readFromStream(ByteBuf data) {
        try (ByteBufInputStream inputStream = new ByteBufInputStream(data)) {
            packetData = NbtIo.readCompressed(inputStream);
        } catch (IOException e) {
            AncientWarfareCore.LOG.error("Error reading gui packet data: ", e);

        }
    }

    @Override
    protected void execute(Player player) {
        if (packetData.contains("requestMenu")) {
            AWMenuTypes.openRequested(player, packetData.getInt("id"), packetData.getInt("x"), packetData.getInt("y"), packetData.getInt("z"));
        } else if (player.containerMenu instanceof ContainerBase container
                && (!packetData.contains(MENU_TARGET) || packetData.getInt(MENU_TARGET) == container.containerId)) {
            container.onPacketData(payload());
        } else if (player.level().isClientSide() && packetData.contains(MENU_TARGET)) {
            NetworkHandler.INSTANCE.queuePendingGuiPacket(packetData.getInt(MENU_TARGET), payload());
        } else {
            AncientWarfareCore.LOG.error("Invalid target found when processing GUI/Container packet : {} packet: {}", player.containerMenu, packetData);
        }
    }

    private CompoundTag payload() {
        CompoundTag payload = packetData.copy();
        payload.remove(MENU_TARGET);
        return payload;
    }

}
