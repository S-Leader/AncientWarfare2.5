package net.shadowmage.ancientwarfare.structure.tile;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.shadowmage.ancientwarfare.structure.block.BlockCoffin;
import net.shadowmage.ancientwarfare.structure.block.BlockStoneCoffin;
import net.shadowmage.ancientwarfare.structure.init.AWStructureSounds;

import java.util.Map;
import java.util.Set;

public class TileStoneCoffin extends TileCoffin {
    private static final int TOTAL_OPEN_TIME = 60;
    private BlockStoneCoffin.Variant variant = BlockStoneCoffin.Variant.STONE;

    public void setVariant(BlockStoneCoffin.Variant variant) {
        BlockStoneCoffin.Variant safeVariant = variant == null ? BlockStoneCoffin.Variant.STONE : variant;
        if (this.variant != safeVariant) {
            this.variant = safeVariant;
            setChanged();
        }
    }

    @Override
    public BlockStoneCoffin.Variant getVariant() {
        return getValueFromMain(TileStoneCoffin.class, TileStoneCoffin::getVariant, variant, () -> BlockStoneCoffin.Variant.STONE);
    }

    @Override
    public Set<BlockPos> getAdditionalPositions(BlockState state) {
        return
                ImmutableSet.of(
                        pos.relative(direction.getFacing()),
                        pos.relative(direction.getFacing()).relative(direction.getFacing()),
                        pos.relative(direction.getFacing()).relative(direction.getFacing()).relative(direction.getFacing()),
                        pos.relative(direction.getFacing().getCounterClockWise()),
                        pos.relative(direction.getFacing().getCounterClockWise()).relative(direction.getFacing()),
                        pos.relative(direction.getFacing().getCounterClockWise()).relative(direction.getFacing()).relative(direction.getFacing()),
                        pos.relative(direction.getFacing().getCounterClockWise()).relative(direction.getFacing()).relative(direction.getFacing()).relative(direction.getFacing())
                );
    }

    private static final Map<BlockCoffin.IVariant, SoundEvent> COFFIN_SOUNDS = ImmutableMap.of(
            BlockStoneCoffin.Variant.STONE, AWStructureSounds.STONE_COFFIN_OPENS,
            BlockStoneCoffin.Variant.SANDSTONE, AWStructureSounds.SANDSTONE_SARCOPHAGUS_OPENS,
            BlockStoneCoffin.Variant.PRISMARINE, AWStructureSounds.PRISMARINE_COFFIN_OPENS,
            BlockStoneCoffin.Variant.DEMONIC, AWStructureSounds.DEMONIC_COFFIN_OPENS);

    @Override
    protected void playSound() {
        world.playSound(null, pos, COFFIN_SOUNDS.get(variant), SoundSource.BLOCKS, 1, 1);
    }

    @Override
    protected int getTotalOpenTime() {
        return TOTAL_OPEN_TIME;
    }

    @Override
    public AABB getRenderBoundingBox() {
        Vec3i vec = direction.getFacing().getNormal();
        return new AABB(pos.offset(-3, 0, -3), pos.offset(3, 2, 3)).expandTowards(vec.getX(), vec.getY(), vec.getZ());
    }

    @Override
    protected void readNBT(CompoundTag compound) {
        super.readNBT(compound);
        variant = BlockStoneCoffin.Variant.fromName(compound.getString("variant"));
    }

    @Override
    protected void writeNBT(CompoundTag compound) {
        super.writeNBT(compound);
        compound.putString("variant", variant.getName());
    }
}
