package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.api.IStructureBuilder;
import net.shadowmage.ancientwarfare.structure.tile.TileMulti;

import javax.annotation.Nullable;
import java.util.Optional;

import static net.shadowmage.ancientwarfare.structure.block.BlockMulti.INVISIBLE;

public abstract class TemplateRuleMulti<T extends TileMulti> extends TemplateRuleBlockTile<T> {
    private final Class<T> teClass;
    private boolean mainBlock = false;

    public TemplateRuleMulti(Level world, BlockPos pos, BlockState state, int turns, Class<T> teClass) {
        super(world, pos, state, turns);
        this.teClass = teClass;

        Optional<TileMulti> te = WorldTools.getTile(world, pos, TileMulti.class);
        if (!te.isPresent()) {
            return;
        }
        TileMulti tileMulti = te.get();
        mainBlock = !tileMulti.getMainBlockPos().isPresent();
    }

    public TemplateRuleMulti(Class<T> teClass) {
        super();
        this.teClass = teClass;
    }

    @Override
    public void handlePlacement(Level world, int turns, BlockPos pos, IStructureBuilder builder) {
        if (mainBlock) {
            super.handlePlacement(world, turns, pos, builder);
            WorldTools.getTile(world, pos, teClass).ifPresent(te -> {
                te.getAdditionalPositions(state).forEach(additionalPos -> world.setBlock(additionalPos, world.getBlockState(pos).getBlock().defaultBlockState().setValue(INVISIBLE, true), 3));
                te.setMainPosOnAdditionalBlocks();
            });
        }
    }

    @Nullable
    @Override
    public BlockEntity getTileEntity(int turns) {
        return super.getTileEntity(turns);
    }

    @Override
    public boolean shouldPlaceOnBuildPass(Level world, int turns, BlockPos pos, int buildPass) {
        return buildPass == 0;
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public boolean shouldReuseRule(Level world, BlockState state, int turns, BlockPos pos) {
        return false;
    }

    @Override
    public void writeRuleData(CompoundTag tag) {
        super.writeRuleData(tag);
        if (mainBlock) {
            tag.putBoolean("mainBlock", mainBlock);
        }
    }

    @Override
    public void parseRule(CompoundTag tag) {
        super.parseRule(tag);
        mainBlock = tag.getBoolean("mainBlock");
    }
}
