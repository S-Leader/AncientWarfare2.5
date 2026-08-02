package net.shadowmage.ancientwarfare.vehicle.entity.types;

import net.minecraft.resources.ResourceLocation;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.vehicle.VehicleVarHelpers.DummyVehicleHelper;
import net.shadowmage.ancientwarfare.vehicle.config.AWVehicleStatics;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;
import net.shadowmage.ancientwarfare.vehicle.entity.materials.VehicleMaterial;
import net.shadowmage.ancientwarfare.vehicle.helpers.VehicleFiringVarsHelper;
import net.shadowmage.ancientwarfare.vehicle.registry.ArmorRegistry;
import net.shadowmage.ancientwarfare.vehicle.registry.UpgradeRegistry;

public class VehicleTypeChestCart extends VehicleType {

    public VehicleTypeChestCart(int typeNum) {
        super(typeNum);
        this.configName = "chest_cart";
        this.vehicleMaterial = VehicleMaterial.materialWood;
        this.materialCount = 3;
        baseHealth = AWVehicleStatics.vehicleStats.vehicleChestCartHealth;
        this.validArmors.add(ArmorRegistry.armorStone);
        this.validArmors.add(ArmorRegistry.armorObsidian);
        this.validArmors.add(ArmorRegistry.armorIron);
        this.validUpgrades.add(UpgradeRegistry.speedUpgrade);
        this.width = 2.7f;
        this.height = 1.8f;
        this.mountable = true;
        this.drivable = true;
        this.combatEngine = false;
        this.riderSits = false;
        this.pilotableBySoldiers = false;
        this.riderVerticalOffset = 0.35f;
        this.riderForwardsOffset = 2.85f;
        this.baseForwardSpeed = 3.7f * 0.05f;
        this.baseStrafeSpeed = 1.75f;
        this.ammoBaySize = 0;
        this.upgradeBaySize = 6;
        this.armorBaySize = 6;
        this.storageBaySize = 54 * 4;
        this.displayName = "item.vehicleSpawner.17";
        this.displayTooltip.add("item.vehicleSpawner.tooltip.noweapon");
        this.displayTooltip.add("item.vehicleSpawner.tooltip.mobile");
        this.displayTooltip.add("item.vehicleSpawner.tooltip.noturret");
        this.displayTooltip.add("item.vehicleSpawner.tooltip.storage");
    }

    @Override
    public VehicleFiringVarsHelper getFiringVarsHelper(VehicleBase veh) {
        return new DummyVehicleHelper(veh);
    }

    @Override
    public ResourceLocation getTextureForMaterialLevel(int level) {
        switch (level) {
            case 0:
                return new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/chest_cart_1.png");
            case 1:
                return new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/chest_cart_2.png");
            case 2:
                return new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/chest_cart_3.png");
            case 3:
                return new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/chest_cart_4.png");
            case 4:
                return new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/chest_cart_5.png");
            default:
                return new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/chest_cart_1.png");
        }
    }

}
