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
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.config.ConfigManager;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelRegistryHelper;
import net.shadowmage.ancientwarfare.core.util.TextureImageBased;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.client.NPCItemColors;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.gui.*;
import net.shadowmage.ancientwarfare.npc.init.AWNPCEntities;
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

@OnlyIn(Dist.CLIENT)
public class NpcClientProxy extends NpcCommonProxy {

    private Set<IClientRegister> clientRegisters = Sets.newHashSet();

    public NpcClientProxy() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerModels);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerRenderers);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        registerOwnedRenderer(event, AWNPCEntities.NPC_WORKER);
        registerOwnedRenderer(event, AWNPCEntities.NPC_COMBAT);
        registerOwnedRenderer(event, AWNPCEntities.NPC_COURIER);
        registerOwnedRenderer(event, AWNPCEntities.NPC_TRADER);
        registerOwnedRenderer(event, AWNPCEntities.NPC_PRIEST);
        registerOwnedRenderer(event, AWNPCEntities.NPC_BARD);
        registerOwnedRenderer(event, AWNPCEntities.NPC_SIEGE_ENGINEER);

        registerFactionRenderer(event, AWNPCEntities.NPC_FACTION_ARCHER);
        registerFactionRenderer(event, AWNPCEntities.NPC_FACTION_SOLDIER);
        registerFactionRenderer(event, AWNPCEntities.NPC_FACTION_PRIEST);
        registerFactionRenderer(event, AWNPCEntities.NPC_FACTION_TRADER);
        registerFactionRenderer(event, AWNPCEntities.NPC_FACTION_COMMANDER);
        registerFactionRenderer(event, AWNPCEntities.NPC_FACTION_CAVALRY);
        registerFactionRenderer(event, AWNPCEntities.NPC_FACTION_MOUNTED_ARCHER);
        registerFactionRenderer(event, AWNPCEntities.NPC_FACTION_CIVILIAN_MALE);
        registerFactionRenderer(event, AWNPCEntities.NPC_FACTION_ARCHER_ELITE);
        registerFactionRenderer(event, AWNPCEntities.NPC_FACTION_SOLDIER_ELITE);
        registerFactionRenderer(event, AWNPCEntities.NPC_FACTION_LEADER_ELITE);
        registerFactionRenderer(event, AWNPCEntities.NPC_FACTION_CIVILIAN_FEMALE);
        registerFactionRenderer(event, AWNPCEntities.NPC_FACTION_BARD);
        registerFactionRenderer(event, AWNPCEntities.NPC_FACTION_SIEGE_ENGINEER);
        registerFactionRenderer(event, AWNPCEntities.NPC_FACTION_SPELLCASTER);
    }

    private static void registerOwnedRenderer(EntityRenderersEvent.RegisterRenderers event, String name) {
        event.registerEntityRenderer(entityType(name), RenderNpcBase::new);
    }

    private static void registerFactionRenderer(EntityRenderersEvent.RegisterRenderers event, String name) {
        event.registerEntityRenderer(entityType(name), RenderNpcFaction::new);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> EntityType<T> entityType(String name) {
        EntityType<?> type = AWNPCEntities.getEntityType(name);
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
