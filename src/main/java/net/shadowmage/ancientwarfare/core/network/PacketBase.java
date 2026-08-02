package net.shadowmage.ancientwarfare.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Base packet retained from the 1.12 implementation, backed by Forge's
 * 1.20.1 SimpleChannel. Subclasses can keep their existing ByteBuf serializers.
 */
public abstract class PacketBase {
    private static final Map<Integer, Registration<? extends PacketBase>> PACKET_TYPES = new LinkedHashMap<>();
    private static SimpleChannel boundChannel;

    public static synchronized <T extends PacketBase> void registerPacketType(
            int typeNum, Class<T> packetClass, Supplier<T> factory) {
        Registration<T> registration = new Registration<>(typeNum, packetClass, factory);
        PACKET_TYPES.put(typeNum, registration);
        if (boundChannel != null) {
            registration.register(boundChannel);
        }
    }

    static synchronized void bindChannel(SimpleChannel channel) {
        if (boundChannel == channel) {
            return;
        }
        boundChannel = channel;
        PACKET_TYPES.values().forEach(registration -> registration.registerUnchecked(channel));
    }

    protected abstract void writeToStream(ByteBuf data);

    protected abstract void readFromStream(ByteBuf data) throws IOException;

    protected void execute() {
    }

    @SuppressWarnings("squid:S1172")
    protected void execute(Player player) {
        execute();
    }

    private static final class Registration<T extends PacketBase> {
        private final int id;
        private final Class<T> packetClass;
        private final Supplier<T> factory;
        private boolean registered;

        private Registration(int id, Class<T> packetClass, Supplier<T> factory) {
            this.id = id;
            this.packetClass = packetClass;
            this.factory = factory;
        }

        private synchronized void register(SimpleChannel channel) {
            if (registered) {
                return;
            }
            channel.registerMessage(
                    id,
                    packetClass,
                    (packet, buffer) -> packet.writeToStream(buffer),
                    buffer -> {
                        T packet = factory.get();
                        try {
                            packet.readFromStream(buffer);
                        } catch (IOException exception) {
                            throw new IllegalStateException("Failed to decode " + packetClass.getName(), exception);
                        }
                        return packet;
                    },
                    Registration::handle
            );
            registered = true;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private void registerUnchecked(SimpleChannel channel) {
            register(channel);
        }

        private static <T extends PacketBase> void handle(T packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                Player sender = context.getSender();
                if (sender != null) {
                    packet.execute(sender);
                } else {
                    ClientPacketExecutor.execute(packet);
                }
            });
            context.setPacketHandled(true);
        }
    }
}
