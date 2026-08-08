package net.shadowmage.ancientwarfare.structure.block;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelLoader;
import net.shadowmage.ancientwarfare.core.render.model.LegacyStateMapperBase;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.NBTBuilder;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.item.ItemBlockTotemPart;
import net.shadowmage.ancientwarfare.structure.tile.TileTotemPart;

import javax.annotation.Nullable;
import java.util.*;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;

public class BlockTotemPart extends BlockBaseStructure {
    public static final String VARIANT_TAG = "variant";
    public static final EnumProperty<Variant> VARIANT = EnumProperty.create(VARIANT_TAG, Variant.class);
    public static final BooleanProperty VISIBLE = BooleanProperty.create("visible");

    public BlockTotemPart() {
        super(LegacyMaterial.WOOD, "totem_part");
        // In 1.12 the model got VARIANT from getActualState(). 1.20 chunk models
        // are baked from the state stored in the world, so every visual property
        // must also have a real default value.
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(VISIBLE, true)
                .setValue(VARIANT, Variant.BASE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VISIBLE, VARIANT);
    }

    public void getSubBlocks(CreativeModeTab itemIn, NonNullList<ItemStack> items) {
        for (Variant variant : Variant.values()) {
            if (!variant.includeInSubBlocks()) {
                continue;
            }
            items.add(getVariantStack(variant));
        }
    }

    private ItemStack getVariantStack(Variant variant) {
        Item item = this.asItem();
        ItemStack stack = new ItemStack(item);
        stack.setTag(new NBTBuilder().setByte(VARIANT_TAG, variant.getId()).build());
        return stack;
    }

    @Override
    public void harvestBlock(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);

        InventoryTools.dropItemInWorld(world, getVariantStack((te instanceof TileTotemPart ? ((TileTotemPart) te).getDropVariant() : Variant.BASE)), pos);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, BlockGetter world, BlockPos pos, BlockState state, int fortune) {
        //drops handled in harvest block because access to tile entity is needed
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, Level world, BlockPos pos, Player player) {
        return getVariantStack(WorldTools.getTile(world, pos, TileTotemPart.class).map(TileTotemPart::getDropVariant).orElse(Variant.BASE));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(VISIBLE) ? super.getRenderShape(state) : RenderShape.INVISIBLE;
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState()
                .setValue(FACING, Direction.from2DDataValue(meta))
                .setValue(VISIBLE, ((meta >> 2) & 1) > 0)
                .setValue(VARIANT, Variant.BASE);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(FACING).get2DDataValue() | (state.getValue(VISIBLE) ? 1 : 0) << 2;
    }

    @Override
    public BlockState getActualState(BlockState state, BlockGetter world, BlockPos pos) {
        return WorldTools.getTile(world, pos, TileTotemPart.class)
                .map(te -> withVariant(state, te.getVariant()))
                .orElse(state);
    }

    /**
     * Mirrors the tile's legacy variant into the real blockstate. Modern chunk
     * rendering never calls the old 1.12 getActualState hook.
     */
    public static BlockState withVariant(BlockState state, Variant variant) {
        if (!state.hasProperty(VARIANT)) {
            return state;
        }
        return state.setValue(VARIANT, variant == null ? Variant.BASE : variant);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity createTileEntity(Level world, BlockState state) {
        return new TileTotemPart();
    }

    @Override
    public BlockState getStateForPlacement(Level worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        return defaultBlockState()
                .setValue(FACING, placer.getDirection())
                .setValue(VISIBLE, true)
                .setValue(VARIANT, Variant.BASE);
    }

    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        Variant variant = ItemBlockTotemPart.getVariant(stack);
        WorldTools.getTile(world, pos, TileTotemPart.class).ifPresent(te -> te.setVariant(variant));
        variant.placeAdditionalParts(world, pos, placer.getDirection());
    }

    @Override
    public RenderType getBlockLayer() {
        return RenderType.cutout();
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(BlockState state) {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        //noinspection ConstantConditions
        ResourceLocation baseLocation = new ResourceLocation(AncientWarfareCore.MOD_ID, "structure/" + getRegistryName().getPath());

        LegacyModelLoader.setCustomStateMapper(this, new LegacyStateMapperBase() {
            @Override
            @OnlyIn(Dist.CLIENT)
            protected ModelResourceLocation getModelResourceLocation(BlockState state) {
                return new ModelResourceLocation(baseLocation, getPropertyString(state.getValues()));
            }
        });

        String modelPropString = "facing=west,variant=%s,visible=true";

        LegacyModelLoader.setCustomMeshDefinition(this.asItem(), stack -> {
            if (!stack.hasTag()) {
                return new ModelResourceLocation(baseLocation, String.format(modelPropString, Variant.BASE.getName().toLowerCase(Locale.ENGLISH)));
            }
            CompoundTag tag = stack.getTag();
            //noinspection ConstantConditions
            Variant variant = Variant.fromId(tag.getByte(VARIANT_TAG));
            return new ModelResourceLocation(baseLocation, String.format(modelPropString, variant.getName().toLowerCase(Locale.ENGLISH)));
        });

        for (Variant variant : Variant.values()) {
            LegacyModelLoader.registerItemVariants(this.asItem(),
                    new ModelResourceLocation(baseLocation, String.format(modelPropString, variant.getName().toLowerCase(Locale.ENGLISH))));
        }
    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
        return WorldTools.getTile(source, pos, TileTotemPart.class).map(TileTotemPart::getVariant).orElse(Variant.BASE).getBoundingBox(state.getValue(FACING));
    }

    public enum Variant implements StringRepresentable {
        BASE(0) {
            @Override
            public Set<BlockPos> getAdditionalPartPositions(BlockPos pos, Direction facing) {
                return ImmutableSet.of(pos.above());
            }

            @Override
            public void placeAdditionalParts(Level world, BlockPos pos, Direction facing) {
                world.setBlock(pos.above(), AWStructureBlocks.TOTEM_PART.defaultBlockState().setValue(VISIBLE, false), 3);
                WorldTools.getTile(world, pos.above(), TileTotemPart.class).ifPresent(t -> t.setMainBlockPos(pos));
            }
        },
        MID(1),
        MID_ALT(2),
        TOP(3) {
            @Override
            public Set<BlockPos> getAdditionalPartPositions(BlockPos pos, Direction facing) {
                return ImmutableSet.of(pos.above(), pos.relative(facing.getClockWise()), pos.relative(facing.getCounterClockWise()));
            }

            @Override
            public void placeAdditionalParts(Level world, BlockPos pos, Direction facing) {
                world.setBlock(pos.above(), AWStructureBlocks.TOTEM_PART.defaultBlockState().setValue(VISIBLE, false), 3);
                WorldTools.getTile(world, pos.above(), TileTotemPart.class).ifPresent(t -> t.setMainBlockPos(pos));
                placeSideBlock(world, pos, pos.relative(facing.getClockWise()), facing.getOpposite(), Variant.WINGS);
                placeSideBlock(world, pos, pos.relative(facing.getCounterClockWise()), facing, Variant.WINGS);
            }
        },
        WINGS(4) {
            @Override
            public AABB getBoundingBox(Direction facing) {
                return facing.getAxis() == Direction.Axis.X ? WING_AABB_X : WING_AABB_Z;
            }
        },
        IRMINSUL_BASE(5) {
            @Override
            public AABB getBoundingBox(Direction facing) {
                return facing.getAxis() == Direction.Axis.X ? IRMINSUL_AABB_X : IRMINSUL_AABB_Z;
            }
        },
        IRMINSUL_MID(6) {
            @Override
            public AABB getBoundingBox(Direction facing) {
                return facing.getAxis() == Direction.Axis.X ? IRMINSUL_AABB_X : IRMINSUL_AABB_Z;
            }
        },
        IRMINSUL_TOP(7) {
            @Override
            public AABB getBoundingBox(Direction facing) {
                return facing.getAxis() == Direction.Axis.X ? IRMINSUL_TOP_AABB_X : IRMINSUL_TOP_AABB_Z;
            }

            @Override
            public Set<BlockPos> getAdditionalPartPositions(BlockPos pos, Direction facing) {
                return ImmutableSet.of(pos.relative(facing.getClockWise()), pos.relative(facing.getCounterClockWise()));
            }

            @Override
            public void placeAdditionalParts(Level world, BlockPos pos, Direction facing) {
                placeSideBlock(world, pos, pos.relative(facing.getClockWise()), facing.getOpposite(), Variant.IRMINSUL_SIDE);
                placeSideBlock(world, pos, pos.relative(facing.getCounterClockWise()), facing, Variant.IRMINSUL_SIDE);
            }

        },
        IRMINSUL_SIDE(8) {
            @Override
            public AABB getBoundingBox(Direction facing) {
                return facing.getAxis() == Direction.Axis.X ? IRMINSUL_SIDE_AABB_X : IRMINSUL_SIDE_AABB_Z;
            }
        },
        ELEPHANT_LEFT(9) {
            @Override
            public AABB getBoundingBox(Direction value) {
                return FULL_BLOCK_AABB;
            }

            @Override
            public boolean includeInSubBlocks() {
                return false;
            }
        },
        ELEPHANT_RIGHT(10) {
            @Override
            public AABB getBoundingBox(Direction value) {
                return FULL_BLOCK_AABB;
            }

            @Override
            public boolean includeInSubBlocks() {
                return false;
            }
        },
        ELEPHANT_BODY(11) {
            @Override
            public void placeAdditionalParts(Level world, BlockPos pos, Direction facing) {
                BlockPos right = pos.relative(facing.getClockWise());
                BlockPos left = pos.relative(facing.getCounterClockWise());
                placeInvisibleBlock(world, pos, right.above(), ELEPHANT_RIGHT);
                placeSideBlock(world, pos, right, facing, ELEPHANT_RIGHT);
                placeInvisibleBlock(world, pos, left.above(), ELEPHANT_LEFT);
                placeSideBlock(world, pos, left, facing, ELEPHANT_LEFT);
                placeInvisibleBlock(world, pos, pos.above(), ELEPHANT_BODY);
            }

            @Override
            public Set<BlockPos> getAdditionalPartPositions(BlockPos pos, Direction facing) {
                BlockPos right = pos.relative(facing.getClockWise());
                BlockPos left = pos.relative(facing.getCounterClockWise());
                return ImmutableSet.of(pos.above(), right, left, right.above(), left.above());
            }

            @Override
            public AABB getBoundingBox(Direction value) {
                return FULL_BLOCK_AABB;
            }
        }, XOLTEC_IDOL_BODY_LEFT(12) {
            @Override
            public void placeAdditionalParts(Level world, BlockPos pos, Direction facing) {
                BlockPos right = pos.relative(facing.getClockWise());
                placeSideBlock(world, pos, right, facing, XOLTEC_IDOL_BODY_RIGHT);
                placeSideBlock(world, pos, right.above(), facing, XOLTEC_IDOL_HEAD_RIGHT);
                placeInvisibleBlock(world, pos, right.above().above(), XOLTEC_IDOL_HEAD_RIGHT, facing);
                placeSideBlock(world, pos, pos.above(), facing, XOLTEC_IDOL_HEAD_LEFT);
                placeInvisibleBlock(world, pos, pos.above().above(), XOLTEC_IDOL_HEAD_LEFT, facing);
            }

            @Override
            public Set<BlockPos> getAdditionalPartPositions(BlockPos pos, Direction facing) {
                BlockPos right = pos.relative(facing.getClockWise());
                return ImmutableSet.of(right, right.above(), right.above().above(), pos.above(), pos.above().above());
            }

            @Override
            public AABB getBoundingBox(Direction facing) {
                return getXoltecIdolAABB(facing);
            }
        }, XOLTEC_IDOL_BODY_RIGHT(13) {
            @Override
            public boolean includeInSubBlocks() {
                return false;
            }

            @Override
            public AABB getBoundingBox(Direction facing) {
                return getXoltecIdolAABB(facing);
            }
        }, XOLTEC_IDOL_HEAD_LEFT(14) {
            @Override
            public boolean includeInSubBlocks() {
                return false;
            }

            @Override
            public AABB getBoundingBox(Direction facing) {
                return getXoltecIdolAABB(facing);
            }
        }, XOLTEC_IDOL_HEAD_RIGHT(15) {
            @Override
            public boolean includeInSubBlocks() {
                return false;
            }

            @Override
            public AABB getBoundingBox(Direction facing) {
                return getXoltecIdolAABB(facing);
            }
        };

        private static AABB getXoltecIdolAABB(Direction facing) {
            switch (facing) {
                case NORTH:
                    return XOLTEC_IDOL_NORTH_AABB;
                case SOUTH:
                    return XOLTEC_IDOL_SOUTH_AABB;
                case WEST:
                    return XOLTEC_IDOL_WEST_AABB;
                case EAST:
                default:
                    return XOLTEC_IDOL_EAST_AABB;
            }
        }

        private int id;

        Variant(int id) {
            this.id = id;
        }

        protected static final AABB FULL_BLOCK_AABB = new AABB(0D, 0D, 0D, 1D, 1D, 1D);
        protected static final AABB DEFAULT_AABB = new AABB(3D / 16D, 0D, 3D / 16D, (16D - 3D) / 16D, 1D, (16D - 3D) / 16D);
        protected static final AABB WING_AABB_X = new AABB(6D / 16D, 4D / 16D, 0D, (16D - 6D) / 16D, (16D - 1D) / 16D, 1D);
        protected static final AABB WING_AABB_Z = new AABB(0D, 4D / 16D, 6D / 16D, 1D, (16D - 1D) / 16D, (16D - 6D) / 16D);
        protected static final AABB IRMINSUL_AABB_X = new AABB(4D / 16D, 0D, 3D / 16D, (16D - 4D) / 16D, 1D, (16D - 3D) / 16D);
        protected static final AABB IRMINSUL_AABB_Z = new AABB(3D / 16D, 0D, 4D / 16D, (16D - 3D) / 16D, 1D, (16D - 4D) / 16D);
        protected static final AABB IRMINSUL_TOP_AABB_X = new AABB(4D / 16D, 0D, 0D, (16D - 4D) / 16D, (16D - 2D) / 16D, 1D);
        protected static final AABB IRMINSUL_TOP_AABB_Z = new AABB(0D, 0D, 4D / 16D, 1D, (16D - 2D) / 16D, (16D - 4D) / 16D);
        protected static final AABB IRMINSUL_SIDE_AABB_X = new AABB(4D / 16D, 3D / 16D, 0D, (16D - 4D) / 16D, (16D - 2D) / 16D, 1D);
        protected static final AABB IRMINSUL_SIDE_AABB_Z = new AABB(0D, 3D / 16D, 4D / 16D, 1D, (16D - 2D) / 16D, (16D - 4D) / 16D);
        protected static final AABB XOLTEC_IDOL_SOUTH_AABB = new AABB(0D, 0D, 8D / 16D, 1D, 1D, 1D);
        protected static final AABB XOLTEC_IDOL_NORTH_AABB = new AABB(0D, 0D, 0D, 1D, 1D, 8D / 16D);
        protected static final AABB XOLTEC_IDOL_WEST_AABB = new AABB(0D, 0D, 0D, 8D / 16D, 1D, 1D);
        protected static final AABB XOLTEC_IDOL_EAST_AABB = new AABB(8D / 16D, 0D, 0D, 1D, 1D, 1D);

        public int getId() {
            return id;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }

        public String getName() {
            return getSerializedName();
        }

        private static Map<Integer, Variant> variants = new HashMap<>();

        static {
            for (Variant variant : Variant.values()) {
                variants.put(variant.getId(), variant);
            }
        }

        @SuppressWarnings("squid:S1172") //actually used when overridden
        public Set<BlockPos> getAdditionalPartPositions(BlockPos pos, Direction facing) {
            return new HashSet<>();
        }

        public boolean canPlace(Level world, BlockPos pos, Player placer) {
            if (!mayPlace(world, pos, placer)) {
                return false;
            }

            Set<BlockPos> positions = getAdditionalPartPositions(pos, placer.getDirection());
            for (BlockPos addPos : positions) {
                if (!mayPlace(world, addPos, placer)) {
                    return false;
                }
            }

            return true;
        }

        public void placeAdditionalParts(Level world, BlockPos pos, Direction facing) {
            //noop by default
        }

        private boolean mayPlace(Level world, BlockPos pos, Player placer) {
            BlockState placeState = AWStructureBlocks.TOTEM_PART.defaultBlockState();
            return world.getBlockState(pos).canBeReplaced() && world.isUnobstructed(placeState, pos, CollisionContext.of(placer));
        }

        public static Variant fromId(int id) {
            return variants.get(id);
        }

        public AABB getBoundingBox(Direction value) {
            return DEFAULT_AABB;
        }

        private static void placeInvisibleBlock(Level world, BlockPos mainPos, BlockPos sidePos, Variant variant) {
            placeInvisibleBlock(world, mainPos, sidePos, variant, Direction.NORTH);
        }

        private static void placeInvisibleBlock(Level world, BlockPos mainPos, BlockPos sidePos, Variant variant, Direction facing) {
            world.setBlock(sidePos, AWStructureBlocks.TOTEM_PART.defaultBlockState().setValue(VISIBLE, false).setValue(FACING, facing).setValue(VARIANT, variant), 3);
            setupTileData(world, mainPos, sidePos, variant);
        }

        private static void setupTileData(Level world, BlockPos mainPos, BlockPos sidePos, Variant variant) {
            WorldTools.getTile(world, sidePos, TileTotemPart.class).ifPresent(t -> {
                t.setVariant(variant);
                t.setMainBlockPos(mainPos);
            });
        }

        private static void placeSideBlock(Level world, BlockPos mainPos, BlockPos sidePos, Direction sideFacing, Variant variant) {
            world.setBlock(sidePos, AWStructureBlocks.TOTEM_PART.defaultBlockState().setValue(FACING, sideFacing).setValue(VARIANT, variant), 3);
            setupTileData(world, mainPos, sidePos, variant);
        }

        public boolean includeInSubBlocks() {
            return true;
        }
    }
}
