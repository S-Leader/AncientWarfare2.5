package net.shadowmage.ancientwarfare.structure.util;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.structure.block.WoodVariant;

import java.util.Locale;
import java.util.function.UnaryOperator;

public class WoodVariantHelper {
    private WoodVariantHelper() {
    }

    private static final String VARIANT_TAG = "variant";

    public static void getSubBlocks(Block block, NonNullList<ItemStack> items) {
        for (WoodVariant variant : WoodVariant.values()) {
            ItemStack stack = new ItemStack(block);
            setStackVariant(variant, stack);
            items.add(stack);
        }
    }

    private static void setStackVariant(WoodVariant variant, ItemStack stack) {
        stack.getOrCreateTag().put(VARIANT_TAG, StringTag.valueOf(variant.getName()));
    }

    public static WoodVariant getVariant(ItemStack stack) {
        //noinspection ConstantConditions
        return stack.hasTag() && stack.getTag().contains(VARIANT_TAG) ? WoodVariant.byName(stack.getTag().getString(VARIANT_TAG)) : WoodVariant.OAK;
    }

    public static ItemStack getPickBlock(Block block, BlockState state) {
        ItemStack stack = new ItemStack(block);
        setStackVariant(state.getValue(BlockStateProperties.VARIANT), stack);
        return stack;
    }

    public static void getDrops(Block block, NonNullList<ItemStack> drops, BlockState state) {
        ItemStack drop = new ItemStack(block);
        setStackVariant(state.getValue(BlockStateProperties.VARIANT), drop);
        drops.add(drop);
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerClient(Block block) {
        // Standard blockstate/item JSONs handle these models in 1.20.
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerClient(Block block, UnaryOperator<String> updatePropertyString) {
        // Standard blockstate/item JSONs handle these models in 1.20.
    }
}
