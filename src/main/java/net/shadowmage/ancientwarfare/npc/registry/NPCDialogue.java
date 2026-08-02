package net.shadowmage.ancientwarfare.npc.registry;

/*
    Helper class for determining what an NPC will say when interacted with.
    Reads all dialogue from assets/ancientwarfare/registry/npc/dialogue.json
 */

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.registry.IRegistryDataParser;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.entity.faction.NpcFaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NPCDialogue {

    private static Map<String, FactionJSON> dialogueData = Collections.emptyMap();

    // Initializes the dialogue lists
    public static class Parser implements IRegistryDataParser {
        @Override
        public String getName() {
            return "dialogue";
        }

        @SuppressWarnings("squid:S2696")
        @Override
        public void parse(JsonObject json) {
            Gson gson = new Gson();
            Map<String, FactionJSON> parsed = gson.fromJson(json, new TypeToken<Map<String, FactionJSON>>() {
            }.getType());
            dialogueData = parsed == null ? Collections.emptyMap() : parsed;
        }
    }


    // Figures out what message to send the given player, based on a variety of factors.
    public static void speakToPlayer(Player player, NpcFaction npc) {
        boolean isFemale = npc.isFemale();
        boolean isHostile = npc.isHostileTowards(player);
        double seed = npc.dialogueSeed;
        String factionName = npc.getFaction();
        String npcName = npc.getName().getString();
        String profession = npc.getNpcType();
        String message = getRandomDialogue(factionName, isHostile, isFemale, profession, seed);
        speakToPlayer(player, ChatFormatting.YELLOW + "[" + npcName + "] " + ChatFormatting.WHITE + message);
    }

    // Sends a chat message to the given player.
    public static void speakToPlayer(Player player, String message) {
        player.sendSystemMessage(Component.literal(message));
    }

    // Searches through all possible dialogue options and picks a valid one.
    // Uses the seeded RNG that each NPC has.
    private static String getRandomDialogue(String factionName, boolean isHostile, boolean isFemale, String profession, double seed) {
        FactionJSON factionJSON = dialogueData.get(factionName);
        if (factionJSON == null) {
            AncientWarfareNPC.LOG.error("Faction {} has no configured dialogue", factionName);
            return "I have no valid dialogue configured.";
        }

        List<String> validLines = new ArrayList<>();
        addLines(validLines, isHostile ? factionJSON.hostile : factionJSON.friendly);
        Map<String, List<String>> genderLines = isHostile ? factionJSON.hostileByGender : factionJSON.friendlyByGender;
        Map<String, List<String>> professionLines = isHostile ? factionJSON.hostileByProfession : factionJSON.friendlyByProfession;
        addLines(validLines, genderLines == null ? null : genderLines.get(isFemale ? "female" : "male"));
        addLines(validLines, professionLines == null ? null : professionLines.get(profession));

        if (validLines.isEmpty()) {
            AncientWarfareNPC.LOG.error("Faction {} has no valid {} dialogue", factionName, isHostile ? "hostile" : "friendly");
            return "I have no valid dialogue configured.";
        }

        // Use the seed from the NPC to pick an option from the list:
        int index = Math.min(validLines.size() - 1, Math.max(0, (int) (seed * validLines.size())));
        return validLines.get(index);
    }

    private static void addLines(List<String> target, List<String> source) {
        if (source != null) {
            source.stream().filter(line -> line != null && !line.isBlank()).forEach(target::add);
        }
    }

    public static class FactionJSON {
        List<String> friendly;
        List<String> hostile;
        @SerializedName("friendly_by_gender")
        Map<String, List<String>> friendlyByGender;
        @SerializedName("hostile_by_gender")
        Map<String, List<String>> hostileByGender;
        @SerializedName("friendly_by_profession")
        Map<String, List<String>> friendlyByProfession;
        @SerializedName("hostile_by_profession")
        Map<String, List<String>> hostileByProfession;

        @Override
        public String toString() {
            return "FactionJSON{" +
                    "friendlyDialogue=" + friendly +
                    ", hostileDialogue=" + hostile +
                    '}';
        }
    }

}
