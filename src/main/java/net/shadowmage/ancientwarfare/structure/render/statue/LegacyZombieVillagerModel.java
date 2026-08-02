package net.shadowmage.ancientwarfare.structure.render.statue;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelRenderer;

/**
 * Replica of the removed 1.12 vanilla ModelZombieVillager geometry. Boxes that used a second
 * texture offset on the same 1.12 renderer are attached as child renderers instead.
 */
@OnlyIn(Dist.CLIENT)
public class LegacyZombieVillagerModel extends LegacyBipedModel {
    public LegacyZombieVillagerModel() {
        this(0.0F, 0.0F);
    }

    public LegacyZombieVillagerModel(float modelSize, float yOffset) {
        super(modelSize, 0.0F, 64, 64);
        bipedHead = part(0, 0);
        bipedHead.setRotationPoint(0.0F, yOffset, 0.0F);
        bipedHead.addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, modelSize);
        LegacyModelRenderer nose = part(24, 0);
        nose.addBox(-1.0F, -3.0F, -6.0F, 2, 4, 2, modelSize);
        bipedHead.addChild(nose);
        bipedBody = part(16, 20);
        bipedBody.setRotationPoint(0.0F, 0.0F + yOffset, 0.0F);
        bipedBody.addBox(-4.0F, 0.0F, -3.0F, 8, 12, 6, modelSize);
        LegacyModelRenderer robe = part(0, 38);
        robe.addBox(-4.0F, 0.0F, -3.0F, 8, 18, 6, modelSize + 0.05F);
        bipedBody.addChild(robe);
        bipedRightArm = part(44, 38);
        bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F + yOffset, 0.0F);
        bipedLeftArm = part(44, 38);
        bipedLeftArm.mirror = true;
        bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F + yOffset, 0.0F);
        bipedRightLeg = part(0, 22);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F + yOffset, 0.0F);
        bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize);
        bipedLeftLeg = part(0, 22);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F + yOffset, 0.0F);
        bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize);
    }
}
