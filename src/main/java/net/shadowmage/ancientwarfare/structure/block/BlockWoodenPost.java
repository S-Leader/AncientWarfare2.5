package net.shadowmage.ancientwarfare.structure.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.util.RayTraceUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;
import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.VISIBLE;

public class BlockWoodenPost extends BlockBaseStructure {
    private static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    public BlockWoodenPost() {
        super(LegacyMaterial.WOOD, "wooden_post");
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VISIBLE, PART);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(FACING, Direction.from2DDataValue((meta >> 2) & 3)).setValue(VISIBLE, ((meta >> 1) & 1) == 1)
                .setValue(PART, Part.byMeta(meta & 1));
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(FACING).get2DDataValue() << 2 | (state.getValue(VISIBLE) ? 1 : 0) << 1 | state.getValue(PART).getMeta();
    }

    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockState placeState = state.setValue(FACING, placer.getDirection().getOpposite());
        world.setBlock(pos, placeState.setValue(PART, Part.BOTTOM).setValue(VISIBLE, true), 3);
        world.setBlock(pos.above(), placeState.setValue(PART, Part.BOTTOM).setValue(VISIBLE, false), 3);
        world.setBlock(pos.above(2), placeState.setValue(PART, Part.TOP).setValue(VISIBLE, true), 3);
        world.setBlock(pos.above(3), placeState.setValue(PART, Part.TOP).setValue(VISIBLE, false), 3);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(VISIBLE) ? super.getRenderShape(state) : RenderShape.INVISIBLE;
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
    public void breakBlock(Level world, BlockPos pos, BlockState state) {
        if (state.getValue(PART) != Part.BOTTOM || !state.getValue(VISIBLE)) {
            BlockState currentState = state;
            BlockPos currentPos = pos;
            for (int i = 1; i < 4 && (currentState.getValue(PART) != Part.BOTTOM || !currentState.getValue(VISIBLE)); i++) {
                currentPos = pos.below(i);
                currentState = world.getBlockState(currentPos);
                if (currentState.getBlock() != this) {
                    return;
                }
            }
            if (currentState.getValue(PART) == Part.BOTTOM && currentState.getValue(VISIBLE)) {
                breakBlock(world, currentPos, currentState);
                return;
            }
        }

        world.removeBlock(pos.above(3), false);
        world.removeBlock(pos.above(2), false);
        world.removeBlock(pos.above(), false);
        world.removeBlock(pos, false);
        super.breakBlock(world, pos, state);
    }

    private static final List<AABB> BOTTOM_BASE_AABBs = ImmutableList.of(
            new AABB(0, 0, 0, 1, 2 / 16D, 1),
            new AABB(6 / 16D, 0, 6 / 16D, 10 / 16D, 1, 10 / 16D));

    private static final AABB POST_AABB = new AABB(6 / 16D, 0, 6 / 16D, 10 / 16D, 1, 10 / 16D);

    private static final Map<Direction, List<AABB>> TOP_AABBs = ImmutableMap.of(
            Direction.NORTH, ImmutableList.of(
                    new AABB(6 / 16D, 0, 6 / 16D, 10 / 16D, 1, 10 / 16D),
                    new AABB(6 / 16D, 4 / 16D, 0, 10 / 16D, 14 / 16D, 6 / 16D)),
            Direction.SOUTH, ImmutableList.of(
                    new AABB(6 / 16D, 0, 6 / 16D, 10 / 16D, 1, 10 / 16D),
                    new AABB(6 / 16D, 4 / 16D, 10 / 16D, 10 / 16D, 14 / 16D, 1)),
            Direction.EAST, ImmutableList.of(
                    new AABB(6 / 16D, 0, 6 / 16D, 10 / 16D, 1, 10 / 16D),
                    new AABB(10 / 16D, 4 / 16D, 6 / 16D, 16 / 16D, 14 / 16D, 10 / 16D)),
            Direction.WEST, ImmutableList.of(
                    new AABB(6 / 16D, 0, 6 / 16D, 10 / 16D, 1, 10 / 16D),
                    new AABB(0, 4 / 16D, 6 / 16D, 6 / 16D, 14 / 16D, 10 / 16D))
    );

    @Nullable
    public HitResult collisionRayTrace(BlockState state, Level world, BlockPos pos, Vec3 start, Vec3 end) {
        if (state.getValue(PART) == Part.BOTTOM) {
            if (state.getValue(VISIBLE)) {
                return RayTraceUtils.raytraceMultiAABB(BOTTOM_BASE_AABBs, pos, start, end, (rtr, aabb) -> rtr);
            } else {
                return RayTraceUtils.raytraceMultiAABB(ImmutableList.of(POST_AABB), pos, start, end);
            }
        } else {
            if (state.getValue(VISIBLE)) {
                return RayTraceUtils.raytraceMultiAABB(ImmutableList.of(POST_AABB), pos, start, end);
            } else {
                return RayTraceUtils.raytraceMultiAABB(TOP_AABBs.get(state.getValue(FACING)), pos, start, end, (rtr, aabb) -> rtr);
            }
        }
    }

    @Nullable
    @Override
    public AABB getCollisionBoundingBox(BlockState blockState, BlockGetter world, BlockPos pos) {
        return POST_AABB;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getSelectedBoundingBox(BlockState state, Level world, BlockPos pos) {
        if (state.getValue(PART) == Part.BOTTOM) {
            if (state.getValue(VISIBLE)) {
                LocalPlayer player = Minecraft.getInstance().player;
                return RayTraceUtils.getSelectedBoundingBox(BOTTOM_BASE_AABBs, pos, player);
            } else {
                return POST_AABB.move(pos);
            }
        } else {
            if (state.getValue(VISIBLE)) {
                return POST_AABB.move(pos);
            } else {
                LocalPlayer player = Minecraft.getInstance().player;
                return RayTraceUtils.getSelectedBoundingBox(TOP_AABBs.get(state.getValue(FACING)), pos, player);
            }
        }
    }

    public enum Part implements StringRepresentable {
        TOP("top", 0),
        BOTTOM("bottom", 1);

        private String name;
        private int meta;

        Part(String name, int meta) {
            this.name = name;
            this.meta = meta;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public String getName() {
            return name;
        }

        public int getMeta() {
            return meta;
        }

        private static final Map<Integer, Part> META_TO_PART;

        static {
            ImmutableMap.Builder<Integer, Part> builder = new ImmutableMap.Builder<>();
            for (Part part : values()) {
                builder.put(part.getMeta(), part);
            }
            META_TO_PART = builder.build();
        }

        public static Part byMeta(int meta) {
            return META_TO_PART.get(meta);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        // Models are loaded normally from 1.20 blockstates/models JSON.
    }
}
