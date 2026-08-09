package net.shadowmage.ancientwarfare.structure.registry;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.npc.Villager;
import net.shadowmage.ancientwarfare.core.registry.IRegistryDataParser;
import net.shadowmage.ancientwarfare.core.util.JsonUtils;
import net.shadowmage.ancientwarfare.core.util.parsing.JsonHelper;

import java.util.*;

public class EntitySpawnNBTRegistry {
    private EntitySpawnNBTRegistry() {
    }

    private static final Map<Class, Set<String>> entityNBT = new HashMap<>();

    static {
        entityNBT.put(Villager.class, ImmutableSet.of("Offers", "Profession", "ProfessionName", "Career", "CareerLevel"));
        entityNBT.put(Horse.class, Collections.singleton("Variant"));
        entityNBT.put(Mob.class, ImmutableSet.of("HandItems", "HandDropChances", "ArmorItems", "ArmorDropChances", "CustomName"));
        entityNBT.put(Entity.class, Collections.singleton("CustomName"));
    }

    public static CompoundTag getEntitySpawnNBT(Entity entity) {
        CompoundTag ret = new CompoundTag();
        CompoundTag fullEntityNbt = new CompoundTag();
        entity.saveWithoutId(fullEntityNbt);

        for (Map.Entry<Class, Set<String>> entry : entityNBT.entrySet()) {
            if (entry.getKey().isInstance(entity)) {
                for (String tag : entry.getValue()) {
                    if (fullEntityNbt.contains(tag)) {
                        if (fullEntityNbt.get(tag) != null) {
                            ret.put(tag, fullEntityNbt.get(tag).copy());
                        }
                    }
                }
            }
        }
        return ret;
    }

    public static class Parser implements IRegistryDataParser {
        @Override
        public String getName() {
            return "entity_spawn_nbt";
        }

        @Override
        public void parse(JsonObject json) {
            Map<Class, Set<String>> parsed = new HashMap<>();

            JsonHelper.mapFromJson(json, "entity_nbt",
                    entry -> entry.getKey(),
                    entry -> JsonHelper.setFromJson(entry.getValue(), element -> JsonUtils.getString(element, "")
                    )
            ).forEach((className, tags) -> {
                Class<?> clazz = getClass(className);

                if (clazz != null) {
                    parsed.put(clazz, tags);
                }
            });

            entityNBT.putAll(parsed);
        }

        private Class<?> getClass(String className) {
            if ("net.minecraft.entity.passive.EntityRabbit".equals(className)) {
                return Rabbit.class;
            }
            try {
                return Class.forName(
                        className,
                        false,
                        EntitySpawnNBTRegistry.class.getClassLoader()
                );
            } catch (ClassNotFoundException | LinkageError ignore) {
                return null;
            }
        }
    }
}
