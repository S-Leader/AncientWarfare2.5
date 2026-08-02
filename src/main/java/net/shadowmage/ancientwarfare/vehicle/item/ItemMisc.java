package net.shadowmage.ancientwarfare.vehicle.item;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;

import javax.annotation.Nullable;
import java.util.List;

public class ItemMisc extends ItemBaseVehicle {
    private String itemTypeTooltip;

    public ItemMisc(String regName, VehicleItemType itemType) {
        super(regName);
        itemTypeTooltip = "item." + itemType.getItemTypeString() + ".tooltip";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level world, List<String> tooltip, TooltipFlag flagIn) {
        tooltip.add(I18n.get(itemTypeTooltip));
    }

    @Override
    @OnlyIn(Dist.CLIENT)

    public void registerClient() {
        ModelLoaderHelper.registerItem(this, (i, m) -> new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    public enum VehicleItemType {
        AMMO_MATERIAL("ammo_material"),
        VEHICLE_COMPONENT("vehicle_component");

        private String itemType;

        VehicleItemType(String itemType) {
            this.itemType = itemType;
        }

        public String getItemTypeString() {
            return itemType;
        }
    }
}
