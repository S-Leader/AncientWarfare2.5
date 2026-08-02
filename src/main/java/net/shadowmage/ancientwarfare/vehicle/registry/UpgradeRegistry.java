package net.shadowmage.ancientwarfare.vehicle.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.util.LegacyRegistryHelper;
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

    /**
     * called during init to register upgrade types as items
     */
    public static void registerUpgrades(RegisterEvent.RegisterHelper<Item> helper) {
        speedUpgrade = registerUpgrade(new VehicleUpgradeSpeed(), helper, "" + AWCoreStatics.vehicleUpgradeMaxSpeed);
        aimUpgrade = registerUpgrade(new VehicleUpgradeAim(), helper, Float.toString(AWCoreStatics.vehicleUpgradeAccuracy));
        reloadUpgrade = registerUpgrade(new VehicleUpgradeReload(), helper, Integer.toString(AWCoreStatics.vehicleUpgradeReloadSpeed));
        powerUpgrade = registerUpgrade(new VehicleUpgradePower(), helper, "" + AWCoreStatics.vehicleUpgradeProjectileSpeed);
        pitchExtUpgrade = registerUpgrade(new VehicleUpgradeTurretPitch(), helper, "" + AWCoreStatics.vehicleUpgradePitchExtension);
        pitchUpUpgrade = registerUpgrade(new VehicleUpgradePitchUp(), helper, "" + AWCoreStatics.vehicleUpgradePitchUp);
        pitchDownUpgrade = registerUpgrade(new VehicleUpgradePitchDown(), helper, "" + AWCoreStatics.vehicleUpgradePitchDown);
    }

    private static IVehicleUpgradeType registerUpgrade(IVehicleUpgradeType upgrade, RegisterEvent.RegisterHelper<Item> helper) {
        upgradeInstances.put(upgrade.getRegistryName(), upgrade);
        ItemUpgrade item = new ItemUpgrade(upgrade.getRegistryName(), "");
        LegacyRegistryHelper.register(helper, item);
        return upgrade;
    }

    private static IVehicleUpgradeType registerUpgrade(IVehicleUpgradeType upgrade, RegisterEvent.RegisterHelper<Item> helper, String dynamicInfo) {
        upgradeInstances.put(upgrade.getRegistryName(), upgrade);
        ItemUpgrade item = new ItemUpgrade(upgrade.getRegistryName(), dynamicInfo);
        LegacyRegistryHelper.register(helper, item);
        return upgrade;
    }

    public static Optional<IVehicleUpgradeType> getUpgrade(ResourceLocation type) {
        return Optional.ofNullable(upgradeInstances.get(type));
    }

    public static Optional<IVehicleUpgradeType> getUpgrade(ItemStack stack) {
        return Optional.ofNullable(upgradeInstances.get(ForgeRegistries.ITEMS.getKey(stack.getItem())));
    }
}
