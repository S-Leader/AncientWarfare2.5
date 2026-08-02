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
import net.shadowmage.ancientwarfare.automation.gui.*;
import net.shadowmage.ancientwarfare.automation.render.*;
import net.shadowmage.ancientwarfare.automation.tile.torque.*;
import net.shadowmage.ancientwarfare.automation.tile.torque.multiblock.TileFlywheelStorage;
import net.shadowmage.ancientwarfare.automation.tile.torque.multiblock.TileWindmillBlade;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.TileWarehouseBase;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.TileWarehouseStockLinker;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.TileWarehouseStockViewer;
import net.shadowmage.ancientwarfare.automation.tile.worksite.TileWorksiteBase;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.compat.client.ClientRegistry;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.proxy.ClientProxyBase;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;
import net.shadowmage.ancientwarfare.vehicle.gui.GuiVehicleStats;


@OnlyIn(Dist.CLIENT)
public class ClientProxyAutomation extends ClientProxyBase {

    public ClientProxyAutomation() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerBlockEntityRenderers);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onPostTextureStitch);
    }

    private void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        BlockEntityRendererProvider<TileWorksiteBase> worksiteRenderer = ignored -> new WorksiteRenderer();
        ClientRegistry.registerBlockEntityRenderer(event, TileWorksiteBase.class, worksiteRenderer);
        ClientRegistry.registerBlockEntityRenderer(event, TileWarehouseBase.class, worksiteRenderer);
        ClientRegistry.registerBlockEntityRenderer(event, TileWarehouseStockViewer.class, ignored -> new WarehouseStockViewerRenderer());
        ClientRegistry.registerBlockEntityRenderer(event, TileWarehouseStockLinker.class, ignored -> new WarehouseStockLinkerRenderer());

        ClientRegistry.registerBlockEntityRenderer(event, TileTorqueSidedCell.class,
                ignored -> new TorqueTransportAnimationRenderer(TorqueJunctionRenderer.INSTANCE));
        BlockEntityRendererProvider<TileTorqueSidedCell> distributorRenderer =
                ignored -> new TorqueTransportAnimationRenderer(TorqueDistributorRenderer.INSTANCE);
        ClientRegistry.registerBlockEntityRenderer(event, TileDistributor.class, distributorRenderer);
        ClientRegistry.registerBlockEntityRenderer(event, TileTorqueShaft.class, ignored -> new TorqueShaftAnimationRenderer());
        ClientRegistry.registerBlockEntityRenderer(event, TileFlywheelController.class, ignored -> new FlywheelControllerAnimationRenderer());
        ClientRegistry.registerBlockEntityRenderer(event, TileFlywheelStorage.class, ignored -> new FlywheelStorageAnimationRenderer());
        ClientRegistry.registerBlockEntityRenderer(event, TileStirlingGenerator.class,
                ignored -> new TorqueAnimationRenderer<>(StirlingGeneratorRenderer.INSTANCE));
        ClientRegistry.registerBlockEntityRenderer(event, TileWaterwheelGenerator.class, ignored ->
                new TorqueAnimationRenderer<TileWaterwheelGenerator>(WaterwheelGeneratorRenderer.INSTANCE) {
                    @Override
                    protected LegacyModelState updateAdditionalProperties(LegacyModelState state, TileTorqueBase tile) {
                        if (tile instanceof TileWaterwheelGenerator waterwheel) {
                            return state.setValue(BlockWaterwheelGenerator.VALID_SETUP, waterwheel.validSetup);
                        }
                        return state;
                    }
                });
        ClientRegistry.registerBlockEntityRenderer(event, TileHandCrankedGenerator.class,
                ignored -> new TorqueAnimationRenderer<>(HandCrankedGeneratorRenderer.INSTANCE));
        ClientRegistry.registerBlockEntityRenderer(event, TileWindmillController.class,
                ignored -> new TorqueAnimationRenderer<>(WindmillGeneratorRenderer.INSTANCE));
        ClientRegistry.registerBlockEntityRenderer(event, TileWindmillBlade.class, ignored -> new WindmillBladeAnimationRenderer());
    }

    @Override
    public void preInit() {
        super.preInit();

        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_INVENTORY_SIDE_ADJUST, GuiWorksiteInventorySideSelection.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_ANIMAL_CONTROL, GuiWorksiteAnimalControl.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_FISH_CONTROL, GuiWorksiteFishControl.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WAREHOUSE_CONTROL, GuiWarehouseControl.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_QUARRY, GuiWorksiteQuarry.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_QUARRY_BOUNDS, GuiWorksiteQuarryBounds.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_TREE_FARM, GuiWorksiteTreeFarm.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_CROP_FARM, GuiWorksiteCropFarm.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_FRUIT_FARM, GuiWorksiteFruitFarm.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_VEHICLE_STATS, GuiVehicleStats.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_ANIMAL_FARM, GuiWorksiteAnimalFarm.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_FISH_FARM, GuiWorksiteFishFarm.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_BOUNDS, GuiWorksiteBoundsAdjust.class);


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
