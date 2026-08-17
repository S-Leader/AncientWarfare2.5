package net.shadowmage.ancientwarfare.vehicle;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.core.util.LegacyCreativeTabContents;
import net.shadowmage.ancientwarfare.vehicle.init.AWVehicleItems;

/**
 * 1.20.1 creative tab registration for vehicle content.
 */
public final class AWVehicleTab {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AncientWarfareVehicles.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("vehicles", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("tabs.vehicles"))
                    .icon(() -> AWVehicleItems.SPAWNER == null ? ItemStack.EMPTY : new ItemStack(AWVehicleItems.SPAWNER.get()))
                    .displayItems((parameters, output) -> ForgeRegistries.ITEMS.getValues().stream()
                            .filter(item -> {
                                var id = ForgeRegistries.ITEMS.getKey(item);
                                return id != null && AncientWarfareVehicles.MOD_ID.equals(id.getNamespace());
                            })
                            .filter(item -> !(item instanceof net.shadowmage.ancientwarfare.vehicle.item.ItemAmmo ammo)
                                    || ammo.isVisibleInCreativeTab())
                            .flatMap(item -> LegacyCreativeTabContents.stacksFor(item).stream())
                            .forEach(output::accept))
                    .build());

    private AWVehicleTab() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
