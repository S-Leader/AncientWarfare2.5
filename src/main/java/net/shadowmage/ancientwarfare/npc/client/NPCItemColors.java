package net.shadowmage.ancientwarfare.npc.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.npc.init.AWNPCItems;
import net.shadowmage.ancientwarfare.npc.item.ItemCoin;
import net.shadowmage.ancientwarfare.npc.item.ItemNpcSpawner;
import net.shadowmage.ancientwarfare.npc.registry.FactionRegistry;

@OnlyIn(Dist.CLIENT)
public class NPCItemColors {
    private NPCItemColors() {
    }

    public static final int FACTION_TOP_COLOR = 0xEF5757;

    public static void init() {
        ItemColors itemColors = Minecraft.getInstance().getItemColors();

        itemColors.register(((stack, tintIndex) -> {
            if (tintIndex == 1 || tintIndex == 2) {
                String factionName = ItemNpcSpawner.getFaction(stack).orElse("");
                if (tintIndex == 2) {
                    return FactionRegistry.getFactionNames().contains(factionName) ? FACTION_TOP_COLOR : -1;
                } else {
                    return FactionRegistry.getFaction(factionName).getColor();
                }
            }

            return -1;

        }), AWNPCItems.NPC_SPAWNER.get(),
                AWNPCItems.NPC_SPAWNER_WORKER.get(), AWNPCItems.NPC_SPAWNER_COMBAT.get(),
                AWNPCItems.NPC_SPAWNER_COURIER.get(), AWNPCItems.NPC_SPAWNER_TRADER.get(),
                AWNPCItems.NPC_SPAWNER_PRIEST.get(), AWNPCItems.NPC_SPAWNER_BARD.get(),
                AWNPCItems.NPC_SPAWNER_SIEGE_ENGINEER.get());

        itemColors.register(((stack, tintindex) -> ItemCoin.getMetal(stack).getColor()), AWNPCItems.COIN.get());
    }
}
