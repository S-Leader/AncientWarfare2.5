package net.shadowmage.ancientwarfare.structure.sounds;

/**
 * Compatibility marker for the removed 1.12 dynamic sound loader.
 *
 * <p>Sound events must be registered before Forge locks the sound registry.
 * The only resource that used the old auto-load directory is now declared in
 * AWStructureSounds and sounds.json, so no runtime reflection or registry
 * mutation is required.</p>
 */
public final class SoundLoader {
    private SoundLoader() {
    }
}
