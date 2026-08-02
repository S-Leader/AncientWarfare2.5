package net.shadowmage.ancientwarfare.vehicle.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.shadowmage.ancientwarfare.core.util.LegacyRegistryHelper;
import net.shadowmage.ancientwarfare.vehicle.config.AWVehicleStatics;
import net.shadowmage.ancientwarfare.vehicle.item.ItemAmmo;
import net.shadowmage.ancientwarfare.vehicle.missiles.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AmmoRegistry {

    /**
     * procedure to make new ammo type:
     * create ammo class
     * create static instance below (or anywhere really)
     * register the render in renderRegistry (or register it with renderregistry during startup)
     * add ammo to applicable vehicle type constructors
     */

    public static IAmmo ammoBallShot;
    public static IAmmo ammoBallIronShot;
    public static IAmmo ammoStoneShot10;
    public static IAmmo ammoStoneShot15;
    public static IAmmo ammoStoneShot30;
    public static IAmmo ammoStoneShot45;
    public static IAmmo ammoFireShot10;
    public static IAmmo ammoFireShot15;
    public static IAmmo ammoFireShot30;
    public static IAmmo ammoFireShot45;
    public static IAmmo ammoExplosive10;
    public static IAmmo ammoExplosive15;
    public static IAmmo ammoExplosive30;
    public static IAmmo ammoExplosive45;
    public static IAmmo ammoHE10;
    public static IAmmo ammoHE15;
    public static IAmmo ammoHE30;
    public static IAmmo ammoHE45;
    public static IAmmo ammoNapalm10;
    public static IAmmo ammoNapalm15;
    public static IAmmo ammoNapalm30;
    public static IAmmo ammoNapalm45;
    public static IAmmo ammoClusterShot10;
    public static IAmmo ammoClusterShot15;
    public static IAmmo ammoClusterShot30;
    public static IAmmo ammoClusterShot45;
    public static IAmmo ammoPebbleShot10;
    public static IAmmo ammoPebbleShot15;
    public static IAmmo ammoPebbleShot30;
    public static IAmmo ammoPebbleShot45;
    public static IAmmo ammoIronShot5;
    public static IAmmo ammoIronShot10;
    public static IAmmo ammoIronShot15;
    public static IAmmo ammoIronShot25;
    public static IAmmo ammoCanisterShot5;
    public static IAmmo ammoCanisterShot10;
    public static IAmmo ammoCanisterShot15;
    public static IAmmo ammoCanisterShot25;
    public static IAmmo ammoGrapeShot5;
    public static IAmmo ammoGrapeShot10;
    public static IAmmo ammoGrapeShot15;
    public static IAmmo ammoGrapeShot25;
    public static IAmmo ammoArrow;
    public static IAmmo ammoBallistaBolt;
    public static IAmmo ammoBallistaBoltFlame;
    public static IAmmo ammoBallistaBoltExplosive;
    public static IAmmo ammoBallistaBoltIron;
    public static IAmmo ammoRocket;
    public static IAmmo ammoHwachaRocketFlame;
    public static IAmmo ammoHwachaRocketExplosive;
    public static IAmmo ammoHwachaRocketAirburst;

    private AmmoRegistry() {
    }

    private static Map<ResourceLocation, IAmmo> ammoInstances = new HashMap<>();
    private static Map<ResourceLocation, ItemAmmo> ammoItemInstances = new HashMap<>();

    public static void registerAmmo(RegisterEvent.RegisterHelper<Item> helper) {

        ammoBallShot = registerAmmoType(new AmmoBallShot(), helper);
        ammoBallIronShot = registerAmmoType(new AmmoIronBallShot(), helper);
        ammoStoneShot10 = registerAmmoType(new AmmoStoneShot(10), helper);
        ammoStoneShot15 = registerAmmoType(new AmmoStoneShot(15), helper);
        ammoStoneShot30 = registerAmmoType(new AmmoStoneShot(30), helper);
        ammoStoneShot45 = registerAmmoType(new AmmoStoneShot(45), helper);
        ammoFireShot10 = registerAmmoType(new AmmoFlameShot(10), helper);
        ammoFireShot15 = registerAmmoType(new AmmoFlameShot(15), helper);
        ammoFireShot30 = registerAmmoType(new AmmoFlameShot(30), helper);
        ammoFireShot45 = registerAmmoType(new AmmoFlameShot(45), helper);
        ammoExplosive10 = registerAmmoType(new AmmoExplosiveShot(10, false), helper);
        ammoExplosive15 = registerAmmoType(new AmmoExplosiveShot(15, false), helper);
        ammoExplosive30 = registerAmmoType(new AmmoExplosiveShot(30, false), helper);
        ammoExplosive45 = registerAmmoType(new AmmoExplosiveShot(45, false), helper);
        ammoHE10 = registerAmmoType(new AmmoExplosiveShot(10, true), helper);
        ammoHE15 = registerAmmoType(new AmmoExplosiveShot(15, true), helper);
        ammoHE30 = registerAmmoType(new AmmoExplosiveShot(30, true), helper);
        ammoHE45 = registerAmmoType(new AmmoExplosiveShot(45, true), helper);
        ammoNapalm10 = registerAmmoType(new AmmoNapalmShot(10), helper);
        ammoNapalm15 = registerAmmoType(new AmmoNapalmShot(15), helper);
        ammoNapalm30 = registerAmmoType(new AmmoNapalmShot(30), helper);
        ammoNapalm45 = registerAmmoType(new AmmoNapalmShot(45), helper);
        ammoClusterShot10 = registerAmmoType(new AmmoClusterShot(10), helper);
        ammoClusterShot15 = registerAmmoType(new AmmoClusterShot(15), helper);
        ammoClusterShot30 = registerAmmoType(new AmmoClusterShot(30), helper);
        ammoClusterShot45 = registerAmmoType(new AmmoClusterShot(45), helper);
        ammoPebbleShot10 = registerAmmoType(new AmmoPebbleShot(10), helper);
        ammoPebbleShot15 = registerAmmoType(new AmmoPebbleShot(15), helper);
        ammoPebbleShot30 = registerAmmoType(new AmmoPebbleShot(30), helper);
        ammoPebbleShot45 = registerAmmoType(new AmmoPebbleShot(45), helper);
        ammoIronShot5 = registerAmmoType(new AmmoIronShot(5, AWVehicleStatics.vehicleStats.ammoCannonBall5kgDamage), helper);
        ammoIronShot10 = registerAmmoType(new AmmoIronShot(10, AWVehicleStatics.vehicleStats.ammoCannonBall10kgDamage), helper);
        ammoIronShot15 = registerAmmoType(new AmmoIronShot(15, AWVehicleStatics.vehicleStats.ammoCannonBall15kgDamage), helper);
        ammoIronShot25 = registerAmmoType(new AmmoIronShot(25, AWVehicleStatics.vehicleStats.ammoCannonBall25kgDamage), helper);
        ammoCanisterShot5 = registerAmmoType(new AmmoCanisterShot(5), helper);
        ammoCanisterShot10 = registerAmmoType(new AmmoCanisterShot(10), helper);
        ammoCanisterShot15 = registerAmmoType(new AmmoCanisterShot(15), helper);
        ammoCanisterShot25 = registerAmmoType(new AmmoCanisterShot(25), helper);
        ammoGrapeShot5 = registerAmmoType(new AmmoGrapeShot(5), helper);
        ammoGrapeShot10 = registerAmmoType(new AmmoGrapeShot(10), helper);
        ammoGrapeShot15 = registerAmmoType(new AmmoGrapeShot(15), helper);
        ammoGrapeShot25 = registerAmmoType(new AmmoGrapeShot(25), helper);
        ammoArrow = registerAmmoType(new AmmoArrow(), helper);
        ammoBallistaBolt = registerAmmoType(new AmmoBallistaBolt(), helper);
        ammoBallistaBoltFlame = registerAmmoType(new AmmoBallistaBoltFlame(), helper);
        ammoBallistaBoltExplosive = registerAmmoType(new AmmoBallistaBoltExplosive(), helper);
        ammoBallistaBoltIron = registerAmmoType(new AmmoBallistaBoltIron(), helper);
        ammoRocket = registerAmmoType(new AmmoHwachaRocket(), helper);
        ammoHwachaRocketFlame = registerAmmoType(new AmmoHwachaRocketFlame(), helper);
        ammoHwachaRocketExplosive = registerAmmoType(new AmmoHwachaRocketExplosive(), helper);
        ammoHwachaRocketAirburst = registerAmmoType(new AmmoHwachaRocketAirburst(), helper);
    }

    private static IAmmo registerAmmoType(IAmmo ammo, RegisterEvent.RegisterHelper<Item> helper) {
        ammoInstances.put(ammo.getRegistryName(), ammo);
        ItemAmmo item = new ItemAmmo(ammo.getRegistryName(), ammo);
        ammoItemInstances.put(ammo.getRegistryName(), item);
        LegacyRegistryHelper.register(helper, item);
        return ammo;
    }

    public static Optional<IAmmo> getAmmoForStack(ItemStack stack) {
        return Optional.ofNullable(ammoInstances.get(ForgeRegistries.ITEMS.getKey(stack.getItem())));
    }

    public static IAmmo getAmmo(ResourceLocation registryName) {
        return ammoInstances.get(registryName);
    }

    public static ItemAmmo getItemForAmmo(IAmmo ammo) {
        return ammoItemInstances.get(ammo.getRegistryName());
    }

    public static ItemAmmo getItem(ResourceLocation ammoRegistryName) {
        return ammoItemInstances.get(ammoRegistryName);
    }
}
