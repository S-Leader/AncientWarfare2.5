package net.shadowmage.ancientwarfare.core.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;

public class ItemBaseCore extends ItemBase implements IClientRegister {
    public ItemBaseCore(String regName) {
        this(regName, new Item.Properties());
    }

    public ItemBaseCore(String regName, Item.Properties properties) {
        super(AncientWarfareCore.MOD_ID, regName, properties);

        AncientWarfareCore.proxy.addClientRegister(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        // Models are loaded normally from 1.20 blockstates/models JSON.
    }
}
