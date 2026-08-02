package net.shadowmage.ancientwarfare.structure.api;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.NBTHelper;
import net.shadowmage.ancientwarfare.core.util.RenderTools;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.registry.StructureBlockRegistry;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;

public abstract class TemplateRuleBlock extends TemplateRule {
    protected BlockState state;
    private ItemStack cachedStack = null;
    private boolean placeInSurvival = false;

    public TemplateRuleBlock(BlockState state, int turns) {
        this.state = BlockTools.rotateFacing(state, turns);
    }

    public TemplateRuleBlock() {
    }

    public abstract boolean shouldReuseRule(Level world, BlockState state, int turns, BlockPos pos);

    @Override
    public List<ItemStack> getResources() {
        if (state.getBlock() == Blocks.AIR) {
            return Collections.emptyList();
        }

        ItemStack stack = getCachedStack();
        if (!stack.isEmpty()) {
            return Collections.singletonList(stack);
        }

        return Collections.emptyList();
    }

    private ItemStack getCachedStack() {
        cacheStack();
        return cachedStack;
    }

    private void cacheStack() {
        if (cachedStack == null) {
            Optional<ItemStack> stack = getStack();
            placeInSurvival = stack.isPresent();
            cachedStack = stack.orElse(ItemStack.EMPTY);
        }
    }

    protected Optional<ItemStack> getStack() {
        return StructureBlockRegistry.getItemStackFrom(state);
    }

    @Override
    public ItemStack getRemainingStack() {
        return StructureBlockRegistry.getRemainingStackFrom(state);
    }

    @Override
    public boolean placeInSurvival() {
        cacheStack();
        return state.getBlock() != Blocks.AIR && placeInSurvival;
    }

    @Override
    protected String getRuleType() {
        return "rule";
    }

    @Override
    public void parseRule(CompoundTag tag) {
        try {
            state = NBTHelper.getBlockState(tag.getCompound("blockState"));
        } catch (MissingResourceException e) {
            AncientWarfareStructure.LOG.warn("Unable to find blockstate while parsing structure template thus replacing it with air - {}.", e.getMessage());
            state = Blocks.AIR.defaultBlockState();
        }
    }

    @Override
    public void writeRuleData(CompoundTag tag) {
        tag.put("blockState", NBTHelper.getBlockStateTag(state));
    }

    @OnlyIn(Dist.CLIENT)
    public void renderRule(int turns, BlockPos pos, BlockGetter blockAccess, BufferBuilder bufferBuilder) {
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
        Minecraft.getInstance().getBlockRenderer()
                .renderBatched(getState(turns), pos, new PreviewBlockAndTintGetter(blockAccess), poseStack, bufferBuilder, true, RandomSource.create());
        poseStack.popPose();
    }

    /**
     * Adapts the template's plain {@link BlockGetter} to the {@link BlockAndTintGetter} required by the
     * 1.20 block renderer; block data comes from the template while light/tint/shade come from the client level.
     */
    @OnlyIn(Dist.CLIENT)
    private static final class PreviewBlockAndTintGetter implements BlockAndTintGetter {
        private final BlockGetter delegate;

        private PreviewBlockAndTintGetter(BlockGetter delegate) {
            this.delegate = delegate;
        }

        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            return delegate.getBlockEntity(pos);
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return delegate.getBlockState(pos);
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return delegate.getFluidState(pos);
        }

        @Override
        public int getHeight() {
            return delegate.getHeight();
        }

        @Override
        public int getMinBuildHeight() {
            return delegate.getMinBuildHeight();
        }

        @Override
        public float getShade(Direction direction, boolean shade) {
            Level level = Minecraft.getInstance().level;
            return level == null ? 1.0F : level.getShade(direction, shade);
        }

        @Override
        public LevelLightEngine getLightEngine() {
            return Minecraft.getInstance().level.getLightEngine();
        }

        @Override
        public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
            Level level = Minecraft.getInstance().level;
            return level == null ? -1 : level.getBlockTint(pos, colorResolver);
        }
    }

    public BlockState getState(int turns) {
        return BlockTools.rotateFacing(state, turns);
    }

    @Nullable
    public BlockEntity getTileEntity(int turns) {
        return null;
    }

    @SuppressWarnings("squid:S1172")
    public boolean isDynamicallyRendered(int turns) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public void renderRuleDynamic(int turns, BlockPos pos) {
        RenderTools.renderTESR(getTileEntity(turns), pos);
    }
}
