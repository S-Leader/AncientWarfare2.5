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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
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

public final class BlockGateProxy extends BlockBaseStructure implements IClientRegister {
    private static final AABB Z_AXIS_AABB = new AABB(8D / 16D, 0, 0, 8D / 16D, 1, 1);
    private static final AABB X_AXIS_AABB = new AABB(0, 0, 8D / 16D, 1, 1, 8D / 16D);
    private static final AABB ZERO_AABB = new AABB(0, 0, 0, 0, 0, 0);
    private static final AABB NO_AABB = new AABB(0, 0, 0, 0, 0, 0);
    private static final AABB FULL_BLOCK_AABB = new AABB(0, 0, 0, 1, 1, 1);

    public BlockGateProxy() {
        super(LegacyMaterial.ROCK, "gate_proxy");
        setResistance(6000000);
        AncientWarfareStructure.proxy.addClientRegister(this);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity createTileEntity(Level world, BlockState state) {
        return new TEGateProxy();
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

    @Nullable
    @Override
    public AABB getCollisionBoundingBox(BlockState state, BlockGetter world, BlockPos pos) {
        return WorldTools.getTile(world, pos, TEGateProxy.class).map(TEGateProxy::isOpen).orElse(false) ? NO_AABB :
                getCorrectAxisAABB(world, pos);
    }

    private AABB getCorrectAxisAABB(BlockGetter world, BlockPos pos) {
        return (world.getBlockState(pos.relative(Direction.WEST)).getBlock() == this || world.getBlockState(pos.relative(Direction.EAST)).getBlock() == this)
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
        if (WorldTools.getTile(world, pos, TEGateProxy.class).map(TEGateProxy::isGateClosed).orElse(false)) {
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
