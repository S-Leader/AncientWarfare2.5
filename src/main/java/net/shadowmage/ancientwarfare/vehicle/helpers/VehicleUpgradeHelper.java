package net.shadowmage.ancientwarfare.vehicle.helpers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraftforge.common.util.INBTSerializable;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.vehicle.armors.IVehicleArmor;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;
import net.shadowmage.ancientwarfare.vehicle.missiles.DamageType;
import net.shadowmage.ancientwarfare.vehicle.network.PacketUpgradeUpdate;
import net.shadowmage.ancientwarfare.vehicle.network.PacketVehicleBase;
import net.shadowmage.ancientwarfare.vehicle.registry.ArmorRegistry;
import net.shadowmage.ancientwarfare.vehicle.registry.UpgradeRegistry;
import net.shadowmage.ancientwarfare.vehicle.upgrades.IVehicleUpgradeType;

import java.util.ArrayList;
import java.util.List;

public class VehicleUpgradeHelper implements INBTSerializable<CompoundTag> {

    /**
     * currently installed upgrades, will be iterated through linearly to call upgrade.applyEffects, multiple upgrades may have cumulative effects
     */
    private List<IVehicleUpgradeType> upgrades = new ArrayList<>();
    private List<IVehicleArmor> installedArmor = new ArrayList<>();

    /**
     * list of all upgrades that are valid for this vehicle, used by inventoryChecking to see whether it can be installed or not
     */
    private List<IVehicleUpgradeType> validUpgrades = new ArrayList<>();
    private List<IVehicleArmor> validArmorTypes = new ArrayList<>();
    private VehicleBase vehicle;

    public VehicleUpgradeHelper(VehicleBase vehicle) {
        this.vehicle = vehicle;
    }

    /**
     * SERVER ONLY
     */
    public void updateUpgrades() {
        if (vehicle.level().isClientSide) {
            return;
        }
        upgrades = vehicle.inventory.getInventoryUpgrades();

        CompoundTag tag = new CompoundTag();
        serializeUpgrades(tag);

        installedArmor = vehicle.inventory.getInventoryArmor();

        serializeInstalledArmors(tag);

        PacketVehicleBase pkt = new PacketUpgradeUpdate(vehicle);
        NetworkHandler.sendToAllTracking(vehicle, pkt);
        this.updateUpgradeStats();
    }

    private void serializeUpgrades(CompoundTag tag) {
        ListTag upgradesNbt = new ListTag();
        for (String upgrade : serializeUpgrades()) {
            upgradesNbt.add(StringTag.valueOf(upgrade));
        }
        tag.put("upgrades", upgradesNbt);
    }

    public String[] serializeUpgrades() {
        int len = this.upgrades.size();
        String[] registryNames = new String[len];
        for (int i = 0; i < this.upgrades.size(); i++) {
            registryNames[i] = this.upgrades.get(i).getRegistryName().toString();
        }
        return registryNames;
    }

    private void serializeInstalledArmors(CompoundTag tag) {
        ListTag armorTypes = new ListTag();
        for (String armor : serializeInstalledArmors()) {
            armorTypes.add(StringTag.valueOf(armor));
        }

        tag.put("armors", armorTypes);
    }

    public String[] serializeInstalledArmors() {
        String[] armors = new String[installedArmor.size()];
        for (int i = 0; i < installedArmor.size(); i++) {
            armors[i] = installedArmor.get(i).getRegistryName().toString();
        }
        return armors;
    }

    /**
     * CLIENT ONLY..receives the packet sent above, and sets upgrade list directly from registry
     */
    public void updateUpgrades(String[] armorRegistryNames, String[] upgradeRegistryNames) {
        deserializeInstalledArmor(armorRegistryNames);
        deserializeUpgrades(upgradeRegistryNames);

        updateUpgradeStats();
    }

    public List<IVehicleUpgradeType> getUpgrades() {
        return upgrades;
    }

    private void deserializeInstalledArmor(CompoundTag tag) {
        ListTag armorTypes = tag.getList("armors", Constants.NBT.TAG_STRING);
        String[] armorRegistryNames = new String[armorTypes.size()];
        for (int i = 0; i < armorRegistryNames.length; i++) {
            armorRegistryNames[i] = armorTypes.get(i).getAsString();
        }
        deserializeInstalledArmor(armorRegistryNames);
    }

    private void deserializeInstalledArmor(String[] armorRegistryNames) {
        installedArmor.clear();
        for (String armorRegistryName : armorRegistryNames) {
            ArmorRegistry.getArmorType(new ResourceLocation(armorRegistryName)).ifPresent(armor -> installedArmor.add(armor));
        }
    }

    private void deserializeUpgrades(CompoundTag tag) {
        ListTag upgradeTypes = tag.getList("upgrades", Constants.NBT.TAG_STRING);
        String[] upgradeRegistryNames = new String[upgradeTypes.size()];
        for (int i = 0; i < upgradeRegistryNames.length; i++) {
            upgradeRegistryNames[i] = upgradeTypes.get(i).getAsString();
        }
        deserializeUpgrades(upgradeRegistryNames);
    }

    private void deserializeUpgrades(String[] upgradeRegistryNames) {
        upgrades.clear();
        for (String upgradeRegistryName : upgradeRegistryNames) {
            UpgradeRegistry.getUpgrade(new ResourceLocation(upgradeRegistryName)).ifPresent(upgrades::add);
        }
    }

    /**
     * reset stats to base stats
     * iterate through upgrades, applying their effects each in turn (multiple same upgrades are cumulative)
     */
    public void updateUpgradeStats() {
        vehicle.resetCurrentStats();
        for (IVehicleUpgradeType upgrade : this.upgrades) {
            upgrade.applyVehicleEffects(vehicle);
        }
        for (IVehicleArmor armor : this.installedArmor) {
            vehicle.currentExplosionResist += armor.getExplosiveDamageReduction();
            vehicle.currentFireResist += armor.getFireDamageReduction();
            vehicle.currentGenericResist += armor.getGeneralDamageReduction();
            vehicle.currentWeight += armor.getArmorWeight();
        }
    }

    public void addValidArmor(IVehicleArmor armor) {
        if (!this.validArmorTypes.contains(armor)) {
            this.validArmorTypes.add(armor);
        }
    }

    public void addValidUpgrade(IVehicleUpgradeType upgrade) {
        if (!this.validUpgrades.contains(upgrade)) {
            this.validUpgrades.add(upgrade);
        }
    }

    public float getScaledDamage(DamageSource src, float amt) {
        if (src == DamageType.explosiveMissile || (src instanceof DamageType type && type.isExplosion()) || src.is(DamageTypeTags.IS_EXPLOSION)) {
            return amt * (1 - (vehicle.currentExplosionResist * 0.01f));
        } else if (src == DamageType.fireMissile || (src instanceof DamageType type && type.isFireDamage()) || src.is(DamageTypes.IN_FIRE)
                || src.is(DamageTypes.LAVA) || src.is(DamageTypes.ON_FIRE) || src.is(DamageTypeTags.IS_FIRE)) {
            return amt * (1 - (vehicle.currentFireResist * 0.01f));
        }
        return amt * (1 - (vehicle.currentGenericResist * 0.01f));
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        serializeUpgrades(tag);
        serializeInstalledArmors(tag);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        deserializeUpgrades(tag);
        deserializeInstalledArmor(tag);
    }

    public boolean hasUpgrade(IVehicleUpgradeType upgrade) {
        return this.upgrades.contains(upgrade);
    }

}
