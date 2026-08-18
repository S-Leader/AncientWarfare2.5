package net.shadowmage.ancientwarfare.npc.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class RangeAttackHelper {
    private RangeAttackHelper() {
    }

    /*
     * @author Funwayguy for the speedFactor (range) calculations
     */
    public static void doRangedAttack(LivingEntity attacker, LivingEntity target, float force, float inaccuracy) {
        ItemStack weapon = attacker.getMainHandItem();
        if (weapon.getItem() instanceof TridentItem) {
            doTridentAttack(attacker, target, weapon, inaccuracy);
            return;
        }
        if (weapon.getItem() instanceof CrossbowItem) {
            doCrossbowAttack(attacker, target, weapon, force);
            return;
        }
        doArrowAttack(attacker, target, force, inaccuracy);
    }

    private static void doArrowAttack(LivingEntity attacker, LivingEntity target, float force, float inaccuracy) {
        double targetDist = Math.sqrt(attacker.distanceToSqr(target.getX() + (target.getX() - target.xOld), target.getBoundingBox().minY, target.getZ() + (target.getZ() - target.zOld)));
        float speedFactor = (float) ((0.00013 * targetDist * targetDist) + (0.02 * targetDist) + 1.25);

        AbstractArrow arrow = new Arrow(attacker.level(), attacker);
        aim(arrow, attacker, target, speedFactor, inaccuracy, 0.2D, 0.0D);
        arrow.setBaseDamage(force * 2.0D + attacker.getRandom().nextGaussian() * 0.25D);

        int bonus = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, attacker.getMainHandItem());
        if (bonus > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() + bonus * 0.5D + 0.5D);
        }

        bonus = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, attacker.getMainHandItem());
        if (bonus > 0) {
            arrow.setKnockback(bonus);
        }

        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, attacker.getMainHandItem()) > 0) {
            arrow.setSecondsOnFire(100);
        }

        attacker.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (attacker.getRandom().nextFloat() * 0.4F + 0.8F));
        attacker.level().addFreshEntity(arrow);
    }

    /** Drowned-style thrown trident: the NPC keeps its equipped weapon, while the projectile carries a copy. */
    private static void doTridentAttack(LivingEntity attacker, LivingEntity target, ItemStack tridentStack, float inaccuracy) {
        ThrownTrident trident = new ThrownTrident(attacker.level(), attacker, tridentStack.copy());
        aim(trident, attacker, target, 1.6F, Math.max(1.0F, inaccuracy), 0.2D, 0.3333333333333333D);
        attacker.playSound(SoundEvents.DROWNED_SHOOT, 1.0F, 1.0F / (attacker.getRandom().nextFloat() * 0.4F + 0.8F));
        attacker.level().addFreshEntity(trident);
    }

    /** Pillager-style crossbow shot, including multishot spread and piercing. */
    private static void doCrossbowAttack(LivingEntity attacker, LivingEntity target, ItemStack crossbow, float force) {
        int shots = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, crossbow) > 0 ? 3 : 1;
        int piercing = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, crossbow);
        float inaccuracy = Math.max(1.0F, 14.0F - attacker.level().getDifficulty().getId() * 4.0F);

        for (int i = 0; i < shots; i++) {
            double spread = shots == 1 ? 0.0D : (i - 1) * 10.0D;
            Arrow arrow = new Arrow(attacker.level(), attacker);
            arrow.setBaseDamage(2.0D + Math.max(0.0F, force));
            if (piercing > 0) {
                arrow.setPierceLevel((byte) Mth.clamp(piercing, 0, 127));
            }
            aim(arrow, attacker, target, 1.6F, inaccuracy, 0.2D, 0.3333333333333333D, spread);
            attacker.level().addFreshEntity(arrow);
        }

        crossbow.getOrCreateTag().putBoolean("Charged", false);
        crossbow.getOrCreateTag().remove("ChargedProjectiles");
        attacker.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F,
                1.0F / (attacker.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    private static void aim(AbstractArrow projectile, LivingEntity attacker, LivingEntity target,
                            float velocity, float inaccuracy, double arc, double targetHeight) {
        aim(projectile, attacker, target, velocity, inaccuracy, arc, targetHeight, 0.0D);
    }

    private static void aim(AbstractArrow projectile, LivingEntity attacker, LivingEntity target,
                            float velocity, float inaccuracy, double arc, double targetHeight, double yawOffsetDegrees) {
        double dx = target.getX() - attacker.getX();
        double dy = target.getY(targetHeight) - projectile.getY();
        double dz = target.getZ() - attacker.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        if (yawOffsetDegrees != 0.0D) {
            double radians = Math.toRadians(yawOffsetDegrees);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            double rotatedX = dx * cos - dz * sin;
            double rotatedZ = dx * sin + dz * cos;
            dx = rotatedX;
            dz = rotatedZ;
        }
        projectile.shoot(dx, dy + horizontal * arc, dz, velocity, inaccuracy);
    }
}
