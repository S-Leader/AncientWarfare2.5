package net.shadowmage.ancientwarfare.structure.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.entity.EntityGate;
import net.shadowmage.ancientwarfare.structure.entity.EntitySeat;

/** Native Forge registration for structure entities. */
public final class AWStructureEntities {
    public static final String GATE_ID = "aw_gate";
    public static final String SEAT_ID = "seat";

    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AncientWarfareStructure.MOD_ID);

    public static final RegistryObject<EntityType<EntityGate>> GATE = ENTITIES.register(GATE_ID, () ->
            EntityType.Builder.of(EntityGate::new, MobCategory.MISC)
                    .sized(4.0F, 4.0F)
                    .clientTrackingRange(250)
                    .updateInterval(1)
                    .setShouldReceiveVelocityUpdates(false)
                    .build(AncientWarfareStructure.MOD_ID + ":" + GATE_ID));

    public static final RegistryObject<EntityType<EntitySeat>> SEAT = ENTITIES.register(SEAT_ID, () ->
            EntityType.Builder.of(EntitySeat::new, MobCategory.MISC)
                    .sized(0.01F, 0.01F)
                    .clientTrackingRange(20)
                    .updateInterval(10)
                    .setShouldReceiveVelocityUpdates(false)
                    .build(AncientWarfareStructure.MOD_ID + ":" + SEAT_ID));

    private AWStructureEntities() {}

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }
}
