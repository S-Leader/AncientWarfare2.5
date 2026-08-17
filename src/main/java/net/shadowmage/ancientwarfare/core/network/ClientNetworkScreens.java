package net.shadowmage.ancientwarfare.core.network;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.shadowmage.ancientwarfare.automation.gui.*;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.core.gui.GuiBackpack;
import net.shadowmage.ancientwarfare.core.gui.GuiInfoTool;
import net.shadowmage.ancientwarfare.core.gui.GuiResearchBook;
import net.shadowmage.ancientwarfare.core.gui.crafting.GuiEngineeringStation;
import net.shadowmage.ancientwarfare.core.gui.manual.GuiManual;
import net.shadowmage.ancientwarfare.core.gui.research.GuiResearchStation;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.npc.gui.*;
import net.shadowmage.ancientwarfare.structure.gui.*;
import net.shadowmage.ancientwarfare.vehicle.gui.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Direct Forge MenuType -> Screen registration.
 *
 * The table is keyed by stable ResourceLocations instead of the old numeric GUI
 * ids, so optional modules can add/remove menus without shifting or mismatching
 * any other screen registration.
 */
@OnlyIn(Dist.CLIENT)
public final class ClientNetworkScreens {
    @FunctionalInterface
    private interface ScreenFactory {
        AbstractContainerScreen<?> create(ContainerBase menu);
    }

    private static final Map<ResourceLocation, ScreenFactory> FACTORIES = new LinkedHashMap<>();
    private static boolean registered;

    private ClientNetworkScreens() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        buildFactoryTable();

        for (AWMenuTypes.MenuRegistration registration : AWMenuTypes.registrations()) {
            ScreenFactory factory = FACTORIES.get(registration.id());
            if (factory == null) {
                AncientWarfareCore.LOG.warn("No client screen factory for native menu {}", registration.id());
                continue;
            }
            registerScreen(registration, factory);
        }
        registered = true;
    }

    private static void buildFactoryTable() {
        if (!FACTORIES.isEmpty()) {
            return;
        }

        screen(NetworkHandler.GUI_CRAFTING, GuiEngineeringStation::new);
        screen(NetworkHandler.GUI_SCANNER, GuiStructureScanner::new);
        screen(NetworkHandler.GUI_BUILDER, GuiStructureSelection::new);
        screen(NetworkHandler.GUI_NPC_INVENTORY, GuiNpcInventory::new);
        screen(NetworkHandler.GUI_WORKSITE_INVENTORY_SIDE_ADJUST, GuiWorksiteInventorySideSelection::new);
        screen(NetworkHandler.GUI_NPC_TRADE_ORDER, GuiTradeOrder::new);
        screen(NetworkHandler.GUI_SPAWNER_ADVANCED, GuiSpawnerAdvanced::new);
        screen(NetworkHandler.GUI_SPAWNER_ADVANCED_BLOCK, GuiSpawnerAdvanced::new);
        screen(NetworkHandler.GUI_SPAWNER_ADVANCED_INVENTORY, GuiSpawnerAdvancedInventory::new);
        screen(NetworkHandler.GUI_SPAWNER_ADVANCED_BLOCK_INVENTORY, GuiSpawnerAdvancedInventory::new);
        screen(NetworkHandler.GUI_GATE_CONTROL, GuiGateControl::new);
        screen(NetworkHandler.GUI_RESEARCH_STATION, GuiResearchStation::new);
        screen(NetworkHandler.GUI_DRAFTING_STATION, GuiDraftingStation::new);
        screen(NetworkHandler.GUI_WORKSITE_ANIMAL_CONTROL, GuiWorksiteAnimalControl::new);
        screen(NetworkHandler.GUI_WORKSITE_AUTO_CRAFT, GuiWorksiteAutoCrafting::new);
        screen(NetworkHandler.GUI_WORKSITE_FISH_CONTROL, GuiWorksiteFishControl::new);
        screen(NetworkHandler.GUI_MAILBOX_INVENTORY, GuiMailboxInventory::new);
        screen(NetworkHandler.GUI_WAREHOUSE_CONTROL, GuiWarehouseControl::new);
        screen(NetworkHandler.GUI_WAREHOUSE_STORAGE, GuiWarehouseStorage::new);
        screen(NetworkHandler.GUI_WAREHOUSE_STOCK, GuiWarehouseStockViewer::new);
        screen(NetworkHandler.GUI_WAREHOUSE_OUTPUT, GuiWarehouseInterface::new);
        screen(NetworkHandler.GUI_WAREHOUSE_CRAFTING, GuiWarehouseCraftingStation::new);
        screen(NetworkHandler.GUI_CHUNK_LOADER_DELUXE, GuiChunkLoaderDeluxe::new);
        screen(NetworkHandler.GUI_WORKSITE_QUARRY, GuiWorksiteQuarry::new);
        screen(NetworkHandler.GUI_WORKSITE_TREE_FARM, GuiWorksiteTreeFarm::new);
        screen(NetworkHandler.GUI_WORKSITE_ANIMAL_FARM, GuiWorksiteAnimalFarm::new);
        screen(NetworkHandler.GUI_WORKSITE_CROP_FARM, GuiWorksiteCropFarm::new);
        screen(NetworkHandler.GUI_WORKSITE_FISH_FARM, GuiWorksiteFishFarm::new);
        screen(NetworkHandler.GUI_WORKSITE_QUARRY_BOUNDS, GuiWorksiteQuarryBounds::new);
        screen(NetworkHandler.GUI_STIRLING_GENERATOR, GuiStirlingGenerator::new);
        screen(NetworkHandler.GUI_WAREHOUSE_STOCK_LINKER, GuiWarehouseStockLinker::new);
        screen(NetworkHandler.GUI_NPC_WORK_ORDER, GuiWorkOrder::new);
        screen(NetworkHandler.GUI_NPC_UPKEEP_ORDER, GuiUpkeepOrder::new);
        screen(NetworkHandler.GUI_NPC_COMBAT_ORDER, GuiCombatOrder::new);
        screen(NetworkHandler.GUI_NPC_ROUTING_ORDER, GuiRoutingOrder::new);
        screen(NetworkHandler.GUI_NPC_FACTION_TRADE_SETUP, GuiNpcFactionTradeSetup::new);
        screen(NetworkHandler.GUI_BACKPACK, GuiBackpack::new);
        screen(NetworkHandler.GUI_NPC_TOWN_HALL, GuiTownHallInventory::new);
        screen(NetworkHandler.GUI_NPC_FACTION_TRADE_VIEW, GuiNpcFactionTradeView::new);
        screen(NetworkHandler.GUI_NPC_BARD, GuiNpcBard::new);
        screen(NetworkHandler.GUI_NPC_CREATIVE, GuiNpcCreativeControls::new);
        screen(NetworkHandler.GUI_RESEARCH_BOOK, GuiResearchBook::new);
        screen(NetworkHandler.GUI_WORKSITE_BOUNDS, GuiWorksiteBoundsAdjust::new);
        screen(NetworkHandler.GUI_NPC_PLAYER_OWNED_TRADE, GuiNpcPlayerOwnedTrade::new);
        screen(NetworkHandler.GUI_SOUND_BLOCK, GuiSoundBlock::new);
        screen(NetworkHandler.GUI_NPC_FACTION_BARD, GuiNpcFactionBard::new);
        screen(NetworkHandler.GUI_VEHICLE_AMMO_SELECTION, GuiVehicleAmmoSelection::new);
        screen(NetworkHandler.GUI_VEHICLE_INVENTORY, GuiVehicleInventory::new);
        screen(NetworkHandler.GUI_VEHICLE_STATS, GuiVehicleStats::new);
        screen(NetworkHandler.GUI_WORKSITE_FRUIT_FARM, GuiWorksiteFruitFarm::new);
        screen(NetworkHandler.GUI_TOWN_BUILDER, GuiTownSelection::new);
        screen(NetworkHandler.GUI_LOOT_CHEST_PLACER, GuiLootChestPlacer::new);
        screen(NetworkHandler.GUI_MANUAL, GuiManual::new);
        screen(NetworkHandler.GUI_INFO_TOOL, GuiInfoTool::new);
        screen(NetworkHandler.GUI_GATE_CONTROL_CREATIVE, GuiGateControlCreative::new);
        screen(NetworkHandler.GUI_LOOT_BASKET, GuiLootBasket::new);
        screen(NetworkHandler.GUI_STAKE, GuiStake::new);
        screen(NetworkHandler.GUI_STATUE, GuiStatue::new);

        if (ModList.get().isLoaded("ebwizardry")) {
            WizardryScreens.register();
        }
    }

    /**
     * Keep optional Wizardry screen bytecode in a lazy holder so removing the
     * optional mod cannot force its GUI class to resolve while this class loads.
     */
    private static final class WizardryScreens {
        private static void register() {
            screen(NetworkHandler.GUI_NPC_FACTION_SPELLCASTER_WIZARDRY,
                    GuiNpcFactionSpellcasterWizardry::new);
        }
    }

    private static void screen(ResourceLocation id, ScreenFactory factory) {
        ScreenFactory previous = FACTORIES.putIfAbsent(id, factory);
        if (previous != null) {
            throw new IllegalStateException("Duplicate client screen registration for " + id);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerScreen(AWMenuTypes.MenuRegistration registration, ScreenFactory factory) {
        MenuType menuType = registration.menuType().get();
        MenuScreens.register(menuType, (MenuScreens.ScreenConstructor) (menu, inventory, title) -> {
            ContainerBase awMenu = (ContainerBase) menu;
            AbstractContainerScreen<?> screen = factory.create(awMenu);
            NetworkHandler.INSTANCE.flushPendingGuiPackets(awMenu);
            return screen;
        });
    }
}
