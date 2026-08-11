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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.item.*;
import net.shadowmage.ancientwarfare.npc.registry.FactionDefinition;
import net.shadowmage.ancientwarfare.npc.registry.FactionRegistry;
import net.shadowmage.ancientwarfare.structure.block.BlockFlag;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.item.ItemBlockColored;

import java.util.Map;

@Mod.EventBusSubscriber(modid = AncientWarfareNPC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AWNPCItems {
    private AWNPCItems() {
    }

    public static ItemCommandBaton IRON_COMMAND_BATON;
    public static ItemShield WOODEN_SHIELD;
    public static Item NPC_SPAWNER;
    public static Item WORK_ORDER;
    public static Item UPKEEP_ORDER;
    public static Item COMBAT_ORDER;
    public static Item ROUTING_ORDER;
    public static Item TRADE_ORDER;
    /** Legacy metadata item kept only so old saves/templates can deserialize. */
    @Deprecated
    public static Item BARD_INSTRUMENT;
    public static ItemBardInstrument BARD_INSTRUMENT_LUTE;
    public static ItemBardInstrument BARD_INSTRUMENT_FLUTE;
    public static ItemBardInstrument BARD_INSTRUMENT_HARP;
    public static ItemBardInstrument BARD_INSTRUMENT_DRUM;
    public static Item COIN;

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS, registry -> {
            item(registry, "wooden_command_baton", new ItemCommandBaton("wooden_command_baton", Tiers.WOOD));
            item(registry, "stone_command_baton", new ItemCommandBaton("stone_command_baton", Tiers.STONE));
            IRON_COMMAND_BATON = item(registry, "iron_command_baton", new ItemCommandBaton("iron_command_baton", Tiers.IRON));
            item(registry, "gold_command_baton", new ItemCommandBaton("gold_command_baton", Tiers.GOLD));
            item(registry, "diamond_command_baton", new ItemCommandBaton("diamond_command_baton", Tiers.DIAMOND));

            BARD_INSTRUMENT = item(registry, "bard_instrument", new ItemBardInstrument("bard_instrument"));
            BARD_INSTRUMENT_LUTE = item(registry, "bard_instrument_lute",
                    new ItemBardInstrument("bard_instrument_lute", ItemBardInstrument.Instrument.LUTE));
            BARD_INSTRUMENT_FLUTE = item(registry, "bard_instrument_flute",
                    new ItemBardInstrument("bard_instrument_flute", ItemBardInstrument.Instrument.FLUTE));
            BARD_INSTRUMENT_HARP = item(registry, "bard_instrument_harp",
                    new ItemBardInstrument("bard_instrument_harp", ItemBardInstrument.Instrument.HARP));
            BARD_INSTRUMENT_DRUM = item(registry, "bard_instrument_drum",
                    new ItemBardInstrument("bard_instrument_drum", ItemBardInstrument.Instrument.DRUM));

            WOODEN_SHIELD = item(registry, "wooden_shield", new ItemShield("wooden_shield", Tiers.WOOD, 336));
            item(registry, "stone_shield", new ItemShield("stone_shield", Tiers.STONE, 506));
            item(registry, "iron_shield", new ItemShield("iron_shield", Tiers.IRON, 759));
            item(registry, "gold_shield", new ItemShield("gold_shield", Tiers.GOLD, 64));
            item(registry, "diamond_shield", new ItemShield("diamond_shield", Tiers.DIAMOND, 1138));
            item(registry, "shield_tribal_1", new ItemShield("shield_tribal_1", Tiers.WOOD, 336));
            item(registry, "shield_tribal_2", new ItemShield("shield_tribal_2", Tiers.WOOD, 336));
            item(registry, "shield_round_1", new ItemShield("shield_round_1", Tiers.WOOD, 336));
            item(registry, "shield_round_2", new ItemShield("shield_round_2", Tiers.WOOD, 336));
            item(registry, "shield_round_3", new ItemShield("shield_round_3", Tiers.WOOD, 336));
            item(registry, "shield_round_4", new ItemShield("shield_round_4", Tiers.WOOD, 336));
            item(registry, "shield_round_5", new ItemShield("shield_round_5", Tiers.WOOD, 336));
            item(registry, "shield_round_6", new ItemShield("shield_round_6", Tiers.WOOD, 336));
            item(registry, "shield_witchbane_1", new ItemShield("shield_witchbane_1", Tiers.IRON, 759));
            item(registry, "shield_witchbane_2", new ItemShield("shield_witchbane_2", Tiers.IRON, 759));
            item(registry, "shield_buffloka", new ItemShield("shield_buffloka", Tiers.WOOD, 336));

            WORK_ORDER = item(registry, "work_order", new ItemWorkOrder());
            UPKEEP_ORDER = item(registry, "upkeep_order", new ItemUpkeepOrder());
            COMBAT_ORDER = item(registry, "combat_order", new ItemCombatOrder());
            ROUTING_ORDER = item(registry, "routing_order", new ItemRoutingOrder());
            TRADE_ORDER = item(registry, "trade_order", new ItemTradeOrder());
            NPC_SPAWNER = item(registry, "npc_spawner", new ItemNpcSpawner());

            COIN = item(registry, "coin", new ItemCoin());
            item(registry, "macuahuitl", new ItemMacuahuitl(Tiers.IRON, "macuahuitl"));
            item(registry, "sickle", new ItemSickle(Tiers.IRON, -2.3F));
            item(registry, "pitchfork", new ItemPitchfork(Tiers.IRON, -2.3F));
            item(registry, "scythe", new ItemScythe(Tiers.IRON, "scythe", 0.0F, -2.3F));
            item(registry, "death_scythe", new ItemScythe(Tiers.DIAMOND, "death_scythe", 0.0F, -2.3F) {
                @Override
                protected void applyPotionEffect(LivingEntity target) {
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100));
                }
            });
            item(registry, "giant_club", new ItemClub(Tiers.DIAMOND, "giant_club", 3.5, -3.6D, 4.2F));
            item(registry, "ice_spear", new ItemIceSpear(Tiers.DIAMOND, "ice_spear", 2, -3, 4.2F));

            item(registry, "food_bundle", new ItemFoodBundle());

            registerExtendedReachWeapons(registry, "spear", 2, -3, 4.2F);
            registerExtendedReachWeapons(registry, "halberd", 3, -3.2D, 4.5F);
            registerExtendedReachWeapons(registry, "lance", 2.5D, -3.2D, 5.5F);
            registerExtendedReachWeapons(registry, "cleaver", 3.5D, -2.8D, 3.0F);

            registerUniqueExtendedReachWeapon(registry, "obsidian_spear", 2, -3, 4.2F);
        });
    }

    private static void registerExtendedReachWeapons(RegisterEvent.RegisterHelper<Item> registry, String name, double attackOffset, double attackSpeed, float reach) {
        registerWeapon(registry, Tiers.WOOD, "wooden_" + name, attackOffset, attackSpeed, reach);
        registerWeapon(registry, Tiers.STONE, "stone_" + name, attackOffset, attackSpeed, reach);
        registerWeapon(registry, Tiers.IRON, "iron_" + name, attackOffset, attackSpeed, reach);
        registerWeapon(registry, Tiers.GOLD, "golden_" + name, attackOffset, attackSpeed, reach);
        registerWeapon(registry, Tiers.DIAMOND, "diamond_" + name, attackOffset, attackSpeed, reach);
    }

    private static void registerUniqueExtendedReachWeapon(RegisterEvent.RegisterHelper<Item> registry, String name, double attackOffset, double attackSpeed, float reach) {
        registerWeapon(registry, Tiers.DIAMOND, name, attackOffset, attackSpeed, reach);
    }

    private static void registerWeapon(RegisterEvent.RegisterHelper<Item> registry, Tier tier, String name, double attackOffset, double attackSpeed, float reach) {
        item(registry, name, new ItemExtendedReachWeapon(tier, name, attackOffset, attackSpeed, reach));
    }

    private static <T extends Item> T item(RegisterEvent.RegisterHelper<Item> registry, String name, T value) {
        registry.register(new ResourceLocation(AncientWarfareNPC.MOD_ID, name), value);
        return value;
    }

    public static void addFactionBlocks() {
        for (FactionDefinition definition : FactionRegistry.getFactionDefinitions()) {
            AWStructureBlocks.PROTECTION_FLAG.addFlagDefinition(new BlockFlag.FlagDefinition(
                    definition.getName()));

            AWStructureBlocks.DECORATIVE_FLAG.addFlagDefinition(new BlockFlag.FlagDefinition(
                    definition.getName()));

            for (Map.Entry<String, CompoundTag> blockData : definition.getThemedBlocksTags().entrySet()) {
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockData.getKey()));
                if (block == null) {
                    AncientWarfareNPC.LOG.warn("Can't find block with registry name {} in block registry, skipping...", blockData.getKey());
                    continue;
                }
                Item itemBlock = block.asItem();
                if (itemBlock instanceof ItemBlockColored) {
                    blockData.getValue().putString("unlocalizedNamePart", "faction");
                    ((ItemBlockColored) itemBlock).addCustomItemTag(blockData.getValue());
                }
            }
        }
    }
}
