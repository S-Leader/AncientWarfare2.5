package net.shadowmage.ancientwarfare.npc.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.shadowmage.ancientwarfare.core.command.RootCommand;
import net.shadowmage.ancientwarfare.core.command.SimpleSubCommand;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.core.util.StringTools;
import net.shadowmage.ancientwarfare.npc.gamedata.Team;
import net.shadowmage.ancientwarfare.npc.gamedata.TeamData;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CommandTeams extends RootCommand {
    public CommandTeams() {
        registerSubCommand(new SimpleSubCommand("list", (server, sender, args) -> {
            for (Team team : AWGameData.INSTANCE.getData(sender.getEntityWorld(), TeamData.class).getTeams()) {
                sender.sendMessage(Component.literal(team.getName().toString()));
            }
        }));
        registerSubCommand(new SimpleSubCommand("describe", (server, sender, args) -> {
            if (args.length == 0) {
                sender.sendMessage(Component.translatable(CommandTeams.this.getUsage(sender)));
                return;
            }
            Optional<Team> t = AWGameData.INSTANCE.getData(sender.getEntityWorld(), TeamData.class).getTeam(new ResourceLocation(args[0]));

            if (t.isPresent()) {
                Team team = t.get();
                sender.sendMessage(Component.translatable("command.aw.teams.team_members", StringTools.joinElements(", ", team.getMembers())));
                outputTeamStandings(sender, team);
            } else {
                sender.sendMessage(Component.translatable("command.aw.teams.team_does_not_exist"));
            }
        }) {
            @Override
            public int getMaxArgs() {
                return 1;
            }
        });
        registerSubCommand(new SimpleSubCommand("player", (server, sender, args) -> {
            sender.sendMessage(Component.translatable("command.aw.teams.member_of"));
            String playerName = args.length == 0 ? sender.getName() : args[0];
            for (Team team : AWGameData.INSTANCE.getData(sender.getEntityWorld(), TeamData.class).getPlayerTeams(playerName)) {
                sender.sendMessage(Component.translatable("command.aw.teams.team_name", team.getName().toString()));
                outputTeamStandings(sender, team);
            }
        }) {
            @Override
            public int getMaxArgs() {
                return 1;
            }
        });
    }

    private void outputTeamStandings(ICommandSender sender, Team team) {
        sender.sendMessage(Component.translatable("command.aw.teams.standings"));
        for (Map.Entry<String, Integer> standing : StreamSupport.stream(team.getFactionStandings().spliterator(), false)
                .sorted(Comparator.comparing(Map.Entry::getKey)).collect(Collectors.toList())) {
            sender.sendMessage(Component.literal(standing.getKey() + ": " + standing.getValue()));
        }
    }

    @Override
    public String getName() {
        return "awteams";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "command.aw.teams.usage";
    }
}
