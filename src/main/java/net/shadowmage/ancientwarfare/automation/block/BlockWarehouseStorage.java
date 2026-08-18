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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.automation.init.AWAutomationBlocks;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelLoader;
import net.shadowmage.ancientwarfare.core.render.model.LegacyStateMapperBase;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

public class BlockWarehouseStorage extends BlockBaseAutomation {
    private static final EnumProperty<Size> SIZE = EnumProperty.create("size", Size.class);

    private final Size fixedSize;

    public BlockWarehouseStorage(String regName) {
        this(regName, null);
    }

    public BlockWarehouseStorage(String regName, Size fixedSize) {
        super(LegacyMaterial.WOOD, regName);
        this.fixedSize = fixedSize;
        setHardness(2.f);
        if (fixedSize != null) {
            registerDefaultState(defaultBlockState().setValue(SIZE, fixedSize));
        }
    }

    public Size getFixedSize() {
        return fixedSize;
    }

    public static EnumProperty<Size> sizeProperty() {
        return SIZE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SIZE);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(SIZE,
                fixedSize == null ? Size.byMetadata(meta) : fixedSize);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return fixedSize == null ? state.getValue(SIZE).getMeta() : 0;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return fixedSize == null
                ? getStateFromMeta(context.getItemInHand().getDamageValue())
                : defaultBlockState().setValue(SIZE, fixedSize);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }


    public void getSubBlocks(CreativeModeTab itemIn, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this));
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        return WorldTools.clickInteractableTileWithHand(world, pos, player, hand);
    }

    @Override
    public int damageDropped(BlockState state) {
        return fixedSize == null ? state.getValue(SIZE).getMeta() : 0;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, BlockGetter level, BlockPos pos, BlockState state, int fortune) {
        Item item = fixedSize == null
                ? AWAutomationBlocks.getWarehouseStorageItem(state.getValue(SIZE))
                : asItem();
        if (item != null) {
            drops.add(new ItemStack(item));
        }
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
            if (meta < 0 || meta >= values().length) {
                return SMALL;
            }
            return values()[meta];
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        final ResourceLocation assetLocation = new ResourceLocation(AncientWarfareCore.MOD_ID, "automation/warehouse_storage");

        LegacyModelLoader.setCustomStateMapper(this, new LegacyStateMapperBase() {
            @Override
            @OnlyIn(Dist.CLIENT)
            protected ModelResourceLocation getModelResourceLocation(BlockState state) {
                return new ModelResourceLocation(assetLocation, getPropertyString(state.getValues()));
            }
        });

        if (fixedSize == null) {
            ModelLoaderHelper.registerItem(this.asItem(), "automation", false,
                    meta -> "size=" + Size.byMetadata(meta).getSerializedName());
        } else {
            // The fixed-id block still uses the original warehouse model.  Register
            // its item directly against that model instead of looking for a new
            // automation/warehouse_storage_<size> model that does not exist.
            ModelLoaderHelper.registerItem(this,
                    new ModelResourceLocation(assetLocation, "size=" + fixedSize.getSerializedName()));
        }
    }
}
