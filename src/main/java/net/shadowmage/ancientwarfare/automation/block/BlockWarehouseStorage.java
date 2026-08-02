package net.shadowmage.ancientwarfare.automation.block;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.automation.gui.GuiWarehouseStorage;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.TileWarehouseStorage;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.TileWarehouseStorageLarge;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.TileWarehouseStorageMedium;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelLoader;
import net.shadowmage.ancientwarfare.core.render.model.LegacyStateMapperBase;
import net.shadowmage.ancientwarfare.core.util.LegacyItemStack;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

public class BlockWarehouseStorage extends BlockBaseAutomation {
    private static final EnumProperty<Size> SIZE = EnumProperty.create("size", Size.class);

    public BlockWarehouseStorage(String regName) {
        super(LegacyMaterial.WOOD, regName);
        setHardness(2.f);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SIZE);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(SIZE, Size.byMetadata(meta));
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(SIZE).getMeta();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getStateFromMeta(context.getItemInHand().getDamageValue());
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity createTileEntity(Level world, BlockState state) {
        switch (state.getValue(SIZE)) {
            case MEDIUM:
                return new TileWarehouseStorageMedium();
            case LARGE:
                return new TileWarehouseStorageLarge();
            default:
                return new TileWarehouseStorage();
        }
    }

    public void getSubBlocks(CreativeModeTab itemIn, NonNullList<ItemStack> items) {
        items.add(LegacyItemStack.of(this, 1, 0));
        items.add(LegacyItemStack.of(this, 1, 1));
        items.add(LegacyItemStack.of(this, 1, 2));
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        return WorldTools.clickInteractableTileWithHand(world, pos, player, hand);
    }

    @Override
    public int damageDropped(BlockState state) {
        return state.getValue(SIZE).getMeta();
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, BlockGetter level, BlockPos pos, BlockState state, int fortune) {
        // The generic self-drop loot table cannot preserve AW's legacy size metadata.
        drops.add(LegacyItemStack.of(this, 1, damageDropped(state)));
    }

    @Override
    public boolean eventReceived(BlockState state, Level world, BlockPos pos, int id, int param) {
        super.eventReceived(state, world, pos, id, param);
        return WorldTools.sendClientEventToTile(world, pos, id, param);
    }

    public enum Size implements StringRepresentable {
        SMALL(0), MEDIUM(1), LARGE(2);

        private int meta;

        Size(int meta) {
            this.meta = meta;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }

        public int getMeta() {
            return meta;
        }

        public static Size byMetadata(int meta) {
            return values()[meta];
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        final ResourceLocation assetLocation = new ResourceLocation(AncientWarfareCore.MOD_ID, "automation/" + getRegistryName().getPath());

        LegacyModelLoader.setCustomStateMapper(this, new LegacyStateMapperBase() {
            @Override
            @OnlyIn(Dist.CLIENT)
            protected ModelResourceLocation getModelResourceLocation(BlockState state) {
                return new ModelResourceLocation(assetLocation, getPropertyString(state.getValues()));
            }
        });

        ModelLoaderHelper.registerItem(this.asItem(), "automation", false, meta -> "size=" + Size.values()[meta].name().toLowerCase());

        NetworkHandler.registerGui(NetworkHandler.GUI_WAREHOUSE_STORAGE, GuiWarehouseStorage.class);
    }
}
