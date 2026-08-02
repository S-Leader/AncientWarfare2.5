package net.shadowmage.ancientwarfare.vehicle.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;

import java.io.IOException;

public class PacketSingleAmmoUpdate extends PacketVehicleBase {
    private String ammoRegistryName;
    private int count;

    public PacketSingleAmmoUpdate() {
    }

    public PacketSingleAmmoUpdate(VehicleBase vehicle, String ammoRegistryName, int count) {
        super(vehicle);
        this.ammoRegistryName = ammoRegistryName;
        this.count = count;
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        super.writeToStream(data);
        FriendlyByteBuf pb = new FriendlyByteBuf(data);
        pb.writeUtf(ammoRegistryName);
        pb.writeInt(count);
    }

    @Override
    protected void readFromStream(ByteBuf data) throws IOException {
        super.readFromStream(data);
        FriendlyByteBuf pb = new FriendlyByteBuf(data);
        ammoRegistryName = pb.readUtf(64);
        count = pb.readInt();
    }

    @Override
    public void execute() {
        vehicle.ammoHelper.updateAmmoCount(ammoRegistryName, count);
    }
}
