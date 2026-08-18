package net.shadowmage.ancientwarfare.automation;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.automation.init.AWAutomationBlocks;
import net.shadowmage.ancientwarfare.automation.init.AWAutomationItems;
import net.shadowmage.ancientwarfare.core.init.AWCoreItems;
import net.shadowmage.ancientwarfare.core.util.LegacyCreativeTabContents;
import net.shadowmage.ancientwarfare.core.util.SortItemsFirstComparator;

import java.util.ArrayList;
import java.util.List;

import static net.shadowmage.ancientwarfare.automation.init.AWAutomationBlocks.*;

/**
 * 1.20.1 creative tab registration for automation content.
 */
public final class AWAutomationTab {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AncientWarfareAutomation.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("automation", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("tabs.automation"))
                    .icon(() -> new ItemStack(AWCoreItems.IRON_HAMMER.get()))
                    .displayItems((parameters, output) -> {
                        List<ItemStack> stacks = new ArrayList<>();
                        ForgeRegistries.ITEMS.getValues().stream()
                                .filter(item -> {
                                    var id = ForgeRegistries.ITEMS.getKey(item);
                                    return id != null
                                            && AncientWarfareAutomation.MOD_ID.equals(id.getNamespace());
                                })
                                .flatMap(item -> LegacyCreativeTabContents.stacksFor(item).stream())
                                .forEach(stacks::add);
                        stacks.sort(new SortItemsFirstComparator(
                                TREE_FARM.get(), CROP_FARM.get(), FRUIT_FARM.get(), ANIMAL_FARM.get(), FISH_FARM.get(),
                                QUARRY.get(), AUTO_CRAFTING.get(), WAREHOUSE_CONTROL.get(), WAREHOUSE_INTERFACE.get(),
                                WAREHOUSE_CRAFTING.get(), WAREHOUSE_STOCK_VIEWER.get(), WAREHOUSE_STOCK_LINKER.get(),
                                MAILBOX.get(), WINDMILL_BLADE.get()));
                        stacks.forEach(output::accept);
                    })
                    .build());

    private AWAutomationTab() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
