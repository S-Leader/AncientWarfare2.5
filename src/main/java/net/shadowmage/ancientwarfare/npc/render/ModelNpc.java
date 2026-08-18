package net.shadowmage.ancientwarfare.npc.render;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.UseAnim;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

public class ModelNpc<T extends LivingEntity> extends PlayerModel<T> {
    public ModelNpc(ModelPart root, boolean useSmallArms) {
        super(root, useSmallArms);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        float rendererAttackTime = attackTime;
        if (entity instanceof NpcBase npc) {
            int remainingAttackTicks = npc.getAttackAnimationTicks();
            if (remainingAttackTicks > 0) {
                // Entity data is the authoritative clock for combat.  ageInTicks
                // contains the render partial tick, so the discrete server counter
                // still produces a smooth single HumanoidModel attack curve.
                float partialTick = Mth.clamp(ageInTicks - entity.tickCount, 0.0F, 1.0F);
                attackTime = Mth.clamp(
                        (NpcBase.ATTACK_ANIMATION_DURATION - remainingAttackTicks + partialTick)
                                / (float) NpcBase.ATTACK_ANIMATION_DURATION,
                        0.0F,
                        1.0F
                );
            } else if (rendererAttackTime <= 0.0F && npc.isSwingingArms()) {
                // Work actions intentionally use a continuous arm cycle.  Combat
                // never enables this flag, so it cannot add a second attack swing.
                attackTime = (Mth.sin(ageInTicks * 0.35F - Mth.HALF_PI) + 1.0F) * 0.5F;
            }
        }
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        attackTime = rendererAttackTime;
    }

    @Override
    public void prepareMobModel(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTickTime) {
        ArmPose mainArmPose = ArmPose.EMPTY;
        ArmPose offArmPose = ArmPose.EMPTY;
        ItemStack mainHandItemStack = entitylivingbaseIn.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHandItemStack = entitylivingbaseIn.getItemInHand(InteractionHand.OFF_HAND);

        if (entitylivingbaseIn instanceof NpcBase npc) {
            if (mainHandItemStack.getItem() instanceof CrossbowItem) {
                if (npc.getRangedWeaponPose() == NpcBase.RANGED_POSE_CROSSBOW_CHARGE) {
                    mainArmPose = ArmPose.CROSSBOW_CHARGE;
                } else if (npc.getRangedWeaponPose() == NpcBase.RANGED_POSE_CROSSBOW_HOLD) {
                    mainArmPose = ArmPose.CROSSBOW_HOLD;
                }
            }
            if (entitylivingbaseIn.isUsingItem()
                    && entitylivingbaseIn.getUsedItemHand() == InteractionHand.MAIN_HAND) {
                if (!mainHandItemStack.isEmpty() && mainHandItemStack.getUseAnimation() == UseAnim.BOW) {
                    mainArmPose = ArmPose.BOW_AND_ARROW;
                } else if (mainHandItemStack.getItem() instanceof TridentItem
                        || mainHandItemStack.getUseAnimation() == UseAnim.SPEAR) {
                    mainArmPose = ArmPose.THROW_SPEAR;
                } else if (mainHandItemStack.getUseAnimation() == UseAnim.BLOCK) {
                    mainArmPose = ArmPose.BLOCK;
                }
            }
        }

        if (entitylivingbaseIn.isUsingItem()
                && entitylivingbaseIn.getUsedItemHand() == InteractionHand.OFF_HAND
                && !offHandItemStack.isEmpty()
                && offHandItemStack.getUseAnimation() == UseAnim.BLOCK) {
            offArmPose = ArmPose.BLOCK;
        }

        if (entitylivingbaseIn.getMainArm() == HumanoidArm.RIGHT) {
            rightArmPose = mainArmPose;
            leftArmPose = offArmPose;
        } else {
            leftArmPose = mainArmPose;
            rightArmPose = offArmPose;
        }

        super.prepareMobModel(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTickTime);
    }
}
