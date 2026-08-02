package net.shadowmage.ancientwarfare.vehicle.item;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;

import javax.annotation.Nullable;
import java.util.List;

public class ItemUpgrade extends ItemBaseVehicle {
    private String tooltipName;
    private String vehicleUpgradeTooltipName;
    private String dynamicInfo;

    public ItemUpgrade(ResourceLocation registryName, String dynamicInfo) {
        super(registryName.getPath());
        this.dynamicInfo = dynamicInfo;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<String> tooltip, TooltipFlag flagIn) {
        tooltip.add(I18n.get(tooltipName));
        tooltip.add(I18n.get(vehicleUpgradeTooltipName));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        ModelLoaderHelper.registerItem(this, (i, m) -> new ModelResourceLocation(getRegistryName(), "inventory"));

        // Some upgrades include their actual effect in their tooltip.
        // Now that these effects are configurable, the tooltip needs to adjust dynamically.
        if (dynamicInfo.isEmpty()) {
            tooltipName = "item." + getRegistryName().getPath() + ".tooltip";
        } else {
            tooltipName = I18n.get("item." + getRegistryName().getPath() + ".tooltip", dynamicInfo);
        }
        vehicleUpgradeTooltipName = "item.vehicle_upgrade_tooltip";
    }
}
