package net.shadowmage.ancientwarfare.npc.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;

public class ItemClub extends ItemExtendedReachWeapon {
    public ItemClub(Tier material, String registryName, double attackOffset, double attackSpeed, float reach) {
        super(material, registryName, attackOffset, attackSpeed, reach);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        int knockback = 1;
        Vec3 movement = target.getDeltaMovement();
        double horizontal = Math.max(0.001D, Math.sqrt(movement.x * movement.x + movement.z * movement.z));
        target.push(movement.x * knockback * 0.6D / horizontal, 0.1D, movement.z * knockback * 0.6D / horizontal);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return !EnchantmentHelper.getEnchantments(book).containsKey(Enchantments.KNOCKBACK);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.world.item.enchantment.Enchantment enchantment) {
        return enchantment != Enchantments.KNOCKBACK;
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return true;
    }

}
