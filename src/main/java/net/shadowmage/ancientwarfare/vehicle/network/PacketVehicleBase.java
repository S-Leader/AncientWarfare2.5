package net.shadowmage.ancientwarfare.vehicle.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.network.PacketBase;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;

import javax.annotation.Nullable;
import java.io.IOException;

public abstract class PacketVehicleBase extends PacketBase {
    private int entityID;
    protected VehicleBase vehicle = null;

    public PacketVehicleBase() {
    }

    public PacketVehicleBase(VehicleBase vehicle) {
        this.entityID = vehicle.getId();
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        data.writeInt(entityID);
    }

    @Override
    protected void readFromStream(ByteBuf data) throws IOException {
        entityID = data.readInt();
    }

    @Nullable
    private VehicleBase getVehicle(Level world) {
        Entity ret = world.getEntity(entityID);
        return ret instanceof VehicleBase ? (VehicleBase) ret : null;
    }

    @Override
    protected void execute(Player player) {
        vehicle = getVehicle(player.level());
        if (vehicle == null) {
            return;
        }
        super.execute(player);
    }
}
