package net.shadowmage.ancientwarfare.core.interfaces;

/**
 * Modern replacement for the removed ForgeChunkManager.Ticket callback API.
 */
public interface IChunkLoaderTile {
    void refreshForcedChunks();

    void releaseForcedChunks();
}
