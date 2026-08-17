package net.shadowmage.ancientwarfare.core.init;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.block.BlockEngineeringStation;
import net.shadowmage.ancientwarfare.core.block.BlockResearchStation;
import net.shadowmage.ancientwarfare.core.item.ItemBlockRotatableMetaTile;
import net.shadowmage.ancientwarfare.core.tile.TileEngineeringStation;
import net.shadowmage.ancientwarfare.core.tile.TileResearchStation;

public final class AWCoreBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AncientWarfareCore.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AncientWarfareCore.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AncientWarfareCore.MOD_ID);

    public static final RegistryObject<BlockEngineeringStation> ENGINEERING_STATION = BLOCKS.register("engineering_station",
            () -> new BlockEngineeringStation().setBlockEntityType(AWCoreBlocks.ENGINEERING_STATION_TILE));
    public static final RegistryObject<BlockResearchStation> RESEARCH_STATION = BLOCKS.register("research_station",
            () -> new BlockResearchStation().setBlockEntityType(AWCoreBlocks.RESEARCH_STATION_TILE));

    public static final RegistryObject<Item> ENGINEERING_STATION_ITEM = ITEMS.register("engineering_station",
            () -> new ItemBlockRotatableMetaTile(AWCoreBlocks.ENGINEERING_STATION.get()));
    public static final RegistryObject<Item> RESEARCH_STATION_ITEM = ITEMS.register("research_station",
            () -> new ItemBlockRotatableMetaTile(AWCoreBlocks.RESEARCH_STATION.get()));

    public static final RegistryObject<BlockEntityType<TileEngineeringStation>> ENGINEERING_STATION_TILE = BLOCK_ENTITIES.register(
            "engineering_station_tile", () -> BlockEntityType.Builder.of(
                    (pos, state) -> new TileEngineeringStation(AWCoreBlocks.ENGINEERING_STATION_TILE.get(), pos, state),
                    AWCoreBlocks.ENGINEERING_STATION.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileResearchStation>> RESEARCH_STATION_TILE = BLOCK_ENTITIES.register(
            "research_station_tile", () -> BlockEntityType.Builder.of(
                    (pos, state) -> new TileResearchStation(AWCoreBlocks.RESEARCH_STATION_TILE.get(), pos, state),
                    AWCoreBlocks.RESEARCH_STATION.get()).build(null));

    private AWCoreBlocks() {}

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }
}
