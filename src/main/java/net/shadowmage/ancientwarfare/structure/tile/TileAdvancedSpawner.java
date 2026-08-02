package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.interfaces.ITickable;
import net.shadowmage.ancientwarfare.core.tile.IBlockBreakHandler;
import net.shadowmage.ancientwarfare.core.tile.TileUpdatable;
import net.shadowmage.ancientwarfare.core.util.BlockTools;

public class TileAdvancedSpawner extends TileUpdatable implements ITickable, IBlockBreakHandler {

    private SpawnerSettings settings = new SpawnerSettings();

    @Override
    public void setLevel(Level world) {
        super.setLevel(world);
        settings.setWorld(world, pos);
    }

    @Override
    public void update() {
        if (!hasWorld() || world.isClientSide) {
            return;
        }
        if (!settings.hasWorld()) {
            settings.setWorld(world, pos);
        }
        settings.onUpdate();
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        CompoundTag ntag = new CompoundTag();
        settings.writeToNBT(ntag);
        tag.put("spawnerSettings", ntag);
        return tag;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        settings.readFromNBT(tag.getCompound("spawnerSettings"));
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        settings.updateSpawnProperties();
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        super.writeUpdateNBT(tag);
        settings.writeToNBT(tag);
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        super.handleUpdateNBT(tag);
        settings.readFromNBT(tag);
        BlockTools.notifyBlockUpdate(this);
    }

    public SpawnerSettings getSettings() {
        return settings;
    }

    public void setSettings(SpawnerSettings settings) {
        this.settings = settings;
        this.settings.setWorld(world, pos);
        this.settings.updateSpawnProperties();

        BlockTools.notifyBlockUpdate(this);
    }

    public float getBlockHardness() {
        return settings.blockHardness;
    }

    @Override
    public void onBlockBroken(BlockState state) {
        if (world.isClientSide) {
            return;
        }
        int xp = settings.getXpToDrop();
        while (xp > 0) {
            int j = ExperienceOrb.getExperienceValue(xp);
            xp -= j;
            this.world.addFreshEntity(new ExperienceOrb(this.world, this.pos.getX() + 0.5d, this.pos.getY(), this.pos.getZ() + 0.5d, j));
        }
        //InventoryTools.dropItemsInWorld(world, settings.getInventory(), pos);
    }
}
