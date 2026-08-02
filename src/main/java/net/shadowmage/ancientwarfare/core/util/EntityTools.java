package net.shadowmage.ancientwarfare.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.npc.entity.faction.NpcFaction;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public final class EntityTools {
    private static final String FACTION_NAME_TAG = "factionName";

    private EntityTools() {
    }

    @Nullable
    public static InteractionHand getHandHoldingItem(LivingEntity entity, Item item) {
        if (entity.getMainHandItem().getItem() == item) {
            return InteractionHand.MAIN_HAND;
        } else if (entity.getOffhandItem().getItem() == item) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    public static ItemStack getItemFromEitherHand(Player player, Class<?>... itemClasses) {
        for (Class<?> itemClass : itemClasses) {
            if (itemClass.isInstance(player.getMainHandItem().getItem())) {
                return player.getMainHandItem();
            } else if (itemClass.isInstance(player.getOffhandItem().getItem())) {
                return player.getOffhandItem();
            }
        }
        return ItemStack.EMPTY;
    }

    public static String getUnlocName(String resourceLocation) {
        return getUnlocName(new ResourceLocation(resourceLocation));
    }

    public static String getUnlocName(ResourceLocation registryName) {
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(registryName);
        return entityType == null ? "entity." + registryName.getNamespace() + "." + registryName.getPath() : entityType.getDescriptionId();
    }

    public static <T extends Entity> List<T> getEntitiesWithinBounds(Level level, Class<? extends T> clazz, BlockPos p1, BlockPos p2) {
        AABB bounds = new AABB(p1, p2.offset(1, 1, 1));
        @SuppressWarnings("unchecked")
        List<T> entities = (List<T>) level.getEntitiesOfClass(clazz, bounds);
        return entities;
    }

    public static void spawnEntity(Level level, ResourceLocation entityId, CompoundTag entityNbt, BlockPos pos, String... tags) {
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityId);
        Entity entity = entityType == null ? null : entityType.create(level);
        if (entity == null) {
            return;
        }

        entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.getRandom().nextFloat() * 360.0F, 0.0F);
        if (entity instanceof Mob mob && level instanceof ServerLevel serverLevel) {
            mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.SPAWNER, null, null);
            mob.spawnAnim();
        }

        setDataFromTag(entity, entityNbt);
        if (tags.length > 0) {
            entity.getTags().addAll(Arrays.asList(tags));
        }
        level.addFreshEntity(entity);
        setDataFromTag(entity, entityNbt);
    }

    private static void setDataFromTag(Entity entity, CompoundTag entityNbt) {
        if (entity instanceof NpcFaction factionNpc && entityNbt.contains(FACTION_NAME_TAG)) {
            factionNpc.setFactionNameAndDefaults(entityNbt.getString(FACTION_NAME_TAG));
        }

        CompoundTag merged = entity.saveWithoutId(new CompoundTag());
        for (String key : entityNbt.getAllKeys()) {
            if (entityNbt.get(key) != null) {
                merged.put(key, entityNbt.get(key).copy());
            }
        }
        entity.load(merged);
    }

    @Nullable
    public static Player findClosestPlayer(Level level, BlockPos pos, int maxSqDistance) {
        return level.players().stream()
                .filter(player -> player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) < maxSqDistance)
                .min((first, second) -> Double.compare(first.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()), second.distanceToSqr(pos.getX(), pos.getY(), pos.getZ())))
                .orElse(null);
    }
}
