package net.shadowmage.ancientwarfare.core.render;

import codechicken.lib.model.bakedmodels.ModelProperties.PerspectiveProperties;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.vec.RedundantTransformation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.uv.IconTransformation;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.render.model.LegacyBakery;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;

import javax.annotation.Nullable;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public abstract class BaseBakery implements LegacyBakery {
    public LegacyModelState handleState(LegacyModelState state, BlockGetter access, BlockPos pos) {
        return state;
    }

    protected Map<String, CCModel> groups;
    public TextureAtlasSprite sprite;
    protected IconTransformation iconTransform;

    protected BaseBakery(String modelPath) {
        groups = new OBJParser(new ResourceLocation(AncientWarfareCore.MOD_ID, "models/block/" + modelPath))
                .quads()
                .coordSystem(getBaseTransformation())
                .parse();

        for (Map.Entry<String, CCModel> group : groups.entrySet()) {
            group.setValue(group.getValue().backfacedCopy().computeNormals());
        }
    }

    protected Transformation getBaseTransformation() {
        return RedundantTransformation.INSTANCE;
    }

    public void setSprite(TextureAtlasSprite textureAtlasSprite) {
        sprite = textureAtlasSprite;
        iconTransform = new IconTransformation(sprite);
    }

    private static final PerspectiveProperties MODEL_PROPERTIES = PerspectiveProperties.DEFAULT_BLOCK;

    @Override
    public List<BakedQuad> bakeQuads(@Nullable Direction face, LegacyModelState state) {
        if (face != null) {
            return Collections.emptyList();
        }

        List<BakedQuad> bakedQuads = new ArrayList<>();
        QuadBakingVertexConsumer buffer = new QuadBakingVertexConsumer(bakedQuads::add);
        TextureAtlasSprite quadSprite = resolveQuadSprite(getQuadSprite(state));
        buffer.setSprite(quadSprite);
        buffer.setTintIndex(-1);
        buffer.setShade(true);
        buffer.setHasAmbientOcclusion(true);
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.bind(buffer, DefaultVertexFormat.BLOCK);

        Collection<CCModel> transformedGroups = applyModelTransforms(groups.values(), face, state);

        renderBlockModels(transformedGroups, ccrs, face, state);

        return finalizeBakedQuads(bakedQuads, quadSprite);
    }

    /**
     * QuadBakingVertexConsumer does not synthesize attributes omitted by the legacy
     * CodeChickenLib pipeline. In particular, a missing color is emitted as black,
     * and the defaults also mark every quad as tint index 0, facing DOWN, with
     * diffuse shading disabled. Normalize those attributes after CCL has emitted
     * each OBJ quad.
     */
    public static List<BakedQuad> finalizeBakedQuads(List<BakedQuad> bakedQuads) {
        return finalizeBakedQuads(bakedQuads, null);
    }

    public static List<BakedQuad> finalizeBakedQuads(List<BakedQuad> bakedQuads,
                                                     @Nullable TextureAtlasSprite fallbackSprite) {
        List<BakedQuad> normalized = new ArrayList<>(bakedQuads.size());
        for (BakedQuad quad : bakedQuads) {
            normalized.add(finalizeBakedQuad(quad, fallbackSprite));
        }
        return normalized;
    }

    private static BakedQuad finalizeBakedQuad(BakedQuad quad,
                                               @Nullable TextureAtlasSprite fallbackSprite) {
        int[] vertices = quad.getVertices().clone();
        for (int vertex = 0; vertex < 4; vertex++) {
            int colorIndex = vertex * IQuadTransformer.STRIDE + IQuadTransformer.COLOR;
            int packedColor = vertices[colorIndex];
            int alpha = packedColor & 0xFF000000;
            if (alpha == 0) {
                alpha = 0xFF000000;
            }
            // Preserve intentional transparency, but never let absent CCL RGB data
            // multiply the atlas texture down to black/greyscale.
            vertices[colorIndex] = alpha | 0x00FFFFFF;
        }

        Direction direction = findQuadDirection(vertices, quad.getDirection());
        TextureAtlasSprite quadSprite = quad.getSprite() != null ? quad.getSprite() : resolveQuadSprite(fallbackSprite);
        return new BakedQuad(vertices, -1, direction, quadSprite, true, true);
    }

    private static TextureAtlasSprite resolveQuadSprite(@Nullable TextureAtlasSprite candidate) {
        if (candidate != null) {
            return candidate;
        }
        return Minecraft.getInstance().getModelManager().getMissingModel().getParticleIcon();
    }

    private static Direction findQuadDirection(int[] vertices, Direction fallback) {
        float normalX = 0.0F;
        float normalY = 0.0F;
        float normalZ = 0.0F;

        for (int vertex = 0; vertex < 4; vertex++) {
            int normalIndex = vertex * IQuadTransformer.STRIDE + IQuadTransformer.NORMAL;
            int packedNormal = vertices[normalIndex];
            normalX += (byte) (packedNormal & 0xFF);
            normalY += (byte) ((packedNormal >>> 8) & 0xFF);
            normalZ += (byte) ((packedNormal >>> 16) & 0xFF);
        }

        if (normalX * normalX + normalY * normalY + normalZ * normalZ < 1.0E-4F) {
            float x0 = Float.intBitsToFloat(vertices[IQuadTransformer.POSITION]);
            float y0 = Float.intBitsToFloat(vertices[IQuadTransformer.POSITION + 1]);
            float z0 = Float.intBitsToFloat(vertices[IQuadTransformer.POSITION + 2]);

            int second = IQuadTransformer.STRIDE + IQuadTransformer.POSITION;
            int third = IQuadTransformer.STRIDE * 2 + IQuadTransformer.POSITION;
            float ax = Float.intBitsToFloat(vertices[second]) - x0;
            float ay = Float.intBitsToFloat(vertices[second + 1]) - y0;
            float az = Float.intBitsToFloat(vertices[second + 2]) - z0;
            float bx = Float.intBitsToFloat(vertices[third]) - x0;
            float by = Float.intBitsToFloat(vertices[third + 1]) - y0;
            float bz = Float.intBitsToFloat(vertices[third + 2]) - z0;

            normalX = ay * bz - az * by;
            normalY = az * bx - ax * bz;
            normalZ = ax * by - ay * bx;
        }

        if (normalX * normalX + normalY * normalY + normalZ * normalZ < 1.0E-4F) {
            return fallback;
        }
        return Direction.getNearest(normalX, normalY, normalZ);
    }

    protected Collection<CCModel> applyModelTransforms(Collection<CCModel> modelGroups, Direction face, LegacyModelState state) {
        return modelGroups;
    }

    protected void renderBlockModels(Collection<CCModel> modelGroups, CCRenderState ccrs, Direction face, LegacyModelState state) {
        renderAllModels(modelGroups, ccrs, state);
    }

    private void renderAllModels(Collection<CCModel> modelGroups, CCRenderState ccrs, LegacyModelState state) {
        for (CCModel group : modelGroups) {
            group.render(ccrs, getIconTransform(state));
        }
    }

    private void renderAllModels(CCRenderState ccrs, ItemStack stack) {
        for (CCModel group : groups.values()) {
            group.render(ccrs, getIconTransform(stack));
        }
    }

    protected IconTransformation getIconTransform(LegacyModelState state) {
        return iconTransform;
    }

    protected IconTransformation getIconTransform(ItemStack stack) {
        return iconTransform;
    }

    /**
     * The destroy-stage renderer remaps each quad through BakedQuad#getSprite().
     * Legacy CCL only writes UV coordinates, so explicitly attach the atlas sprite
     * used by the icon transformation instead of leaving the quad sprite null.
     */
    protected TextureAtlasSprite getQuadSprite(LegacyModelState state) {
        return sprite;
    }

    protected TextureAtlasSprite getQuadSprite(ItemStack stack) {
        return sprite;
    }

    protected void renderItemModels(CCRenderState ccrs, ItemStack stack) {
        renderAllModels(ccrs, stack);
    }

    @Override
    public List<BakedQuad> bakeItemQuads(@Nullable Direction face, ItemStack stack) {
        if (face != null) {
            return Collections.emptyList();
        }
        List<BakedQuad> bakedQuads = new ArrayList<>();
        QuadBakingVertexConsumer buffer = new QuadBakingVertexConsumer(bakedQuads::add);
        TextureAtlasSprite quadSprite = resolveQuadSprite(getQuadSprite(stack));
        buffer.setSprite(quadSprite);
        buffer.setTintIndex(-1);
        buffer.setShade(true);
        buffer.setHasAmbientOcclusion(true);
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.bind(buffer, DefaultVertexFormat.BLOCK);

        renderItemModels(ccrs, stack);

        return finalizeBakedQuads(bakedQuads, quadSprite);
    }

    @Override
    public PerspectiveProperties getModelProperties(ItemStack stack) {
        return MODEL_PROPERTIES;
    }
}
