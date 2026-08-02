package net.shadowmage.ancientwarfare.core.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.block.BlockEngineeringStation;
import net.shadowmage.ancientwarfare.core.block.BlockResearchStation;
import net.shadowmage.ancientwarfare.core.item.ItemBlockRotatableMetaTile;
import net.shadowmage.ancientwarfare.core.tile.LegacyBlockEntityRegistry;
import net.shadowmage.ancientwarfare.core.tile.TileEngineeringStation;
import net.shadowmage.ancientwarfare.core.tile.TileResearchStation;

@Mod.EventBusSubscriber(modid = AncientWarfareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AWCoreBlocks {
    private AWCoreBlocks() {
    }

    public static Block ENGINEERING_STATION;
    public static Block RESEARCH_STATION;
    public static BlockEntityType<TileEngineeringStation> ENGINEERING_STATION_TILE;
    public static BlockEntityType<TileResearchStation> RESEARCH_STATION_TILE;

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.BLOCKS, helper -> {
            ENGINEERING_STATION = new BlockEngineeringStation();
            RESEARCH_STATION = new BlockResearchStation();
            helper.register(new ResourceLocation(AncientWarfareCore.MOD_ID, "engineering_station"), ENGINEERING_STATION);
            helper.register(new ResourceLocation(AncientWarfareCore.MOD_ID, "research_station"), RESEARCH_STATION);
        });
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
            helper.register(new ResourceLocation(AncientWarfareCore.MOD_ID, "engineering_station"), new ItemBlockRotatableMetaTile(ENGINEERING_STATION));
            helper.register(new ResourceLocation(AncientWarfareCore.MOD_ID, "research_station"), new ItemBlockRotatableMetaTile(RESEARCH_STATION));
        });
        event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, helper -> {
            ENGINEERING_STATION_TILE = LegacyBlockEntityRegistry.register(helper,
                    new ResourceLocation(AncientWarfareCore.MOD_ID, "engineering_station_tile"),
                    TileEngineeringStation::new, ENGINEERING_STATION);
            RESEARCH_STATION_TILE = LegacyBlockEntityRegistry.register(helper,
                    new ResourceLocation(AncientWarfareCore.MOD_ID, "research_station_tile"),
                    TileResearchStation::new, RESEARCH_STATION);
        });
    }
}
