package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;

public class TileDecorativeFlag extends TileFlag {
    @Override
    public ItemStack getItemStack() {
        ItemStack stack = new ItemStack(AWStructureBlocks.DECORATIVE_FLAG);
        CompoundTag tag = new CompoundTag();
        writeNBT(tag);
        stack.setTag(tag);
        return stack;
    }
}
