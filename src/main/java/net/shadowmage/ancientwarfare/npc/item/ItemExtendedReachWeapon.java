package net.shadowmage.ancientwarfare.npc.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;

public class ItemExtendedReachWeapon extends SwordItem implements IClientRegister, IExtendedReachWeapon {
    private final float reach;

    public ItemExtendedReachWeapon(Tier material, String registryName, double attackOffset, double attackSpeed, float reach) {
        super(material, (int) Math.round(attackOffset), (float) attackSpeed, new Item.Properties());
        this.reach = reach;
        AncientWarfareNPC.proxy.addClientRegister(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        ModelLoaderHelper.registerItem(this, "npc");
    }

    @Override
    public float getReach() {
        return reach;
    }
}
