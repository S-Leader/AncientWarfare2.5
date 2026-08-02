package net.shadowmage.ancientwarfare.structure.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;

import java.util.Optional;

/**
 * Forge 1.20.1 capability registration and NBT codec for one-shot respawns.
 */
public final class CapabilityRespawnData {
    private CapabilityRespawnData() {
    }

    private static final String RESPAWN_POS_TAG = "respawnPos";
    private static final String SPAWNER_SETTINGS_TAG = "spawnerSettings";
    private static final String SPAWN_TIME_TAG = "spawnTime";

    public static final Capability<IRespawnData> RESPAWN_DATA_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IRespawnData.class);
    }

    public static void onAttach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            RespawnDataCapabilityProvider provider = new RespawnDataCapabilityProvider();
            event.addCapability(new ResourceLocation(AncientWarfareStructure.MOD_ID, "respawn_data"), provider);
            event.addListener(provider::invalidate);
        }
    }

    public static Optional<IRespawnData> get(Entity entity) {
        return entity.getCapability(RESPAWN_DATA_CAPABILITY).resolve();
    }

    static CompoundTag serialize(IRespawnData instance) {
        CompoundTag tag = new CompoundTag();
        if (instance.canRespawn()) {
            tag.putLong(RESPAWN_POS_TAG, instance.getRespawnPos().asLong());
            tag.put(SPAWNER_SETTINGS_TAG, instance.getSpawnerSettings().copy());
            tag.putLong(SPAWN_TIME_TAG, instance.getSpawnTime());
        }
        return tag;
    }

    static void deserialize(IRespawnData instance, CompoundTag tag) {
        if (tag.contains(RESPAWN_POS_TAG)) {
            instance.setRespawnPos(net.minecraft.core.BlockPos.of(tag.getLong(RESPAWN_POS_TAG)));
            instance.setSpawnerSettings(tag.getCompound(SPAWNER_SETTINGS_TAG));
            instance.setSpawnTime(tag.getLong(SPAWN_TIME_TAG));
        }
    }
}
