package net.shadowmage.ancientwarfare.vehicle.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;

import java.io.IOException;

public class PacketAmmoUpdate extends PacketVehicleBase {
    private CompoundTag ammoTag;

    public PacketAmmoUpdate() {
    }

    public PacketAmmoUpdate(VehicleBase vehicle, CompoundTag ammoTag) {
        super(vehicle);
        this.ammoTag = ammoTag;
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        super.writeToStream(data);
        FriendlyByteBuf pb = new FriendlyByteBuf(data);
        pb.writeNbt(ammoTag);
    }

    @Override
    protected void readFromStream(ByteBuf data) throws IOException {
        super.readFromStream(data);
        FriendlyByteBuf pb = new FriendlyByteBuf(data);
        ammoTag = pb.readNbt();
    }

    @Override
    public void execute() {
        vehicle.ammoHelper.updateAmmo(ammoTag);
    }
}
