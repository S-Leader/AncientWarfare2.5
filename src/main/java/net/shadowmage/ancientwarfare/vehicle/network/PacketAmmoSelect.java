package net.shadowmage.ancientwarfare.vehicle.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;

import java.io.IOException;

public class PacketAmmoSelect extends PacketVehicleBase {
    private String ammoRegistryName;

    public PacketAmmoSelect() {
    }

    public PacketAmmoSelect(VehicleBase vehicle, String ammoRegistryName) {
        super(vehicle);
        this.ammoRegistryName = ammoRegistryName;
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        super.writeToStream(data);
        FriendlyByteBuf pb = new FriendlyByteBuf(data);
        pb.writeUtf(ammoRegistryName);
    }

    @Override
    protected void readFromStream(ByteBuf data) throws IOException {
        super.readFromStream(data);
        FriendlyByteBuf pb = new FriendlyByteBuf(data);
        ammoRegistryName = pb.readUtf(64);
    }

    @Override
    public void execute() {
        vehicle.ammoHelper.updateSelectedAmmo(ammoRegistryName);
        super.execute();
    }
}
