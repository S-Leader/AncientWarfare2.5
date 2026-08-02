package net.shadowmage.ancientwarfare.vehicle.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.vehicle.AncientWarfareVehicles;

public class AWVehicleSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AncientWarfareVehicles.MOD_ID);
    public static final SoundEvent BALLISTA_BOLT_HIT_ENTITY = createSoundEvent("ballista_bolt_hit_entity");
    public static final SoundEvent BALLISTA_BOLT_HIT_GROUND = createSoundEvent("ballista_bolt_hit_ground");
    public static final SoundEvent BALLISTA_LAUNCH = createSoundEvent("ballista_launch");
    public static final SoundEvent BALLISTA_RELOAD = createSoundEvent("ballista_reload");
    public static final SoundEvent BATTERING_RAM_HIT_WOOD = createSoundEvent("battering_ram_hit_wood");
    public static final SoundEvent BATTERING_RAM_HIT_IRON = createSoundEvent("battering_ram_hit_iron");
    public static final SoundEvent BATTERING_RAM_HIT_STONE = createSoundEvent("battering_ram_hit_stone");
    public static final SoundEvent BATTERING_RAM_LAUNCH = createSoundEvent("battering_ram_launch");
    public static final SoundEvent CATAPULT_LAUNCH = createSoundEvent("catapult_launch");
    public static final SoundEvent CATAPULT_RELOAD = createSoundEvent("catapult_reload");
    public static final SoundEvent GIANT_TREBUCHET_LAUNCH = createSoundEvent("giant_trebuchet_launch");
    public static final SoundEvent TREBUCHET_LAUNCH = createSoundEvent("trebuchet_launch");
    public static final SoundEvent TREBUCHET_RELOAD = createSoundEvent("trebuchet_reload");
    public static final SoundEvent VEHICLE_MOVING = createSoundEvent("vehicle_moving");

    private AWVehicleSounds() {
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }

    private static SoundEvent createSoundEvent(String soundName) {
        ResourceLocation registryName = new ResourceLocation(AncientWarfareVehicles.MOD_ID, soundName);
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(registryName);
        SOUNDS.register(soundName, () -> soundEvent);
        return soundEvent;
    }

}
