package net.shadowmage.ancientwarfare.structure.block;


import codechicken.lib.texture.TextureUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.render.BlockStateKeyGenerator;
import net.shadowmage.ancientwarfare.core.render.model.*;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.gui.GuiSoundBlock;
import net.shadowmage.ancientwarfare.structure.render.SoundBlockRenderer;
import net.shadowmage.ancientwarfare.structure.tile.TileSoundBlock;

public class BlockSoundBlock extends BlockBaseStructure implements LegacyBakeryProvider {

    public static final LegacyModelProperty<String> DISGUISE_BLOCK = LegacyModelProperty.create("disguise");

    public BlockSoundBlock() {
        super(LegacyMaterial.ROCK, "sound_block");
    }

    @Override
    public RenderType getBlockLayer() {
        return RenderType.cutout();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyModelState getLegacyModelState(BlockState state, BlockGetter world, BlockPos pos) {
        return SoundBlockRenderer.INSTANCE.handleState(LegacyModelState.of(state), world, pos);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity createTileEntity(Level world, BlockState state) {
        return new TileSoundBlock();
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem) {
            WorldTools.getTile(world, pos, TileSoundBlock.class).ifPresent(s -> s.setDisguiseState(itemStack));
        }
        if (!world.isClientSide) {
            AWMenuTypes.open(player, NetworkHandler.GUI_SOUND_BLOCK, pos);
        }
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        LegacyModelLoader.setCustomStateMapper(this, new LegacyStateMapperBase() {
            @Override
            @OnlyIn(Dist.CLIENT)
            protected ModelResourceLocation getModelResourceLocation(BlockState state) {
                return SoundBlockRenderer.MODEL_LOCATION;
            }
        });
        LegacyModelRegistryHelper.register(SoundBlockRenderer.MODEL_LOCATION, new LegacyBakeryModel() {
            @Override
            public TextureAtlasSprite getParticleTexture() {
                return TextureUtils.getTexture("minecraft:blocks/jukebox_side");
            }
        });

        ModelLoaderHelper.registerItem(this, SoundBlockRenderer.MODEL_LOCATION);

        LegacyModelBakery.registerBlockKeyGenerator(this, new BlockStateKeyGenerator.Builder().addKeyProperties(DISGUISE_BLOCK).build());

        NetworkHandler.registerGui(NetworkHandler.GUI_SOUND_BLOCK, GuiSoundBlock.class);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyBakery getBakery() {
        return SoundBlockRenderer.INSTANCE;
    }
}
