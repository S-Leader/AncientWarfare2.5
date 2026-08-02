package net.shadowmage.ancientwarfare.structure.proxy;

import codechicken.lib.util.ResourceUtils;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.shadowmage.ancientwarfare.core.compat.client.ClientRegistry;
import net.shadowmage.ancientwarfare.core.entity.AWEntityRegistry;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.client.AWStructureBlockColors;
import net.shadowmage.ancientwarfare.structure.client.AWStructureItemColors;
import net.shadowmage.ancientwarfare.structure.gui.GuiGateControl;
import net.shadowmage.ancientwarfare.structure.gui.GuiGateControlCreative;
import net.shadowmage.ancientwarfare.structure.render.*;
import net.shadowmage.ancientwarfare.structure.render.statue.StatueRenderer;
import net.shadowmage.ancientwarfare.structure.tile.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class ClientProxyStructure extends CommonProxyStructure {
    private Set<IClientRegister> clientRegisters = Sets.newHashSet();
    private final Map<BlockPos, SoundInstance> currentSounds = new HashMap<>();

    public ClientProxyStructure() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerModels);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerRenderers);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerBlockEntityRenderers);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(entityType(AWEntityRegistry.AW_GATES), RenderGateInvisible::new);
        event.registerEntityRenderer(entityType(AWEntityRegistry.SEAT), RenderSeatInvisible::new);
    }

    private void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ClientRegistry.registerBlockEntityRenderer(event, TileStoneCoffin.class, ignored -> new StoneCoffinRenderer());
        ClientRegistry.registerBlockEntityRenderer(event, TileWoodenCoffin.class, ignored -> new WoodenCoffinRenderer());
        BlockEntityRendererProvider<TileFlag> flagRendererProvider = ignored -> new ProtectionFlagRenderer();
        ClientRegistry.registerBlockEntityRenderer(event, TileDecorativeFlag.class, flagRendererProvider);
        ClientRegistry.registerBlockEntityRenderer(event, TileProtectionFlag.class, flagRendererProvider);
        ClientRegistry.registerBlockEntityRenderer(event, TileAdvancedSpawner.class, ignored -> new RenderAdvancedSpawner());
        ClientRegistry.registerBlockEntityRenderer(event, TileAdvancedLootChest.class, ignored -> new RenderAdvancedLootChest());
        ClientRegistry.registerBlockEntityRenderer(event, TileStructureScanner.class, ignored -> new StructureScannerRenderer());
        ClientRegistry.registerBlockEntityRenderer(event, TEGateProxy.class, ignored -> new GateProxyRenderer());
        ClientRegistry.registerBlockEntityRenderer(event, TileStructureBuilder.class, ignored -> new RenderStructureBuilder());
        ClientRegistry.registerBlockEntityRenderer(event, TileStake.class, ignored -> new StakeRenderer());
        ClientRegistry.registerBlockEntityRenderer(event, TileGravestone.class, ignored -> new RenderLootInfo<>());
        ClientRegistry.registerBlockEntityRenderer(event, TileUrn.class, ignored -> new RenderLootInfo<>());
        ClientRegistry.registerBlockEntityRenderer(event, TileStatue.class, ignored -> new StatueRenderer());
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> EntityType<T> entityType(String name) {
        EntityType<?> type = AWEntityRegistry.getType(new ResourceLocation(AncientWarfareStructure.MOD_ID, name));
        if (type == null) {
            throw new IllegalStateException("Missing registered structure entity type: " + name);
        }
        return (EntityType<T>) type;
    }

    public void registerModels(ModelEvent.RegisterAdditional event) {
        for (IClientRegister register : clientRegisters) {
            register.registerClient();
        }
    }

    @Override
    public void addClientRegister(IClientRegister register) {
        clientRegisters.add(register);
    }

    @Override
    public void preInit() {
        super.preInit();

        NetworkHandler.registerGui(NetworkHandler.GUI_GATE_CONTROL, GuiGateControl.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_GATE_CONTROL_CREATIVE, GuiGateControlCreative.class);
        MinecraftForge.EVENT_BUS.register(new StructureBoundingBoxRenderer());
        MinecraftForge.EVENT_BUS.register(new BlockHighlightRenderer());
        MinecraftForge.EVENT_BUS.register(new StructureEntryBBRenderer());
        MinecraftForge.EVENT_BUS.register(this);


        ResourceUtils.registerReloadListener(ParticleOnlyModel.INSTANCE);
    }

    @Override
    public void init() {
        super.init();


        AWStructureBlockColors.init();
        AWStructureItemColors.init();
    }

    @Override
    public void clearTemplatePreviewCache() {
        PreviewRenderer.clearCache();
    }

    @Override
    public void resetSoundAt(BlockPos pos) {
        currentSounds.remove(pos);
    }

    @Override
    public void setSoundAt(BlockPos pos, SoundEvent soundEvent, float volume) {
        currentSounds.put(pos, getPositionedSoundRecord(soundEvent, (float) pos.getX(), (float) pos.getY(), (float) pos.getZ(), volume));
    }

    private SoundInstance getPositionedSoundRecord(SoundEvent soundEvent, float x, float y, float z, float volume) {
        return new SimpleSoundInstance(soundEvent.getLocation(), SoundSource.RECORDS, volume, 1.0F,
                RandomSource.create(), false, 0, SoundInstance.Attenuation.LINEAR,
                x + 0.5D, y + 0.5D, z + 0.5D, false);
    }

    @Override
    public void stopSoundAt(BlockPos pos) {
        if (currentSounds.containsKey(pos)) {
            Minecraft.getInstance().getSoundManager().stop(currentSounds.get(pos));
        }
    }

    @Override
    public boolean hasSoundAt(BlockPos pos) {
        return currentSounds.containsKey(pos);
    }

    @Override
    public boolean isSoundPlayingAt(BlockPos pos) {
        if (!hasSoundAt(pos)) {
            return false;
        }
        SoundInstance positionedSound = currentSounds.get(pos);
        return Minecraft.getInstance().getSoundManager().isActive(positionedSound);
    }

    @Override
    public void playSoundAt(BlockPos pos) {
        if (hasSoundAt(pos)) {
            Minecraft.getInstance().getSoundManager().play(currentSounds.get(pos));
        }
    }

    @Override
    public double getClientPlayerDistanceTo(BlockPos pos) {
        Player player = Minecraft.getInstance().player;
        return player == null ? Double.POSITIVE_INFINITY
                : Math.sqrt(player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D));
    }

    @Override
    public Optional<Player> getPlayer() {
        return Optional.ofNullable(Minecraft.getInstance().player);
    }
}
