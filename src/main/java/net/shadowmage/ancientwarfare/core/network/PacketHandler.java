package net.shadowmage.ancientwarfare.core.network;

/**
 * Kept as a source-compatibility marker. Forge 1.20.1 packet dispatch is now
 * performed by {@link PacketBase} through a SimpleChannel.
 */
@Deprecated(forRemoval = false)
public final class PacketHandler {
    private PacketHandler() {
    }
}
