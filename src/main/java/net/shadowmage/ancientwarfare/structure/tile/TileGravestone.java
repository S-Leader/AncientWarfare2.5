package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler;
import net.shadowmage.ancientwarfare.core.tile.IBlockBreakHandler;
import net.shadowmage.ancientwarfare.core.tile.TileUpdatable;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.EntityTools;
import net.shadowmage.ancientwarfare.structure.util.LootHelper;

public class TileGravestone extends TileUpdatable implements ISpecialLootContainer, IBlockBreakHandler, BlockRotationHandler.IRotatableTile {
    private Direction facing = Direction.NORTH;
    private int variant = 1;
    private LootSettings lootSettings = new LootSettings();

    @Override
    public Direction getPrimaryFacing() {
        return facing;
    }

    @Override
    public void setPrimaryFacing(Direction face) {
        facing = face;
    }

    public void setVariant(int variant) {
        this.variant = variant;
    }

    public int getVariant() {
        return variant;
    }

    @Override
    public void setLootSettings(LootSettings settings) {
        lootSettings = settings;
    }

    @Override
    public LootSettings getLootSettings() {
        return lootSettings;
    }

    @Override
    public void onBlockBroken(BlockState state) {
        LootHelper.dropLoot(this, EntityTools.findClosestPlayer(world, pos, 100));
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        readNBT(compound);
        markDirty();
    }

    private void readNBT(CompoundTag compound) {
        variant = compound.getInt("variant");
        lootSettings = LootSettings.deserializeNBT(compound.getCompound("lootSettings"));
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        super.writeUpdateNBT(tag);
        writeNBT(tag);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag compound) {
        compound = super.writeToNBT(compound);
        writeNBT(compound);
        return compound;
    }

    private CompoundTag writeNBT(CompoundTag compound) {
        compound.put("lootSettings", lootSettings.serializeNBT());
        compound.putInt("variant", variant);
        return compound;
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        readNBT(tag);
        BlockTools.notifyBlockUpdate(this);
    }

    public void activate(Player player) {
        if (6 <= getVariant() && getVariant() <= 8) { // only for runestones: variant 6,7,8
            dropLoot(player);
        }
    }

    private void dropLoot(Player player) {
        if (!world.isClientSide) {
            LootHelper.dropLoot(this, player);
        }
    }
}
