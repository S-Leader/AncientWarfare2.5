package net.shadowmage.ancientwarfare.npc;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.core.util.LegacyCreativeTabContents;
import net.shadowmage.ancientwarfare.core.util.SortItemsFirstComparator;
import net.shadowmage.ancientwarfare.npc.item.ItemCommandBaton;
import net.shadowmage.ancientwarfare.npc.item.ItemOrders;

import java.util.ArrayList;
import java.util.List;

import static net.shadowmage.ancientwarfare.npc.init.AWNPCBlocks.TOWN_HALL;
import static net.shadowmage.ancientwarfare.npc.init.AWNPCItems.BARD_INSTRUMENT;
import static net.shadowmage.ancientwarfare.npc.init.AWNPCItems.NPC_SPAWNER;

/**
 * 1.20.1 creative tab registration for NPC content.
 */
public final class AWNPCTab {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AncientWarfareNPC.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("npc", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("tabs.npc"))
                    .icon(() -> new ItemStack(NPC_SPAWNER))
                    .displayItems((parameters, output) -> {
                        List<ItemStack> stacks = new ArrayList<>();
                        ForgeRegistries.ITEMS.getValues().stream()
                                .filter(item -> {
                                    var id = ForgeRegistries.ITEMS.getKey(item);
                                    return id != null && AncientWarfareNPC.MOD_ID.equals(id.getNamespace());
                                })
                                .flatMap(item -> LegacyCreativeTabContents.stacksFor(item).stream())
                                .forEach(stacks::add);
                        stacks.sort(new SortItemsFirstComparator(
                                TOWN_HALL, ItemOrders.class, ItemCommandBaton.class,
                                BARD_INSTRUMENT, NPC_SPAWNER));
                        stacks.forEach(output::accept);
                    })
                    .build());

    private AWNPCTab() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
