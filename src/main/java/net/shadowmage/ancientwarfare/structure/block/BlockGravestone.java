package net.shadowmage.ancientwarfare.structure.block;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelLoader;
import net.shadowmage.ancientwarfare.core.render.model.LegacyStateMapperBase;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.item.ItemBlockGravestone;
import net.shadowmage.ancientwarfare.structure.tile.TileGravestone;

import javax.annotation.Nullable;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;

public class BlockGravestone extends BlockBaseStructure {
    private static final AABB AABB_NORTH = new AABB(0, 0, 6 / 16D, 1, 19 / 16D, 10 / 16D);
    private static final AABB AABB_SOUTH = new AABB(0, 0, 6 / 16D, 1, 19 / 16D, 10 / 16D);
    private static final AABB AABB_WEST = new AABB(6 / 16D, 0, 0, 10 / 16D, 19 / 16D, 1D);
    private static final AABB AABB_EAST = new AABB(6 / 16D, 0, 0, 10 / 16D, 19 / 16D, 1D);

    private static final IntegerProperty VARIANT = IntegerProperty.create("variant", 1, 8);

    public BlockGravestone() {
        super(LegacyMaterial.ROCK, "gravestone");
        setHardness(4F);
    }

    @Override
    public SoundType getSoundType(BlockState state) {
        return SoundType.STONE;
    }

    @Override
    public BlockState getActualState(BlockState state, BlockGetter world, BlockPos pos) {
        return state.setValue(VARIANT, WorldTools.getTile(world, pos, TileGravestone.class).map(TileGravestone::getVariant).orElse(1));
    }

    public void getSubBlocks(CreativeModeTab itemIn, NonNullList<ItemStack> items) {
        for (int variant = 1; variant <= 8; variant++) {
            items.add(ItemBlockGravestone.getVariantStack(variant));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT, FACING);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(FACING, Direction.from2DDataValue(meta));
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(FACING).get2DDataValue();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }


    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!stack.hasTag()) {
            return;
        }

        world.setBlock(pos, state.setValue(VARIANT, ItemBlockGravestone.getVariant(stack)), 3);
        WorldTools.getTile(world, pos, TileGravestone.class).ifPresent(te -> te.setPrimaryFacing(placer.getDirection().getOpposite()));
        WorldTools.getTile(world, pos, TileGravestone.class).ifPresent(te -> te.setVariant(ItemBlockGravestone.getVariant(stack)));
        super.onBlockPlacedBy(world, pos, state, placer, stack);
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(world, pos, state, player);
        if (player.getAbilities().instabuild) {
            return;
        }
        WorldTools.getTile(world, pos, TileGravestone.class)
                .ifPresent(te -> InventoryTools.dropItemInWorld(world, ItemBlockGravestone.getVariantStack(te.getVariant()), pos));
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, BlockGetter world, BlockPos pos, BlockState state, int fortune) {
        //drops handled in onBlockHarvested
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        WorldTools.getTile(world, pos, TileGravestone.class).ifPresent(te -> te.activate(player)); // only runestones can be activated
        return true;
    }

    @Override
    public BlockState getStateForPlacement(Level worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        return defaultBlockState().setValue(FACING, placer.getDirection().getOpposite()).setValue(VARIANT, ItemBlockGravestone.getVariant(placer.getUseItem()));
    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case NORTH:
                return AABB_NORTH;
            case SOUTH:
                return AABB_SOUTH;
            case WEST:
                return AABB_WEST;
            case EAST:
                return AABB_EAST;
        }
        return new AABB(0, 0, 6 / 16D, 1, 19 / 16D, 10 / 16D);
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        //noinspection ConstantConditions
        ResourceLocation baseLocation = new ResourceLocation(AncientWarfareCore.MOD_ID, "structure/" + getRegistryName().getPath());

        LegacyModelLoader.setCustomStateMapper(this, new LegacyStateMapperBase() {
            @Override
            @OnlyIn(Dist.CLIENT)
            protected ModelResourceLocation getModelResourceLocation(BlockState state) {
                return new ModelResourceLocation(baseLocation, getPropertyString(state.getValues()));
            }
        });

        String modelPropString = "facing=west,variant=%d";

        LegacyModelLoader.setCustomMeshDefinition(this.asItem(), stack -> {
            if (!stack.hasTag()) {
                return new ModelResourceLocation(baseLocation, String.format(modelPropString, 8));
            }

            return new ModelResourceLocation(baseLocation, String.format(modelPropString, ItemBlockGravestone.getVariant(stack)));
        });

        for (int variant = 1; variant < 9; variant++) {
            LegacyModelLoader.registerItemVariants(this.asItem(),
                    new ModelResourceLocation(baseLocation, String.format(modelPropString, variant)));
        }

    }

}
