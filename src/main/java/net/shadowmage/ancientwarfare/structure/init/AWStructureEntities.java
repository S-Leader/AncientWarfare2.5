package net.shadowmage.ancientwarfare.structure.init;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.shadowmage.ancientwarfare.core.entity.AWEntityRegistry;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.entity.EntityGate;
import net.shadowmage.ancientwarfare.structure.entity.EntitySeat;

/**
 * EntityType declarations for gates and seat anchors.
 */
public final class AWStructureEntities {
    private static boolean prepared;

    private AWStructureEntities() {
    }

    public static synchronized void register(IEventBus modBus) {
        if (!prepared) {
            AWEntityRegistry.registerEntity(new Declaration(EntityGate.class, AWEntityRegistry.AW_GATES, 0, 4.0F, 4.0F, 250, 3));
            AWEntityRegistry.registerEntity(new Declaration(EntitySeat.class, AWEntityRegistry.SEAT, 1, 0.01F, 0.01F, 20, 10));
            prepared = true;
        }
        AWEntityRegistry.attachRegisterListener(modBus, AncientWarfareStructure.MOD_ID);
    }

    private static final class Declaration extends AWEntityRegistry.EntityDeclaration {
        private final float width;
        private final float height;
        private final int range;
        private final int interval;

        private Declaration(Class<? extends Entity> entityClass, String name, int legacyId,
                            float width, float height, int range, int interval) {
            super(entityClass, name, legacyId, AncientWarfareStructure.MOD_ID);
            this.width = width;
            this.height = height;
            this.range = range;
            this.interval = interval;
        }

        @Override
        public Object mod() {
            return AncientWarfareStructure.class;
        }

        @Override
        public int trackingRange() {
            return range;
        }

        @Override
        public int updateFrequency() {
            return interval;
        }

        @Override
        public boolean sendsVelocityUpdates() {
            return false;
        }

        @Override
        public float width() {
            return width;
        }

        @Override
        public float height() {
            return height;
        }
    }
}
