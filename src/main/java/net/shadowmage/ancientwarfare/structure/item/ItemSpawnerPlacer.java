package net.shadowmage.ancientwarfare.structure.item;


import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.input.IItemKeyInterface;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.EntityTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.registry.EntitySpawnNBTRegistry;
import net.shadowmage.ancientwarfare.structure.tile.SpawnerSettings;
import net.shadowmage.ancientwarfare.structure.tile.TileAdvancedSpawner;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ItemSpawnerPlacer extends ItemBaseStructure implements IItemKeyInterface {
    private static final String SPAWNER_DATA_TAG = "spawnerData";

    public ItemSpawnerPlacer(String name) {
        super(name);
        setMaxStackSize(1);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level world, List<String> tooltip, TooltipFlag flag) {
        tooltip.add(I18n.get("guistrings.selected_mob") + ":");
        if (stack.hasTag() && hasSpawnerData(stack)) {
            SpawnerSettings settings = new SpawnerSettings();
            settings.readFromNBT(getSpawnerData(stack));
            if (!settings.getSpawnGroups().isEmpty() && !settings.getSpawnGroups().get(0).getEntitiesToSpawn().isEmpty()) {
                tooltip.add(I18n.get(settings.getSpawnGroups().get(0).getEntitiesToSpawn().get(0).getEntityName()));
            }
        } else {
            tooltip.add(I18n.get("guistrings.no_selection"));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.level().isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        if (stack.isEmpty()) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }
        Optional<BlockPos> placementPosition = getPlacementPosition(world, player);
        if (placementPosition.isPresent()) {
            if (hasSpawnerData(stack)) {
                placeSpawner(player, stack, placementPosition.get());
            } else {
                player.sendSystemMessage(Component.translatable("guistrings.spawner.nodata"));
            }
        } else {
            player.sendSystemMessage(Component.translatable("guistrings.spawner.noblock"));
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    private void placeSpawner(Player player, ItemStack stack, BlockPos placePos) {
        if (player.level().setBlock(placePos, AWStructureBlocks.ADVANCED_SPAWNER.defaultBlockState(), 3)) {
            WorldTools.getTile(player.level(), placePos, TileAdvancedSpawner.class)
                    .ifPresent(t -> {
                        SpawnerSettings settings = new SpawnerSettings();
                        settings.readFromNBT(getSpawnerData(stack));
                        t.setSettings(settings);
                    });
        }
    }

    public static CompoundTag getSpawnerData(ItemStack stack) {
        //noinspection ConstantConditions
        return stack.getTag().getCompound(SPAWNER_DATA_TAG);
    }

    public static boolean hasSpawnerData(ItemStack stack) {
        //noinspection ConstantConditions
        return stack.hasTag() && stack.getTag().contains(SPAWNER_DATA_TAG);
    }

    private Optional<BlockPos> getPlacementPosition(Level world, Player player) {
        BlockHitResult traceResult = getPlayerPOVHitResult(player.level(), player, player.isShiftKeyDown() ? ClipContext.Fluid.NONE : ClipContext.Fluid.SOURCE_ONLY);

        if (traceResult.getType() != HitResult.Type.BLOCK) {
            return Optional.empty();
        }

        BlockPos placementPos = traceResult.getBlockPos().relative(traceResult.getDirection());
        if (!world.getBlockState(placementPos).canBeReplaced()) {
            Direction offset;
            if (traceResult.getDirection().getAxis().isHorizontal()) {
                offset = player.getXRot() < 0 ? Direction.DOWN : Direction.UP;
            } else {
                offset = player.getDirection().getOpposite();
            }
            placementPos = placementPos.relative(offset);
            if (!world.getBlockState(placementPos).canBeReplaced()) {
                return Optional.empty();
            }
        }
        return Optional.of(placementPos);
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack spawnerPlacer = EntityTools.getItemFromEitherHand(player, ItemSpawnerPlacer.class);
        if (spawnerPlacer.isEmpty()) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (player.level().isClientSide) {
            return;
        }

        Entity entity = event.getTarget();

        SpawnerSettings settings = new SpawnerSettings();
        SpawnerSettings.EntitySpawnGroup group = new SpawnerSettings.EntitySpawnGroup(settings);
        SpawnerSettings.EntitySpawnSettings spawnSettings = new SpawnerSettings.EntitySpawnSettings(group);
        if (hasSpawnerData(spawnerPlacer)) {
            settings.readFromNBT(getSpawnerData(spawnerPlacer));
            spawnSettings = getFirstEntitySpawnSettings(settings);
        } else {
            setDefaultEntitySpawnSettings(spawnSettings);
            group.addSpawnSetting(spawnSettings);
            settings.addSpawnGroup(group);
            settings.setSpawnDelay(0);
            settings.setMinDelay(10);
            settings.setMaxDelay(10);
            settings.setSpawnRange(0);
            settings.setPlayerRange(16);
            settings.toggleTransparent();
            settings.setSpawnYOffset(0);
        }

        spawnSettings.setEntityToSpawn(entity);
        spawnSettings.setCustomSpawnTag(EntitySpawnNBTRegistry.getEntitySpawnNBT(entity));
        setSpawnerData(spawnerPlacer, settings);
        entity.discard();

        event.getEntity().sendSystemMessage(Component.translatable("guistrings.spawner.entity_set", entity.getName()));
    }

    private void setDefaultEntitySpawnSettings(SpawnerSettings.EntitySpawnSettings spawnSettings) {
        spawnSettings.setSpawnLimitTotal(1);
        spawnSettings.setSpawnCountMin(1);
        spawnSettings.setSpawnCountMax(1);
    }

    private SpawnerSettings.EntitySpawnSettings getFirstEntitySpawnSettings(SpawnerSettings settings) {
        SpawnerSettings.EntitySpawnSettings spawnSettings;
        SpawnerSettings.EntitySpawnGroup group;
        if (settings.getSpawnGroups().isEmpty()) {
            group = new SpawnerSettings.EntitySpawnGroup(settings);
            settings.addSpawnGroup(group);
        } else {
            group = settings.getSpawnGroups().iterator().next();
        }

        if (group.getEntitiesToSpawn().isEmpty()) {
            spawnSettings = new SpawnerSettings.EntitySpawnSettings(group);
            setDefaultEntitySpawnSettings(spawnSettings);
            group.addSpawnSetting(spawnSettings);
        } else {
            spawnSettings = group.getEntitiesToSpawn().iterator().next();
        }
        return spawnSettings;
    }

    private static void setSpawnerData(ItemStack spawnerPlacer, SpawnerSettings settings) {
        setSpawnerData(spawnerPlacer, settings.writeToNBT(new CompoundTag()));
    }

    public static void setSpawnerData(ItemStack spawnerPlacer, CompoundTag settingsNbt) {
        spawnerPlacer.getOrCreateTag().put(SPAWNER_DATA_TAG, settingsNbt);
    }

    @Override
    public boolean onKeyActionClient(Player player, ItemStack stack, ItemAltFunction altFunction) {
        return altFunction == ItemAltFunction.ALT_FUNCTION_1;
    }

    @Override
    public void onKeyAction(Player player, ItemStack stack, ItemAltFunction altFunction) {
        if (!hasSpawnerData(stack)) {
            player.sendSystemMessage(Component.literal("Must have an entity set first!"));
            return;
        }

        if (player.isShiftKeyDown()) {
            AWMenuTypes.open(player, NetworkHandler.GUI_SPAWNER_ADVANCED_INVENTORY, 0, 0, 0);
        } else {
            AWMenuTypes.open(player, NetworkHandler.GUI_SPAWNER_ADVANCED, 0, 0, 0);
        }
    }
}
