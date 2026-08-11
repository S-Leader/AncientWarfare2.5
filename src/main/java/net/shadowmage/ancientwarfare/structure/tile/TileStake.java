package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.shadowmage.ancientwarfare.core.tile.TileUpdatable;
import net.shadowmage.ancientwarfare.core.util.BlockTools;

import java.util.Optional;

public class TileStake extends TileUpdatable {
    private final EntityStatueInfo entityStatueInfo = new EntityStatueInfo();
    private boolean burns = true;

    public Optional<Entity> getRenderEntity() {
        return entityStatueInfo.getRenderEntity(world);
    }

    public boolean isEntityOnFire() {
        return entityStatueInfo.isEntityOnFire();
    }

    public boolean burns() {
        return burns;
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        readNBT(compound);
        markDirty();
    }

    private void readNBT(CompoundTag compound) {
        entityStatueInfo.deserializeNBT(compound);
        burns = compound.getBoolean("burns");
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        writeNBT(tag);
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        readNBT(tag);
        BlockTools.notifyBlockUpdate(this);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag compound) {
        compound = super.writeToNBT(compound);
        return writeNBT(compound);
    }

    private CompoundTag writeNBT(CompoundTag compound) {
        compound = entityStatueInfo.serializeNBT(compound);
        compound.putBoolean("burns", burns);
        return compound;
    }

    public void resetEntityName() {
        entityStatueInfo.resetEntityName();
        syncVisualState();
    }

    public ResourceLocation getEntityName() {
        return entityStatueInfo.getEntityName();
    }

    public void setEntityName(ResourceLocation entityName) {
        entityStatueInfo.setEntityName(entityName);
        syncVisualState();
    }

    public void setEntityOnFire(boolean entityOnFire) {
        entityStatueInfo.setEntityOnFire(entityOnFire);
        syncVisualState();
    }

    public void setBurns(boolean burns) {
        this.burns = burns;
        syncVisualState();
    }

    private void syncVisualState() {
        markDirty();
        requestModelDataUpdate();
        if (world != null) {
            BlockTools.notifyBlockUpdate(this);
            world.getLightEngine().checkBlock(pos);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(pos, pos.offset(0, 3, 0));
    }
}
