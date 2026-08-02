package net.shadowmage.ancientwarfare.npc.item;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;

public class ItemFoodBundle extends Item implements IClientRegister {
    public ItemFoodBundle() {
        super(new Item.Properties().food(new FoodProperties.Builder().nutrition(15).saturationMod(1.0F).alwaysEat().build()));
        AncientWarfareNPC.proxy.addClientRegister(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        ModelLoaderHelper.registerItem(this, "npc");
    }
}
