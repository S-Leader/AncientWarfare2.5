package net.shadowmage.ancientwarfare.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.gui.research.GuiResearchStation;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.tile.TileResearchStation;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;

public class BlockResearchStation extends BlockBaseCore {
    public static final String HAS_BOOK_TAG = "has_book";
    public static final BooleanProperty HAS_BOOK = BooleanProperty.create(HAS_BOOK_TAG);

    public BlockResearchStation() {
        super(LegacyMaterial.WOOD, "research_station");
        setHardness(2.f);
        registerDefaultState(stateDefinition.any().setValue(HAS_BOOK, false).setValue(FACING, Direction.NORTH));
        AncientWarfareCore.proxy.addClientRegister(this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_BOOK);
    }

    public BlockState getStateForPlacement(Level worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        return defaultBlockState().setValue(FACING, placer.getDirection().getOpposite()).setValue(HAS_BOOK, false);
    }

    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(HAS_BOOK, (meta & 4) != 0).setValue(FACING, Direction.from2DDataValue(meta & 3));
    }

    public int getMetaFromState(BlockState state) {
        int i = 0;
        i = i | (state.getValue(FACING)).get2DDataValue();

        if (state.getValue(HAS_BOOK)) {
            i |= 4;
        }
        return i;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity createTileEntity(Level world, BlockState state) {
        return new TileResearchStation();
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        return WorldTools.clickInteractableTileWithHand(world, pos, player, hand);
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

    @Override
    public boolean invertFacing() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        super.registerClient();

        NetworkHandler.registerGui(NetworkHandler.GUI_RESEARCH_STATION, GuiResearchStation.class);
    }
}