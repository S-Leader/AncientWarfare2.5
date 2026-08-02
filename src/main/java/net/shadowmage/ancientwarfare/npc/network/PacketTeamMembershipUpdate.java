package net.shadowmage.ancientwarfare.npc.network;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.core.network.PacketBase;
import net.shadowmage.ancientwarfare.npc.gamedata.TeamData;

import java.io.IOException;
import java.util.Map;

public class PacketTeamMembershipUpdate extends PacketBase {
    private ResourceLocation teamName;
    private String playerName;
    private Action action;

    public PacketTeamMembershipUpdate() {
    }

    public PacketTeamMembershipUpdate(ResourceLocation teamName, String playerName, Action action) {
        this.teamName = teamName;
        this.playerName = playerName;
        this.action = action;
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(data);
        buffer.writeUtf(teamName.toString());
        buffer.writeUtf(playerName);
        buffer.writeShort(action.getId());
    }

    @Override
    protected void readFromStream(ByteBuf data) throws IOException {
        FriendlyByteBuf buffer = new FriendlyByteBuf(data);
        teamName = new ResourceLocation(buffer.readUtf(100));
        playerName = buffer.readUtf(100);
        action = Action.fromId(buffer.readShort());
    }

    @Override
    protected void execute(Player player) {
        TeamData teamData = AWGameData.INSTANCE.getData(player.level(), TeamData.class);
        if (action == Action.ADD) {
            teamData.addTeamMember(teamName, playerName);
        } else if (action == Action.REMOVE) {
            teamData.removeTeamMember(teamName, playerName, player.level().getGameTime(), false);
        }
    }

    public enum Action {
        ADD(0),
        REMOVE(1);

        private int id;

        Action(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        private static final Map<Integer, Action> VALUES;

        static {
            ImmutableMap.Builder<Integer, Action> builder = new ImmutableMap.Builder<>();
            for (Action action : values()) {
                builder.put(action.getId(), action);
            }
            VALUES = builder.build();
        }

        public static Action fromId(int id) {
            return VALUES.get(id);
        }
    }
}
