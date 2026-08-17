package net.shadowmage.ancientwarfare.structure.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.tile.TileMulti;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public abstract class BlockMulti<T extends TileMulti> extends BlockBaseStructure {
    public static final BooleanProperty INVISIBLE = BooleanProperty.create("invisible");
    private final Class<T> teClass;

    public BlockMulti(LegacyMaterial material, String regName, Class<T> teClass) {
        super(material, regName);
        this.teClass = teClass;
    }

    @Override
    protected final void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INVISIBLE);
        for (Property property : getAdditionalProperties()) {
            builder.add(property);
        }
    }

    protected List<Property> getAdditionalProperties() {
        return Collections.emptyList();
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(INVISIBLE, (meta & 1) == 1);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return Boolean.TRUE.equals(state.getValue(INVISIBLE)) ? 1 : 0;
    }

    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        WorldTools.getTile(world, pos, teClass).ifPresent(te -> {
            setPlacementProperties(world, pos, placer, stack, te);
            te.setPlacementDirection(world, pos, state, placer.getDirection(), placer.getYRot());
            placeInvisibleBlocks(world, state, te);
            te.setMainPosOnAdditionalBlocks();
            te.setChanged();
            if (!world.isClientSide) {
                // setPlacedBy runs after the block state has already been sent to
                // clients.  Send the completed BE data (direction, variant,
                // upright state, main-block links, etc.) once placement is done.
                BlockTools.notifyBlockUpdate(te);
            }
        });
    }

    private void placeInvisibleBlocks(Level world, BlockState state, T te) {
        te.getAdditionalPositions(state).forEach(additionalPos -> world.setBlock(additionalPos, defaultBlockState().setValue(INVISIBLE, true), 3));
    }

    protected abstract void setPlacementProperties(Level world, BlockPos pos, LivingEntity placer, ItemStack stack, T te);
}
