package net.shadowmage.ancientwarfare.core.util;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.shadowmage.ancientwarfare.core.compat.WorldSavedData;
import net.shadowmage.ancientwarfare.core.interfaces.IInteractableTile;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Predicate;

public final class WorldTools {
    private WorldTools() {
    }

    /**
     * Server-side block-entity query preserving the old inclusive bounds.
     */
    public static List<BlockEntity> getTileEntitiesInArea(Level level, int x1, int y1, int z1, int x2, int y2, int z2) {
        if (!(level instanceof ServerLevel)) {
            return Collections.emptyList();
        }

        BlockPos min = new BlockPos(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2));
        BlockPos max = new BlockPos(Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
        List<BlockEntity> result = Lists.newArrayList();
        for (int chunkX = min.getX() >> 4; chunkX <= max.getX() >> 4; chunkX++) {
            for (int chunkZ = min.getZ() >> 4; chunkZ <= max.getZ() >> 4; chunkZ++) {
                LevelChunk chunk = level.getChunk(chunkX, chunkZ);
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (!blockEntity.isRemoved() && isTileInArea(min, max, blockEntity)) {
                        result.add(blockEntity);
                    }
                }
            }
        }
        return result;
    }

    private static boolean isTileInArea(BlockPos min, BlockPos max, BlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return pos.getX() >= min.getX() && pos.getY() >= min.getY() && pos.getZ() >= min.getZ()
                && pos.getX() <= max.getX() && pos.getY() <= max.getY() && pos.getZ() <= max.getZ();
    }

    public static Optional<IItemHandler> getItemHandlerFromTile(BlockGetter level, BlockPos pos, @Nullable Direction side) {
        return getTile(level, pos, BlockEntity.class)
                .flatMap(blockEntity -> InventoryTools.getItemHandlerFrom(blockEntity, side));
    }

    public static boolean sendClientEventToTile(BlockGetter level, BlockPos pos, int id, int param) {
        return getTile(level, pos).map(blockEntity -> blockEntity.triggerEvent(id, param)).orElse(false);
    }

    public static Optional<BlockEntity> getTile(BlockGetter level, BlockPos pos) {
        return getTile(level, pos, BlockEntity.class);
    }

    public static <T> Optional<T> getTile(@Nullable BlockGetter level, @Nullable BlockPos pos, Class<T> type) {
        if (level == null || pos == null) {
            return Optional.empty();
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null || blockEntity.isRemoved()) {
            return Optional.empty();
        }
        return type.isInstance(blockEntity) ? Optional.of(type.cast(blockEntity)) : Optional.empty();
    }

    public static boolean clickInteractableTileWithHand(Level level, BlockPos pos, Player player, InteractionHand hand) {
        return getTile(level, pos, IInteractableTile.class)
                .map(interactable -> interactable.onBlockClicked(player, hand))
                .orElse(false);
    }

    private static final Map<ResourceLocation, Predicate<Level>> DIMENSION_DAY_TIMES = new HashMap<>();

    public static void registerDimensionDaytimeLogic(ResourceLocation dimension, Predicate<Level> isDayTime) {
        DIMENSION_DAY_TIMES.put(dimension, isDayTime);
    }

    public static boolean isDaytimeInDimension(Level level) {
        return DIMENSION_DAY_TIMES
                .getOrDefault(level.dimension().location(), Level::isDay)
                .test(level);
    }

    public static boolean hasItemHandler(Level level, BlockPos pos) {
        return getTile(level, pos, BlockEntity.class)
                .map(blockEntity -> blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).isPresent())
                .orElse(false);
    }

    /**
     * Compatibility bridge for legacy callers that request arbitrary named saved data.
     * 1.20.1 stores both variants in DimensionDataStorage; global data uses the overworld.
     */
    public static <T extends WorldSavedData> Optional<T> getWorldSavedData(Level level, Class<T> dataClass,
                                                                           String name, boolean perWorldStorage) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        ServerLevel storageLevel = perWorldStorage ? serverLevel : serverLevel.getServer().overworld();
        DimensionDataStorage storage = storageLevel.getDataStorage();
        T data = storage.computeIfAbsent(
                tag -> {
                    T loaded = constructSavedData(dataClass, name);
                    loaded.readFromNBT(tag);
                    return loaded;
                },
                () -> constructSavedData(dataClass, name),
                name);
        return Optional.of(data);
    }

    private static <T extends WorldSavedData> T constructSavedData(Class<T> dataClass, String name) {
        try {
            Constructor<T> constructor = dataClass.getConstructor(String.class);
            return constructor.newInstance(name);
        } catch (ReflectiveOperationException namedFailure) {
            try {
                return dataClass.getConstructor().newInstance();
            } catch (ReflectiveOperationException emptyFailure) {
                throw new IllegalArgumentException("Saved data " + dataClass.getName()
                        + " must expose a public no-arg or String constructor", namedFailure);
            }
        }
    }

    /**
     * Updates the biome quart containing {@code pos}; callers still pass the legacy raw biome value.
     */
    public static void changeBiome(Level level, BlockPos pos, Biome biome) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        LevelChunk chunk = serverLevel.getChunkAt(pos);
        int sectionIndex = chunk.getSectionIndex(pos.getY());
        if (sectionIndex < 0 || sectionIndex >= chunk.getSections().length) {
            return;
        }
        var biomeRegistry = serverLevel.registryAccess().registryOrThrow(Registries.BIOME);
        if (chunk.getSection(sectionIndex).getBiomes() instanceof PalettedContainer<Holder<Biome>> biomes) {
            biomes.set(
                    QuartPos.fromBlock(pos.getX()) & 3,
                    QuartPos.fromBlock(pos.getY()) & 3,
                    QuartPos.fromBlock(pos.getZ()) & 3,
                    biomeRegistry.wrapAsHolder(biome));
            chunk.setUnsaved(true);
        }
    }
}
