package net.shadowmage.ancientwarfare.npc.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class RangeAttackHelper {
    private RangeAttackHelper() {
    }

    /*
     * @author Funwayguy for the speedFactor (range) calculations
     */
    public static void doRangedAttack(LivingEntity attacker, LivingEntity target, float force, float inaccuracy) {
        double targetDist = Math.sqrt(attacker.distanceToSqr(target.getX() + (target.getX() - target.xOld), target.getBoundingBox().minY, target.getZ() + (target.getZ() - target.zOld)));
        float speedFactor = (float) ((0.00013 * (targetDist) * (targetDist)) + (0.02 * targetDist) + 1.25);

        AbstractArrow entityarrow = new Arrow(attacker.level(), attacker);
        double d0 = target.getX() - attacker.getX();
        double d1 = target.getBoundingBox().minY + (double) (target.getBbHeight() / 3.0F) - entityarrow.getY();
        double d2 = target.getZ() - attacker.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        entityarrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, speedFactor, inaccuracy);

        entityarrow.setBaseDamage(force * 2.0D + attacker.getRandom().nextGaussian() * 0.25D);

        int bonus = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, attacker.getMainHandItem());
        if (bonus > 0) {
            entityarrow.setBaseDamage(entityarrow.getBaseDamage() + bonus * 0.5D + 0.5D);
        }

        bonus = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, attacker.getMainHandItem());
        if (bonus > 0) {
            entityarrow.setKnockback(bonus);
        }

        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, attacker.getMainHandItem()) > 0) {
            entityarrow.setSecondsOnFire(100);
        }

        attacker.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (attacker.getRandom().nextFloat() * 0.4F + 0.8F));
        attacker.level().addFreshEntity(entityarrow);
    }
}
