package net.shadowmage.ancientwarfare.vehicle.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.vehicle.AncientWarfareVehicles;
import net.shadowmage.ancientwarfare.vehicle.entity.IVehicleType;
import net.shadowmage.ancientwarfare.vehicle.entity.types.VehicleType;
import net.shadowmage.ancientwarfare.vehicle.item.ItemMisc;
import net.shadowmage.ancientwarfare.vehicle.item.ItemSpawner;
import net.shadowmage.ancientwarfare.vehicle.registry.VehicleRegistry;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Native Forge item registration for the vehicle module.
 *
 * Every concrete vehicle has its own registry entry.  Vehicle material level and preserved
 * health remain per-stack data, but registry identity is never selected through metadata or
 * through a registration-event callback.
 */
public final class AWVehicleItems {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AncientWarfareVehicles.MOD_ID);

    public static final RegistryObject<ItemMisc> FLAME_CHARGE = misc("flame_charge", ItemMisc.VehicleItemType.AMMO_MATERIAL);
    public static final RegistryObject<ItemMisc> EXPLOSIVE_CHARGE = misc("explosive_charge", ItemMisc.VehicleItemType.AMMO_MATERIAL);
    public static final RegistryObject<ItemMisc> ROCKET_CHARGE = misc("rocket_charge", ItemMisc.VehicleItemType.AMMO_MATERIAL);
    public static final RegistryObject<ItemMisc> CLUSTER_CHARGE = misc("cluster_charge", ItemMisc.VehicleItemType.AMMO_MATERIAL);
    public static final RegistryObject<ItemMisc> NAPALM_CHARGE = misc("napalm_charge", ItemMisc.VehicleItemType.AMMO_MATERIAL);
    public static final RegistryObject<ItemMisc> CLAY_CASING = misc("clay_casing", ItemMisc.VehicleItemType.AMMO_MATERIAL);
    public static final RegistryObject<ItemMisc> IRON_CASING = misc("iron_casing", ItemMisc.VehicleItemType.AMMO_MATERIAL);

    public static final RegistryObject<ItemMisc> MOBILITY_UNIT = misc("mobility_unit", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
    public static final RegistryObject<ItemMisc> TURRET_COMPONENTS = misc("turret_components", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
    public static final RegistryObject<ItemMisc> TORSION_UNIT = misc("torsion_unit", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
    public static final RegistryObject<ItemMisc> COUNTER_WEIGHT_UNIT = misc("counter_weight_unit", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
    public static final RegistryObject<ItemMisc> POWDER_CASE = misc("powder_case", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
    public static final RegistryObject<ItemMisc> EQUIPMENT_BAY = misc("equipment_bay", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
    public static final RegistryObject<ItemMisc> ROUGH_WOOD = misc("rough_wood", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
    public static final RegistryObject<ItemMisc> TREATED_WOOD = misc("treated_wood", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
    public static final RegistryObject<ItemMisc> IRONSHOD_WOOD = misc("ironshod_wood", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
    public static final RegistryObject<ItemMisc> IRON_CORE_WOOD = misc("iron_core_wood", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
    public static final RegistryObject<ItemMisc> ROUGH_IRON = misc("rough_iron", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
    public static final RegistryObject<ItemMisc> FINE_IRON = misc("fine_iron", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
    public static final RegistryObject<ItemMisc> TEMPERED_IRON = misc("tempered_iron", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
    public static final RegistryObject<ItemMisc> MINOR_ALLOY = misc("minor_alloy", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);
    public static final RegistryObject<ItemMisc> MAJOR_ALLOY = misc("major_alloy", ItemMisc.VehicleItemType.VEHICLE_COMPONENT);

    private static final Map<Integer, RegistryObject<ItemSpawner>> VEHICLE_ITEMS = new LinkedHashMap<>();
    private static boolean vehicleItemsPrepared;

    /** First concrete vehicle item; used only as the creative-tab icon. */
    @Nullable
    public static RegistryObject<ItemSpawner> SPAWNER;

    private AWVehicleItems() {
    }

    private static RegistryObject<ItemMisc> misc(String name, ItemMisc.VehicleItemType type) {
        return ITEMS.register(name, () -> new ItemMisc(name, type));
    }

    /**
     * Registers the concrete vehicle item suppliers before the DeferredRegister is attached.
     * Ammo/armor/upgrade definitions must have been prepared first because VehicleType
     * constructors reference those definitions.
     */
    private static synchronized void prepareVehicleItems() {
        if (vehicleItemsPrepared) {
            return;
        }

        VehicleRegistry.registerVehicles();
        for (IVehicleType type : VehicleType.vehicleTypes) {
            if (type == null || type.getMaterialType() == null) {
                continue;
            }
            String registryPath = type.getConfigName();
            RegistryObject<ItemSpawner> item = ITEMS.register(registryPath, () -> new ItemSpawner(type));
            VEHICLE_ITEMS.put(type.getGlobalVehicleType(), item);
            if (SPAWNER == null) {
                SPAWNER = item;
            }
        }
        vehicleItemsPrepared = true;
    }

    public static void register(IEventBus modBus) {
        prepareVehicleItems();
        ITEMS.register(modBus);
    }

    @Nullable
    public static ItemSpawner getVehicleItem(IVehicleType type) {
        return type == null ? null : getVehicleItem(type.getGlobalVehicleType());
    }

    @Nullable
    public static ItemSpawner getVehicleItem(int globalType) {
        RegistryObject<ItemSpawner> item = VEHICLE_ITEMS.get(globalType);
        return item == null ? null : item.get();
    }

    public static Collection<ItemSpawner> getVehicleItems() {
        return Collections.unmodifiableList(VEHICLE_ITEMS.values().stream()
                .map(RegistryObject::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }
}
