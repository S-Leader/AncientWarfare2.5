package net.shadowmage.ancientwarfare.vehicle.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.vehicle.AncientWarfareVehicles;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;
import net.shadowmage.ancientwarfare.vehicle.missiles.MissileBase;

/** Native Forge registration for vehicle entities. */
public final class AWVehicleEntities {
    public static final String VEHICLE_ID = "vehicle";
    public static final String MISSILE_ID = "missile";

    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AncientWarfareVehicles.MOD_ID);

    public static final RegistryObject<EntityType<VehicleBase>> VEHICLE = ENTITIES.register(VEHICLE_ID, () ->
            EntityType.Builder.of(VehicleBase::new, MobCategory.MISC)
                    .sized(2.5F, 2.0F)
                    .clientTrackingRange(120)
                    .updateInterval(3)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(AncientWarfareVehicles.MOD_ID + ":" + VEHICLE_ID));

    public static final RegistryObject<EntityType<MissileBase>> MISSILE = ENTITIES.register(MISSILE_ID, () ->
            EntityType.Builder.of(MissileBase::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(120)
                    .updateInterval(3)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(AncientWarfareVehicles.MOD_ID + ":" + MISSILE_ID));

    private AWVehicleEntities() {}

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }
}
