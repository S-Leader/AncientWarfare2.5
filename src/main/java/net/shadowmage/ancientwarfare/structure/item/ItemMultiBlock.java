package net.shadowmage.ancientwarfare.structure.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.item.ItemBlockBase;

public class ItemMultiBlock extends ItemBlockBase {
    private final Vec3i minOffset;
    private final Vec3i maxOffset;

    public ItemMultiBlock(Block block, Vec3i minOffset, Vec3i maxOffset) {
        super(block);
        this.minOffset = minOffset;
        this.maxOffset = maxOffset;
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        if (!super.canPlace(context, state)) {
            return false;
        }
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(minOffset), pos.offset(maxOffset).above())) {
            if (p.equals(pos)) {
                continue;
            }
            BlockState posState = world.getBlockState(p);
            if (!posState.canBeReplaced()) {
                return false;
            }
        }

        return true;
    }
}
