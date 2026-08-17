package net.shadowmage.ancientwarfare.core.block;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.interfaces.ITickable;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;
import net.shadowmage.ancientwarfare.core.tile.IBlockBreakHandler;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class BlockBase extends Block implements EntityBlock {
    private final ResourceLocation legacyRegistryName;
    @Nullable
    private Supplier<? extends BlockEntityType<?>> blockEntityType;

    public BlockBase(LegacyMaterial material, String modID, String regName) {
        this(material.properties(), modID, regName);
    }

    /**
     * Modern-properties constructor for the few legacy blocks whose shape is
     * block-entity dependent.  In particular gate proxies must opt into
     * dynamicShape(), otherwise 1.20 caches the first collision shape it sees.
     */
    public BlockBase(BlockBehaviour.Properties properties, String modID, String regName) {
        super(properties);
        legacyRegistryName = new ResourceLocation(modID, regName);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            breakBlock(world, pos, state);
            WorldTools.getTile(world, pos, IBlockBreakHandler.class).ifPresent(handler -> handler.onBlockBroken(state));
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }

    public ResourceLocation getLegacyRegistryName() {
        return legacyRegistryName;
    }

    /**
     * Transitional 1.12 registry-name access for shared block code.
     */
    @Deprecated
    public ResourceLocation getRegistryName() {
        return legacyRegistryName;
    }

    /**
     * Modern BlockEntityType binding. Registry classes provide the RegistryObject directly
     * when the block is created; there is no global block-to-type lookup or construction
     * ThreadLocal.
     */
    @SuppressWarnings("unchecked")
    public <T extends BlockBase> T setBlockEntityType(Supplier<? extends BlockEntityType<?>> type) {
        this.blockEntityType = type;
        return (T) this;
    }

    public boolean hasTileEntity(BlockState state) {
        return blockEntityType != null;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (blockEntityType == null) {
            return null;
        }
        BlockEntityType<?> type = blockEntityType.get();
        return type == null ? null : type.create(pos, state);
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            // A block entity can remain in the tick list until the end of the current
            // tick after its block is mined. Never run legacy torque/warehouse logic
            // against that stale instance.
            if (!blockEntity.isRemoved()
                    && blockEntity.getLevel() == tickerLevel
                    && tickerLevel.getBlockEntity(tickerPos) == blockEntity
                    && blockEntity instanceof ITickable tickable) {
                tickable.update();
            }
        };
    }

    public boolean onBlockActivated(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand,
                                    Direction face, float hitX, float hitY, float hitZ) {
        return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        boolean handled = onBlockActivated(level, pos, state, player, hand, hit.getDirection(),
                (float) hit.getLocation().x, (float) hit.getLocation().y, (float) hit.getLocation().z);
        return handled ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
    }

    public void onBlockPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        onBlockPlacedBy(level, pos, state, placer, stack);
    }

    public void breakBlock(Level level, BlockPos pos, BlockState state) {
    }

    public boolean eventReceived(BlockState state, Level level, BlockPos pos, int id, int param) {
        return super.triggerEvent(state, level, pos, id, param);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        return eventReceived(state, level, pos, id, param);
    }

    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState();
    }

    public int getMetaFromState(BlockState state) {
        return 0;
    }

    public BlockState getActualState(BlockState state, BlockGetter level, BlockPos pos) {
        return state;
    }

    public LegacyModelState getLegacyModelState(BlockState state, BlockGetter level, BlockPos pos) {
        return LegacyModelState.of(state);
    }

    public int damageDropped(BlockState state) {
        return 0;
    }

    /* Additional 1.12-shape/drop hooks retained while structure blocks are migrated. */
    @Nullable
    public AABB getCollisionBoundingBox(BlockState state, BlockGetter level, BlockPos pos) {
        return getBoundingBox(state, level, pos);
    }

    public AABB getBoundingBox(BlockState state, BlockGetter level, BlockPos pos) {
        return new AABB(0, 0, 0, 1, 1, 1);
    }

    public AABB getSelectedBoundingBox(BlockState state, Level level, BlockPos pos) {
        return getBoundingBox(state, level, pos).move(pos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        AABB bounds = getBoundingBox(state, level, pos);
        return bounds == null ? Shapes.empty() : Shapes.create(bounds);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        AABB bounds = getCollisionBoundingBox(state, level, pos);
        return bounds == null ? Shapes.empty() : Shapes.create(bounds);
    }

    /**
     * Carry the 1.12 opaque-cube contract into the modern face-culling and light
     * engine.  Block properties are immutable in 1.20, so merely retaining the
     * old isOpaqueCube override left cut-out/model blocks occluding the blocks
     * behind them and produced the familiar x-ray holes.
     */
    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return isOpaqueCube(state) ? getShape(state, level, pos,
                net.minecraft.world.phys.shapes.CollisionContext.empty()) : Shapes.empty();
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return isOpaqueCube(state) ? super.getLightBlock(state, level, pos) : 0;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return !isOpaqueCube(state) || super.propagatesSkylightDown(state, level, pos);
    }

    public boolean isPassable(BlockGetter level, BlockPos pos) {
        return false;
    }

    public boolean canSpawnInBlock() {
        return false;
    }

    public boolean canCollideCheck(BlockState state, boolean hitIfLiquid) {
        return true;
    }

    public void getDrops(NonNullList<ItemStack> drops, BlockGetter level, BlockPos pos, BlockState state, int fortune) {
    }

    @Override
    public java.util.List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder params) {
        //Bridge modern loot-table harvesting to the legacy per-block drop hook when a block overrides it.
        NonNullList<ItemStack> legacyDrops = NonNullList.create();
        net.minecraft.world.phys.Vec3 origin = params.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN);
        BlockPos pos = origin == null ? BlockPos.ZERO : BlockPos.containing(origin);
        ItemStack tool = params.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL);
        int fortune = tool == null ? 0 : net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.BLOCK_FORTUNE, tool);
        getDrops(legacyDrops, params.getLevel(), pos, state, fortune);
        if (!legacyDrops.isEmpty()) {
            return legacyDrops;
        }
        return super.getDrops(state, params);
    }

    //Legacy rotation hook dispatched by ItemHammer and module blocks (1.12 Block#rotateBlock).
    public boolean rotateBlock(Level world, BlockPos pos, Direction axis) {
        return false;
    }

    public void harvestBlock(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
    }

    public ItemStack getPickBlock(BlockState state, HitResult target, Level level, BlockPos pos, Player player) {
        return new ItemStack(this);
    }

    public BlockState getStateForPlacement(Level level, BlockPos pos, Direction face, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        return defaultBlockState();
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LivingEntity placer = context.getPlayer();
        return getStateForPlacement(context.getLevel(), context.getClickedPos(), context.getClickedFace(),
                (float) context.getClickLocation().x, (float) context.getClickLocation().y, (float) context.getClickLocation().z, 0, placer);
    }

    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.entity.Entity entity) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level level, java.util.List<String> tooltip, TooltipFlag flag) {
    }

    public boolean isOpaqueCube(BlockState state) {
        return state.canOcclude();
    }

    public boolean isNormalCube(BlockState state) {
        return state.canOcclude();
    }

    public boolean isFullCube(BlockState state) {
        return state.canOcclude();
    }

    public boolean isSideSolid(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return state.isFaceSturdy(level, pos, side);
    }

    public boolean shouldSideBeRendered(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public RenderType getBlockLayer() {
        return RenderType.solid();
    }

    @OnlyIn(Dist.CLIENT)
    public boolean canRenderInLayer(BlockState state, RenderType layer) {
        return layer == getBlockLayer();
    }

    /**
     * Block properties are immutable in modern Minecraft; retained until every constructor supplies strength directly.
     */
    @Deprecated
    protected BlockBase setHardness(float hardness) {
        return this;
    }

    @Deprecated
    protected BlockBase setResistance(float resistance) {
        return this;
    }

    @Deprecated
    protected BlockBase setLightLevel(float lightLevel) {
        return this;
    }

    @Deprecated
    protected BlockBase setLightOpacity(int opacity) {
        return this;
    }
}
