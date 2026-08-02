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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ConquerHelper {
    private static final Cache<StructureBB, Boolean> STRUCTURE_BB_CONQUERED = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();

    private ConquerHelper() {
    }

    private static boolean checkBBConquered(Level world, StructureBB bb) {
        return checkBBConquered(world, bb, npc -> {
        }, pos -> {
        });
    }

    public static boolean checkBBConquered(Player player, StructureBB bb) {
        return checkBBConquered(player.level(), bb, npc -> markNpcAndMessagePlayer(player, npc), pos -> markSpawnerAndMessagePlayer(player, pos));
    }

    private static void markSpawnerAndMessagePlayer(Player player, BlockPos pos) {
        NetworkHandler.sendToPlayer((ServerPlayer) player, new PacketHighlightBlock(new BlockHighlightInfo(pos, player.level().getGameTime() + 6000)));
        player.displayClientMessage(Component.translatable("gui.ancientwarfarestructure.structure_spawner_present"), true);
    }

    private static void markNpcAndMessagePlayer(Player player, NpcFaction npc) {
        npc.addEffect(new MobEffectInstance(MobEffects.GLOWING, 6000));
        player.displayClientMessage(Component.translatable("gui.ancientwarfarestructure.structure_hostile_alive",
                TextUtils.getSimpleBlockPosString(npc.blockPosition())), true);
    }

    private static boolean checkBBConquered(Level world, StructureBB bb, Consumer<NpcFaction> onHostileNpcFound, Consumer<BlockPos> onHostileSpawnerFound) {
        AABB boundingBox = bb.getAABB();
        ArrayList<NpcFaction> remainingEnemies = new ArrayList<>();
        ArrayList<BlockPos> remainingEnemyBlocks = new ArrayList<>();
        int resistanceValue = 0; // A measure of how much resistance is left in unkilled enemy NPCs.
        for (NpcFaction factionNpc : world.getEntitiesOfClass(NpcFaction.class, boundingBox)) {
            if (!factionNpc.isPassive()) {
                onHostileNpcFound.accept(factionNpc);
                remainingEnemies.add(factionNpc);
                //System.out.println("Found NPC with type="+factionNpc.getNpcFullType());
                if (factionNpc.getNpcFullType().contains("leader")) {
                    resistanceValue += AWCoreStatics.bossConquerResistance; // Boss enemies count as 5
                } else if (factionNpc.getNpcFullType().contains("elite")) {
                    resistanceValue += AWCoreStatics.eliteConquerResistance; // Elite enemies count as 2
                } else {
                    resistanceValue += AWCoreStatics.normalConquerResistance;
                }
            }
        }

        for (BlockPos blockPos : BlockPos.betweenClosed(bb.min, bb.max)) {
            if (!world.isLoaded(blockPos)) {
                return false;
            }
            if (world.getBlockState(blockPos).getBlock() == AWStructureBlocks.ADVANCED_SPAWNER &&
                    WorldTools.getTile(world, blockPos, TileAdvancedSpawner.class).map(te -> SpawnerSettings.spawnsHostileNpcs(te.getSettings())).orElse(false)) {
                BlockPos immutablePos = blockPos.immutable();
                onHostileSpawnerFound.accept(immutablePos);
                remainingEnemyBlocks.add(immutablePos);
                resistanceValue += AWCoreStatics.spawnerConquerResistance;
            }
        }

        // If there are 5 or more enemies alive, structure cannot be claimed. Elites count as 2, bosses count as 5
        if (resistanceValue >= AWCoreStatics.conquerThreshold) {
            return false;
        } else {
            // Despawn all remaining enemies (discard() removes the entity without triggering death drops, matching 1.12 setDropItemsWhenDead(false) + setDead())
            for (NpcFaction factionNpc : remainingEnemies) {
                factionNpc.discard();
            }
            // Turn spawners to air
            for (BlockPos blockPos : remainingEnemyBlocks) {
                world.removeBlock(blockPos, false);
            }
            return true;
        }
    }

    public static boolean checkBBNotConquered(Level world, StructureBB bb) {
        try {
            return !STRUCTURE_BB_CONQUERED.get(bb, () -> checkBBConquered(world, bb));
        } catch (ExecutionException e) {
            AncientWarfareNPC.LOG.error("Error getting conquered structureBB info ", e);
            return false;
        }
    }
}
