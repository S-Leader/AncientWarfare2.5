package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;

public class TileDecorativeFlag extends TileFlag {
    public TileDecorativeFlag(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack stack = new ItemStack(AWStructureBlocks.DECORATIVE_FLAG.get());
        CompoundTag tag = new CompoundTag();
        writeNBT(tag);
        stack.setTag(tag);
        return stack;
    }
}
