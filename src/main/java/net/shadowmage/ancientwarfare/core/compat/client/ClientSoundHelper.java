package net.shadowmage.ancientwarfare.core.compat.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

/**
 * Modern replacement for the removed PositionedSoundRecord helpers.
 */
public final class ClientSoundHelper {
    private ClientSoundHelper() {
    }

    public static void playButtonClick() {
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
