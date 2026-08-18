package net.shadowmage.ancientwarfare.npc.ai;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

import java.util.function.IntConsumer;

/**
 * Small state machine mirroring the visible combat phases used by drowned
 * trident attacks and pillager crossbow attacks.  It only owns the special
 * weapon timing; target selection/navigation remains Ancient Warfare's AI.
 */
public final class NpcRangedWeaponAttackController {
    private enum CrossbowState { UNCHARGED, CHARGING, HOLDING }

    private CrossbowState crossbowState = CrossbowState.UNCHARGED;
    private int crossbowTicks;
    private int tridentChargeTicks;

    private static final double TRIDENT_MELEE_SWITCH_DISTANCE_SQ = 25.0D;
    private static final float TRIDENT_MELEE_REACH = 2.5F + AWCoreStatics.meleeReachModifier;


    /**
     * Tridents are hybrid weapons for NPCs. Once an enemy gets within five blocks,
     * stop treating the trident as a stationary ranged weapon and use normal melee
     * closing/attack behaviour until the target moves back out of that radius.
     */
    public boolean isTridentMeleeMode(NpcBase npc, double distanceSq) {
        return npc.getMainHandItem().getItem() instanceof TridentItem
                && distanceSq <= TRIDENT_MELEE_SWITCH_DISTANCE_SQ;
    }

    public boolean shouldCloseForTridentMelee(NpcBase npc, LivingEntity target, double distanceSq) {
        if (!isTridentMeleeMode(npc, distanceSq)) {
            return false;
        }
        double attackDistance = (npc.getBbWidth() / 2.0D) + (target.getBbWidth() / 2.0D) + TRIDENT_MELEE_REACH;
        return distanceSq > attackDistance * attackDistance || !npc.getSensing().hasLineOfSight(target);
    }

    public boolean tickTridentMelee(NpcBase npc, LivingEntity target, double distanceSq,
                                    int currentAttackDelay, IntConsumer setAttackDelay) {
        if (!isTridentMeleeMode(npc, distanceSq)) {
            return false;
        }
        double attackDistance = (npc.getBbWidth() / 2.0D) + (target.getBbWidth() / 2.0D) + TRIDENT_MELEE_REACH;
        if (distanceSq > attackDistance * attackDistance) {
            // Normally the goal will keep closing until this is false.  Returning
            // false here also preserves do-not-pursue behaviour without granting
            // an out-of-reach melee hit.
            return false;
        }

        // Cancel a throw that was charging before the target entered melee range.
        reset(npc);
        if (currentAttackDelay <= 0) {
            npc.triggerAttackAnimation();
            npc.doHurtTarget(target);
            double attackSpeed = npc.getAttributeValue(Attributes.ATTACK_SPEED);
            int cooldown = attackSpeed > 0.0D ? (int) (20.0D / attackSpeed) : 20;
            setAttackDelay.accept(Math.max(1, cooldown));
            npc.addExperience(AWNPCStatics.npcXpFromAttack);
        }
        return true;
    }

    public boolean tickSpecial(NpcBase npc, RangedAttackMob rangedAttacker, LivingEntity target,
                               float power, int currentAttackDelay, int cooldown,
                               IntConsumer setAttackDelay) {
        ItemStack weapon = npc.getMainHandItem();
        if (weapon.getItem() instanceof CrossbowItem) {
            tickCrossbow(npc, rangedAttacker, target, weapon, power, currentAttackDelay, cooldown, setAttackDelay);
            return true;
        }
        if (weapon.getItem() instanceof TridentItem) {
            tickTrident(npc, rangedAttacker, target, power, currentAttackDelay, cooldown, setAttackDelay);
            return true;
        }
        resetSpecialPose(npc, false);
        return false;
    }

    private void tickCrossbow(NpcBase npc, RangedAttackMob rangedAttacker, LivingEntity target,
                              ItemStack crossbow, float power, int currentAttackDelay, int cooldown,
                              IntConsumer setAttackDelay) {
        if (crossbowState == CrossbowState.UNCHARGED) {
            if (currentAttackDelay > 0) {
                return;
            }
            setCrossbowCharged(crossbow, false);
            npc.startUsingItem(InteractionHand.MAIN_HAND);
            npc.setRangedWeaponPose(NpcBase.RANGED_POSE_CROSSBOW_CHARGE);
            int quickCharge = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, crossbow);
            crossbowTicks = Math.max(5, 25 - quickCharge * 5);
            crossbowState = CrossbowState.CHARGING;
            npc.playSound(SoundEvents.CROSSBOW_LOADING_START, 1.0F, 1.0F);
            return;
        }

        if (crossbowState == CrossbowState.CHARGING) {
            if (--crossbowTicks > 0) {
                return;
            }
            npc.stopUsingItem();
            setCrossbowCharged(crossbow, true);
            npc.setRangedWeaponPose(NpcBase.RANGED_POSE_CROSSBOW_HOLD);
            crossbowTicks = 20 + npc.getRandom().nextInt(20);
            crossbowState = CrossbowState.HOLDING;
            npc.playSound(SoundEvents.CROSSBOW_LOADING_END, 1.0F, 1.0F);
            return;
        }

        if (--crossbowTicks <= 0) {
            rangedAttacker.performRangedAttack(target, power);
            setCrossbowCharged(crossbow, false);
            npc.setRangedWeaponPose(NpcBase.RANGED_POSE_NONE);
            crossbowState = CrossbowState.UNCHARGED;
            setAttackDelay.accept(cooldown);
        }
    }

    private void tickTrident(NpcBase npc, RangedAttackMob rangedAttacker, LivingEntity target,
                             float power, int currentAttackDelay, int cooldown, IntConsumer setAttackDelay) {
        npc.setRangedWeaponPose(NpcBase.RANGED_POSE_NONE);
        if (!npc.isUsingItem()) {
            npc.startUsingItem(InteractionHand.MAIN_HAND);
            tridentChargeTicks = 10;
            return;
        }
        if (currentAttackDelay > 0) {
            return;
        }
        if (--tridentChargeTicks <= 0) {
            rangedAttacker.performRangedAttack(target, power);
            npc.stopUsingItem();
            tridentChargeTicks = 10;
            setAttackDelay.accept(cooldown);
        }
    }

    public void reset(NpcBase npc) {
        resetSpecialPose(npc, true);
        crossbowState = CrossbowState.UNCHARGED;
        crossbowTicks = 0;
        tridentChargeTicks = 0;
    }

    private void resetSpecialPose(NpcBase npc, boolean stopUsing) {
        npc.setRangedWeaponPose(NpcBase.RANGED_POSE_NONE);
        if (stopUsing && npc.isUsingItem()) {
            ItemStack used = npc.getUseItem();
            if (used.getItem() instanceof CrossbowItem || used.getItem() instanceof TridentItem) {
                npc.stopUsingItem();
            }
        }
    }

    private static void setCrossbowCharged(ItemStack stack, boolean charged) {
        stack.getOrCreateTag().putBoolean("Charged", charged);
        if (!charged) {
            stack.getOrCreateTag().remove("ChargedProjectiles");
        }
    }
}
