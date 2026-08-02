package net.shadowmage.ancientwarfare.core.interfaces;

/**
 * Legacy AW tick contract, invoked by the modern block-entity ticker bridge.
 */
public interface ITickable {
    void update();
}
