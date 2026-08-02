package net.shadowmage.ancientwarfare.structure.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.shadowmage.ancientwarfare.core.util.NBTBuilder;
import net.shadowmage.ancientwarfare.structure.block.BlockCoffin;
import net.shadowmage.ancientwarfare.structure.block.BlockWoodenCoffin;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class ItemBlockWoodenCoffin extends ItemBlockCoffin {
    private static final String VARIANT_TAG = "variant";

    public ItemBlockWoodenCoffin(Block block) {
        super(block);
    }

    @Override
    public boolean mayPlace(Level world, BlockPos pos, Direction sidePlacedOn, Player placer) {
        return canPlace(world, pos, sidePlacedOn, placer);
    }

    public static boolean canPlace(Level world, BlockPos pos, Direction sidePlacedOn, Player placer) {
        return canPlaceHorizontal(world, pos, sidePlacedOn, placer) || canPlaceVertical(world, pos, sidePlacedOn);
    }

    private static boolean canPlaceVertical(Level world, BlockPos pos, Direction sidePlacedOn) {
        for (int offset = 0; offset < 3; offset++) {
            if (!mayPlaceAt(world, pos.relative(Direction.UP, offset), sidePlacedOn, offset == 0)) {
                return false;
            }
        }
        return true;
    }

    public static boolean canPlaceHorizontal(Level world, BlockPos pos, Direction sidePlacedOn, LivingEntity placer) {
        Direction facing = placer.getDirection();
        for (int offset = 1; offset < 3; offset++) {
            if (!mayPlaceAt(world, pos.relative(facing, offset), sidePlacedOn, false)) {
                return false;
            }
        }
        return true;
    }

    public static BlockWoodenCoffin.Variant getVariant(ItemStack stack) {
        //noinspection ConstantConditions
        return stack.hasTag() ? BlockWoodenCoffin.Variant.fromName(stack.getTag().getString(VARIANT_TAG)) : BlockWoodenCoffin.Variant.getDefault();
    }

    public static ItemStack getVariantStack(BlockCoffin.IVariant variant) {
        ItemStack stack = new ItemStack(AWStructureBlocks.WOODEN_COFFIN);
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
