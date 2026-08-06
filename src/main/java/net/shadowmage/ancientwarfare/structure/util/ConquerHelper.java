package net.shadowmage.ancientwarfare.structure.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.TextUtils;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.entity.faction.NpcFaction;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.network.PacketHighlightBlock;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;
import net.shadowmage.ancientwarfare.structure.tile.SpawnerSettings;
import net.shadowmage.ancientwarfare.structure.tile.TileAdvancedSpawner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ConquerHelper {
    /*
     * The old cache used only StructureBB as its key. StructureBB equality is based on
     * coordinates, so structures at the same coordinates in different levels/worlds
     * could reuse each other's conquest result. Keep the level identity in the key.
     */
    private static final Cache<ConquerCacheKey, Boolean> STRUCTURE_BB_CONQUERED = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();

    private ConquerHelper() {
    }

    /**
     * Non-player checks must be read-only. They are used by natural-spawn prevention
     * and must never despawn defenders or remove spawners as a side effect.
     */
    private static boolean checkBBConquered(Level world, StructureBB bb) {
        return checkBBConquered(world, bb, npc -> {
        }, pos -> {
        }, false);
    }

    /**
     * Explicit flag activation may use the configured flee threshold and perform the
     * cleanup only after the structure is successfully claimed.
     */
    public static boolean checkBBConquered(Player player, StructureBB bb) {
        return checkBBConquered(player.level(), bb,
                npc -> markNpcAndMessagePlayer(player, npc),
                pos -> markSpawnerAndMessagePlayer(player, pos),
                true);
    }

    private static void markSpawnerAndMessagePlayer(Player player, BlockPos pos) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToPlayer(serverPlayer,
                    new PacketHighlightBlock(new BlockHighlightInfo(pos, player.level().getGameTime() + 6000)));
        }
        player.displayClientMessage(Component.translatable("gui.ancientwarfarestructure.structure_spawner_present"), true);
    }

    private static void markNpcAndMessagePlayer(Player player, NpcFaction npc) {
        npc.addEffect(new MobEffectInstance(MobEffects.GLOWING, 6000));
        player.displayClientMessage(Component.translatable("gui.ancientwarfarestructure.structure_hostile_alive",
                TextUtils.getSimpleBlockPosString(npc.blockPosition())), true);
    }

    private static boolean checkBBConquered(Level world, StructureBB bb,
                                             Consumer<NpcFaction> onHostileNpcFound,
                                             Consumer<BlockPos> onHostileSpawnerFound,
                                             boolean explicitClaim) {
        if (world.isClientSide) {
            return false;
        }

        AABB boundingBox = bb.getAABB();
        List<NpcFaction> remainingEnemies = new ArrayList<>();
        List<BlockPos> remainingEnemyBlocks = new ArrayList<>();
        int resistanceValue = 0;

        for (NpcFaction factionNpc : world.getEntitiesOfClass(NpcFaction.class, boundingBox)) {
            if (!factionNpc.isAlive() || factionNpc.isRemoved() || factionNpc.isPassive()) {
                continue;
            }

            remainingEnemies.add(factionNpc);
            if (factionNpc.getNpcFullType().contains("leader")) {
                resistanceValue += AWCoreStatics.bossConquerResistance;
            } else if (factionNpc.getNpcFullType().contains("elite")) {
                resistanceValue += AWCoreStatics.eliteConquerResistance;
            } else {
                resistanceValue += AWCoreStatics.normalConquerResistance;
            }
        }

        for (BlockPos blockPos : BlockPos.betweenClosed(bb.min, bb.max)) {
            if (!world.isLoaded(blockPos)) {
                return false;
            }
            if (world.getBlockState(blockPos).getBlock() == AWStructureBlocks.ADVANCED_SPAWNER
                    && WorldTools.getTile(world, blockPos, TileAdvancedSpawner.class)
                    .map(te -> SpawnerSettings.spawnsHostileNpcs(te.getSettings()))
                    .orElse(false)) {
                remainingEnemyBlocks.add(blockPos.immutable());
                resistanceValue += AWCoreStatics.spawnerConquerResistance;
            }
        }

        // Background checks are strict and read-only: any remaining defender/spawner
        // means the structure is not conquered. Only clicking the flag may make a small
        // remaining force flee according to conquer_threshold.
        if (!explicitClaim) {
            return remainingEnemies.isEmpty() && remainingEnemyBlocks.isEmpty();
        }

        int threshold = AWCoreStatics.conquerThreshold;
        // threshold <= 0 means original AW behaviour: any positive resistance blocks
        // claiming, but an actually empty structure must still be claimable.
        boolean claimBlocked = resistanceValue > 0 && (threshold <= 0 || resistanceValue >= threshold);
        if (claimBlocked) {
            remainingEnemies.forEach(onHostileNpcFound);
            remainingEnemyBlocks.forEach(onHostileSpawnerFound);
            return false;
        }

        // Below the configured threshold, the remaining force flees. Do this only for
        // an explicit successful flag claim, never during spawn-prevention checks.
        for (NpcFaction factionNpc : remainingEnemies) {
            factionNpc.discard();
        }
        for (BlockPos blockPos : remainingEnemyBlocks) {
            world.removeBlock(blockPos, false);
        }

        invalidate(world, bb);
        return true;
    }

    public static boolean checkBBNotConquered(Level world, StructureBB bb) {
        ConquerCacheKey key = new ConquerCacheKey(world, bb);
        try {
            return !STRUCTURE_BB_CONQUERED.get(key, () -> checkBBConquered(world, bb));
        } catch (ExecutionException e) {
            AncientWarfareNPC.LOG.error("Error getting conquered structureBB info ", e);
            // Fail closed: an uncertain result must not remove protection.
            return true;
        }
    }

    public static void invalidate(Level world, StructureBB bb) {
        STRUCTURE_BB_CONQUERED.invalidate(new ConquerCacheKey(world, bb));
    }

    private static final class ConquerCacheKey {
        private final Level world;
        private final int minX;
        private final int minY;
        private final int minZ;
        private final int maxX;
        private final int maxY;
        private final int maxZ;

        private ConquerCacheKey(Level world, StructureBB bb) {
            this.world = world;
            minX = bb.min.getX();
            minY = bb.min.getY();
            minZ = bb.min.getZ();
            maxX = bb.max.getX();
            maxY = bb.max.getY();
            maxZ = bb.max.getZ();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ConquerCacheKey other)) {
                return false;
            }
            return world == other.world
                    && minX == other.minX && minY == other.minY && minZ == other.minZ
                    && maxX == other.maxX && maxY == other.maxY && maxZ == other.maxZ;
        }

        @Override
        public int hashCode() {
            return Objects.hash(System.identityHashCode(world), minX, minY, minZ, maxX, maxY, maxZ);
        }
    }
}
