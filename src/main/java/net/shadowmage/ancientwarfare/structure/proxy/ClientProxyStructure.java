package net.shadowmage.ancientwarfare.structure.proxy;

import codechicken.lib.util.ResourceUtils;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
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
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.shadowmage.ancientwarfare.core.compat.client.ClientRegistry;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.client.AWStructureBlockColors;
import net.shadowmage.ancientwarfare.structure.client.AWStructureItemColors;
import net.shadowmage.ancientwarfare.structure.gui.GuiGateControl;
import net.shadowmage.ancientwarfare.structure.gui.GuiGateControlCreative;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.init.AWStructureEntities;
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
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }


    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks.ALTAR_CANDLE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks.ALTAR_LONG_CLOTH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks.ALTAR_SHORT_CLOTH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks.ALTAR_LECTERN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks.ALTAR_SUN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks.LOOT_BASKET.get(), RenderType.cutout());
            // Legacy getBlockLayer() is no longer consulted by the 1.20 chunk renderer.
            // These models contain alpha-cut fire/metal planes and must explicitly use cutout.
            ItemBlockRenderTypes.setRenderLayer(net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks.FIRE_PIT.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks.BRAZIER_FLAME.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks.BRAZIER_EMBER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks.STAKE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks.ADVANCED_SPAWNER.get(), RenderType.cutout());
        });
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AWStructureEntities.GATE.get(), RenderGateInvisible::new);
        event.registerEntityRenderer(AWStructureEntities.SEAT.get(), RenderSeatInvisible::new);
    }

    private void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ClientRegistry.registerBlockEntityRenderer(event, AWStructureBlocks.STONE_COFFIN_TILE,
                ignored -> new StoneCoffinRenderer());
        ClientRegistry.registerBlockEntityRenderer(event, AWStructureBlocks.WOODEN_COFFIN_TILE,
                ignored -> new WoodenCoffinRenderer());
        BlockEntityRendererProvider<TileFlag> flagRendererProvider = ignored -> new ProtectionFlagRenderer();
        ClientRegistry.registerBlockEntityRenderer(event, AWStructureBlocks.DECORATIVE_FLAG_TILE, flagRendererProvider);
        ClientRegistry.registerBlockEntityRenderer(event, AWStructureBlocks.PROTECTION_FLAG_TILE, flagRendererProvider);
        ClientRegistry.registerBlockEntityRenderer(event, AWStructureBlocks.ADVANCED_SPAWNER_TILE,
                ignored -> new RenderAdvancedSpawner());
        ClientRegistry.registerBlockEntityRenderer(event, AWStructureBlocks.ADVANCED_LOOT_CHEST_TILE,
                ignored -> new RenderAdvancedLootChest());
        ClientRegistry.registerBlockEntityRenderer(event, AWStructureBlocks.STRUCTURE_SCANNER_BLOCK_TILE,
                ignored -> new StructureScannerRenderer());
        ClientRegistry.registerBlockEntityRenderer(event, AWStructureBlocks.GATE_PROXY_TILE,
                ignored -> new GateProxyRenderer());
        ClientRegistry.registerBlockEntityRenderer(event, AWStructureBlocks.STRUCTURE_BUILDER_TICKED_TILE,
                ignored -> new RenderStructureBuilder());
        ClientRegistry.registerBlockEntityRenderer(event, AWStructureBlocks.STAKE_TILE,
                ignored -> new StakeRenderer());
        ClientRegistry.registerBlockEntityRenderer(event, AWStructureBlocks.GRAVESTONE_TILE,
                ignored -> new RenderLootInfo<>());
        ClientRegistry.registerBlockEntityRenderer(event, AWStructureBlocks.URN_TILE,
                ignored -> new RenderLootInfo<>());
        ClientRegistry.registerBlockEntityRenderer(event, AWStructureBlocks.STATUE_TILE,
                ignored -> new StatueRenderer());
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
