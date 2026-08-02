package net.shadowmage.ancientwarfare.structure.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.EntityTools;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.structure.tile.ISpecialLootContainer;
import net.shadowmage.ancientwarfare.structure.tile.LootSettings;

import javax.annotation.Nullable;
import java.util.Random;

import static net.shadowmage.ancientwarfare.npc.event.EventHandler.NO_SPAWN_PREVENTION_TAG;

public class LootHelper {
    private LootHelper() {
    }

    public static <T extends BlockEntity & ISpecialLootContainer> boolean fillWithLootAndCheckIfGoodToOpen(T te, @Nullable Player player) {
        return processLootAndCheckIfGoodToOpen(te, player, new ILootTableProcessor() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public <U extends BlockEntity & ISpecialLootContainer> void processLootTable(U te,
                                                                                         @Nullable Player player, LootSettings lootSettings, ResourceLocation lootTable) {
                InventoryTools.getItemHandlerFrom(te, null).ifPresent(handler ->
                        InventoryTools.generateLootFor(te.getWorld(), player, handler,
                                new Random(te.getWorld().getRandom().nextLong()), lootTable, lootSettings.getLootRolls()));
            }
        });
    }

    public static <T extends BlockEntity & ISpecialLootContainer> void dropLoot(T te, @Nullable Player player) {
        processLootAndCheckIfGoodToOpen(te, player, new ILootTableProcessor() {
            @Override
            public <U extends BlockEntity & ISpecialLootContainer> void processLootTable(U te,
                                                                                         @Nullable Player player, LootSettings lootSettings, ResourceLocation lootTable) {
                for (int roll = 0; roll < lootSettings.getLootRolls(); roll++) {
                    InventoryTools.dropItemsInWorld(te.getWorld(), InventoryTools.getLootStacks(te.getWorld(), player, new Random(te.getWorld().getRandom().nextLong()), lootTable), te.getPos().offset(getPlayerOffset(te, player)));
                }
            }
        });
    }

    private static Vec3i getPlayerOffset(BlockEntity te, @Nullable Player player) {
        if (player == null) {
            return new Vec3i(0, 0, 0);
        }
        Vec3i playerVector = player.blockPosition().subtract(te.getBlockPos());

        int offsetX = Math.max(Math.min(playerVector.getX(), 1), -1);
        int offsetY = Math.max(Math.min(playerVector.getY(), 1), 0);
        int offsetZ = Math.max(Math.min(playerVector.getZ(), 1), -1);

        return new Vec3i(offsetX, offsetY, offsetZ);
    }

    public static <T extends BlockEntity & ISpecialLootContainer> boolean processLootAndCheckIfGoodToOpen(T te,
                                                                                                          @Nullable Player player, ILootTableProcessor lootTableProcessor) {
        LootSettings lootSettings = te.getLootSettings();
        boolean goodToOpen = true;
        if (!te.getWorld().isClientSide) {
            if (lootSettings.getSplashPotion()) {
                lootSettings.setSplashPotion(false);
                ItemStack potion = new ItemStack(Items.SPLASH_POTION);
                PotionUtils.setCustomEffects(potion, lootSettings.getEffects());
                BlockPos startPos = te.getPos().offset(getPlayerOffset(te, player));
                ThrownPotion potionEntity = new ThrownPotion(te.getWorld(), startPos.getX() + 0.5,
                        startPos.getY() + 0.5, startPos.getZ() + 0.5);
                potionEntity.setItem(potion);
                Vec3 playerPos = player == null ? new Vec3(startPos.getX() + 0.5, startPos.getY() + 1.5, startPos.getZ() + 0.5) :
                        new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());

                potionEntity.shoot(playerPos.x - (startPos.getX() + 0.5), playerPos.y - (startPos.getY() + 0.5), playerPos.z - (startPos.getZ() + 0.5), 0.5F, 1.0F);
                te.getWorld().addFreshEntity(potionEntity);
                goodToOpen = false;
            }
            if (lootSettings.getSpawnEntity()) {
                lootSettings.setSpawnEntity(false);
                EntityTools.spawnEntity(te.getWorld(), lootSettings.getEntity(), lootSettings.getEntityNBT(), te.getPos().offset(getPlayerOffset(te, player)), NO_SPAWN_PREVENTION_TAG);
                goodToOpen = false;
            }
            if (lootSettings.hasLoot()) {
                lootSettings.setHasLoot(false);
                lootSettings.getLootTableName().ifPresent(lootTable -> {
                    lootTableProcessor.processLootTable(te, player, lootSettings, lootTable);
                    BlockTools.notifyBlockUpdate(te);
                });
                lootSettings.removeLoot();
            }
            if (lootSettings.hasMessage() && player != null) {
                lootSettings.setHasMessage(false);
                player.sendSystemMessage(Component.translatable(lootSettings.getPlayerMessage()));
            }
        }
        return goodToOpen;
    }

    public interface ILootTableProcessor {
        <T extends BlockEntity & ISpecialLootContainer> void processLootTable(T te,
                                                                              @Nullable Player player, LootSettings lootSettings, ResourceLocation lootTable);
    }
}
