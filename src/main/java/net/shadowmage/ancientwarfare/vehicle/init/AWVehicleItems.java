package net.shadowmage.ancientwarfare.vehicle.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.shadowmage.ancientwarfare.core.item.ItemBase;
import net.shadowmage.ancientwarfare.core.util.LegacyRegistryHelper;
import net.shadowmage.ancientwarfare.vehicle.AncientWarfareVehicles;
import net.shadowmage.ancientwarfare.vehicle.item.ItemMisc;
import net.shadowmage.ancientwarfare.vehicle.item.ItemSpawner;
import net.shadowmage.ancientwarfare.vehicle.registry.AmmoRegistry;
import net.shadowmage.ancientwarfare.vehicle.registry.ArmorRegistry;
import net.shadowmage.ancientwarfare.vehicle.registry.UpgradeRegistry;
import net.shadowmage.ancientwarfare.vehicle.registry.VehicleRegistry;

@Mod.EventBusSubscriber(modid = AncientWarfareVehicles.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AWVehicleItems {
    private AWVehicleItems() {
    }

    public static ItemBase SPAWNER;

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
            SPAWNER = LegacyRegistryHelper.register(helper, new ItemSpawner());
            registerMisc(helper, "flame_charge", ItemMisc.VehicleItemType.AMMO_MATERIAL);
            registerMisc(helper, "explosive_charge", ItemMisc.VehicleItemType.AMMO_MATERIAL);
            registerMisc(helper, "rocket_charge", ItemMisc.VehicleItemType.AMMO_MATERIAL);
            registerMisc(helper, "cluster_charge", ItemMisc.VehicleItemType.AMMO_MATERIAL);
            registerMisc(helper, "napalm_charge", ItemMisc.VehicleItemType.AMMO_MATERIAL);
            registerMisc(helper, "clay_casing", ItemMisc.VehicleItemType.AMMO_MATERIAL);
            registerMisc(helper, "iron_casing", ItemMisc.VehicleItemType.AMMO_MATERIAL);
            registerMisc(helper, "mobility_unit", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
            registerMisc(helper, "turret_components", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
            registerMisc(helper, "torsion_unit", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
            registerMisc(helper, "counter_weight_unit", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
            registerMisc(helper, "powder_case", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
            registerMisc(helper, "equipment_bay", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
            registerMisc(helper, "rough_wood", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
            registerMisc(helper, "treated_wood", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
            registerMisc(helper, "ironshod_wood", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
            registerMisc(helper, "iron_core_wood", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
            registerMisc(helper, "rough_iron", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
            registerMisc(helper, "fine_iron", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
            registerMisc(helper, "tempered_iron", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
            registerMisc(helper, "minor_alloy", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
            registerMisc(helper, "major_alloy", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);

            AmmoRegistry.registerAmmo(helper);
            ArmorRegistry.registerArmorTypes(helper);
            UpgradeRegistry.registerUpgrades(helper);
            VehicleRegistry.registerVehicles();
        });
    }

    private static void registerMisc(RegisterEvent.RegisterHelper<Item> helper, String name, ItemMisc.VehicleItemType type) {
        LegacyRegistryHelper.register(helper, new ItemMisc(name, type));
    }
}
