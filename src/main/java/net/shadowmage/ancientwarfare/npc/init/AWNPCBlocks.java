package net.shadowmage.ancientwarfare.npc.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.shadowmage.ancientwarfare.core.item.ItemBlockOwned;
import net.shadowmage.ancientwarfare.core.tile.LegacyBlockEntityRegistry;
import net.shadowmage.ancientwarfare.core.util.LegacyBlockRegistryHelper;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.block.BlockTownHall;
import net.shadowmage.ancientwarfare.npc.tile.TileTownHall;

import static net.shadowmage.ancientwarfare.npc.AncientWarfareNPC.MOD_ID;

@Mod.EventBusSubscriber(modid = AncientWarfareNPC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AWNPCBlocks {
    private AWNPCBlocks() {
    }

    public static Block TOWN_HALL;
    public static BlockEntityType<TileTownHall> TOWN_HALL_TILE;

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.BLOCKS, helper ->
                TOWN_HALL = LegacyBlockRegistryHelper.register(helper, new BlockTownHall()));

        event.register(ForgeRegistries.Keys.ITEMS, helper ->
                LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockOwned(TOWN_HALL)));

        event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, helper ->
                TOWN_HALL_TILE = LegacyBlockEntityRegistry.register(helper,
                        new ResourceLocation(MOD_ID, "town_hall_tile"), TileTownHall::new, TOWN_HALL));
    }
}
