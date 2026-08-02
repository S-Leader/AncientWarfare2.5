package net.shadowmage.ancientwarfare.core.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegisterEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Allows legacy no-argument AW block entities to be constructed by modern
 * BlockEntityType factories while the individual tile classes are migrated.
 */
public final class LegacyBlockEntityRegistry {
    private static final Map<Block, BlockEntityType<?>> TYPES_BY_BLOCK = new IdentityHashMap<>();
    private static final Map<BlockEntityType<?>, Set<Class<? extends BlockEntity>>> CLASSES_BY_TYPE = new LinkedHashMap<>();
    private static final ThreadLocal<CreationContext> CREATION_CONTEXT = new ThreadLocal<>();

    private LegacyBlockEntityRegistry() {
    }

    public static <T extends BlockEntity> BlockEntityType<T> register(
            RegisterEvent.RegisterHelper<BlockEntityType<?>> helper,
            ResourceLocation id,
            Supplier<T> legacyFactory,
            Block... validBlocks) {
        return registerStateAware(helper, id, ignored -> legacyFactory.get(), validBlocks);
    }

    /**
     * Registers a legacy no-argument block entity whose concrete implementation
     * depends on the placed block state (for example warehouse size or torque tier).
     */
    public static <T extends BlockEntity> BlockEntityType<T> registerStateAware(
            RegisterEvent.RegisterHelper<BlockEntityType<?>> helper,
            ResourceLocation id,
            Function<BlockState, T> legacyFactory,
            Block... validBlocks) {
        AtomicReference<BlockEntityType<T>> typeRef = new AtomicReference<>();
        BlockEntityType<T> type = BlockEntityType.Builder.of((pos, state) -> {
            CreationContext previous = CREATION_CONTEXT.get();
            CREATION_CONTEXT.set(new CreationContext(typeRef.get(), pos, state));
            try {
                T blockEntity = legacyFactory.apply(state);
                rememberClass(typeRef.get(), blockEntity);
                return blockEntity;
            } finally {
                if (previous == null) {
                    CREATION_CONTEXT.remove();
                } else {
                    CREATION_CONTEXT.set(previous);
                }
            }
        }, validBlocks).build(null);
        typeRef.set(type);
        helper.register(id, type);
        for (Block block : validBlocks) {
            TYPES_BY_BLOCK.put(block, type);
            // Renderer registration happens before any block is placed. Construct a
            // representative instance now so legacy class-based renderer bindings can
            // be resolved to the real BlockEntityType.
            CreationContext previous = CREATION_CONTEXT.get();
            BlockState sampleState = block.defaultBlockState();
            CREATION_CONTEXT.set(new CreationContext(type, BlockPos.ZERO, sampleState));
            try {
                rememberClass(type, legacyFactory.apply(sampleState));
            } catch (RuntimeException ignored) {
                // Some legacy constructors still require runtime-only data. Their class
                // will be recorded on first real construction instead.
            } finally {
                if (previous == null) {
                    CREATION_CONTEXT.remove();
                } else {
                    CREATION_CONTEXT.set(previous);
                }
            }
        }
        return type;
    }

    private static synchronized void rememberClass(BlockEntityType<?> type, @Nullable BlockEntity blockEntity) {
        if (type != null && blockEntity != null) {
            CLASSES_BY_TYPE.computeIfAbsent(type, ignored -> new LinkedHashSet<>()).add(blockEntity.getClass());
        }
    }

    /**
     * Resolves an old tile implementation class to every compatible registered type.
     */
    public static synchronized List<BlockEntityType<?>> getTypesForClass(Class<? extends BlockEntity> tileClass) {
        return CLASSES_BY_TYPE.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(tileClass::isAssignableFrom))
                .map(Map.Entry::getKey)
                .toList();
    }

    @Nullable
    public static BlockEntityType<?> getType(Block block) {
        return TYPES_BY_BLOCK.get(block);
    }

    public static BlockEntityType<?> currentType() {
        return requireContext().type();
    }

    public static BlockPos currentPos() {
        return requireContext().pos();
    }

    public static BlockState currentState() {
        return requireContext().state();
    }

    private static CreationContext requireContext() {
        CreationContext context = CREATION_CONTEXT.get();
        if (context == null) {
            throw new IllegalStateException("Legacy block entity constructed outside its BlockEntityType factory");
        }
        return context;
    }

    private record CreationContext(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    }
}
