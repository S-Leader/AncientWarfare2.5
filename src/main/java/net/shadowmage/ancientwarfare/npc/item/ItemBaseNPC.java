package net.shadowmage.ancientwarfare.npc.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.item.ItemBase;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;

public abstract class ItemBaseNPC extends ItemBase implements IClientRegister {
    public ItemBaseNPC(String regName) {
        this(regName, new Item.Properties());
    }

    public ItemBaseNPC(String regName, Item.Properties properties) {
        super(AncientWarfareNPC.MOD_ID, regName, properties);

        AncientWarfareNPC.proxy.addClientRegister(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        // Models are loaded normally from 1.20 blockstates/models JSON.
    }
}
