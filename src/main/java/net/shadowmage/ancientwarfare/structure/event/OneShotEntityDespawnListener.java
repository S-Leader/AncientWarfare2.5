package net.shadowmage.ancientwarfare.structure.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.npc.entity.faction.NpcFaction;
import net.shadowmage.ancientwarfare.structure.util.CapabilityRespawnData;
import net.shadowmage.ancientwarfare.structure.util.SpawnerHelper;

/**
 * Replaces the removed IWorldEventListener entity-removal callback.
 */
public final class OneShotEntityDespawnListener {
    public static final OneShotEntityDespawnListener INSTANCE = new OneShotEntityDespawnListener();

    private OneShotEntityDespawnListener() {
    }

    @SubscribeEvent
    public void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living) || living.getHealth() <= 0 || living instanceof NpcFaction) {
            return;
        }

        ResourceLocation typeId = ForgeRegistries.ENTITY_TYPES.getKey(living.getType());
        if (typeId != null && "iceandfire".equals(typeId.getNamespace())) {
            return;
        }

        CapabilityRespawnData.get(living)
                .filter(data -> data.canRespawn())
                .ifPresent(data -> SpawnerHelper.createSpawner(data, event.getLevel()));
    }
}
