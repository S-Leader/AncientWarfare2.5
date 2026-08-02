package net.shadowmage.ancientwarfare.structure.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.item.ItemBlockBase;
import net.shadowmage.ancientwarfare.structure.util.MultiBlockHelper;

public abstract class ItemBlockCoffin extends ItemBlockBase {
    public ItemBlockCoffin(Block block) {
        super(block);
    }

    @Override
    public InteractionResult onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        return MultiBlockHelper.onMultiBlockItemUse(this, player, world, pos, hand, facing, hitX, hitY, hitZ, this::mayPlace);
    }

    protected abstract boolean mayPlace(Level world, BlockPos pos, Direction sidePlacedOn, Player placer);

    protected static boolean mayPlaceAt(Level world, BlockPos pos, Direction sidePlacedOn, boolean checkSide) {
        BlockState state = world.getBlockState(pos);
        VoxelShape collisionShape = state.getBlock().defaultBlockState().getCollisionShape(world, pos);

        if (!collisionShape.isEmpty() && !world.isUnobstructed(null, collisionShape.move(pos.getX(), pos.getY(), pos.getZ()))) {
            return false;
        } else if (LegacyMaterial.of(state) == LegacyMaterial.CIRCUITS && state.getBlock() == Blocks.ANVIL) {
            return true;
        } else {
            return state.canBeReplaced() && (!checkSide || world.getBlockState(pos).canSurvive(world, pos));
        }
    }
}
