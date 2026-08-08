package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.block.BlockTotemPart;
import net.shadowmage.ancientwarfare.structure.block.BlockTotemPart.Variant;

import java.util.Set;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;

public class TileTotemPart extends TileMulti {
    private static final String VARIANT_TAG = "variant";
    private Variant variant = Variant.BASE;
    private Variant dropVariant = Variant.BASE;

    public void setVariant(Variant variant) {
        this.variant = variant == null ? Variant.BASE : variant;
        this.dropVariant = this.variant;
        setChanged();
        syncVariantToBlockState();
    }

    /**
     * 1.12 rendered the tile-backed variant through Block#getActualState.
     * Modern chunk rendering only gets the stored BlockState, so keep the two
     * representations synchronized whenever NBT/placement changes the variant.
     */
    private void syncVariantToBlockState() {
        if (level == null) {
            return;
        }

        BlockState current = level.getBlockState(worldPosition);
        if (!(current.getBlock() instanceof BlockTotemPart)) {
            return;
        }

        BlockState updated = BlockTotemPart.withVariant(current, variant);
        if (!updated.equals(current)) {
            level.setBlock(worldPosition, updated, Block.UPDATE_CLIENTS);
        }

        if (level.isClientSide) {
            requestModelDataUpdate();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        syncVariantToBlockState();
    }

    public Variant getVariant() {
        return variant;
    }

    @Override
    public void setMainBlockPos(BlockPos mainBlockPos) {
        super.setMainBlockPos(mainBlockPos);
        getMainBlockPos().flatMap(mainPos -> WorldTools.getTile(world, mainPos, TileTotemPart.class)).ifPresent(te -> dropVariant = te.getVariant());
    }

    @Override
    protected void readNBT(CompoundTag compound) {
        super.readNBT(compound);
        Variant loadedVariant = Variant.fromId(compound.getByte(VARIANT_TAG));
        variant = loadedVariant == null ? Variant.BASE : loadedVariant;
        dropVariant = variant;
        getMainBlockPos().flatMap(mainPos -> WorldTools.getTile(world, mainPos, TileTotemPart.class)).ifPresent(te -> dropVariant = te.getVariant());
        syncVariantToBlockState();
    }

    @Override
    protected void writeNBT(CompoundTag compound) {
        super.writeNBT(compound);
        compound.putByte(VARIANT_TAG, (byte) variant.getId());
    }

    @Override
    public Set<BlockPos> getAdditionalPositions(BlockState state) {
        return getVariant().getAdditionalPartPositions(pos, state.getValue(FACING));
    }

    public Variant getDropVariant() {
        return dropVariant;
    }
}
