package net.shadowmage.ancientwarfare.core.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

import java.util.HashMap;
import java.util.Map;

/**
 * Adds Ancient Warfare loot to vanilla chest tables.
 *
 * <p>Forge 1.20 loads the actual tables from {@code data/ancientwarfare/loot_tables};
 * this class only attaches references to the seven vanilla tables that the 1.12
 * version augmented by mutating private loot-pool fields.</p>
 */
@Mod.EventBusSubscriber(modid = AncientWarfareCore.MOD_ID)
public final class AWCoreLoot {
    private static final Map<String, String> INJECTIONS = new HashMap<>();

    static {
        INJECTIONS.put("chests/abandoned_mineshaft", "abandoned_mineshaft");
        INJECTIONS.put("chests/desert_pyramid", "desert_pyramid");
        INJECTIONS.put("chests/igloo_chest", "igloo_chest");
        INJECTIONS.put("chests/jungle_temple", "jungle_temple");
        INJECTIONS.put("chests/simple_dungeon", "simple_dungeon");
        INJECTIONS.put("chests/stronghold_corridor", "stronghold_corridor");
        // The old blacksmith table was split into profession-specific village tables.
        INJECTIONS.put("chests/village/village_armorer", "village_blacksmith");
        INJECTIONS.put("chests/village/village_toolsmith", "village_blacksmith");
        INJECTIONS.put("chests/village/village_weaponsmith", "village_blacksmith");
    }

    private AWCoreLoot() {
    }

    /**
     * Data-pack loot tables need no eager Java registration on modern Minecraft.
     */
    public static void init() {
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        String injectedName = INJECTIONS.get(event.getName().getPath());
        if (injectedName == null || !"minecraft".equals(event.getName().getNamespace())) {
            return;
        }

        ResourceLocation injectedTable = new ResourceLocation(
                AncientWarfareCore.MOD_ID, "chests/inject/" + injectedName);
        LootPool pool = LootPool.lootPool()
                .name("ancientwarfare_inject_" + injectedName)
                .setRolls(ConstantValue.exactly(1))
                .add(LootTableReference.lootTableReference(injectedTable))
                .build();
        event.getTable().addPool(pool);
    }
}
