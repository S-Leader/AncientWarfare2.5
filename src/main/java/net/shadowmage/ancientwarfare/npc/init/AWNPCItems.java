package net.shadowmage.ancientwarfare.npc.init;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.item.*;
import net.shadowmage.ancientwarfare.npc.registry.FactionDefinition;
import net.shadowmage.ancientwarfare.npc.registry.FactionRegistry;
import net.shadowmage.ancientwarfare.structure.block.BlockFlag;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.item.ItemBlockColored;

import java.util.Map;
import java.util.function.Supplier;

public final class AWNPCItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AncientWarfareNPC.MOD_ID);

    public static final RegistryObject<ItemCommandBaton> WOODEN_COMMAND_BATON = item("wooden_command_baton", () -> new ItemCommandBaton("wooden_command_baton", Tiers.WOOD));
    public static final RegistryObject<ItemCommandBaton> STONE_COMMAND_BATON = item("stone_command_baton", () -> new ItemCommandBaton("stone_command_baton", Tiers.STONE));
    public static final RegistryObject<ItemCommandBaton> IRON_COMMAND_BATON = item("iron_command_baton", () -> new ItemCommandBaton("iron_command_baton", Tiers.IRON));
    public static final RegistryObject<ItemCommandBaton> GOLD_COMMAND_BATON = item("gold_command_baton", () -> new ItemCommandBaton("gold_command_baton", Tiers.GOLD));
    public static final RegistryObject<ItemCommandBaton> DIAMOND_COMMAND_BATON = item("diamond_command_baton", () -> new ItemCommandBaton("diamond_command_baton", Tiers.DIAMOND));

    public static final RegistryObject<ItemBardInstrument> BARD_INSTRUMENT_LUTE = item("bard_instrument_lute", () -> new ItemBardInstrument("bard_instrument_lute", ItemBardInstrument.Instrument.LUTE));
    public static final RegistryObject<ItemBardInstrument> BARD_INSTRUMENT_FLUTE = item("bard_instrument_flute", () -> new ItemBardInstrument("bard_instrument_flute", ItemBardInstrument.Instrument.FLUTE));
    public static final RegistryObject<ItemBardInstrument> BARD_INSTRUMENT_HARP = item("bard_instrument_harp", () -> new ItemBardInstrument("bard_instrument_harp", ItemBardInstrument.Instrument.HARP));
    public static final RegistryObject<ItemBardInstrument> BARD_INSTRUMENT_DRUM = item("bard_instrument_drum", () -> new ItemBardInstrument("bard_instrument_drum", ItemBardInstrument.Instrument.DRUM));

    public static final RegistryObject<ItemShield> WOODEN_SHIELD = shield("wooden_shield", Tiers.WOOD, 336);
    public static final RegistryObject<ItemShield> STONE_SHIELD = shield("stone_shield", Tiers.STONE, 506);
    public static final RegistryObject<ItemShield> IRON_SHIELD = shield("iron_shield", Tiers.IRON, 759);
    public static final RegistryObject<ItemShield> GOLD_SHIELD = shield("gold_shield", Tiers.GOLD, 64);
    public static final RegistryObject<ItemShield> DIAMOND_SHIELD = shield("diamond_shield", Tiers.DIAMOND, 1138);

    static {
        shield("shield_tribal_1", Tiers.WOOD, 336); shield("shield_tribal_2", Tiers.WOOD, 336);
        shield("shield_round_1", Tiers.WOOD, 336); shield("shield_round_2", Tiers.WOOD, 336);
        shield("shield_round_3", Tiers.WOOD, 336); shield("shield_round_4", Tiers.WOOD, 336);
        shield("shield_round_5", Tiers.WOOD, 336); shield("shield_round_6", Tiers.WOOD, 336);
        shield("shield_witchbane_1", Tiers.IRON, 759); shield("shield_witchbane_2", Tiers.IRON, 759);
        shield("shield_buffloka", Tiers.WOOD, 336);
    }

    public static final RegistryObject<ItemWorkOrder> WORK_ORDER = item("work_order", ItemWorkOrder::new);
    public static final RegistryObject<ItemUpkeepOrder> UPKEEP_ORDER = item("upkeep_order", ItemUpkeepOrder::new);
    public static final RegistryObject<ItemCombatOrder> COMBAT_ORDER = item("combat_order", ItemCombatOrder::new);
    public static final RegistryObject<ItemRoutingOrder> ROUTING_ORDER = item("routing_order", ItemRoutingOrder::new);
    public static final RegistryObject<ItemTradeOrder> TRADE_ORDER = item("trade_order", ItemTradeOrder::new);

    @Deprecated public static final RegistryObject<ItemNpcSpawner> NPC_SPAWNER = item("npc_spawner", ItemNpcSpawner::new);
    public static final RegistryObject<ItemNpcSpawner> NPC_SPAWNER_WORKER = spawner("npc_spawner_worker", "worker");
    public static final RegistryObject<ItemNpcSpawner> NPC_SPAWNER_COMBAT = spawner("npc_spawner_combat", "combat");
    public static final RegistryObject<ItemNpcSpawner> NPC_SPAWNER_COURIER = spawner("npc_spawner_courier", "courier");
    public static final RegistryObject<ItemNpcSpawner> NPC_SPAWNER_TRADER = spawner("npc_spawner_trader", "trader");
    public static final RegistryObject<ItemNpcSpawner> NPC_SPAWNER_PRIEST = spawner("npc_spawner_priest", "priest");
    public static final RegistryObject<ItemNpcSpawner> NPC_SPAWNER_BARD = spawner("npc_spawner_bard", "bard");
    public static final RegistryObject<ItemNpcSpawner> NPC_SPAWNER_SIEGE_ENGINEER = spawner("npc_spawner_siege_engineer", "siege_engineer");

    public static final RegistryObject<ItemCoin> COIN = item("coin", ItemCoin::new);

    static {
        item("macuahuitl", () -> new ItemMacuahuitl(Tiers.IRON, "macuahuitl"));
        item("sickle", () -> new ItemSickle(Tiers.IRON, -2.3F));
        item("pitchfork", () -> new ItemPitchfork(Tiers.IRON, -2.3F));
        item("scythe", () -> new ItemScythe(Tiers.IRON, "scythe", 0.0F, -2.3F));
        item("death_scythe", () -> new ItemScythe(Tiers.DIAMOND, "death_scythe", 0.0F, -2.3F) {
            @Override protected void applyPotionEffect(LivingEntity target) { target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100)); }
        });
        item("giant_club", () -> new ItemClub(Tiers.DIAMOND, "giant_club", 3.5, -3.6D, 4.2F));
        item("ice_spear", () -> new ItemIceSpear(Tiers.DIAMOND, "ice_spear", 2, -3, 4.2F));
        item("food_bundle", ItemFoodBundle::new);
        extendedReachWeapons("spear", 2, -3, 4.2F);
        extendedReachWeapons("halberd", 3, -3.2D, 4.5F);
        extendedReachWeapons("lance", 2.5D, -3.2D, 5.5F);
        extendedReachWeapons("cleaver", 3.5D, -2.8D, 3.0F);
        weapon(Tiers.DIAMOND, "obsidian_spear", 2, -3, 4.2F);
    }

    private AWNPCItems() {}

    public static void register(IEventBus modBus) { ITEMS.register(modBus); }

    public static ItemNpcSpawner getNpcSpawnerItem(String npcType) {
        RegistryObject<ItemNpcSpawner> object = switch (npcType) {
            case "worker" -> NPC_SPAWNER_WORKER;
            case "combat" -> NPC_SPAWNER_COMBAT;
            case "courier" -> NPC_SPAWNER_COURIER;
            case "trader" -> NPC_SPAWNER_TRADER;
            case "priest" -> NPC_SPAWNER_PRIEST;
            case "bard" -> NPC_SPAWNER_BARD;
            case "siege_engineer" -> NPC_SPAWNER_SIEGE_ENGINEER;
            default -> NPC_SPAWNER;
        };
        return object.get();
    }

    private static void extendedReachWeapons(String name, double attackOffset, double attackSpeed, float reach) {
        weapon(Tiers.WOOD, "wooden_" + name, attackOffset, attackSpeed, reach);
        weapon(Tiers.STONE, "stone_" + name, attackOffset, attackSpeed, reach);
        weapon(Tiers.IRON, "iron_" + name, attackOffset, attackSpeed, reach);
        weapon(Tiers.GOLD, "golden_" + name, attackOffset, attackSpeed, reach);
        weapon(Tiers.DIAMOND, "diamond_" + name, attackOffset, attackSpeed, reach);
    }

    private static void weapon(Tier tier, String name, double attackOffset, double attackSpeed, float reach) {
        item(name, () -> new ItemExtendedReachWeapon(tier, name, attackOffset, attackSpeed, reach));
    }

    private static RegistryObject<ItemShield> shield(String name, Tier tier, int durability) {
        return item(name, () -> new ItemShield(name, tier, durability));
    }

    private static RegistryObject<ItemNpcSpawner> spawner(String name, String npcType) {
        return item(name, () -> new ItemNpcSpawner(name, npcType));
    }

    private static <T extends Item> RegistryObject<T> item(String name, Supplier<T> value) { return ITEMS.register(name, value); }

    public static void addFactionBlocks() {
        for (FactionDefinition definition : FactionRegistry.getFactionDefinitions()) {
            AWStructureBlocks.PROTECTION_FLAG.get().addFlagDefinition(new BlockFlag.FlagDefinition(definition.getName()));
            AWStructureBlocks.DECORATIVE_FLAG.get().addFlagDefinition(new BlockFlag.FlagDefinition(definition.getName()));
            for (Map.Entry<String, CompoundTag> blockData : definition.getThemedBlocksTags().entrySet()) {
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockData.getKey()));
                if (block == null) {
                    AncientWarfareNPC.LOG.warn("Can't find block with registry name {} in block registry, skipping...", blockData.getKey());
                    continue;
                }
                Item itemBlock = block.asItem();
                if (itemBlock instanceof ItemBlockColored colored) {
                    blockData.getValue().putString("unlocalizedNamePart", "faction");
                    colored.addCustomItemTag(blockData.getValue());
                }
            }
        }
    }
}
