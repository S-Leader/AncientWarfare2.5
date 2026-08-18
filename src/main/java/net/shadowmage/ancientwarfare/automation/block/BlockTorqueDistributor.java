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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.automation.init.AWAutomationBlocks;
import net.shadowmage.ancientwarfare.automation.render.TorqueDistributorRenderer;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileDistributorHeavy;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileDistributorLight;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileDistributorMedium;
import net.shadowmage.ancientwarfare.core.render.model.*;
import net.shadowmage.ancientwarfare.core.render.property.CoreProperties;

public class BlockTorqueDistributor extends BlockTorqueTransportSided implements LegacyBakeryProvider {
    public BlockTorqueDistributor(String regName) {
        super(regName);
    }

    public BlockTorqueDistributor(String regName, TorqueTier fixedTier) {
        super(regName, fixedTier);
    }

    @Override
    protected Item getVariantItem(TorqueTier tier) {
        return AWAutomationBlocks.getTorqueDistributorItem(tier);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyModelState getLegacyModelState(BlockState state, BlockGetter world, BlockPos pos) {
        return TorqueDistributorRenderer.INSTANCE.handleState(LegacyModelState.of(state), world, pos);
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        DynamicModelRegistry.registerBlock(this, getBakery(),
                state -> TorqueDistributorRenderer.INSTANCE.getSprite(getTier(state)));
        DynamicModelRegistry.registerItem(this.asItem(), getBakery(),
                () -> TorqueDistributorRenderer.INSTANCE.getSprite(getFixedTier() == null ? TorqueTier.LIGHT : getFixedTier()));
    }

    private static ModelResourceLocation modelLocation(TorqueTier tier) {
        return switch (tier) {
            case LIGHT -> TorqueDistributorRenderer.LIGHT_MODEL_LOCATION;
            case MEDIUM -> TorqueDistributorRenderer.MEDIUM_MODEL_LOCATION;
            case HEAVY -> TorqueDistributorRenderer.HEAVY_MODEL_LOCATION;
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyBakery getBakery() {
        return TorqueDistributorRenderer.INSTANCE;
    }
}
