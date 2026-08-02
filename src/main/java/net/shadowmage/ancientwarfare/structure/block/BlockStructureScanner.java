package net.shadowmage.ancientwarfare.structure.block;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.tile.TileStructureScanner;

import javax.annotation.Nullable;
import java.util.Optional;

public class BlockStructureScanner extends BlockBaseStructure {
    public static final DirectionProperty FACING = DirectionalBlock.FACING;

    public BlockStructureScanner() {
        super(LegacyMaterial.WOOD, "structure_scanner_block");
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (!world.isClientSide) {
            AWMenuTypes.open(player, NetworkHandler.GUI_SCANNER, pos);
        }
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getActualState(BlockState state, BlockGetter worldIn, BlockPos pos) {
        Optional<TileStructureScanner> tile = WorldTools.getTile(worldIn, pos, TileStructureScanner.class);
        return tile.map(tileStructureScanner -> state.setValue(FACING, tileStructureScanner.getRenderFacing())).orElse(state);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return 0;
    }

    @Override
    public void onBlockPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        Direction facing = placer.getDirection().getOpposite();
        worldIn.setBlock(pos, state.setValue(FACING, facing), 3);
        WorldTools.getTile(worldIn, pos, TileStructureScanner.class).ifPresent(t -> t.setFacing(facing));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity createTileEntity(Level world, BlockState state) {
        return new TileStructureScanner();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        super.registerClient();

    }
}
