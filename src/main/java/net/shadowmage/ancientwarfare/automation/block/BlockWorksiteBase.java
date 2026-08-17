package net.shadowmage.ancientwarfare.automation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.shadowmage.ancientwarfare.automation.tile.worksite.TileWorksiteBase;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.IRotatableBlock;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.IRotatableTile;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.RotationType;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.interfaces.IInteractableTile;
import net.shadowmage.ancientwarfare.core.interfaces.IWorkSite;
import net.shadowmage.ancientwarfare.core.owner.IOwnable;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

import java.util.Optional;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;

public class BlockWorksiteBase extends BlockBaseAutomation implements IRotatableBlock {
    private static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public BlockWorksiteBase(String regName) {
        super(LegacyMaterial.WOOD, regName);
        setHardness(2.f);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return 0;
    }

    @Override
    public BlockState getActualState(BlockState state, BlockGetter world, BlockPos pos) {
        return WorldTools.getTile(world, pos, IRotatableTile.class).map(t -> state.setValue(FACING, t.getPrimaryFacing())
                .setValue(ACTIVE, t instanceof TileWorksiteBase && ((TileWorksiteBase) t).isActive())).orElse(state);
    }

    /*
     * made into a generic method so that farm blocks are easier to setup
     * returned tiles must implement IWorksite (for team reference) and IInteractableTile (for interaction callback) if they wish to receive onBlockActivated calls<br>
     * returned tiles must implement IBoundedTile if they want workbounds set from ItemBlockWorksite<br>
     * returned tiles must implement IOwnable if they want owner-name set from ItemBlockWorksite<br>
     */

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY,
                                    float hitZ) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof IInteractableTile) {
            boolean canClick = false;
            if (te instanceof IOwnable && ((IOwnable) te).isOwner(player))
                canClick = true;
            else if (te instanceof IWorkSite && ((IWorkSite) te).getOwner().isOwnerOrSameTeamOrFriend(player)) {
                canClick = true;
            }
            if (canClick) {
                return ((IInteractableTile) te).onBlockClicked(player, hand);
            }
        }
        return false;
    }

    @Override
    public final RotationType getRotationType() {
        return RotationType.FOUR_WAY;
    }

    @Override
    public boolean invertFacing() {
        return true;
    }

    public final boolean rotateBlock(Level world, BlockPos pos, Direction facing) {
        if (facing == Direction.DOWN || facing == Direction.UP) {
            Optional<IRotatableTile> te = WorldTools.getTile(world, pos, IRotatableTile.class);
            if (te.isPresent()) {
                if (!world.isClientSide) {
                    rotateTile(facing, te.get());
                }
                return true;
            }
        }
        return false;
    }

    private void rotateTile(Direction facing, IRotatableTile te) {
        Direction o = te.getPrimaryFacing().getClockWise(facing.getAxis());
        te.setPrimaryFacing(o);//twb will send update packets / etc
    }

    public final Direction[] getValidRotations(Level world, BlockPos pos) {
        return new Direction[]{Direction.DOWN, Direction.UP};
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 5;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 20;
    }
}
