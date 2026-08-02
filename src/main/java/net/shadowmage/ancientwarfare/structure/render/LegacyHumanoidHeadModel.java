package net.shadowmage.ancientwarfare.structure.render;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelBase;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelRenderer;

/**
 * Replica of the removed 1.12 vanilla ModelHumanoidHead geometry built on the legacy model bridge.
 */
@OnlyIn(Dist.CLIENT)
public class LegacyHumanoidHeadModel extends LegacyModelBase {
    public final LegacyModelRenderer skeletonHead;
    private final LegacyModelRenderer head;

    public LegacyHumanoidHeadModel() {
        textureWidth = 64;
        textureHeight = 64;
        skeletonHead = new LegacyModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
        skeletonHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
        skeletonHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        head = new LegacyModelRenderer(this, 32, 0).setTextureSize(textureWidth, textureHeight);
        head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.25F);
        head.setRotationPoint(0.0F, 0.0F, 0.0F);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch, float scale) {
        skeletonHead.rotateAngleY = yaw * 0.017453292F;
        skeletonHead.rotateAngleX = pitch * 0.017453292F;
        head.rotateAngleY = skeletonHead.rotateAngleY;
        head.rotateAngleX = skeletonHead.rotateAngleX;
        skeletonHead.render(scale);
        head.render(scale);
    }
}
