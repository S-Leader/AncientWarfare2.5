package net.shadowmage.ancientwarfare.structure.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.network.PacketBase;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.tile.TileSoundBlock;

import java.io.IOException;

public class PacketSoundBlockPlayerSpecValues extends PacketBase {
    private BlockPos tilePos;
    private boolean stopped;
    private long lastTimePlayerNear;
    private int numberOfTimesRepeated;

    public PacketSoundBlockPlayerSpecValues() {
    }

    public PacketSoundBlockPlayerSpecValues(BlockPos tilePos, TileSoundBlock.PersistentValues values) {
        this.tilePos = tilePos;
        this.stopped = values.isStopped();
        this.lastTimePlayerNear = values.getLastTimePlayerNear();
        this.numberOfTimesRepeated = values.getNumberOfTimesRepeated();
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        data.writeLong(tilePos.asLong());
        data.writeBoolean(stopped);
        data.writeLong(lastTimePlayerNear);
        data.writeInt(numberOfTimesRepeated);
    }

    @Override
    protected void readFromStream(ByteBuf data) throws IOException {
        tilePos = BlockPos.of(data.readLong());
        stopped = data.readBoolean();
        lastTimePlayerNear = data.readLong();
        numberOfTimesRepeated = data.readInt();
    }

    @Override
    protected void execute(Player player) {
        WorldTools.getTile(player.level(), tilePos, TileSoundBlock.class).ifPresent(tile ->
                tile.updatePlayerSpecValues(player.getUUID(), stopped, lastTimePlayerNear, numberOfTimesRepeated));

    }

}
