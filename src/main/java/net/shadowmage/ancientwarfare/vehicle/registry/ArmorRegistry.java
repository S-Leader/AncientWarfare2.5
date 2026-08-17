package net.shadowmage.ancientwarfare.vehicle.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.vehicle.AncientWarfareVehicles;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
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

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AncientWarfareVehicles.MOD_ID);
    private static final Map<ResourceLocation, IVehicleArmor> armorInstances = new HashMap<>();
    private static final Map<ResourceLocation, RegistryObject<ItemArmor>> armorItems = new HashMap<>();
    private static boolean prepared;

    public static synchronized void register(IEventBus modBus) {
        if (!prepared) {
            armorStone = registerArmorType(new VehicleArmorStone());
            armorIron = registerArmorType(new VehicleArmorIron());
            armorObsidian = registerArmorType(new VehicleArmorObsidian());
            prepared = true;
        }
        ITEMS.register(modBus);
    }

    private static IVehicleArmor registerArmorType(IVehicleArmor armor) {
        ResourceLocation id = armor.getRegistryName();
        armorInstances.put(id, armor);
        armorItems.put(id, ITEMS.register(id.getPath(), () -> new ItemArmor(id, armor)));
        return armor;
    }

    public static Optional<IVehicleArmor> getArmorType(ResourceLocation registryName) {
        return Optional.ofNullable(armorInstances.get(registryName));
    }

    public static Optional<IVehicleArmor> getArmorForStack(ItemStack stack) {
        return Optional.ofNullable(armorInstances.get(ForgeRegistries.ITEMS.getKey(stack.getItem())));
    }
}
