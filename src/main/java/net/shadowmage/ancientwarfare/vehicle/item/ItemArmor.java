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
import net.shadowmage.ancientwarfare.vehicle.armors.IVehicleArmor;

import javax.annotation.Nullable;
import java.util.List;

public class ItemArmor extends ItemBaseVehicle {
    private String defenseTooltip;
    private String fireTooltip;
    private String explosiveTooltip;
    private IVehicleArmor armor;

    public ItemArmor(ResourceLocation registryName, IVehicleArmor armor) {
        super(registryName.getPath());
        this.armor = armor;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level world, List<String> tooltip, TooltipFlag flagIn) {
        tooltip.add(defenseTooltip);
        tooltip.add(fireTooltip);
        tooltip.add(explosiveTooltip);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        ModelLoaderHelper.registerItem(this, (i, m) -> new ModelResourceLocation(getRegistryName(), "inventory"));

        defenseTooltip = I18n.get("item.armor_defense.tooltip", armor.getGeneralDamageReduction());
        fireTooltip = I18n.get("item.armor_fire.tooltip", armor.getFireDamageReduction());
        explosiveTooltip = I18n.get("item.armor_explosive.tooltip", armor.getExplosiveDamageReduction());
    }
}
