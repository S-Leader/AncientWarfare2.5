package net.shadowmage.ancientwarfare.core.compat;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Replacement for the block-material classifier removed from modern Minecraft.
 */
public enum LegacyMaterial {
    AIR(false, false, SoundType.STONE), CACTUS(false, true, SoundType.WOOL),
    CIRCUITS(false, false, SoundType.METAL), CLAY(false, true, SoundType.GRAVEL),
    FIRE(false, false, SoundType.WOOL), GOURD(false, true, SoundType.WOOD),
    GRASS(false, true, SoundType.GRASS), GROUND(false, true, SoundType.GRAVEL),
    ICE(false, true, SoundType.GLASS), IRON(false, true, SoundType.METAL),
    LAVA(true, false, SoundType.STONE), LEAVES(false, false, SoundType.GRASS),
    PACKED_ICE(false, true, SoundType.GLASS), PLANTS(false, false, SoundType.GRASS),
    ROCK(false, true, SoundType.STONE), SAND(false, true, SoundType.SAND),
    SNOW(false, false, SoundType.SNOW), VINE(false, false, SoundType.VINE),
    WATER(true, false, SoundType.STONE), WOOD(false, true, SoundType.WOOD);

    private final boolean liquid;
    private final boolean blocksMovement;
    private final SoundType sound;

    LegacyMaterial(boolean liquid, boolean blocksMovement, SoundType sound) {
        this.liquid = liquid;
        this.blocksMovement = blocksMovement;
        this.sound = sound;
    }

    public boolean isLiquid() {
        return liquid;
    }

    public boolean blocksMovement() {
        return blocksMovement;
    }

    public BlockBehaviour.Properties properties() {
        BlockBehaviour.Properties result = BlockBehaviour.Properties.of().sound(sound).strength(2.0F);
        return blocksMovement ? result : result.noCollission();
    }

    public static LegacyMaterial of(BlockState state) {
        if (state.isAir()) return AIR;
        if (state.getFluidState().is(FluidTags.WATER)) return WATER;
        if (state.getFluidState().is(FluidTags.LAVA)) return LAVA;
        if (state.is(BlockTags.LOGS)) return WOOD;
        if (state.is(BlockTags.LEAVES)) return LEAVES;
        if (state.is(BlockTags.SAND)) return SAND;
        if (state.is(BlockTags.SNOW)) return SNOW;
        if (state.is(BlockTags.FLOWERS) || state.is(BlockTags.SAPLINGS)) return PLANTS;
        if (state.is(Blocks.VINE)) return VINE;
        if (state.is(Blocks.CACTUS)) return CACTUS;
        if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) return FIRE;
        if (state.is(Blocks.PUMPKIN) || state.is(Blocks.MELON)) return GOURD;
        if (state.is(Blocks.ICE)) return ICE;
        if (state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE)) return PACKED_ICE;
        if (state.is(Blocks.CLAY)) return CLAY;
        if (state.is(BlockTags.ANVIL)) return IRON;
        if (state.is(BlockTags.DIRT)) return GROUND;
        return state.canOcclude() ? ROCK : PLANTS;
    }
}
