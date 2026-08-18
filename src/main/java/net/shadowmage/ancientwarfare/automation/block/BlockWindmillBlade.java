package net.shadowmage.ancientwarfare.automation.block;

import net.shadowmage.ancientwarfare.core.render.model.DynamicModelRegistry;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.automation.render.WindmillBladeRenderer;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.multiblock.TileWindmillBlade;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.render.model.*;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

import java.util.Optional;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.UNLISTED_HORIZONTAL_FACING;

public class BlockWindmillBlade extends BlockBaseAutomation implements LegacyBakeryProvider {
    public static final LegacyModelProperty<Boolean> FORMED = LegacyModelProperty.create("formed", false);

    public BlockWindmillBlade(String regName) {
        super(LegacyMaterial.WOOD, regName);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(UNLISTED_HORIZONTAL_FACING);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyModelState getLegacyModelState(BlockState state, BlockGetter world, BlockPos pos) {
        return WindmillBladeRenderer.INSTANCE.handleState(LegacyModelState.of(state), world, pos);
    }

    @Override
    public boolean shouldSideBeRendered(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public boolean eventReceived(BlockState state, Level world, BlockPos pos, int id, int param) {
        return WorldTools.sendClientEventToTile(world, pos, id, param);
    }

    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        WorldTools.getTile(world, pos, TileWindmillBlade.class).ifPresent(TileWindmillBlade::blockPlaced);
    }

    @Override
    public void breakBlock(Level world, BlockPos pos, BlockState state) {
        Optional<TileWindmillBlade> te = WorldTools.getTile(world, pos, TileWindmillBlade.class);
        super.breakBlock(world, pos, state);
        te.ifPresent(TileWindmillBlade::blockBroken); //have to call post block-break so that the tile properly sees the block/tile as gone

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
    public boolean isFullCube(BlockState state) {
        return false;
    }


    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 60;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 20;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        DynamicModelRegistry.registerBlock(this, getBakery(), state -> WindmillBladeRenderer.INSTANCE.cubeSprite);
        DynamicModelRegistry.registerItem(this.asItem(), getBakery(), () -> WindmillBladeRenderer.INSTANCE.sprite);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyBakery getBakery() {
        return WindmillBladeRenderer.INSTANCE;
    }
}
