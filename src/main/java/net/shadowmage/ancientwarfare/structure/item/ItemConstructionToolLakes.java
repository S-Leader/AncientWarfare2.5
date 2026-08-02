package net.shadowmage.ancientwarfare.structure.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.util.BlockTools;

import java.util.Set;

public class ItemConstructionToolLakes extends ItemBaseStructure {

    public ItemConstructionToolLakes(String name) {
        super(name);
        setMaxStackSize(1);
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (world.isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        BlockPos pos = BlockTools.getBlockClickedOn(player, world, true);
        if (pos == null) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }
        if (!world.isEmptyBlock(pos)) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }
        BlockState state = world.getBlockState(pos);
        FloodFillPathfinder pf = new FloodFillPathfinder(player.level(), pos, state.getBlock(), state, false, true);
        Set<BlockPos> blocks = pf.doFloodFill();
        for (BlockPos p : blocks) {
            player.level().setBlock(p, Blocks.WATER.defaultBlockState(), 3);
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

}
