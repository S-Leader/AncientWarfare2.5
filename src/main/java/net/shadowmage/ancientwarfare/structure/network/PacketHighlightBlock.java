package net.shadowmage.ancientwarfare.structure.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.network.PacketBase;
import net.shadowmage.ancientwarfare.structure.render.BlockHighlightRenderer;
import net.shadowmage.ancientwarfare.structure.util.BlockHighlightInfo;

import java.io.IOException;

public class PacketHighlightBlock extends PacketBase {
    private BlockHighlightInfo blockHighlightInfo;

    public PacketHighlightBlock(BlockHighlightInfo blockHighlightInfo) {
        this.blockHighlightInfo = blockHighlightInfo;
    }

    public PacketHighlightBlock() {
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        blockHighlightInfo.serializeToBuffer(data);
    }

    @Override
    protected void readFromStream(ByteBuf data) throws IOException {
        blockHighlightInfo = BlockHighlightInfo.deserializeFromBuffer(data);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void execute() {
        BlockHighlightRenderer.setBlockHighlightInfo(blockHighlightInfo);
    }
}
