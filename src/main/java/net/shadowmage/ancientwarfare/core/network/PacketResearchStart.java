package net.shadowmage.ancientwarfare.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.research.ResearchTracker;

public class PacketResearchStart extends PacketBase {
    private String playerName;
    private String toAdd;
    private boolean start;

    public PacketResearchStart(String playerName, String toAdd, boolean start) {
        this.playerName = playerName;
        this.toAdd = toAdd;
        this.start = start;
    }

    public PacketResearchStart() {
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(data);
        buffer.writeUtf(toAdd);
        buffer.writeUtf(playerName);
        buffer.writeBoolean(start);
    }

    @Override
    protected void readFromStream(ByteBuf data) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(data);
        toAdd = buffer.readUtf(40);
        playerName = buffer.readUtf(16);
        start = buffer.readBoolean();
    }

    @Override
    protected void execute(Player player) {
        if (start) {
            ResearchTracker.INSTANCE.startResearch(player.level(), playerName, toAdd);
        } else {
            ResearchTracker.INSTANCE.finishResearch(player.level(), playerName, toAdd);
        }
    }

}
