package net.shadowmage.ancientwarfare.npc.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.core.network.PacketBase;
import net.shadowmage.ancientwarfare.npc.gamedata.TeamData;

import java.io.IOException;

public class PacketTeamStandingUpdate extends PacketBase {
    private ResourceLocation teamName;
    private String factionName;
    private int standing;

    public PacketTeamStandingUpdate() {
    }

    public PacketTeamStandingUpdate(ResourceLocation teamName, String factionName, int standing) {
        this.teamName = teamName;
        this.factionName = factionName;
        this.standing = standing;
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(data);
        buffer.writeUtf(teamName.toString());
        buffer.writeUtf(factionName);
        buffer.writeInt(standing);
    }

    @Override
    protected void readFromStream(ByteBuf data) throws IOException {
        FriendlyByteBuf buffer = new FriendlyByteBuf(data);
        teamName = new ResourceLocation(buffer.readUtf(100));
        factionName = buffer.readUtf(100);
        standing = buffer.readInt();
    }

    @Override
    protected void execute(Player player) {
        TeamData teamData = AWGameData.INSTANCE.getData(player.level(), TeamData.class);
        teamData.updateTeamStanding(teamName, factionName, standing);
    }
}
