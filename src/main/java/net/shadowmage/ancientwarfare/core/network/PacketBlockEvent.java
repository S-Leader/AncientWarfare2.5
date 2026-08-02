package net.shadowmage.ancientwarfare.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

public class PacketBlockEvent extends PacketBase {
    private BlockPos pos;
    private short id;
    private short a;
    private short b;

    public PacketBlockEvent() {
    }

    /*
     * @param pos    coordinates of block in the world
     * @param block type to validate on client-side prior to reading event (id written as short)
     * @param a     data part a - (written as a unsigned byte)
     * @param b     data part b - (written as a unsigned byte)
     */
    public PacketBlockEvent(BlockPos pos, Block block, short a, short b) {
        this.pos = pos;
        this.id = (short) BuiltInRegistries.BLOCK.getId(block);
        this.a = a;
        this.b = b;
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        data.writeLong(pos.asLong());
        data.writeShort(id);
        data.writeByte(a & 0xff);
        data.writeByte(b & 0xff);
    }

    @Override
    protected void readFromStream(ByteBuf data) {
        pos = BlockPos.of(data.readLong());
        id = data.readShort();
        a = data.readUnsignedByte();
        b = data.readUnsignedByte();
    }

    @Override
    protected void execute(Player player) {
        player.level().blockEvent(pos, BuiltInRegistries.BLOCK.byId(id), a, b);
    }
}
