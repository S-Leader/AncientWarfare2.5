package net.shadowmage.ancientwarfare.structure.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.structure.util.BlockStateProperties;
import net.shadowmage.ancientwarfare.structure.util.RotationLimit;
import net.shadowmage.ancientwarfare.structure.util.WoodVariantHelper;

public class BlockStool extends BlockSeat {
    private static final Vec3 SEAT_OFFSET = new Vec3(0.5, 0.35, 0.5);
    private static final AABB STOOL_AABB = new AABB(3 / 16D, 0D, 3 / 16D, 13 / 16D, 9 / 16D, 13 / 16D);

    public BlockStool() {
        super(LegacyMaterial.WOOD, "stool");
    }

    public void getSubBlocks(CreativeModeTab itemIn, NonNullList<ItemStack> items) {
        WoodVariantHelper.getSubBlocks(this, items);
    }

    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!stack.hasTag()) {
            return;
        }
        world.setBlock(pos, state.setValue(BlockStateProperties.VARIANT, WoodVariantHelper.getVariant(stack)), 3);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, Level world, BlockPos pos, Player player) {
        return WoodVariantHelper.getPickBlock(this, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.VARIANT);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(BlockStateProperties.VARIANT, WoodVariant.byMeta(meta));
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(BlockStateProperties.VARIANT).getMeta();
    }

    @Override
    protected Vec3 getSeatOffset() {
        return SEAT_OFFSET;
    }

    @Override
    public RotationLimit getRotationLimit(Level world, BlockPos pos, BlockState state) {
        return RotationLimit.NO_LIMIT;
    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
        return STOOL_AABB;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        WoodVariantHelper.registerClient(this);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, BlockGetter world, BlockPos pos, BlockState state, int fortune) {
        WoodVariantHelper.getDrops(this, drops, state);
    }
}
