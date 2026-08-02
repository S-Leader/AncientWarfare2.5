package net.shadowmage.ancientwarfare.structure.block;


import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.gui.GuiSpawnerAdvanced;
import net.shadowmage.ancientwarfare.structure.gui.GuiSpawnerAdvancedInventory;
import net.shadowmage.ancientwarfare.structure.tile.SpawnerSettings;
import net.shadowmage.ancientwarfare.structure.tile.TileAdvancedSpawner;

public class BlockAdvancedSpawner extends BlockBaseStructure {
    private static final BooleanProperty TRANSPARENT = BooleanProperty.create("transparent");

    public BlockAdvancedSpawner() {
        super(LegacyMaterial.ROCK, "advanced_spawner");
        setHardness(2.f);
    }

    public void getSubBlocks(CreativeModeTab tab, NonNullList<ItemStack> items) {
        ItemStack stack = new ItemStack(this);
        SpawnerSettings settings = SpawnerSettings.getDefaultSettings();
        CompoundTag defaultTag = new CompoundTag();
        settings.writeToNBT(defaultTag);
        stack.getOrCreateTag().put("spawnerSettings", defaultTag);
        items.add(stack);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, BlockGetter world, BlockPos pos, BlockState state, int fortune) {
        //no drops from spawner
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(TRANSPARENT);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return 0;
    }

    @Override
    public BlockState getActualState(BlockState state, BlockGetter world, BlockPos pos) {
        return super.getActualState(state, world, pos).setValue(TRANSPARENT, WorldTools.getTile(world, pos, TileAdvancedSpawner.class)
                .map(s -> s.getSettings().isTransparent()).orElse(false));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldSideBeRendered(BlockState blockState, BlockGetter world, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext) {
            Entity entity = entityContext.getEntity();
            if (entity != null && entity.isAddedToWorld()) {
                return super.getCollisionShape(state, world, pos, context);
            }
        }
        return Shapes.empty();
    }

    @Override
    public RenderType getBlockLayer() {
        return RenderType.cutout();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity createTileEntity(Level world, BlockState state) {
        return new TileAdvancedSpawner();
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        float hardness = WorldTools.getTile(world, pos, TileAdvancedSpawner.class).map(TileAdvancedSpawner::getBlockHardness).orElse(state.getDestroySpeed(world, pos));
        if (hardness < 0.0F) {
            return 0.0F;
        }
        int divider = ForgeHooks.isCorrectToolForDrops(state, player) ? 30 : 100;
        return player.getDestroySpeed(state) / hardness / (float) divider;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, Level world, BlockPos pos, Player player) {
        return WorldTools.getTile(world, pos, TileAdvancedSpawner.class).map(this::getSpawnerItem).orElse(super.getPickBlock(state, target, world, pos, player));
    }

    private ItemStack getSpawnerItem(TileAdvancedSpawner te) {
        ItemStack item = new ItemStack(this);
        CompoundTag settings = new CompoundTag();
        te.getSettings().writeToNBT(settings);
        item.getOrCreateTag().put("spawnerSettings", settings);
        return item;
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (player.getAbilities().instabuild) {
            if (!world.isClientSide) {
                if (player.isShiftKeyDown()) {
                    AWMenuTypes.open(player, NetworkHandler.GUI_SPAWNER_ADVANCED_BLOCK_INVENTORY, pos);
                } else {
                    AWMenuTypes.open(player, NetworkHandler.GUI_SPAWNER_ADVANCED_BLOCK, pos);
                }
            }
            return true;
        }
        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        super.registerClient();

        NetworkHandler.registerGui(NetworkHandler.GUI_SPAWNER_ADVANCED, GuiSpawnerAdvanced.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_SPAWNER_ADVANCED_BLOCK, GuiSpawnerAdvanced.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_SPAWNER_ADVANCED_INVENTORY, GuiSpawnerAdvancedInventory.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_SPAWNER_ADVANCED_BLOCK_INVENTORY, GuiSpawnerAdvancedInventory.class);

    }
}
