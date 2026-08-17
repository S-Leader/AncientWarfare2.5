package net.shadowmage.ancientwarfare.automation.proxy;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.shadowmage.ancientwarfare.automation.AutomationInputHandler;
import net.shadowmage.ancientwarfare.automation.block.BlockWaterwheelGenerator;
import net.shadowmage.ancientwarfare.automation.block.TorqueTier;
import net.shadowmage.ancientwarfare.automation.init.AWAutomationBlocks;
import net.shadowmage.ancientwarfare.automation.render.*;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueBase;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueSidedCell;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileWaterwheelGenerator;
import net.shadowmage.ancientwarfare.automation.tile.worksite.TileWorksiteBase;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.compat.client.ClientRegistry;
import net.shadowmage.ancientwarfare.core.proxy.ClientProxyBase;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;


@OnlyIn(Dist.CLIENT)
public class ClientProxyAutomation extends ClientProxyBase {

    public ClientProxyAutomation() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerBlockEntityRenderers);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onPostTextureStitch);
    }

    private void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        BlockEntityRendererProvider<TileWorksiteBase> worksiteRenderer = ignored -> new WorksiteRenderer();
        ClientRegistry.registerBlockEntityRenderer(event, AWAutomationBlocks.QUARRY_TILE, worksiteRenderer);
        ClientRegistry.registerBlockEntityRenderer(event, AWAutomationBlocks.TREE_FARM_TILE, worksiteRenderer);
        ClientRegistry.registerBlockEntityRenderer(event, AWAutomationBlocks.CROP_FARM_TILE, worksiteRenderer);
        ClientRegistry.registerBlockEntityRenderer(event, AWAutomationBlocks.FRUIT_FARM_TILE, worksiteRenderer);
        ClientRegistry.registerBlockEntityRenderer(event, AWAutomationBlocks.ANIMAL_FARM_TILE, worksiteRenderer);
        ClientRegistry.registerBlockEntityRenderer(event, AWAutomationBlocks.FISH_FARM_TILE, worksiteRenderer);
        ClientRegistry.registerBlockEntityRenderer(event, AWAutomationBlocks.AUTO_CRAFTING_TILE, worksiteRenderer);
        ClientRegistry.registerBlockEntityRenderer(event, AWAutomationBlocks.WAREHOUSE_CONTROL_TILE, worksiteRenderer);

        ClientRegistry.registerBlockEntityRenderer(event, AWAutomationBlocks.WAREHOUSE_STOCK_VIEWER_TILE,
                ignored -> new WarehouseStockViewerRenderer());
        ClientRegistry.registerBlockEntityRenderer(event, AWAutomationBlocks.WAREHOUSE_STOCK_LINKER_TILE,
                ignored -> new WarehouseStockLinkerRenderer());

        ClientRegistry.registerBlockEntityRenderer(event, AWAutomationBlocks.TORQUE_JUNCTION_TILE,
                ignored -> new TorqueTransportAnimationRenderer(TorqueJunctionRenderer.INSTANCE));
        BlockEntityRendererProvider<TileTorqueSidedCell> distributorRenderer =
                ignored -> new TorqueTransportAnimationRenderer(TorqueDistributorRenderer.INSTANCE);
        ClientRegistry.registerBlockEntityRenderer(event, AWAutomationBlocks.TORQUE_DISTRIBUTOR_TILE, distributorRenderer);
        ClientRegistry.registerBlockEntityRenderer(event, AWAutomationBlocks.TORQUE_SHAFT_TILE,
                ignored -> new TorqueShaftAnimationRenderer());
        ClientRegistry.registerBlockEntityRenderer(event, AWAutomationBlocks.FLYWHEEL_CONTROLLER_TILE,
                ignored -> new FlywheelControllerAnimationRenderer());
        ClientRegistry.registerBlockEntityRenderer(event, AWAutomationBlocks.FLYWHEEL_STORAGE_TILE,
                ignored -> new FlywheelStorageAnimationRenderer());
        ClientRegistry.registerBlockEntityRenderer(event, AWAutomationBlocks.STIRLING_GENERATOR_TILE,
                ignored -> new TorqueAnimationRenderer<>(StirlingGeneratorRenderer.INSTANCE));
        ClientRegistry.registerBlockEntityRenderer(event, AWAutomationBlocks.WATERWHEEL_GENERATOR_TILE, ignored ->
                new TorqueAnimationRenderer<TileWaterwheelGenerator>(WaterwheelGeneratorRenderer.INSTANCE) {
                    @Override
                    protected LegacyModelState updateAdditionalProperties(LegacyModelState state, TileTorqueBase tile) {
                        if (tile instanceof TileWaterwheelGenerator waterwheel) {
                            return state.setValue(BlockWaterwheelGenerator.VALID_SETUP, waterwheel.validSetup);
                        }
                        return state;
                    }
                });
        ClientRegistry.registerBlockEntityRenderer(event, AWAutomationBlocks.HAND_CRANKED_GENERATOR_TILE,
                ignored -> new TorqueAnimationRenderer<>(HandCrankedGeneratorRenderer.INSTANCE));
        ClientRegistry.registerBlockEntityRenderer(event, AWAutomationBlocks.WINDMILL_GENERATOR_TILE,
                ignored -> new TorqueAnimationRenderer<>(WindmillGeneratorRenderer.INSTANCE));
        ClientRegistry.registerBlockEntityRenderer(event, AWAutomationBlocks.WINDMILL_BLADE_TILE,
                ignored -> new WindmillBladeAnimationRenderer());
    }

    @Override
    public void preInit() {
        super.preInit();
    }

    @Override
    public void init() {
        super.init();

        AutomationInputHandler.initKeyBindings();
    }

    /*
     * TextureStitchEvent.Pre was removed in 1.20; the sprites listed in SPRITES
     * must be registered through assets/ancientwarfare/atlases/blocks.json instead.
     */
    public void onPostTextureStitch(TextureStitchEvent.Post evt) {
        if (!InventoryMenu.BLOCK_ATLAS.equals(evt.getAtlas().location())) {
            return;
        }

        StirlingGeneratorRenderer.INSTANCE.setSprite(sprite(evt, "stirling_generator"));
        HandCrankedGeneratorRenderer.INSTANCE.setSprite(sprite(evt, "hand_cranked_generator"));
        WaterwheelGeneratorRenderer.INSTANCE.setSprite(sprite(evt, "waterwheel_generator"));
        WindmillGeneratorRenderer.INSTANCE.setSprite(sprite(evt, "windmill_generator"));
        WindmillBladeRenderer.INSTANCE.setSprite(sprite(evt, "windmill_blade"));
        WindmillBladeRenderer.INSTANCE.setCubeSprite(sprite(evt, "windmill_blade_cube"));

        TorqueShaftRenderer.INSTANCE.setSprite(TorqueTier.HEAVY, sprite(evt, "torque_shaft_heavy"));
        TorqueShaftRenderer.INSTANCE.setSprite(TorqueTier.MEDIUM, sprite(evt, "torque_shaft_medium"));
        TorqueShaftRenderer.INSTANCE.setSprite(TorqueTier.LIGHT, sprite(evt, "torque_shaft_light"));

        TorqueJunctionRenderer.INSTANCE.setSprite(TorqueTier.HEAVY, sprite(evt, "torque_junction_heavy"));
        TorqueJunctionRenderer.INSTANCE.setSprite(TorqueTier.MEDIUM, sprite(evt, "torque_junction_medium"));
        TorqueJunctionRenderer.INSTANCE.setSprite(TorqueTier.LIGHT, sprite(evt, "torque_junction_light"));

        TorqueDistributorRenderer.INSTANCE.setSprite(TorqueTier.HEAVY, sprite(evt, "torque_distributor_heavy"));
        TorqueDistributorRenderer.INSTANCE.setSprite(TorqueTier.MEDIUM, sprite(evt, "torque_distributor_medium"));
        TorqueDistributorRenderer.INSTANCE.setSprite(TorqueTier.LIGHT, sprite(evt, "torque_distributor_light"));

        FlywheelControllerRenderer.INSTANCE.setSprite(TorqueTier.HEAVY, sprite(evt, "flywheel_controller_heavy"));
        FlywheelControllerRenderer.INSTANCE.setSprite(TorqueTier.MEDIUM, sprite(evt, "flywheel_controller_medium"));
        FlywheelControllerRenderer.INSTANCE.setSprite(TorqueTier.LIGHT, sprite(evt, "flywheel_controller_light"));

        FlywheelStorageRenderer.INSTANCE.setSprite(false, TorqueTier.HEAVY, sprite(evt, "flywheel_small_heavy"));
        FlywheelStorageRenderer.INSTANCE.setSprite(false, TorqueTier.MEDIUM, sprite(evt, "flywheel_small_medium"));
        FlywheelStorageRenderer.INSTANCE.setSprite(false, TorqueTier.LIGHT, sprite(evt, "flywheel_small_light"));
        FlywheelStorageRenderer.INSTANCE.setSprite(true, TorqueTier.HEAVY, sprite(evt, "flywheel_large_heavy"));
        FlywheelStorageRenderer.INSTANCE.setSprite(true, TorqueTier.MEDIUM, sprite(evt, "flywheel_large_medium"));
        FlywheelStorageRenderer.INSTANCE.setSprite(true, TorqueTier.LIGHT, sprite(evt, "flywheel_large_light"));
    }

    private static TextureAtlasSprite sprite(TextureStitchEvent.Post evt, String name) {
        return evt.getAtlas().getSprite(spriteId(name));
    }

    private static ResourceLocation spriteId(String name) {
        return new ResourceLocation(AncientWarfareCore.MOD_ID, "model/automation/" + name);
    }

    private static final String[] SPRITES = {
            "stirling_generator", "hand_cranked_generator", "waterwheel_generator",
            "windmill_generator", "windmill_blade", "windmill_blade_cube",
            "torque_shaft_heavy", "torque_shaft_medium", "torque_shaft_light",
            "torque_junction_heavy", "torque_junction_medium", "torque_junction_light",
            "torque_distributor_heavy", "torque_distributor_medium", "torque_distributor_light",
            "flywheel_controller_heavy", "flywheel_controller_medium", "flywheel_controller_light",
            "flywheel_small_heavy", "flywheel_small_medium", "flywheel_small_light",
            "flywheel_large_heavy", "flywheel_large_medium", "flywheel_large_light"
    };

}
