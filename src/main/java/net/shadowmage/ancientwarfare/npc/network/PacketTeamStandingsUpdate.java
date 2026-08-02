package net.shadowmage.ancientwarfare.npc.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.core.network.PacketBase;
import net.shadowmage.ancientwarfare.npc.faction.FactionEntry;
import net.shadowmage.ancientwarfare.npc.gamedata.TeamData;

import java.io.IOException;

public class PacketTeamStandingsUpdate extends PacketBase {
    private ResourceLocation teamName;
    private FactionEntry factionEntry;

    public PacketTeamStandingsUpdate() {
    }

    public PacketTeamStandingsUpdate(ResourceLocation teamName, FactionEntry factionEntry) {
        this.teamName = teamName;
        this.factionEntry = factionEntry;
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(data);
        buffer.writeUtf(teamName.toString());
        buffer.writeNbt(factionEntry.writeToNBT(new CompoundTag()));
    }

    @Override
    protected void readFromStream(ByteBuf data) throws IOException {
        FriendlyByteBuf buffer = new FriendlyByteBuf(data);
        teamName = new ResourceLocation(buffer.readUtf(100));
        CompoundTag tag = buffer.readNbt();
        factionEntry = tag == null ? null : new FactionEntry(tag);
    }

    @Override
    protected void execute(Player player) {
        TeamData teamData = AWGameData.INSTANCE.getData(player.level(), TeamData.class);
        teamData.updateTeamStandings(teamName, factionEntry);
    }
}
