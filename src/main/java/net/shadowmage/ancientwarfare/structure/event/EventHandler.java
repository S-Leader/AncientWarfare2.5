package net.shadowmage.ancientwarfare.structure.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.structure.gamedata.StructureMap;
import net.shadowmage.ancientwarfare.structure.network.PacketStructureEntry;

public final class EventHandler {
    private EventHandler() {
    }

    public static final EventHandler INSTANCE = new EventHandler();

    @SubscribeEvent
    public void onChunkWatch(ChunkWatchEvent.Watch event) {
        ChunkPos chunk = event.getPos();
        ServerPlayer player = event.getPlayer();
        var level = player.serverLevel();

        AWGameData.INSTANCE.getPerWorldData(level, StructureMap.class)
                .getStructureAt(level, chunk.x, chunk.z)
                .ifPresent(structureEntry -> NetworkHandler.sendToPlayer(player,
                        new PacketStructureEntry(level.dimension().location().toString(), chunk.x, chunk.z, structureEntry)));
    }
}
