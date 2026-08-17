package net.shadowmage.ancientwarfare.structure.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.shadowmage.ancientwarfare.core.util.NBTBuilder;
import net.shadowmage.ancientwarfare.structure.block.BlockCoffin;
import net.shadowmage.ancientwarfare.structure.block.BlockStoneCoffin;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class ItemBlockStoneCoffin extends ItemBlockCoffin {
    private static final String VARIANT_TAG = "variant";

    public ItemBlockStoneCoffin(Block block) {
        super(block);
    }

    @Override
    protected boolean mayPlace(Level world, BlockPos pos, Direction sidePlacedOn, Player placer) {
        return canPlace(world, pos, sidePlacedOn, placer);
    }

    public static boolean canPlace(Level world, BlockPos pos, Direction sidePlacedOn, Player placer) {
        Direction facing = placer.getDirection();
        for (int offset = 1; offset < 4; offset++) {
            if (!mayPlaceAt(world, pos.relative(facing, offset), sidePlacedOn, false)) {
                return false;
            }
            if (!mayPlaceAt(world, pos.relative(facing.getCounterClockWise(), 1).relative(facing, offset), sidePlacedOn, false)) {
                return false;
            }
        }
        return true;
    }

    public static BlockStoneCoffin.Variant getVariant(ItemStack stack) {
        //noinspection ConstantConditions
        return stack.hasTag() ? BlockStoneCoffin.Variant.fromName(stack.getTag().getString(VARIANT_TAG)) : BlockStoneCoffin.Variant.getDefault();
    }

    public static ItemStack getVariantStack(BlockCoffin.IVariant variant) {
        ItemStack stack = new ItemStack(AWStructureBlocks.STONE_COFFIN.get());
        stack.setTag(new NBTBuilder().setString(VARIANT_TAG, variant.getSerializedName()).build());
        return stack;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        if (!stack.hasTag()) {
            return super.getDescriptionId(stack);
        }

        //noinspection ConstantConditions
        return String.format("%s.%s", super.getDescriptionId(stack), stack.getTag().getString(VARIANT_TAG));
    }
}
