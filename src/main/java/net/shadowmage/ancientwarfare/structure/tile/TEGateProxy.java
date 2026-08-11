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
    /** Last known collision state, used while the owner entity is being resolved after chunk load. */
    private boolean gateClosed = true;
    /** Entity id most recently sent to clients. */
    private int syncedOwnerEntityId = Integer.MIN_VALUE;

    public void setOwner(EntityGate gate) {
        if (gate == null) {
            return;
        }

        boolean changed = owner != gate
                || entityID == null
                || !entityID.equals(gate.getUUID())
                || syncedOwnerEntityId != gate.getId()
                || gateClosed != gate.isClosed();
        owner = gate;
        entityID = gate.getUUID();
        gateClosed = gate.isClosed();

        if (changed) {
            syncedOwnerEntityId = gate.getId();
            setChanged();
            BlockTools.notifyBlockUpdate(this);
        }
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
        if (tag.contains("gateClosed")) {
            gateClosed = tag.getBoolean("gateClosed");
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        if (entityID != null) {
            tag.putLong("msb", entityID.getMostSignificantBits());
            tag.putLong("lsb", entityID.getLeastSignificantBits());
        }
        tag.putBoolean(RENDER_TAG, render);
        tag.putBoolean("gateClosed", owner != null ? owner.isClosed() : gateClosed);
        return tag;
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        tag.putBoolean(RENDER_TAG, render);
        tag.putBoolean("gateClosed", owner != null ? owner.isClosed() : gateClosed);
        tag.putInt("owner", owner != null ? owner.getId() : 0);
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        render = tag.getBoolean(RENDER_TAG);
        gateClosed = tag.contains("gateClosed") ? tag.getBoolean("gateClosed") : gateClosed;
        int newOwnerId = tag.getInt("owner");
        if (newOwnerId != clientEntityID || owner == null || owner.isRemoved()) {
            owner = null;
        }
        clientEntityID = newOwnerId;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return getGate().map(EntityGate::getRenderBoundingBox).orElse(super.getRenderBoundingBox());
    }

    @Override
    public void update() {
        if (!hasWorld()) {
            return;
        }

        if (world.isClientSide) {
            if (!render || clientEntityID <= 0) {
                return;
            }
            if (owner == null || owner.isRemoved() || owner.getId() != clientEntityID) {
                Entity entity = world.getEntity(clientEntityID);
                owner = entity instanceof EntityGate ? (EntityGate) entity : null;
            }
            return;
        }

        if (owner != null && !owner.isRemoved()) {
            boolean stateChanged = gateClosed != owner.isClosed();
            boolean idChanged = syncedOwnerEntityId != owner.getId();
            if (stateChanged || idChanged) {
                gateClosed = owner.isClosed();
                syncedOwnerEntityId = owner.getId();
                setChanged();
                BlockTools.notifyBlockUpdate(this);
            }
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
        return getOwner().map(EntityGate::isClosed).orElse(gateClosed);
    }

    public Optional<EntityGate> getOwner() {
        return Optional.ofNullable(owner);
    }

    public void setRender() {
        if (!render) {
            render = true;
            setChanged();
            BlockTools.notifyBlockUpdate(this);
        }
    }

    public boolean doesRender() {
        return render;
    }

    public Optional<EntityGate> getGate() {
        return Optional.ofNullable(owner);
    }

    public boolean isOpen() {
        return owner != null ? !owner.isClosed() : !gateClosed;
    }
}
