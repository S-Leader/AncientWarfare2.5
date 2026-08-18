package net.shadowmage.ancientwarfare.automation.block;

import net.shadowmage.ancientwarfare.core.render.model.DynamicModelRegistry;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.automation.init.AWAutomationBlocks;
import net.shadowmage.ancientwarfare.automation.render.TorqueShaftRenderer;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueShaft;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueShaftHeavy;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueShaftLight;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueShaftMedium;
import net.shadowmage.ancientwarfare.core.render.model.*;
import net.shadowmage.ancientwarfare.core.render.property.CoreProperties;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

import java.util.Optional;

public class BlockTorqueTransportShaft extends BlockTorqueTransport implements LegacyBakeryProvider {
    public static final LegacyModelProperty<Boolean> HAS_PREVIOUS = LegacyModelProperty.create("has_previous", false);
    public static final LegacyModelProperty<Boolean> HAS_NEXT = LegacyModelProperty.create("has_next", false);
    private static final AABB CENTER_BOX = new AABB(0.1875D, 0.1875D, 0.1875D, 0.8125D, 0.8125D, 0.8125D);
    private static final AABB X_AXIS_BOX = new AABB(0D, 0.1875D, 0.1875D, 1D, 0.8125D, 0.8125D);
    private static final AABB Y_AXIS_BOX = new AABB(0.1875D, 0D, 0.1875D, 0.8125D, 1D, 0.8125D);
    private static final AABB Z_AXIS_BOX = new AABB(0.1875D, 0.1875D, 0D, 0.8125D, 0.8125D, 1D);

    public BlockTorqueTransportShaft(String regName) {
        super(regName);
    }

    public BlockTorqueTransportShaft(String regName, TorqueTier fixedTier) {
        super(regName, fixedTier);
    }

    @Override
    protected Item getVariantItem(TorqueTier tier) {
        return AWAutomationBlocks.getTorqueShaftItem(tier);
    }


    @Override
    protected void addProperties(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.addProperties(builder);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyModelState getLegacyModelState(BlockState state, BlockGetter world, BlockPos pos) {
        return TorqueShaftRenderer.INSTANCE.handleState(LegacyModelState.of(state), world, pos);
    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter world, BlockPos pos) {
        Optional<TileTorqueShaft> te = WorldTools.getTile(world, pos, TileTorqueShaft.class);
        if (te.isPresent()) {
            return switch (te.get().getPrimaryFacing().getAxis()) {
                case X -> X_AXIS_BOX;
                case Y -> Y_AXIS_BOX;
                case Z -> Z_AXIS_BOX;
            };
        }
        return CENTER_BOX;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        DynamicModelRegistry.registerBlock(this, getBakery(),
                state -> TorqueShaftRenderer.INSTANCE.getSprite(getTier(state)));
        DynamicModelRegistry.registerItem(this.asItem(), getBakery(),
                () -> TorqueShaftRenderer.INSTANCE.getSprite(getFixedTier() == null ? TorqueTier.LIGHT : getFixedTier()));
    }

    private static ModelResourceLocation modelLocation(TorqueTier tier) {
        return switch (tier) {
            case LIGHT -> TorqueShaftRenderer.LIGHT_MODEL_LOCATION;
            case MEDIUM -> TorqueShaftRenderer.MEDIUM_MODEL_LOCATION;
            case HEAVY -> TorqueShaftRenderer.HEAVY_MODEL_LOCATION;
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyBakery getBakery() {
        return TorqueShaftRenderer.INSTANCE;
    }
}
