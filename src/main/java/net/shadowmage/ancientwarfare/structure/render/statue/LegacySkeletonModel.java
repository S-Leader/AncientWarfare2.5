package net.shadowmage.ancientwarfare.structure.render.statue;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Replica of the removed 1.12 vanilla ModelSkeleton geometry.
 */
@OnlyIn(Dist.CLIENT)
public class LegacySkeletonModel extends LegacyBipedModel {
    public LegacySkeletonModel() {
        this(0.0F);
    }

    public LegacySkeletonModel(float modelSize) {
        super(modelSize, 0.0F, 64, 32);
        bipedRightArm = part(40, 16);
        bipedRightArm.addBox(-1.0F, -2.0F, -1.0F, 2, 12, 2, modelSize);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedLeftArm = part(40, 16);
        bipedLeftArm.mirror = true;
        bipedLeftArm.addBox(-1.0F, -2.0F, -1.0F, 2, 12, 2, modelSize);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedRightLeg = part(0, 16);
        bipedRightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, modelSize);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedLeftLeg = part(0, 16);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, modelSize);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
    }
}
