package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.tile.IBlockBreakHandler;
import net.shadowmage.ancientwarfare.core.tile.TileUpdatable;
import net.shadowmage.ancientwarfare.core.util.EntityTools;
import net.shadowmage.ancientwarfare.structure.util.LootHelper;

public class TileUrn extends TileUpdatable implements ISpecialLootContainer, IBlockBreakHandler {
    private LootSettings lootSettings = new LootSettings();

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
    protected void writeUpdateNBT(CompoundTag tag) {
        super.writeUpdateNBT(tag);
        writeNBT(tag);
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        super.handleUpdateNBT(tag);
        readNBT(tag);
    }

    private void readNBT(CompoundTag tag) {
        lootSettings = LootSettings.deserializeNBT(tag.getCompound("lootSettings"));
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        readNBT(compound);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag compound) {
        return writeNBT(super.writeToNBT(compound));
    }

    private CompoundTag writeNBT(CompoundTag tagCompound) {
        tagCompound.put("lootSettings", lootSettings.serializeNBT());
        return tagCompound;
    }
}
