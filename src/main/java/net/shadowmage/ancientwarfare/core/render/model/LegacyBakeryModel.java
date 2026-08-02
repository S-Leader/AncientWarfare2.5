package net.shadowmage.ancientwarfare.core.render.model;

import codechicken.lib.model.DummyBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * Particle-bearing placeholder replaced with LegacyBakedModel after model baking.
 */
public class LegacyBakeryModel extends DummyBakedModel {
    public TextureAtlasSprite getParticleTexture() {
        return Minecraft.getInstance().getModelManager().getMissingModel().getParticleIcon();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return getParticleTexture();
    }
}
