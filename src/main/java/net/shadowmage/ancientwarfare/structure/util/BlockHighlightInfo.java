package net.shadowmage.ancientwarfare.structure.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;

public class BlockHighlightInfo {
    public static final BlockHighlightInfo EXPIRED = new BlockHighlightInfo(BlockPos.ZERO, 0);
    private BlockPos pos;
    private long expirationTime;

    public BlockHighlightInfo(BlockPos pos, long expirationTime) {
        this.pos = pos;
        this.expirationTime = expirationTime;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void serializeToBuffer(ByteBuf data) {
        data.writeLong(pos.asLong());
        data.writeLong(expirationTime);
    }

    public static BlockHighlightInfo deserializeFromBuffer(ByteBuf data) {
        return new BlockHighlightInfo(BlockPos.of(data.readLong()), data.readLong());
    }
}
