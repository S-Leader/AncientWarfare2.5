package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.interfaces.ITickable;
import net.shadowmage.ancientwarfare.core.tile.TileUpdatable;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.structure.entity.EntityGate;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class TEGateProxy extends TileUpdatable implements ITickable {
    private static final String RENDER_TAG = "render";
    @Nullable
    private EntityGate owner = null;
    private UUID entityID = null;
    private int clientEntityID = 0;
    private int noParentTicks = 0;
    private boolean render = false;

    public void setOwner(EntityGate gate) {
        owner = gate;
        entityID = owner.getUUID();
        BlockTools.notifyBlockUpdate(this);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        if (tag.contains("msb") && tag.contains("lsb")) {
            long msb = tag.getLong("msb");
            long lsb = tag.getLong("lsb");
            entityID = new UUID(msb, lsb);
        }
        render = tag.getBoolean(RENDER_TAG);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        if (entityID != null) {
            tag.putLong("msb", entityID.getMostSignificantBits());
            tag.putLong("lsb", entityID.getLeastSignificantBits());
        }
        tag.putBoolean(RENDER_TAG, render);
        return tag;
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        tag.putBoolean(RENDER_TAG, render);
        tag.putInt("owner", owner != null ? owner.getId() : 0);
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        render = tag.getBoolean(RENDER_TAG);
        clientEntityID = tag.getInt("owner");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return getGate().map(EntityGate::getRenderBoundingBox).orElse(super.getRenderBoundingBox());
    }

    @Override
    public void update() {
        if (!hasWorld() || (world.isClientSide && (!render || clientEntityID <= 0 || owner != null))) {
            return;
        }
        if (world.isClientSide) {
            Entity entity = world.getEntity(clientEntityID);
            owner = entity instanceof EntityGate ? (EntityGate) entity : null;
            return;
        }

        handleMissingOwner();
    }

    private void handleMissingOwner() {
        if (entityID == null) {
            noParentTicks++;
        } else if (!getOwner().isPresent()) {
            noParentTicks++;

            if (world instanceof ServerLevel serverLevel) {
                Entity ent = serverLevel.getEntity(entityID);
                if (ent instanceof EntityGate) {
                    setOwner((EntityGate) ent);
                    noParentTicks = 0;
                }
            }
        }
        if (noParentTicks >= 100 || getOwner().map(o -> o.isRemoved()).orElse(false)) {
            owner = null;
            world.removeBlock(pos, false);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    public boolean isGateClosed() {
        return getOwner().map(EntityGate::isClosed).orElse(false);
    }

    public Optional<EntityGate> getOwner() {
        return Optional.ofNullable(owner);
    }

    public void setRender() {
        render = true;
        BlockTools.notifyBlockUpdate(this);
    }

    public boolean doesRender() {
        return render;
    }

    public Optional<EntityGate> getGate() {
        return Optional.ofNullable(owner);
    }

    public boolean isOpen() {
        return owner == null || !owner.isClosed();
    }
}
