package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.shadowmage.ancientwarfare.structure.api.IStructureBuilder;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TemplateRuleBed extends TemplateRuleVanillaBlocks {
    public static final String PLUGIN_NAME = "bed";
    private DyeColor color = DyeColor.RED;
    private Tuple<Integer, BedBlockEntity> tileCache = null;

    public TemplateRuleBed(Level world, BlockPos pos, BlockState state, int turns) {
        super(world, pos, state, turns);
        if (state.getBlock() instanceof BedBlock bedBlock) {
            color = bedBlock.getColor();
        }
    }

    public TemplateRuleBed() {
        super();
    }

    @Override
    protected Optional<ItemStack> getStack() {
        return Optional.of(new ItemStack(state.getBlock().asItem()));
    }

    @Override
    public List<ItemStack> getResources() {
        if (state.getValue(BedBlock.PART) == BedPart.FOOT) {
            return super.getResources();
        }
        return Collections.emptyList();
    }

    @Override
    public boolean placeInSurvival() {
        return true;
    }

    @Override
    public void handlePlacement(Level world, int turns, BlockPos pos, IStructureBuilder builder) {
        super.handlePlacement(world, turns, pos, builder);
    }

    @Override
    public boolean shouldReuseRule(Level world, BlockState state, int turns, BlockPos pos) {
        DyeColor blockColor = state.getBlock() instanceof BedBlock bedBlock ? bedBlock.getColor() : DyeColor.RED;
        return color == blockColor && super.shouldReuseRule(world, state, turns, pos);
    }

    @Override
    public void writeRuleData(CompoundTag tag) {
        super.writeRuleData(tag);
        tag.putInt("bedColor", color.getId());
    }

    @Override
    public void parseRule(CompoundTag tag) {
        super.parseRule(tag);
        color = DyeColor.byId(tag.getInt("bedColor"));
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Nullable
    @Override
    public BlockEntity getTileEntity(int turns) {
        if (tileCache == null || tileCache.getA() != turns) {
            BlockState rotatedState = getState(turns);
            tileCache = new Tuple<>(turns, new BedBlockEntity(BlockPos.ZERO, rotatedState, color));
        }
        return tileCache.getB();
    }

    @Override
    public boolean isDynamicallyRendered(int turns) {
        return true;
    }
}
