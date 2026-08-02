package net.shadowmage.ancientwarfare.structure.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.util.NBTBuilder;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.tile.TileFlag;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Set;

public class BlockFlag extends BlockBaseStructure {
    private static final AABB STANDING_AABB = new AABB(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D);
    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 15);

    private Set<BlockFlag.FlagDefinition> flagDefinitions = new LinkedHashSet<>();

    BlockFlag(LegacyMaterial material, String regname) {
        super(material, regname);
    }

    @Override
    public SoundType getSoundType(BlockState state) {
        return SoundType.WOOD;
    }

    @Nullable
    @Override
    public AABB getCollisionBoundingBox(BlockState blockState, BlockGetter worldIn, BlockPos pos) {
        return null;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isPassable(BlockGetter worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean canSpawnInBlock() {
        return true;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
        return STANDING_AABB;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(ROTATION, rot.rotate(state.getValue(ROTATION), 16));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.setValue(ROTATION, mirrorIn.mirror(state.getValue(ROTATION), 16));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(ROTATION, meta);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(ROTATION);
    }

    public void getSubBlocks(CreativeModeTab itemIn, NonNullList<ItemStack> items) {
        for (BlockFlag.FlagDefinition flagDefinition : flagDefinitions) {
            ItemStack stack = new ItemStack(this);
            stack.setTag(new NBTBuilder()
                    .setString("name", flagDefinition.getName())
                    .build());
            items.add(stack);
        }
    }

    @Override
    public void harvestBlock(Level worldIn, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        if (te instanceof TileFlag) {
            popResource(worldIn, pos, ((TileFlag) te).getItemStack());
        } else {
            super.harvestBlock(worldIn, player, pos, state, te, stack);
        }
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, Level world, BlockPos pos, Player player) {
        return WorldTools.getTile(world, pos, TileFlag.class).map(TileFlag::getItemStack).orElse(new ItemStack(this));
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, BlockGetter world, BlockPos pos, BlockState state, int fortune) {
        drops.add(WorldTools.getTile(world, pos, TileFlag.class).map(TileFlag::getItemStack).orElse(new ItemStack(this)));
    }

    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        WorldTools.getTile(world, pos, TileFlag.class).ifPresent(te -> te.setFromStack(stack));
    }

    @Override
    public BlockState getStateForPlacement(Level worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        int rotation = Mth.floor((double) ((placer.getYRot() + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
        return defaultBlockState().setValue(ROTATION, rotation);
    }

    public void addFlagDefinition(BlockFlag.FlagDefinition flagDefinition) {
        flagDefinitions.add(flagDefinition);
    }

    public static class FlagDefinition {

        private String name;

        public FlagDefinition(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}
