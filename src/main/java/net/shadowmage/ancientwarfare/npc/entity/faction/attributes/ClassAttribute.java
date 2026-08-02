package net.shadowmage.ancientwarfare.npc.entity.faction.attributes;

import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.monster.Spider;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;

import java.util.Map;
import java.util.Optional;

public class ClassAttribute extends BaseAttribute<Class> {
    private static final Map<String, Class<?>> LEGACY_ENTITY_CLASSES = Map.of(
            "net.minecraft.entity.passive.EntityHorse", Horse.class,
            "net.minecraft.entity.passive.EntitySkeletonHorse", SkeletonHorse.class,
            "net.minecraft.entity.passive.EntityZombieHorse", ZombieHorse.class,
            "net.minecraft.entity.passive.EntityLlama", Llama.class,
            "net.minecraft.entity.passive.EntityChicken", Chicken.class,
            "net.minecraft.entity.passive.EntityPig", Pig.class,
            "net.minecraft.entity.monster.EntityPolarBear", PolarBear.class,
            "net.minecraft.entity.monster.EntitySpider", Spider.class);
    private Class baseClass;

    public ClassAttribute(String name, Class baseClass) {
        super(name);
        this.baseClass = baseClass;
    }

    @Override
    public Class<Class> getValueClass() {
        return Class.class;
    }

    @Override
    public Optional<Class> parseValue(String value) {
        Class clazz = LEGACY_ENTITY_CLASSES.get(value);
        try {
            if (clazz == null) {
                clazz = Class.forName(value);
            }
        } catch (ClassNotFoundException e) {
            AncientWarfareNPC.LOG.error("Horse entity class was not found for: " + value);
            return Optional.empty();
        }

        if (!baseClass.isAssignableFrom(clazz)) {
            return Optional.empty();
        }

        return Optional.of(clazz);
    }
}
