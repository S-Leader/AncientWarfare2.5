package net.shadowmage.ancientwarfare.structure.item;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.item.ItemBase;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;

public class ItemBaseStructure extends ItemBase implements IClientRegister {
    public ItemBaseStructure(String regName) {
        super(AncientWarfareStructure.MOD_ID, regName);

        AncientWarfareStructure.proxy.addClientRegister(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        // Models are loaded normally from 1.20 blockstates/models JSON.
    }
}
