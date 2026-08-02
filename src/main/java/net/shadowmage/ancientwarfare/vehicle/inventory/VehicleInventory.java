package net.shadowmage.ancientwarfare.vehicle.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.shadowmage.ancientwarfare.vehicle.armors.IVehicleArmor;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;
import net.shadowmage.ancientwarfare.vehicle.registry.AmmoRegistry;
import net.shadowmage.ancientwarfare.vehicle.registry.ArmorRegistry;
import net.shadowmage.ancientwarfare.vehicle.registry.UpgradeRegistry;
import net.shadowmage.ancientwarfare.vehicle.registry.VehicleAmmoEntry;
import net.shadowmage.ancientwarfare.vehicle.upgrades.IVehicleUpgradeType;

import java.util.ArrayList;
import java.util.List;

public class VehicleInventory {
    private static final String INVENTORY_TAG = "inventory";
    private VehicleBase vehicle;

    /**
     * individual inventories
     */
    public ItemStackHandler upgradeInventory;
    public ItemStackHandler ammoInventory;
    public ItemStackHandler armorInventory;
    public ItemStackHandler storageInventory;

    public VehicleInventory(VehicleBase vehicle) {
        this.vehicle = vehicle;
    }

    public void setInventorySizes(int upgrade, int ammo, int armor, int storage) {
        // Vehicle type is selected after entity construction, so handlers are initialized here.
        this.ammoInventory = new AmmoStackHandler(vehicle, ammo);
        this.armorInventory = new ArmorStackHandler(vehicle, armor);
        this.storageInventory = new ItemStackHandler(storage);
        this.upgradeInventory = new UpgradeStackHandler(vehicle, upgrade);
    }

    public void writeToNBT(CompoundTag commonTag) {
        CompoundTag tag = new CompoundTag();
        if (this.upgradeInventory != null) {
            tag.put("upgradeInventory", this.upgradeInventory.serializeNBT());
        }
        if (this.ammoInventory != null) {
            tag.put("ammoInventory", this.ammoInventory.serializeNBT());
        }
        if (this.storageInventory != null) {
            tag.put("storageInventory", this.storageInventory.serializeNBT());
        }
        if (this.armorInventory != null) {
            tag.put("armorInventory", this.armorInventory.serializeNBT());
        }
        commonTag.put(INVENTORY_TAG, tag);
    }

    public void readFromNBT(CompoundTag commonTag) {
        if (!commonTag.contains(INVENTORY_TAG)) {
            return;
        }
        CompoundTag tag = commonTag.getCompound(INVENTORY_TAG);
        if (this.upgradeInventory != null) {
            this.upgradeInventory.deserializeNBT(tag.getCompound("upgradeInventory"));
        }
        if (this.ammoInventory != null) {
            this.ammoInventory.deserializeNBT(tag.getCompound("ammoInventory"));
        }
        if (this.storageInventory != null) {
            this.storageInventory.deserializeNBT(tag.getCompound("storageInventory"));
        }
        if (this.armorInventory != null) {
            this.armorInventory.deserializeNBT(tag.getCompound("armorInventory"));
        }
    }

    public List<IVehicleArmor> getInventoryArmor() {
        ArrayList<IVehicleArmor> armors = new ArrayList<IVehicleArmor>();
        for (int i = 0; i < this.armorInventory.getSlots(); i++) {
            ItemStack stack = this.armorInventory.getStackInSlot(i);
            ArmorRegistry.getArmorForStack(stack).ifPresent(armors::add);
        }
        return armors;
    }

    public List<IVehicleUpgradeType> getInventoryUpgrades() {
        ArrayList<IVehicleUpgradeType> upgrades = new ArrayList<>();
        for (int i = 0; i < this.upgradeInventory.getSlots(); i++) {
            ItemStack stack = this.upgradeInventory.getStackInSlot(i);
            UpgradeRegistry.getUpgrade(stack).ifPresent(upgrades::add);
        }
        return upgrades;
    }

    public List<VehicleAmmoEntry> getAmmoCounts() {
        ArrayList<VehicleAmmoEntry> counts = new ArrayList<VehicleAmmoEntry>();
        for (int i = 0; i < this.ammoInventory.getSlots(); i++) {
            ItemStack stack = this.ammoInventory.getStackInSlot(i);
            AmmoRegistry.getAmmoForStack(stack).ifPresent(ammo -> {
                boolean found = false;
                for (VehicleAmmoEntry ent : counts) {
                    if (ent.baseAmmoType == ammo) {
                        found = true;
                        ent.ammoCount += stack.getCount();
                        break;
                    }
                }
                if (!found) {
                    counts.add(new VehicleAmmoEntry(ammo));
                    counts.get(counts.size() - 1).ammoCount += stack.getCount();
                }
            });
        }
        return counts;
    }
}
