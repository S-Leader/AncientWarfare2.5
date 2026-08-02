package net.shadowmage.ancientwarfare.npc.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;

public class ItemIceSpear extends ItemExtendedReachWeapon {
    public ItemIceSpear(Tier material, String registryName, double attackOffset, double attackSpeed, float reach) {
        super(material, registryName, attackOffset, attackSpeed, reach);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50));
        target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 150));
        return true;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }
}
