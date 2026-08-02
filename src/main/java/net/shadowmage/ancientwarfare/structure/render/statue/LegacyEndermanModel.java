package net.shadowmage.ancientwarfare.structure.render.statue;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Replica of the removed 1.12 vanilla ModelEnderman geometry.
 */
@OnlyIn(Dist.CLIENT)
public class LegacyEndermanModel extends LegacyBipedModel {
    public LegacyEndermanModel(float scale) {
        super(0.0F, -14.0F, 64, 32);
        bipedHeadwear = part(0, 16);
        bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, scale - 0.5F);
        bipedHeadwear.setRotationPoint(0.0F, -14.0F, 0.0F);
        bipedBody = part(32, 16);
        bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, scale);
        bipedBody.setRotationPoint(0.0F, -14.0F, 0.0F);
        bipedRightArm = part(56, 0);
        bipedRightArm.addBox(-1.0F, -2.0F, -1.0F, 2, 30, 2, scale);
        bipedRightArm.setRotationPoint(-3.0F, -12.0F, 0.0F);
        bipedLeftArm = part(56, 0);
        bipedLeftArm.mirror = true;
        bipedLeftArm.addBox(-1.0F, -2.0F, -1.0F, 2, 30, 2, scale);
        bipedLeftArm.setRotationPoint(5.0F, -12.0F, 0.0F);
        bipedRightLeg = part(56, 0);
        bipedRightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 30, 2, scale);
        bipedRightLeg.setRotationPoint(-2.0F, -2.0F, 0.0F);
        bipedLeftLeg = part(56, 0);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 30, 2, scale);
        bipedLeftLeg.setRotationPoint(2.0F, -2.0F, 0.0F);
    }
}
