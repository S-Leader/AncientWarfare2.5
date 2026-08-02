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
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.tile.TileMulti;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public abstract class BlockMulti<T extends TileMulti> extends BlockBaseStructure {
    public static final BooleanProperty INVISIBLE = BooleanProperty.create("invisible");
    private final Supplier<T> instantiateTe;
    private final Class<T> teClass;

    public BlockMulti(LegacyMaterial material, String regName, Supplier<T> instantiateTe, Class<T> teClass) {
        super(material, regName);
        this.instantiateTe = instantiateTe;
        this.teClass = teClass;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity createTileEntity(Level world, BlockState state) {
        return instantiateTe.get();
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
        });
    }

    private void placeInvisibleBlocks(Level world, BlockState state, T te) {
        te.getAdditionalPositions(state).forEach(additionalPos -> world.setBlock(additionalPos, defaultBlockState().setValue(INVISIBLE, true), 3));
    }

    protected abstract void setPlacementProperties(Level world, BlockPos pos, LivingEntity placer, ItemStack stack, T te);
}
