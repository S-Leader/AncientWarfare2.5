package net.shadowmage.ancientwarfare.vehicle.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.shadowmage.ancientwarfare.core.util.LegacyRegistryHelper;
import net.shadowmage.ancientwarfare.vehicle.armors.IVehicleArmor;
import net.shadowmage.ancientwarfare.vehicle.armors.VehicleArmorIron;
import net.shadowmage.ancientwarfare.vehicle.armors.VehicleArmorObsidian;
import net.shadowmage.ancientwarfare.vehicle.armors.VehicleArmorStone;
import net.shadowmage.ancientwarfare.vehicle.item.ItemArmor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ArmorRegistry {
    private ArmorRegistry() {
    }

    public static IVehicleArmor armorStone;
    public static IVehicleArmor armorIron;
    public static IVehicleArmor armorObsidian;

    private static Map<ResourceLocation, IVehicleArmor> armorInstances = new HashMap<>();

    public static void registerArmorTypes(RegisterEvent.RegisterHelper<Item> helper) {
        armorStone = registerArmorType(new VehicleArmorStone(), helper);
        armorIron = registerArmorType(new VehicleArmorIron(), helper);
        armorObsidian = registerArmorType(new VehicleArmorObsidian(), helper);
    }

    private static IVehicleArmor registerArmorType(IVehicleArmor armor, RegisterEvent.RegisterHelper<Item> helper) {

        armorInstances.put(armor.getRegistryName(), armor);
        ItemArmor item = new ItemArmor(armor.getRegistryName(), armor);
        LegacyRegistryHelper.register(helper, item);
        return armor;
    }

    public static Optional<IVehicleArmor> getArmorType(ResourceLocation registryName) {
        return Optional.ofNullable(armorInstances.get(registryName));
    }

    public static Optional<IVehicleArmor> getArmorForStack(ItemStack stack) {
        return Optional.ofNullable(armorInstances.get(ForgeRegistries.ITEMS.getKey(stack.getItem())));
    }
}
