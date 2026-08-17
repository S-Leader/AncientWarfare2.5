package net.shadowmage.ancientwarfare.vehicle.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.shadowmage.ancientwarfare.vehicle.AncientWarfareVehicles;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.vehicle.item.ItemUpgrade;
import net.shadowmage.ancientwarfare.vehicle.upgrades.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UpgradeRegistry {

    public static IVehicleUpgradeType speedUpgrade;
    public static IVehicleUpgradeType aimUpgrade;
    public static IVehicleUpgradeType reloadUpgrade;

    public static IVehicleUpgradeType powerUpgrade;
    public static IVehicleUpgradeType pitchExtUpgrade;
    public static IVehicleUpgradeType pitchUpUpgrade;
    public static IVehicleUpgradeType pitchDownUpgrade;

    private static Map<ResourceLocation, IVehicleUpgradeType> upgradeInstances = new HashMap<>();

    private UpgradeRegistry() {
    }

    public static UpgradeRegistry instance() {
        if (INSTANCE == null) {
            INSTANCE = new UpgradeRegistry();
        }
        return INSTANCE;
    }

    private static UpgradeRegistry INSTANCE;

    public Collection<IVehicleUpgradeType> getUpgradeList() {
        return this.upgradeInstances.values();
    }

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AncientWarfareVehicles.MOD_ID);
    private static boolean prepared;

    public static synchronized void register(IEventBus modBus) {
        if (!prepared) {
            speedUpgrade = registerUpgrade(new VehicleUpgradeSpeed(), "" + AWCoreStatics.vehicleUpgradeMaxSpeed);
            aimUpgrade = registerUpgrade(new VehicleUpgradeAim(), Float.toString(AWCoreStatics.vehicleUpgradeAccuracy));
            reloadUpgrade = registerUpgrade(new VehicleUpgradeReload(), Integer.toString(AWCoreStatics.vehicleUpgradeReloadSpeed));
            powerUpgrade = registerUpgrade(new VehicleUpgradePower(), "" + AWCoreStatics.vehicleUpgradeProjectileSpeed);
            pitchExtUpgrade = registerUpgrade(new VehicleUpgradeTurretPitch(), "" + AWCoreStatics.vehicleUpgradePitchExtension);
            pitchUpUpgrade = registerUpgrade(new VehicleUpgradePitchUp(), "" + AWCoreStatics.vehicleUpgradePitchUp);
            pitchDownUpgrade = registerUpgrade(new VehicleUpgradePitchDown(), "" + AWCoreStatics.vehicleUpgradePitchDown);
            prepared = true;
        }
        ITEMS.register(modBus);
    }

    private static IVehicleUpgradeType registerUpgrade(IVehicleUpgradeType upgrade, String dynamicInfo) {
        ResourceLocation id = upgrade.getRegistryName();
        upgradeInstances.put(id, upgrade);
        ITEMS.register(id.getPath(), () -> new ItemUpgrade(id, dynamicInfo));
        return upgrade;
    }

    public static Optional<IVehicleUpgradeType> getUpgrade(ResourceLocation type) {
        return Optional.ofNullable(upgradeInstances.get(type));
    }

    public static Optional<IVehicleUpgradeType> getUpgrade(ItemStack stack) {
        return Optional.ofNullable(upgradeInstances.get(ForgeRegistries.ITEMS.getKey(stack.getItem())));
    }
}
