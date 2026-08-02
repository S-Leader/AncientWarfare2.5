package net.shadowmage.ancientwarfare.core.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

/**
 * Keeps client classes outside the common packet registration path.
 */
final class ClientPacketExecutor {
    private ClientPacketExecutor() {
    }

    static void execute(PacketBase packet) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> executeClient(packet));
    }

    @OnlyIn(Dist.CLIENT)
    private static void executeClient(PacketBase packet) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            packet.execute(player);
        }
    }
}
