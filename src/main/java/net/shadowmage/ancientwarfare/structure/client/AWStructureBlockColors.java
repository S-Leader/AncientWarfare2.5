package net.shadowmage.ancientwarfare.structure.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.tile.TileColored;
import net.shadowmage.ancientwarfare.structure.tile.TileSoundBlock;

import static net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks.*;

@OnlyIn(Dist.CLIENT)
public class AWStructureBlockColors {
    private AWStructureBlockColors() {
    }

    public static void init() {
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();

        blockColors.register((state, world, pos, tintIndex) -> WorldTools.getTile(world, pos, TileColored.class).map(TileColored::getColor).orElse(-1)
                , ALTAR_CANDLE.get(), ALTAR_LONG_CLOTH.get(), ALTAR_SHORT_CLOTH.get());

        blockColors.register((state, world, pos, tintIndex) -> {
            BlockState disguiseState = WorldTools.getTile(world, pos, TileSoundBlock.class).filter(t -> t.getDisguiseState() != null)
                    .map(TileSoundBlock::getDisguiseState).orElse(Blocks.JUKEBOX.defaultBlockState());
            return Minecraft.getInstance().getBlockColors().getColor(disguiseState, world, pos, 0);
        }, SOUND_BLOCK.get());
    }
}
