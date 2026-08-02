package net.shadowmage.ancientwarfare.vehicle.input;

import net.minecraft.client.KeyMapping;
import net.shadowmage.ancientwarfare.vehicle.network.PacketVehicleInput;

import java.util.function.Consumer;

interface IVehicleMovementHandler {
    KeyMapping getKeyBinding();

    KeyMapping getReverseKeyBinding();

    void updatePacket(PacketVehicleInput pkt);

    class Impl implements IVehicleMovementHandler {
        private KeyMapping keyBinding;
        private KeyMapping reverseKeyBinding;
        private Consumer<PacketVehicleInput> doUpdatePacket;

        public Impl(KeyMapping keyBinding, KeyMapping reverseKeyBinding, Consumer<PacketVehicleInput> doUpdatePacket) {
            this.keyBinding = keyBinding;
            this.reverseKeyBinding = reverseKeyBinding;
            this.doUpdatePacket = doUpdatePacket;
        }

        @Override
        public KeyMapping getKeyBinding() {
            return keyBinding;
        }

        @Override
        public KeyMapping getReverseKeyBinding() {
            return reverseKeyBinding;
        }

        @Override
        public void updatePacket(PacketVehicleInput pkt) {
            doUpdatePacket.accept(pkt);
        }
    }
}
