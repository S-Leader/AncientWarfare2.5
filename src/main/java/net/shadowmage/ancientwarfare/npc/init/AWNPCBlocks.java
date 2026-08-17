package net.shadowmage.ancientwarfare.npc.init;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.core.item.ItemBlockOwned;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.block.BlockTownHall;
import net.shadowmage.ancientwarfare.npc.tile.TileTownHall;

public final class AWNPCBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AncientWarfareNPC.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AncientWarfareNPC.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AncientWarfareNPC.MOD_ID);

    public static final RegistryObject<BlockTownHall> TOWN_HALL = BLOCKS.register("town_hall",
            () -> new BlockTownHall().setBlockEntityType(AWNPCBlocks.TOWN_HALL_TILE));
    public static final RegistryObject<Item> TOWN_HALL_ITEM = ITEMS.register("town_hall", () -> new ItemBlockOwned(AWNPCBlocks.TOWN_HALL.get()));
    public static final RegistryObject<BlockEntityType<TileTownHall>> TOWN_HALL_TILE = BLOCK_ENTITIES.register("town_hall_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new TileTownHall(AWNPCBlocks.TOWN_HALL_TILE.get(), pos, state), AWNPCBlocks.TOWN_HALL.get()).build(null));

    private AWNPCBlocks() {}

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }
}
