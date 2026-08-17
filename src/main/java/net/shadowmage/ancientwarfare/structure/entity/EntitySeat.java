package net.shadowmage.ancientwarfare.structure.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.shadowmage.ancientwarfare.structure.block.BlockSeat;
import net.shadowmage.ancientwarfare.structure.util.RotationLimit;

import java.util.Optional;

@SuppressWarnings("squid:S2160") //no need to override equals as entityId comparison works for this as well
public class EntitySeat extends Entity implements IEntityAdditionalSpawnData {
    private BlockPos seatPos = BlockPos.ZERO;

    public EntitySeat(EntityType<?> type, Level world) {
        super(type, world);
    }

    public EntitySeat configure(Vec3 position, BlockPos seatPos) {
        this.seatPos = seatPos;
        setPos(position.x, position.y, position.z);
        return this;
    }

    @Override
    protected void defineSynchedData() {
        //noop
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        getRotationLimit().ifPresent(rotationLimit -> {
            if (!rotationLimit.isWithinLimit(passenger.getYRot())) {
                passenger.setYRot(rotationLimit.getMidPoint());
                passenger.yRotO = rotationLimit.getMidPoint();
            }
        });
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && (!isVehicle() || level().isEmptyBlock(seatPos))) {
            discard();
            level().updateNeighbourForOutputSignal(blockPosition(), level().getBlockState(blockPosition()).getBlock());
            return;
        }

        if (!level().isClientSide || !isVehicle()) {
            return;
        }

        restrictPlayerRotation();
    }

    private void restrictPlayerRotation() {
        getRotationLimit().ifPresent(rotationLimit -> {
                    if (!(getPassengers().get(0) instanceof Player)) {
                        return;
                    }
                    Player passenger = (Player) getPassengers().get(0);

                    if (!rotationLimit.isWithinLimit(passenger.getYRot())) {
                        float rotation = rotationLimit.restrictToLimit(passenger.getYRot());
                        passenger.yRotO = rotation;
                        passenger.setYRot(rotation);
                    }
                }
        );

    }

    private Optional<RotationLimit> getRotationLimit() {
        BlockState state = level().getBlockState(seatPos);
        Block block = state.getBlock();
        if (!(block instanceof BlockSeat)) {
            return Optional.empty();
        }
        BlockSeat seat = (BlockSeat) block;
        RotationLimit rotationLimit = seat.getRotationLimit(level(), seatPos, state);
        return Optional.of(rotationLimit);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        seatPos = BlockPos.of(compound.getLong("seatPos"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putLong("seatPos", seatPos.asLong());
    }

    public BlockPos getSeatPos() {
        return seatPos;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeLong(seatPos.asLong());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        seatPos = BlockPos.of(additionalData.readLong());
    }
}
