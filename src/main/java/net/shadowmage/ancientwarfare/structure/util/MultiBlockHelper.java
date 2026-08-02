package net.shadowmage.ancientwarfare.structure.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class MultiBlockHelper {
    private MultiBlockHelper() {
    }

    public static InteractionResult onMultiBlockItemUse(BlockItem itemBlock, Player player, Level world, BlockPos clickedPos,
                                                        InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ, IPlaceChecker placeChecker) {
        BlockPos placePos = world.getBlockState(clickedPos).canBeReplaced() ? clickedPos : clickedPos.relative(facing);
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || !player.getAbilities().mayBuild || !placeChecker.mayPlace(world, placePos, facing, player)) {
            return InteractionResult.FAIL;
        }

        Vec3 hitLocation = new Vec3(placePos.getX() + hitX, placePos.getY() + hitY, placePos.getZ() + hitZ);
        BlockHitResult hitResult = new BlockHitResult(hitLocation, facing, placePos, false);
        BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(player, hand, hitResult));
        return itemBlock.place(context);
    }

    public interface IPlaceChecker {
        boolean mayPlace(Level world, BlockPos pos, Direction sidePlacedOn, Player placer);
    }
}
