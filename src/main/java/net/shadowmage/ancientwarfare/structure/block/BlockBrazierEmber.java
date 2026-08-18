package net.shadowmage.ancientwarfare.structure.block;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.util.NBTBuilder;

public class BlockBrazierEmber extends BlockBaseStructure {
    public static final String LIT_TAG = "lit";
    private static final BooleanProperty LIT = BooleanProperty.create(LIT_TAG);

    private static final AABB AABB = new AABB(0D, 0D, 0D, 1D, 0.88D, 1D);

    public BlockBrazierEmber() {
        super(LegacyMaterial.ROCK, "brazier_ember");
        setHardness(2);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    public void getSubBlocks(CreativeModeTab itemIn, NonNullList<ItemStack> items) {
        Item item = this.asItem();
        ItemStack stack = new ItemStack(item);
        stack.setTag(new NBTBuilder().setBoolean(LIT_TAG, false).build());
        items.add(stack);
        stack = new ItemStack(item);
        stack.setTag(new NBTBuilder().setBoolean(LIT_TAG, true).build());
        items.add(stack);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(LIT, (meta == 1));
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(LIT) ? 1 : 0;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(BlockState state) {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public RenderType getBlockLayer() {
        return RenderType.cutout();
    }


    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
        return AABB;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return state.getValue(LIT) ? 6 : 0;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.below()).isFaceSturdy(worldIn, pos.below(), Direction.UP);
    }

    @Override
    public void onBlockPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!stack.hasTag()) {
            return;
        }
        //noinspection ConstantConditions
        state = state.setValue(LIT, stack.getTag().getBoolean(LIT_TAG));
        worldIn.setBlock(pos, state, 3);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, Level world, BlockPos pos, Player player) {
        ItemStack pickStack = new ItemStack(this.asItem());
        pickStack.setTag(new NBTBuilder().setBoolean(LIT_TAG, state.getValue(LIT)).build());
        return pickStack;
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getItemInHand(hand);
        if (!state.getValue(LIT) && isFireStarter(stack)) {
            if (world.isClientSide) {
                return true;
            }

            world.setBlock(pos, state.setValue(LIT, true), 3);
            if (stack.isDamageableItem()) {
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
            world.playSound(null, pos, SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1, 1);

            return true;
        } else if (state.getValue(LIT) && stack.getItem() == Items.WATER_BUCKET) {
            if (world.isClientSide) {
                return true;
            }

            world.setBlock(pos, state.setValue(LIT, false), 3);
            if (!player.getAbilities().instabuild) {
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            }
            world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5f, 1);
        }

        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    private boolean isFireStarter(ItemStack heldItem) {
        return heldItem.getItem() == Items.FLINT_AND_STEEL || heldItem.getItem() == Blocks.TORCH.asItem();
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, BlockGetter world, BlockPos pos, BlockState state, int fortune) {
        ItemStack BlockBrazierEmber = new ItemStack(this);
        BlockBrazierEmber.setTag(new NBTBuilder().setBoolean(LIT_TAG, false).build());
        drops.add(BlockBrazierEmber);
    }

    public int tickRate(Level worldIn) {
        return 30;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
        if (!stateIn.getValue(LIT)) {
            return;
        }

        if (rand.nextInt(10) == 0) {
            worldIn.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F, false);
        }

        for (int i = 0; i < 3; ++i) {
            double x = pos.getX() + 0.25D + rand.nextDouble() * 0.5D;
            double y = pos.getY() + rand.nextDouble() * 0.5D + 0.5D;
            double z = pos.getZ() + 0.25D + rand.nextDouble() * 0.5D;
            worldIn.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    public static float getItemModelProperty(ItemStack stack) {
        return stack.hasTag() && !stack.getTag().getBoolean(LIT_TAG) ? 1.0F : 0.0F;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        // Models are loaded normally from 1.20 blockstates/models JSON.
    }

}
