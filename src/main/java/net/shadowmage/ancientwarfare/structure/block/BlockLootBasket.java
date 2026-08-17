package net.shadowmage.ancientwarfare.structure.block;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.gui.GuiLootBasket;
import net.shadowmage.ancientwarfare.structure.tile.TileLootBasket;

import javax.annotation.Nullable;
import java.util.Optional;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;

public class BlockLootBasket extends BlockBaseStructure {
    private static final AABB SINGLE_SOUTH_NORTH = new AABB(0D, 0D, 1 / 16D, 1D, 12 / 16D, 15 / 16D);
    private static final AABB SINGLE_WEST_EAST = new AABB(1 / 16D, 0D, 0D, 15 / 16D, 12 / 16D, 1D);
    private static final AABB DOUBLE_NORTH = new AABB(0D, 0D, 0D, 1D, 12 / 16D, 13 / 16D);
    private static final AABB DOUBLE_SOUTH = new AABB(0D, 0D, 3 / 16D, 1D, 12 / 16D, 1D);
    private static final AABB DOUBLE_WEST = new AABB(0D, 0D, 0D, 13 / 16D, 12 / 16D, 1D);
    private static final AABB DOUBLE_EAST = new AABB(3 / 16D, 0D, 0D, 1D, 12 / 16D, 1D);

    private static final BooleanProperty DOUBLE = BooleanProperty.create("double");
    private static final BooleanProperty VISIBLE = BooleanProperty.create("visible");

    public BlockLootBasket() {
        super(LegacyMaterial.GRASS, "loot_basket");
        setHardness(2);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(DOUBLE, false)
                .setValue(VISIBLE, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, DOUBLE, VISIBLE);
    }

    @Override
    public BlockState getActualState(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return computeRenderState(state, worldIn, pos);
    }

    private BlockState computeRenderState(BlockState state, BlockGetter world, BlockPos pos) {
        return getDoubleDirection(world, pos)
                .map(facing -> facing == Direction.NORTH || facing == Direction.WEST
                        ? state.setValue(DOUBLE, false).setValue(VISIBLE, false)
                        : state.setValue(FACING, facing.getClockWise()).setValue(DOUBLE, true).setValue(VISIBLE, true))
                .orElse(state.setValue(DOUBLE, false).setValue(VISIBLE, true));
    }

    private void refreshRenderState(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        BlockState current = level.getBlockState(pos);
        if (!current.is(this)) {
            return;
        }
        BlockState updated = computeRenderState(current, level, pos);
        if (updated != current) {
            // The 1.12 renderer consumed getActualState directly. Modern chunk
            // rendering only sees the stored BlockState, so persist these visual
            // properties without recursively notifying every neighbour again.
            level.setBlock(pos, updated, Block.UPDATE_CLIENTS);
        }
    }

    private void refreshBasketAndNeighbours(Level level, BlockPos pos) {
        refreshRenderState(level, pos);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            refreshRenderState(level, pos.relative(direction));
        }
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
    public boolean isFullCube(BlockState state) {
        return false;
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
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
        if (source.getBlockState(pos.north()).getBlock() == this) {
            return DOUBLE_NORTH;
        } else if (source.getBlockState(pos.south()).getBlock() == this) {
            return DOUBLE_SOUTH;
        } else if (source.getBlockState(pos.west()).getBlock() == this) {
            return DOUBLE_WEST;
        } else if (source.getBlockState(pos.east()).getBlock() == this) {
            return DOUBLE_EAST;
        }
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SINGLE_WEST_EAST : SINGLE_SOUTH_NORTH;
    }


    @Override
    public BlockState getStateForPlacement(Level world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        return defaultBlockState().setValue(FACING, placer.getDirection().getOpposite())
                .setValue(DOUBLE, false).setValue(VISIBLE, true);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !oldState.is(this)) {
            // Structure placement does not call setPlacedBy.  Rebuild the paired
            // basket render state one tick later, after both halves exist.
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        refreshBasketAndNeighbours(level, pos);
    }

    @Override
    public void onBlockPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
        super.onBlockPlacedBy(level, pos, state, placer, stack);
        refreshBasketAndNeighbours(level, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, fromPos, movedByPiston);
        refreshRenderState(level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        boolean removed = !state.is(newState.getBlock());
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (removed) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                refreshRenderState(level, pos.relative(direction));
            }
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        int basketsAround = 0;
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            BlockPos offsetPos = pos.relative(facing);
            if (world.getBlockState(offsetPos).getBlock() == this) {
                if (isDoubleBasket(world, offsetPos)) {
                    return false;
                }
                basketsAround++;
                if (basketsAround > 1) {
                    return false;
                }
            }
        }

        return true;
    }

    private Optional<Direction> getDoubleDirection(BlockGetter world, BlockPos pos) {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            if (world.getBlockState(pos.relative(facing)).getBlock() == this) {
                return Optional.of(facing);
            }
        }
        return Optional.empty();
    }

    private boolean isDoubleBasket(BlockGetter world, BlockPos pos) {
        return getDoubleDirection(world, pos).isPresent();
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (!world.isClientSide) {
            if (WorldTools.getTile(world, pos, TileLootBasket.class).map(te -> te.fillWithLootAndCheckIfGoodToOpen(player)).orElse(false)) {
                AWMenuTypes.open(player, NetworkHandler.GUI_LOOT_BASKET, pos);
            }
        }
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        super.registerClient();
    }
}
