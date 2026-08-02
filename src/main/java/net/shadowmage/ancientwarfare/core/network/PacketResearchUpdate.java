package net.shadowmage.ancientwarfare.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.research.ResearchTracker;

public class PacketResearchUpdate extends PacketBase {

    private String playerName;
    private String toAdd;
    private boolean add;
    private boolean live;

    public PacketResearchUpdate(String playerName, String toAdd, boolean add, boolean live) {
        this.playerName = playerName;
        this.toAdd = toAdd;
        this.add = add;
        this.live = live;
    }

    public PacketResearchUpdate() {
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(data);
        buffer.writeUtf(toAdd);
        buffer.writeBoolean(add);
        buffer.writeBoolean(live);
        buffer.writeUtf(playerName);
    }

    @Override
    protected void readFromStream(ByteBuf data) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(data);
        toAdd = buffer.readUtf(40);
        add = buffer.readBoolean();
        live = buffer.readBoolean();
        playerName = buffer.readUtf(16);
    }

    @Override
    protected void execute(Player player) {
        if (live) {
            if (add) {
                ResearchTracker.INSTANCE.addResearch(player.level(), playerName, toAdd);
            }
        } else {
            if (add) {
                ResearchTracker.INSTANCE.addQueuedGoal(player.level(), playerName, toAdd);
            } else {
                ResearchTracker.INSTANCE.removeQueuedGoal(player.level(), playerName, toAdd);
            }
        }
    }

}
