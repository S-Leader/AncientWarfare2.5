package net.shadowmage.ancientwarfare.structure.render;

import codechicken.lib.model.DummyBakedModel;
import codechicken.lib.render.particle.IModelParticleProvider;
import codechicken.lib.texture.TextureUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

public class ParticleOnlyModel extends DummyBakedModel implements IModelParticleProvider, ResourceManagerReloadListener {
    public static final ParticleOnlyModel INSTANCE = new ParticleOnlyModel("oak_planks");
    private Set<TextureAtlasSprite> sprite;
    private String blockTexture;

    public ParticleOnlyModel(String blockTexture) {
        this.blockTexture = blockTexture;
    }

    private Set<TextureAtlasSprite> getSprite() {
        if (sprite == null) {
            sprite = Collections.singleton(TextureUtils.getBlockTexture(blockTexture));
        }
        return sprite;
    }

    @Override
    public Set<TextureAtlasSprite> getHitEffects(@Nonnull BlockHitResult traceResult, BlockState state, BlockAndTintGetter world, BlockPos pos, ModelData modelData) {
        return getSprite();
    }

    @Override
    public Set<TextureAtlasSprite> getDestroyEffects(BlockState state, BlockAndTintGetter world, BlockPos pos, ModelData modelData) {
        return getSprite();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return getSprite().iterator().next();
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        //textures are rebuilt on every full reload, matching the 1.12 TEXTURES predicate check
        sprite = null;
    }
}
