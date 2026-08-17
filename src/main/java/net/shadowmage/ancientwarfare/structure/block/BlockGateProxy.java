package net.shadowmage.ancientwarfare.structure.block;

import codechicken.lib.model.DummyBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelLoader;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelRegistryHelper;
import net.shadowmage.ancientwarfare.core.render.model.LegacyStateMapperBase;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.tile.TEGateProxy;

import javax.annotation.Nullable;
import java.util.Optional;

public final class BlockGateProxy extends BlockBaseStructure implements IClientRegister {
    // 1.12 tolerated a zero-thickness AABB here.  Shapes.create() in modern
    // Minecraft treats that as an empty shape, so the gate stopped colliding.
    // Keep the visual centre plane but give it a real 2/16-block thickness.
    private static final AABB Z_AXIS_AABB = new AABB(7D / 16D, 0, 0, 9D / 16D, 1, 1);
    private static final AABB X_AXIS_AABB = new AABB(0, 0, 7D / 16D, 1, 1, 9D / 16D);
    private static final AABB ZERO_AABB = new AABB(0, 0, 0, 0, 0, 0);
    private static final AABB NO_AABB = new AABB(0, 0, 0, 0, 0, 0);
    private static final AABB FULL_BLOCK_AABB = new AABB(0, 0, 0, 1, 1, 1);

    public BlockGateProxy() {
        // Collision/open state comes from TEGateProxy, therefore BlockState's
        // normal cached shape is invalid for this block.
        super(LegacyMaterial.ROCK.properties()
                .strength(-1.0F, 3600000.0F)
                .noOcclusion()
                .dynamicShape(), "gate_proxy");
        AncientWarfareStructure.proxy.addClientRegister(this);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }


    @Override
    public void getDrops(NonNullList<ItemStack> drops, BlockGetter world, BlockPos pos, BlockState state, int fortune) {
        //nothing gets dropped
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    /**
     * Proxy blocks are an implementation detail of EntityGate. They must collide
     * while the gate is closed, but must never win the player's block ray trace:
     * otherwise survival players mine the proxy instead of damaging EntityGate.
     */
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Nullable
    @Override
    public AABB getCollisionBoundingBox(BlockState state, BlockGetter world, BlockPos pos) {
        return WorldTools.getTile(world, pos, TEGateProxy.class).map(TEGateProxy::isOpen).orElse(false) ? NO_AABB :
                getCorrectAxisAABB(world, pos);
    }

    private AABB getCorrectAxisAABB(BlockGetter world, BlockPos pos) {
        // Prefer the gate itself when the owner has already been resolved.
        // This is also correct for a one-block-wide gate where there is no
        // neighbouring proxy from which the axis can be inferred.
        Optional<net.shadowmage.ancientwarfare.structure.entity.EntityGate> gate =
                WorldTools.getTile(world, pos, TEGateProxy.class).flatMap(TEGateProxy::getGate);
        if (gate.isPresent() && gate.get().pos1 != null && gate.get().pos2 != null) {
            return gate.get().pos1.getX() != gate.get().pos2.getX() ? X_AXIS_AABB : Z_AXIS_AABB;
        }

        return (world.getBlockState(pos.relative(Direction.WEST)).getBlock() == this
                || world.getBlockState(pos.relative(Direction.EAST)).getBlock() == this)
                ? X_AXIS_AABB : Z_AXIS_AABB;
    }

    @Override
    public boolean canCollideCheck(BlockState state, boolean hitIfLiquid) {
        return true;
    }

    @Override
    public AABB getSelectedBoundingBox(BlockState state, Level world, BlockPos pos) {
        return ZERO_AABB;
    }

    //Actually "can go through", for mob pathing
    @Override
    public boolean isPassable(BlockGetter world, BlockPos pos) {
        if (WorldTools.getTile(world, pos, TEGateProxy.class).map(TEGateProxy::isGateClosed).orElse(true)) {
            return false;
        }

        //Gate is probably open, Search identical neighbour
        if (world.getBlockState(pos.relative(Direction.WEST)).getBlock() == this) {
            return world.getBlockState(pos.relative(Direction.EAST)).getBlock() == this;
        } else if (world.getBlockState(pos.relative(Direction.NORTH)).getBlock() == this) {
            return world.getBlockState(pos.relative(Direction.SOUTH)).getBlock() == this;
        }
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type) {
        // Bridge the legacy isPassable() gate logic into the path finder used by
        // 1.20 mobs.  A closed proxy is an actual obstacle; an open one is not.
        return isPassable(world, pos);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        //noinspection ConstantConditions
        ModelResourceLocation modelLocation = new ModelResourceLocation(getRegistryName(), "normal");
        ModelLoaderHelper.registerItem(this, modelLocation);
        LegacyModelLoader.setCustomStateMapper(this, new LegacyStateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(BlockState state) {
                return modelLocation;
            }
        });
        LegacyModelRegistryHelper.register(modelLocation, new DummyBakedModel());

    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
        return WorldTools.getTile(source, pos, TEGateProxy.class).map(TEGateProxy::isOpen).orElse(false) ? NO_AABB : FULL_BLOCK_AABB;
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter world, BlockPos pos, Entity entity) {
        return false;
    }
}
