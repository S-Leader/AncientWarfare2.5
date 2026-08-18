package net.shadowmage.ancientwarfare.vehicle.item;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.vehicle.missiles.IAmmo;

import javax.annotation.Nullable;
import java.util.List;

public class ItemAmmo extends ItemBaseVehicle {
    private final String tooltipName;
    private final String tooltipVehicleList;
    //1.12 called setCreativeTab(null) for ammo that never exists as an item; the tab filters on this instead.
    private final boolean visibleInCreativeTab;

    public ItemAmmo(ResourceLocation registryName, IAmmo ammo) {
        super(registryName.getPath());
        visibleInCreativeTab = ammo.isAvailableAsItem();
        tooltipName = "item." + registryName.getPath() + ".tooltip";
        tooltipVehicleList = "item." + registryName.getPath() + ".tooltipVehicleList";
    }

    public boolean isVisibleInCreativeTab() {
        return visibleInCreativeTab;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level world, List<String> tooltip, TooltipFlag flagIn) {
        tooltip.add(I18n.get(tooltipName));
        tooltip.add(I18n.get(tooltipVehicleList));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        // Models are loaded normally from 1.20 blockstates/models JSON.
    }
}
