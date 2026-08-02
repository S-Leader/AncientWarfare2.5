package net.shadowmage.ancientwarfare.core.util;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.entity.AWFakePlayer;
import net.shadowmage.ancientwarfare.core.util.parsing.PropertyState;
import net.shadowmage.ancientwarfare.structure.config.AWStructureStatics;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.function.Function;

public final class BlockTools {
    private BlockTools() {
    }

    public static BlockPos rotateHorizontal(BlockPos offset, int turns) {
        int x = offset.getX();
        int z = offset.getZ();
        for (int i = 0; i < Math.floorMod(turns, 4); i++) {
            int oldX = x;
            x = -z;
            z = oldX;
        }
        return new BlockPos(x, offset.getY(), z);
    }

    public static float rotateFloatX(float x, float z, int turns) {
        for (int i = 0; i < Math.floorMod(turns, 4); i++) {
            float oldX = x;
            x = 1F - z;
            z = oldX;
        }
        return x;
    }

    public static float rotateFloatZ(float x, float z, int turns) {
        for (int i = 0; i < Math.floorMod(turns, 4); i++) {
            float oldX = x;
            x = 1F - z;
            z = oldX;
        }
        return z;
    }

    public static BlockPos getAverageOf(BlockPos... positions) {
        if (positions.length == 0) return BlockPos.ZERO;
        double x = 0, y = 0, z = 0;
        for (BlockPos pos : positions) {
            x += pos.getX();
            y += pos.getY();
            z += pos.getZ();
        }
        return BlockPos.containing(x / positions.length, y / positions.length, z / positions.length);
    }

    @Nullable
    public static BlockPos getBlockClickedOn(Player player, Level world, boolean offset) {
        HitResult hit = player.pick(5D, 1F, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) return null;
        return offset ? blockHit.getBlockPos().relative(blockHit.getDirection()) : blockHit.getBlockPos();
    }

    public static BlockPos rotateAroundOrigin(BlockPos pos, int turns) {
        return rotateHorizontal(pos, turns);
    }

    public static boolean isPositionWithinBounds(BlockPos test, BlockPos pos1, BlockPos pos2) {
        return isPositionWithinHorizontalBounds(test, pos1, pos2) && test.getY() >= pos1.getY() && test.getY() <= pos2.getY();
    }

    public static boolean isPositionWithinHorizontalBounds(BlockPos test, BlockPos min, BlockPos max) {
        return test.getX() >= min.getX() && test.getX() <= max.getX() && test.getZ() >= min.getZ() && test.getZ() <= max.getZ();
    }

    public static BlockPos getMin(BlockPos a, BlockPos b) {
        return new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
    }

    public static BlockPos getMax(BlockPos a, BlockPos b) {
        return new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
    }

    public static BlockPos rotateInArea(BlockPos pos, int xSize, int zSize, int turns) {
        int x = pos.getX();
        int z = pos.getZ();
        for (int i = 0; i < Math.floorMod(turns, 4); i++) {
            int oldX = x;
            x = zSize - 1 - z;
            z = oldX;
            int oldXSize = xSize;
            xSize = zSize;
            zSize = oldXSize;
        }
        return new BlockPos(x, pos.getY(), z);
    }

    /**
     * Modern loot-table based replacement for the removed pre-1.13 Block#getDrops overload.
     * Fortune is supplied through a temporary tool so vanilla and Forge loot modifiers see
     * the same enchantment context as a real harvest.
     */
    public static NonNullList<ItemStack> getDrops(Level world, BlockPos pos, BlockState state, int fortune) {
        NonNullList<ItemStack> drops = NonNullList.create();
        if (!(world instanceof ServerLevel serverLevel)) {
            return drops;
        }

        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        if (fortune > 0) {
            tool.enchant(Enchantments.BLOCK_FORTUNE, fortune);
        }
        drops.addAll(Block.getDrops(state, serverLevel, pos, world.getBlockEntity(pos), AWFakePlayer.get(world), tool));
        return drops;
    }

    public static boolean breakBlockAndDrop(Level world, BlockPos pos) {
        return breakBlock(world, pos, 0, true);
    }

    public static boolean breakBlock(Level world, BlockPos pos, int fortune, boolean doDrop) {
        if (world.isClientSide || world.isEmptyBlock(pos)) return false;
        BlockState state = world.getBlockState(pos);
        if (state.getDestroySpeed(world, pos) < 0 || !canBreakBlock(world, pos, state)) return false;
        Player player = AWFakePlayer.get(world);
        if (doDrop && world instanceof ServerLevel serverLevel) {
            ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
            if (fortune > 0) tool.enchant(Enchantments.BLOCK_FORTUNE, fortune);
            for (ItemStack drop : Block.getDrops(state, serverLevel, pos, world.getBlockEntity(pos), player, tool)) {
                Block.popResource(world, pos, drop);
            }
        }
        return world.removeBlock(pos, false);
    }

    private static boolean canBreakBlock(Level world, BlockPos pos, BlockState state) {
        return !AWCoreStatics.fireBlockBreakEvents || !MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world, pos, state, AWFakePlayer.get(world)));
    }

    public static boolean breakBlockNoDrops(Level world, BlockPos pos, BlockState state) {
        if (!canBreakBlock(world, pos, state) || !world.removeBlock(pos, false)) return false;
        world.levelEvent(2001, pos, Block.getId(state));
        return true;
    }

    public static boolean placeItemBlockRightClick(ItemStack stack, Level world, BlockPos pos) {
        Player player = AWFakePlayer.get(world);
        player.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.setXRot(90F);
        return stack.use(world, player, InteractionHand.MAIN_HAND).getResult().consumesAction();
    }

    public static boolean placeItemBlock(ItemStack stack, Level world, BlockPos pos, Direction face) {
        Player player = AWFakePlayer.get(world);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos against = pos.relative(face.getOpposite());
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(against), face, against, false);
        return stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)).consumesAction();
    }

    public static void notifyBlockUpdate(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        world.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
    }

    public static void notifyBlockUpdate(BlockEntity tile) {
        if (tile.getLevel() != null) notifyBlockUpdate(tile.getLevel(), tile.getBlockPos());
    }

    public static void notifyNeighbors(BlockEntity tile) {
        if (tile.getLevel() != null)
            tile.getLevel().updateNeighborsAt(tile.getBlockPos(), tile.getBlockState().getBlock());
    }

    public static JsonElement serializeToJson(BlockState state) {
        JsonObject result = new JsonObject();
        result.addProperty("name", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
        JsonObject properties = new JsonObject();
        for (Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet()) {
            properties.addProperty(entry.getKey().getName(), serializeValue(entry.getKey(), entry.getValue()));
        }
        if (!properties.entrySet().isEmpty()) result.add("properties", properties);
        return result;
    }

    private static <T extends Comparable<T>> String serializeValue(Property<T> property, Comparable<?> value) {
        return property.getName((T) value);
    }

    public static Direction getHorizontalFacingFromMeta(int meta) {
        Direction direction = Direction.from3DDataValue(meta);
        return direction.getAxis().isHorizontal() ? direction : Direction.NORTH;
    }

    public static <T> T getBlockState(Tuple<String, Map<String, String>> blockProps, Function<Block, T> init, AddPropertyFunction<T> addProperty) {
        if (blockProps.getA().startsWith("minecraft:")) {
            CompoundTag legacyState = new CompoundTag();
            legacyState.putString("blockName", blockProps.getA());
            CompoundTag properties = new CompoundTag();
            blockProps.getB().forEach(properties::putString);
            legacyState.put("properties", properties);
            BlockState normalized = NBTHelper.getBlockState(legacyState);
            T normalizedResult = init.apply(normalized.getBlock());
            for (Map.Entry<Property<?>, Comparable<?>> entry : normalized.getValues().entrySet()) {
                normalizedResult = addProperty.apply(normalizedResult, entry.getKey(), entry.getValue());
            }
            return normalizedResult;
        }
        Block block = RegistryTools.getBlock(blockProps.getA());
        T result = init.apply(block);
        StateDefinition<Block, BlockState> definition = block.getStateDefinition();
        for (Map.Entry<String, String> entry : blockProps.getB().entrySet()) {
            Property<?> property = definition.getProperty(entry.getKey());
            if (property == null) throw missing(block, entry.getKey());
            result = addProperty.apply(result, property, getPropertyState(block, definition, entry.getKey(), entry.getValue()).getValue());
        }
        return result;
    }

    public static <T extends Comparable<T>> BlockState updateProperty(BlockState state, Property<T> property, Comparable<?> value) {
        return state.setValue(property, (T) value);
    }

    public static BlockState rotateFacing(BlockState state, int turns) {
        for (Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet()) {
            IRotator<?> rotator = ROTATORS.get(entry.getKey().getValueClass());
            if (rotator != null) state = rotate(state, entry.getKey(), turns, rotator);
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState rotate(BlockState state, Property<T> property, int turns, IRotator<?> rawRotator) {
        IRotator<T> rotator = (IRotator<T>) rawRotator;
        return state.setValue(property, rotator.rotateY(state.getValue(property), turns));
    }

    private static final Map<Class<?>, IRotator<?>> ROTATORS = new ImmutableMap.Builder<Class<?>, IRotator<?>>()
            .put(Direction.class, (IRotator<Direction>) (direction, turns) -> {
                if (direction.getAxis().isVertical()) return direction;
                for (int i = 0; i < Math.floorMod(turns, 4); i++) direction = direction.getClockWise();
                return direction;
            })
            .put(Direction.Axis.class, (IRotator<Direction.Axis>) (axis, turns) -> axis == Direction.Axis.Y || turns % 2 == 0 ? axis : axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X)
            .put(RailShape.class, (IRotator<RailShape>) BlockTools::rotateRail)
            .build();

    private static RailShape rotateRail(RailShape shape, int turns) {
        for (int i = 0; i < Math.floorMod(turns, 4); i++) {
            shape = switch (shape) {
                case NORTH_SOUTH -> RailShape.EAST_WEST;
                case EAST_WEST -> RailShape.NORTH_SOUTH;
                case ASCENDING_EAST -> RailShape.ASCENDING_SOUTH;
                case ASCENDING_SOUTH -> RailShape.ASCENDING_WEST;
                case ASCENDING_WEST -> RailShape.ASCENDING_NORTH;
                case ASCENDING_NORTH -> RailShape.ASCENDING_EAST;
                case SOUTH_EAST -> RailShape.SOUTH_WEST;
                case SOUTH_WEST -> RailShape.NORTH_WEST;
                case NORTH_WEST -> RailShape.NORTH_EAST;
                case NORTH_EAST -> RailShape.SOUTH_EAST;
            };
        }
        return shape;
    }

    public static PropertyState getPropertyState(Block block, StateDefinition<Block, BlockState> definition, String propertyName, String propertyValue) {
        Property<?> property = definition.getProperty(propertyName);
        if (property == null) throw missing(block, propertyName);
        return getPropertyState(property, propertyName, propertyValue);
    }

    private static MissingResourceException missing(Block block, String property) {
        return new MissingResourceException("Block '" + BuiltInRegistries.BLOCK.getKey(block) + "' has no property '" + property + "'", Property.class.getName(), property);
    }

    private static <T extends Comparable<T>, V extends T> PropertyState<T, V> getPropertyState(Property<T> property, String name, String value) {
        Optional<T> parsed = property.getValue(value);
        if (parsed.isEmpty())
            throw new MissingResourceException("Invalid value '" + value + "' for property '" + name + "'", Property.class.getName(), name);
        return new PropertyState<>(property, (V) parsed.get());
    }

    public static int getTopFilledHeight(Level world, int x, int z, boolean skippables, int maxY) {
        return getTopFilledHeight(world.getChunk(x >> 4, z >> 4), x, z, skippables, maxY);
    }

    public static int getTopFilledHeight(Level world, int x, int z, boolean skippables) {
        return getTopFilledHeight(world.getChunk(x >> 4, z >> 4), x, z, skippables);
    }

    public static int getTopFilledHeight(LevelChunk chunk, int x, int z, boolean skippables) {
        return getTopFilledHeight(chunk, x, z, skippables, chunk.getMaxBuildHeight() - 1);
    }

    private static int getTopFilledHeight(LevelChunk chunk, int x, int z, boolean skippables, int maxY) {
        for (int y = Math.min(maxY, chunk.getMaxBuildHeight() - 1); y >= chunk.getMinBuildHeight(); y--) {
            BlockState state = chunk.getBlockState(new BlockPos(x, y, z));
            if (state.isAir() || skippables && AWStructureStatics.isSkippable(state)) continue;
            return y;
        }
        return -1;
    }

    private interface IRotator<T extends Comparable<T>> {
        T rotateY(T value, int turns);
    }

    public interface AddPropertyFunction<T> {
        T apply(T object, Property<?> property, Comparable<?> value);
    }

    public static Iterable<BlockPos> getAllInBoxTopDown(BlockPos from, BlockPos to) {
        return getAllInBoxTopDown(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()), Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
    }

    public static Iterable<BlockPos> getAllInBoxTopDown(int x1, int y1, int z1, int x2, int y2, int z2) {
        return () -> new AbstractIterator<>() {
            private int x = x1, y = y2, z = z1;
            private boolean first = true;

            @Override
            protected BlockPos computeNext() {
                if (first) {
                    first = false;
                    return new BlockPos(x, y, z);
                }
                if (x == x2 && y == y1 && z == z2) return endOfData();
                if (x < x2) x++;
                else if (z < z2) {
                    x = x1;
                    z++;
                } else {
                    x = x1;
                    z = z1;
                    y--;
                }
                return new BlockPos(x, y, z);
            }
        };
    }
}
