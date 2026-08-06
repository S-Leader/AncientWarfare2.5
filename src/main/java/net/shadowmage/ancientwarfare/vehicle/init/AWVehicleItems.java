package net.shadowmage.ancientwarfare.vehicle.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.shadowmage.ancientwarfare.core.item.ItemBase;
import net.shadowmage.ancientwarfare.core.util.LegacyRegistryHelper;
import net.shadowmage.ancientwarfare.vehicle.AncientWarfareVehicles;
import net.shadowmage.ancientwarfare.vehicle.entity.IVehicleType;
import net.shadowmage.ancientwarfare.vehicle.entity.types.VehicleType;
import net.shadowmage.ancientwarfare.vehicle.item.ItemMisc;
import net.shadowmage.ancientwarfare.vehicle.item.ItemSpawner;
import net.shadowmage.ancientwarfare.vehicle.registry.AmmoRegistry;
import net.shadowmage.ancientwarfare.vehicle.registry.ArmorRegistry;
import net.shadowmage.ancientwarfare.vehicle.registry.UpgradeRegistry;
import net.shadowmage.ancientwarfare.vehicle.registry.VehicleRegistry;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = AncientWarfareVehicles.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AWVehicleItems {
    private AWVehicleItems() {
    }

    /**
     * Compatibility alias for integrations that only need a vehicle-tab icon. It points to the
     * first independently registered vehicle item; it is no longer a shared metadata spawner.
     */
    @Deprecated
    public static ItemBase SPAWNER;

    private static final Map<Integer, ItemSpawner> VEHICLE_ITEMS = new LinkedHashMap<>();

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
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

            // Loading/configuring VehicleRegistry initializes all concrete IVehicleType instances.
            VehicleRegistry.registerVehicles();
            VEHICLE_ITEMS.clear();
            for (IVehicleType type : VehicleType.vehicleTypes) {
                if (type == null || type.getMaterialType() == null) {
                    continue;
                }
                ItemSpawner item = LegacyRegistryHelper.register(helper, new ItemSpawner(type));
                VEHICLE_ITEMS.put(type.getGlobalVehicleType(), item);
                if (SPAWNER == null) {
                    SPAWNER = item;
                }
            }
        });
    }

    @Nullable
    public static ItemSpawner getVehicleItem(IVehicleType type) {
        return type == null ? null : VEHICLE_ITEMS.get(type.getGlobalVehicleType());
    }

    @Nullable
    public static ItemSpawner getVehicleItem(int globalType) {
        return VEHICLE_ITEMS.get(globalType);
    }

    public static Collection<ItemSpawner> getVehicleItems() {
        return Collections.unmodifiableCollection(VEHICLE_ITEMS.values());
    }

    private static void registerMisc(RegisterEvent.RegisterHelper<Item> helper, String name, ItemMisc.VehicleItemType type) {
        LegacyRegistryHelper.register(helper, new ItemMisc(name, type));
    }
}
