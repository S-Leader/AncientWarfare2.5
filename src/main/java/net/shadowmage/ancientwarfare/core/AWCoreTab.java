package net.shadowmage.ancientwarfare.core;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.core.init.AWCoreItems;
import net.shadowmage.ancientwarfare.core.util.LegacyCreativeTabContents;

/**
 * 1.20.1 creative tab registration for the core module.
 */
public final class AWCoreTab {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AncientWarfareCore.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("core", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("tabs.awcore"))
                    .icon(() -> new ItemStack(AWCoreItems.RESEARCH_BOOK))
                    .displayItems((parameters, output) -> ForgeRegistries.ITEMS.getValues().stream()
                            .filter(item -> {
                                var id = ForgeRegistries.ITEMS.getKey(item);
                                return item != AWCoreItems.LEGACY_COMPONENT
                                        && id != null && AncientWarfareCore.MOD_ID.equals(id.getNamespace());
                            })
                            .flatMap(item -> LegacyCreativeTabContents.stacksFor(item).stream())
                            .forEach(output::accept))
                    .build());

    private AWCoreTab() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
