package net.shadowmage.ancientwarfare.structure.render;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelBase;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelRenderer;

/**
 * Replica of the removed 1.12 vanilla ModelBanner geometry built on the legacy model bridge.
 */
@OnlyIn(Dist.CLIENT)
public class LegacyBannerModel extends LegacyModelBase {
    public final LegacyModelRenderer bannerSlate;
    public final LegacyModelRenderer bannerStand;
    public final LegacyModelRenderer bannerTop;

    public LegacyBannerModel() {
        textureWidth = 64;
        textureHeight = 64;
        bannerSlate = new LegacyModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
        bannerSlate.addBox(-10.0F, 0.0F, -2.0F, 20, 40, 1, 0.0F);
        bannerStand = new LegacyModelRenderer(this, 44, 0).setTextureSize(textureWidth, textureHeight);
        bannerStand.addBox(-1.0F, -30.0F, -1.0F, 2, 42, 2, 0.0F);
        bannerTop = new LegacyModelRenderer(this, 0, 42).setTextureSize(textureWidth, textureHeight);
        bannerTop.addBox(-10.0F, -32.0F, -1.0F, 20, 2, 2, 0.0F);
    }

    /**
     * Renders the banner model.
     */
    public void renderBanner() {
        bannerSlate.rotationPointY = -32.0F;
        bannerSlate.render(0.0625F);
        bannerStand.render(0.0625F);
        bannerTop.render(0.0625F);
    }
}
