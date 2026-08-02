package net.shadowmage.ancientwarfare.npc.proxy;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.config.ConfigManager;
import net.shadowmage.ancientwarfare.core.entity.AWEntityRegistry;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelRegistryHelper;
import net.shadowmage.ancientwarfare.core.util.TextureImageBased;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.client.NPCItemColors;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.gui.*;
import net.shadowmage.ancientwarfare.npc.item.IExtendedReachWeapon;
import net.shadowmage.ancientwarfare.npc.render.RenderCommandOverlay;
import net.shadowmage.ancientwarfare.npc.render.RenderNpcBase;
import net.shadowmage.ancientwarfare.npc.render.RenderNpcFaction;
import net.shadowmage.ancientwarfare.npc.render.RenderWorkLines;
import net.shadowmage.ancientwarfare.npc.skin.NpcSkinManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class NpcClientProxy extends NpcCommonProxy {

    private Set<IClientRegister> clientRegisters = Sets.newHashSet();

    public NpcClientProxy() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerModels);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerRenderers);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        registerOwnedRenderer(event, AWEntityRegistry.NPC_WORKER);
        registerOwnedRenderer(event, AWEntityRegistry.NPC_COMBAT);
        registerOwnedRenderer(event, AWEntityRegistry.NPC_COURIER);
        registerOwnedRenderer(event, AWEntityRegistry.NPC_TRADER);
        registerOwnedRenderer(event, AWEntityRegistry.NPC_PRIEST);
        registerOwnedRenderer(event, AWEntityRegistry.NPC_BARD);
        registerOwnedRenderer(event, AWEntityRegistry.NPC_SIEGE_ENGINEER);

        registerFactionRenderer(event, AWEntityRegistry.NPC_FACTION_ARCHER);
        registerFactionRenderer(event, AWEntityRegistry.NPC_FACTION_SOLDIER);
        registerFactionRenderer(event, AWEntityRegistry.NPC_FACTION_PRIEST);
        registerFactionRenderer(event, AWEntityRegistry.NPC_FACTION_TRADER);
        registerFactionRenderer(event, AWEntityRegistry.NPC_FACTION_COMMANDER);
        registerFactionRenderer(event, AWEntityRegistry.NPC_FACTION_CAVALRY);
        registerFactionRenderer(event, AWEntityRegistry.NPC_FACTION_MOUNTED_ARCHER);
        registerFactionRenderer(event, AWEntityRegistry.NPC_FACTION_CIVILIAN_MALE);
        registerFactionRenderer(event, AWEntityRegistry.NPC_FACTION_ARCHER_ELITE);
        registerFactionRenderer(event, AWEntityRegistry.NPC_FACTION_SOLDIER_ELITE);
        registerFactionRenderer(event, AWEntityRegistry.NPC_FACTION_LEADER_ELITE);
        registerFactionRenderer(event, AWEntityRegistry.NPC_FACTION_CIVILIAN_FEMALE);
        registerFactionRenderer(event, AWEntityRegistry.NPC_FACTION_BARD);
        registerFactionRenderer(event, AWEntityRegistry.NPC_FACTION_SIEGE_ENGINEER);
        registerFactionRenderer(event, AWEntityRegistry.NPC_FACTION_SPELLCASTER);
    }

    private static void registerOwnedRenderer(EntityRenderersEvent.RegisterRenderers event, String name) {
        event.registerEntityRenderer(entityType(name), RenderNpcBase::new);
    }

    private static void registerFactionRenderer(EntityRenderersEvent.RegisterRenderers event, String name) {
        event.registerEntityRenderer(entityType(name), RenderNpcFaction::new);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> EntityType<T> entityType(String name) {
        EntityType<?> type = AWEntityRegistry.getType(new ResourceLocation(AncientWarfareNPC.MOD_ID, name));
        if (type == null) {
            throw new IllegalStateException("Missing registered NPC entity type: " + name);
        }
        return (EntityType<T>) type;
    }

    public void registerModels(ModelEvent.RegisterAdditional event) {
        for (IClientRegister register : clientRegisters) {
            register.registerClient();
        }
        LegacyModelRegistryHelper.registerAdditional(event);
    }

    @Override
    public void addClientRegister(IClientRegister register) {
        clientRegisters.add(register);
    }

    @Override
    public void preInit() {
        super.preInit();

        NetworkHandler.registerGui(NetworkHandler.GUI_NPC_INVENTORY, GuiNpcInventory.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_NPC_FACTION_TRADE_SETUP, GuiNpcFactionTradeSetup.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_NPC_FACTION_TRADE_VIEW, GuiNpcFactionTradeView.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_NPC_WORK_ORDER, GuiWorkOrder.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_NPC_UPKEEP_ORDER, GuiUpkeepOrder.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_NPC_COMBAT_ORDER, GuiCombatOrder.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_NPC_ROUTING_ORDER, GuiRoutingOrder.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_NPC_TOWN_HALL, GuiTownHallInventory.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_NPC_BARD, GuiNpcBard.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_NPC_CREATIVE, GuiNpcCreativeControls.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_NPC_TRADE_ORDER, GuiTradeOrder.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_NPC_PLAYER_OWNED_TRADE, GuiNpcPlayerOwnedTrade.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_NPC_FACTION_BARD, GuiNpcFactionBard.class);

        /* optional dependency for EBWizardry spell casters
         * References to the EBWizardry specific class can only be here, to avoid class loading if the mod is no present.
         * Any reference outside of the lambdas will crash the game if EBWizardry is not present */
        Supplier<Runnable> registerGuiNpcFactionSpellcasterWizardry = () -> () -> {
            NetworkHandler.registerGui(NetworkHandler.GUI_NPC_FACTION_SPELLCASTER_WIZARDRY, GuiNpcFactionSpellcasterWizardry.class);
        };

        if (ModList.get().isLoaded("ebwizardry")) {
            registerGuiNpcFactionSpellcasterWizardry.get().run();
        }


        MinecraftForge.EVENT_BUS.register(RenderWorkLines.INSTANCE);
        MinecraftForge.EVENT_BUS.register(RenderCommandOverlay.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new IExtendedReachWeapon.MouseClickHandler());

        registerClientOptions();
    }

    @Override
    public void init() {
        super.init();

        NPCItemColors.init();
    }

    private void registerClientOptions() {
        Runnable save = AncientWarfareNPC.statics::save;
        ConfigManager.registerBoolean("awconfig.npc.render_ai", AWNPCStatics.renderAI, save);
        ConfigManager.registerBoolean("awconfig.npc.render_friendly_names", AWNPCStatics.renderFriendlyNames, save);
        ConfigManager.registerBoolean("awconfig.npc.render_friendly_health", AWNPCStatics.renderFriendlyHealth, save);
        ConfigManager.registerBoolean("awconfig.npc.render_hostile_names", AWNPCStatics.renderHostileNames, save);
        ConfigManager.registerBoolean("awconfig.npc.render_hostile_health", AWNPCStatics.renderHostileHealth, save);
        ConfigManager.registerBoolean("awconfig.npc.render_team_colors", AWNPCStatics.renderTeamColors, save);
        ConfigManager.registerBoolean("awconfig.npc.render_work_points", AWNPCStatics.renderWorkPoints, save);
    }

    @Override
    public void loadSkins() {
        NpcSkinManager.loadSkins();
    }

    @Override
    public Optional<ResourceLocation> loadSkinPackImage(String imageName, InputStream is) {
        try {
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                AncientWarfareNPC.LOG.error("Unable to decode skin image {}", imageName);
                return Optional.empty();
            }
            // Bundled and user supplied 1.12 skin packs may contain spaces and upper-case
            // characters. They are valid file names, but are not valid 1.20 resource paths.
            String resourcePath = imageName.replace('\\', '/')
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9/._-]", "_");
            ResourceLocation loc = new ResourceLocation(AncientWarfareCore.MOD_ID, "skinpack/" + resourcePath);
            TextureImageBased tex = new TextureImageBased(loc, image);
            Minecraft.getInstance().getTextureManager().register(loc, tex);
            return Optional.of(loc);
        } catch (IOException e) {
            AncientWarfareNPC.LOG.error("Error reading image {}", imageName);
        }
        return Optional.empty();
    }

    @Override
    public Optional<ResourceLocation> getPlayerSkin(String name) {
        Optional<GameProfile> gp = getProfile(name);
        if (gp.isPresent()) {
            SkinManager manager = Minecraft.getInstance().getSkinManager();
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = manager.getInsecureSkinInformation(gp.get());
            if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                return Optional.of(manager.registerTexture(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN));
            }
        }
        return Optional.empty();
    }

}
