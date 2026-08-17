package net.shadowmage.ancientwarfare.structure;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.core.util.LegacyCreativeTabContents;
import net.shadowmage.ancientwarfare.structure.init.AWStructureItems;

/**
 * 1.20.1 creative tab registration for structure content.
 */
public final class AWStructureTab {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AncientWarfareStructure.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("structures", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("tabs.structures"))
                    .icon(() -> new ItemStack(AWStructureItems.STRUCTURE_SCANNER.get()))
                    .displayItems((parameters, output) -> ForgeRegistries.ITEMS.getValues().stream()
                            .filter(item -> {
                                var id = ForgeRegistries.ITEMS.getKey(item);
                                return id != null && AncientWarfareStructure.MOD_ID.equals(id.getNamespace())
                                        && !"gate_spawner".equals(id.getPath())
                                        && !"gate_proxy".equals(id.getPath());
                            })
                            .flatMap(item -> LegacyCreativeTabContents.stacksFor(item).stream())
                            .forEach(output::accept))
                    .build());

    private AWStructureTab() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
