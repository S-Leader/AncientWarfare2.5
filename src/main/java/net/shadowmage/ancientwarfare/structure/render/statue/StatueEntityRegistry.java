package net.shadowmage.ancientwarfare.structure.render.statue;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class StatueEntityRegistry {
    private StatueEntityRegistry() {
    }

    private static final Map<String, StatueEntity> STATUE_ENTITIES;

    static {
        ImmutableMap.Builder<String, StatueEntity> builder = new ImmutableMap.Builder<>();
        builder.put("Zombie", new StatueEntity("Zombie", Zombie::new, () -> new BipedStatueModel<LegacyBipedModel>(new LegacyZombieModel())));
        builder.put("Enderman", new StatueEntity("Enderman", world -> new EnderMan(EntityType.ENDERMAN, world), () -> new BipedStatueModel<LegacyBipedModel>(new LegacyEndermanModel(0f))));
        builder.put("Skeleton", new StatueEntity("Skeleton", world -> new Skeleton(EntityType.SKELETON, world), () -> new BipedStatueModel<LegacyBipedModel>(new LegacySkeletonModel())));
        builder.put("Vex", new StatueEntity("Vex", world -> new Vex(EntityType.VEX, world), VexStatueModel::new));
        builder.put("Zombie Villager", new StatueEntity("Zombie Villager", world -> new ZombieVillager(EntityType.ZOMBIE_VILLAGER, world), () -> new BipedStatueModel<LegacyBipedModel>(new LegacyZombieVillagerModel())));
        builder.put("Elder Guardian", new StatueEntity("Elder Guardian", world -> new ElderGuardian(EntityType.ELDER_GUARDIAN, world), GuardianStatueModel::new));
        builder.put("Guardian", new StatueEntity("Guardian", world -> new Guardian(EntityType.GUARDIAN, world), GuardianStatueModel::new));
        STATUE_ENTITIES = builder.build();
    }

    public static Set<String> getStatueEntityNames() {
        return STATUE_ENTITIES.keySet();
    }

    public static StatueEntity getStatueEntity(String name) {
        return STATUE_ENTITIES.get(name);
    }

    public static class StatueEntity {
        private String name;
        private Function<Level, LivingEntity> instantiateEntity;
        private Supplier<IStatueModel> getStatueModel;

        public StatueEntity(String name, Function<Level, LivingEntity> instantiateEntity, Supplier<IStatueModel> getStatueModel) {
            this.name = name;
            this.instantiateEntity = instantiateEntity;
            this.getStatueModel = getStatueModel;
        }

        public LivingEntity instantiateEntity(Level world) {
            return instantiateEntity.apply(world);
        }

        public IStatueModel getStatueModel() {
            return getStatueModel.get();
        }
    }

}
