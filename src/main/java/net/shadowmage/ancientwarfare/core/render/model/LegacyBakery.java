package net.shadowmage.ancientwarfare.core.render.model;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.client.ChunkRenderTypeSet;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Common 1.20 rendering contract replacing removed CodeChicken bakery interfaces.
 */
public interface LegacyBakery {
    default LegacyModelState handleState(LegacyModelState state, BlockGetter level, BlockPos pos) {
        return state;
    }

    default List<BakedQuad> bakeQuads(@Nullable Direction face, LegacyModelState state) {
        return Collections.emptyList();
    }

    default List<BakedQuad> bakeLayerFace(@Nullable Direction face, RenderType layer, LegacyModelState state) {
        return layer == RenderType.solid() ? bakeQuads(face, state) : Collections.emptyList();
    }

    default ChunkRenderTypeSet getRenderTypes(LegacyModelState state) {
        return ChunkRenderTypeSet.of(RenderType.solid());
    }

    default List<BakedQuad> bakeItemQuads(@Nullable Direction face, ItemStack stack) {
        return Collections.emptyList();
    }

    default Object getModelProperties(ItemStack stack) {
        return null;
    }
}
