package net.shadowmage.ancientwarfare.structure.init;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;

public class AWStructureSoundTypes {
    private AWStructureSoundTypes() {
    }

    public static final SoundType URN = new SoundType(1.0F, 1.0F, AWStructureSounds.URN_BREAK, SoundEvents.STONE_STEP,
            SoundEvents.STONE_PLACE, SoundEvents.STONE_HIT, SoundEvents.STONE_FALL);

    public static final SoundType COINSTACK = new SoundType(1.0F, 1.3F, AWStructureSounds.COIN_STACK_BREAK, AWStructureSounds.COIN_STACK_INTERACT,
            AWStructureSounds.COIN_STACK_INTERACT, AWStructureSounds.COIN_STACK_INTERACT, AWStructureSounds.COIN_STACK_BREAK);
}
