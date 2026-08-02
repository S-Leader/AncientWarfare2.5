package net.shadowmage.ancientwarfare.vehicle.item;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.item.ItemBase;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.vehicle.AncientWarfareVehicles;

public abstract class ItemBaseVehicle extends ItemBase implements IClientRegister {
    public ItemBaseVehicle(String regName) {
        super(AncientWarfareVehicles.MOD_ID, regName);

        AncientWarfareNPC.proxy.addClientRegister(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        ModelLoaderHelper.registerItem(this, "vehicle");
    }
}
