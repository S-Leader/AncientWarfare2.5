package net.shadowmage.ancientwarfare.npc.render;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
        if (rendererAttackTime <= 0.0F && entity instanceof NpcBase npc && npc.isSwingingArms()) {
            // The attack goal state is synchronized continuously, while vanilla's
            // one-shot swing packet can arrive before a newly tracked NPC is rendered.
            // Feed a visible local cycle into the normal humanoid attack animation.
            attackTime = (Mth.sin(ageInTicks * 0.35F - Mth.HALF_PI) + 1.0F) * 0.5F;
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

        if (entitylivingbaseIn.getUseItemRemainingTicks() > 0) {
            if (entitylivingbaseIn.getUsedItemHand() == InteractionHand.MAIN_HAND && !mainHandItemStack.isEmpty() && mainHandItemStack.getUseAnimation() == UseAnim.BOW) {
                mainArmPose = ArmPose.BOW_AND_ARROW;
            }

            if (entitylivingbaseIn.getUsedItemHand() == InteractionHand.OFF_HAND && !offHandItemStack.isEmpty() && offHandItemStack.getUseAnimation() == UseAnim.BLOCK) {
                offArmPose = ArmPose.BLOCK;
            }
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
