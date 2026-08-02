package net.shadowmage.ancientwarfare.structure.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.render.model.LegacyBakery;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;
import net.shadowmage.ancientwarfare.core.util.LegacyBlockState;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.block.BlockSoundBlock;
import net.shadowmage.ancientwarfare.structure.tile.TileSoundBlock;

import javax.annotation.Nullable;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SoundBlockRenderer implements LegacyBakery {
    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(
            new ResourceLocation(AncientWarfareCore.MOD_ID, "structure/sound_block"), "normal");
    public static final SoundBlockRenderer INSTANCE = new SoundBlockRenderer();

    private SoundBlockRenderer() {
    }

    @Override
    public List<BakedQuad> bakeQuads(@Nullable Direction face, LegacyModelState state) {
        BlockState disguiseState = Blocks.JUKEBOX.defaultBlockState();
        String encoded = state.getValue(BlockSoundBlock.DISGUISE_BLOCK);
        if (encoded != null) {
            String[] parts = encoded.split("\\|", 2);
            if (parts.length == 2) {
                ResourceLocation id = ResourceLocation.tryParse(parts[0]);
                if (id != null && ForgeRegistries.BLOCKS.containsKey(id)) {
                    disguiseState = LegacyBlockState.fromMeta(ForgeRegistries.BLOCKS.getValue(id), parseMeta(parts[1]));
                }
            }
        }
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(disguiseState)
                .getQuads(disguiseState, face, RandomSource.create(0L));
    }

    @Override
    public LegacyModelState handleState(LegacyModelState state, BlockGetter access, BlockPos pos) {
        BlockState disguiseState = WorldTools.getTile(access, pos, TileSoundBlock.class)
                .map(TileSoundBlock::getDisguiseState)
                .orElse(Blocks.JUKEBOX.defaultBlockState());
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(disguiseState.getBlock());
        String encoded = (id == null ? "minecraft:jukebox" : id.toString()) + "|" + LegacyBlockState.toMeta(disguiseState);
        return state.setValue(BlockSoundBlock.DISGUISE_BLOCK, encoded);
    }

    @Override
    public List<BakedQuad> bakeItemQuads(@Nullable Direction face, ItemStack stack) {
        BlockState state = Blocks.JUKEBOX.defaultBlockState();
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(state)
                .getQuads(state, face, RandomSource.create(0L));
    }

    private static int parseMeta(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
