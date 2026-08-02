package net.shadowmage.ancientwarfare.core.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.research.ResearchData;
import net.shadowmage.ancientwarfare.core.research.ResearchTracker;

import java.io.IOException;

public class PacketResearchInit extends PacketBase {
    private CompoundTag researchDataTag;

    public PacketResearchInit(ResearchData data) {
        researchDataTag = new CompoundTag();
        data.writeToNBT(researchDataTag);
    }

    public PacketResearchInit() {
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        try (ByteBufOutputStream outputStream = new ByteBufOutputStream(data)) {
            NbtIo.writeCompressed(researchDataTag, outputStream);
        } catch (IOException e) {
            AncientWarfareCore.LOG.error("Error writing research packet data: ", e);
        }
    }

    @Override
    protected void readFromStream(ByteBuf data) {
        try (ByteBufInputStream inputStream = new ByteBufInputStream(data)) {
            researchDataTag = NbtIo.readCompressed(inputStream);
        } catch (IOException e) {
            AncientWarfareCore.LOG.error("Error reading research packet data: ", e);
        }
    }

    @Override
    protected void execute() {
        ResearchTracker.INSTANCE.onClientResearchReceived(researchDataTag);
    }

}
