package net.shadowmage.ancientwarfare.npc.item;

import com.google.common.collect.Maps;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelLoader;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.npc.entity.NpcPlayerOwned;
import net.shadowmage.ancientwarfare.npc.entity.faction.NpcFaction;
import net.shadowmage.ancientwarfare.npc.init.AWNPCEntities;
import net.shadowmage.ancientwarfare.npc.init.AWNPCItems;
import net.shadowmage.ancientwarfare.npc.registry.FactionRegistry;
import net.shadowmage.ancientwarfare.npc.registry.NpcDefaultsRegistry;

import javax.annotation.Nullable;
import java.util.*;

public class ItemNpcSpawner extends ItemBaseNPC {

    private static final String NPC_TYPE_TAG = "npcType";
    private static final String NPC_SUBTYPE_TAG = "npcSubtype";
    private static final String FACTION_TAG = "faction";
    private static final String NPC_STORED_DATA_TAG = "npcStoredData";

    public ItemNpcSpawner() {
        super("npc_spawner", new Item.Properties().stacksTo(16));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<String> tooltip, TooltipFlag flagIn) {
        if (getNpcDisplayNameFromTag(stack).isPresent()) {
            tooltip.add(I18n.get(getDescriptionId(stack) + ".name"));
        }
        tooltip.add(I18n.get("guistrings.npc.spawner.right_click_to_place"));
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        String npcName = getNpcType(stack);
        if (npcName != null) {
            npcName = npcName.replace("faction.", "");
            String npcSub = getNpcSubtype(stack);
            if (!npcSub.isEmpty()) {
                npcName = npcName + "." + npcSub;
            }
            return "entity.ancientwarfarenpc." + (getFaction(stack).map(s -> s + ".").orElse("")) + npcName;
        }
        return super.getDescriptionId(stack);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return getNpcDisplayNameFromTag(stack).isPresent() ? getNpcDisplayNameFromTag(stack).get() : super.getItemStackDisplayName(stack);
    }

    private static Optional<String> getNpcDisplayNameFromTag(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag.contains(NPC_STORED_DATA_TAG) && tag.getCompound(NPC_STORED_DATA_TAG).contains("name")) {
                return Optional.of(tag.getCompound(NPC_STORED_DATA_TAG).getString("name"));
            }
        }
        return Optional.empty();
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        BlockPos hit = BlockTools.getBlockClickedOn(player, player.level(), true);
        if (hit == null) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }
        NpcBase npc = createNpcFromItem(player.level(), stack);
        if (npc != null) {
            if (npc instanceof NpcPlayerOwned) {
                npc.setOwner(player);
            }
            npc.setPos(hit.getX() + 0.5d, hit.getY(), hit.getZ() + 0.5d);
            npc.setHomeAreaAtCurrentPosition();
            player.level().addFreshEntity(npc);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    /*
     * create an NPC from the input item stack if valid, else return null<br>
     * npc will have type, subtype, equipment, levels, health, food and owner set from item.
     */
    public static NpcBase createNpcFromItem(Level world, ItemStack stack) {
        String type = getNpcType(stack);
        if (type == null) {
            return null;
        }
        String subType = getNpcSubtype(stack);
        Optional<String> faction = getFaction(stack);
        NpcBase npc = AWNPCEntities.createNpc(world, type, subType, faction.orElse(""));
        if (npc == null) {
            return null;
        }
        if (stack.hasTag() && stack.getTag().contains(NPC_STORED_DATA_TAG)) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                npc.setItemSlot(slot, ItemStack.EMPTY);
            }
            npc.readAdditionalItemData(stack.getTag().getCompound(NPC_STORED_DATA_TAG));
        }
        return npc;
    }

    /*
     * return an itemstack of npc spawner item that contains the data to spawn the input npc<br>
     * npc type, subtype, equipment, levels health, food value, and owner will be stored.
     */
    public static ItemStack getSpawnerItemForNpc(NpcBase npc) {
        String type = npc.getNpcType();
        String sub = npc.getNpcSubType();
        ItemStack stack = npc instanceof NpcFaction ? getStackForNpcType("faction." + type, sub, ((NpcFaction) npc).getFaction()) : getStackForNpcType(type, sub);
        CompoundTag tag = new CompoundTag();
        npc.writeAdditionalItemData(tag);
        stack.getOrCreateTag().put(NPC_STORED_DATA_TAG, tag);
        return stack;
    }

    public void getSubItems(CreativeModeTab tab, NonNullList<ItemStack> items) {
        getSpawnerSubItems(items);
    }

    private static void getSpawnerSubItems(NonNullList<ItemStack> list) {
        List<ItemStack> playerOwned = new ArrayList<>();
        List<ItemStack> factionOwned = new ArrayList<>();

        for (AWNPCEntities.NpcDeclaration dec : AWNPCEntities.getNpcMap().values()) {
            if (dec.canSpawnBaseEntity()) {
                if (dec.getNpcType().startsWith("faction.")) {
                    for (String factionName : FactionRegistry.getFactionNames()) {
                        if (NpcDefaultsRegistry.getFactionNpcDefault(factionName, getShortNpcType(dec)).isEnabled()) {
                            factionOwned.add(getStackForNpcType(dec.getNpcType(), "", factionName));
                        }
                    }
                } else {
                    playerOwned.add(getStackForNpcType(dec.getNpcType(), "", ""));
                }
            }
            for (String sub : dec.getSubTypes()) {
                playerOwned.add(getStackForNpcType(dec.getNpcType(), sub));
            }
        }

        list.addAll(playerOwned);
        list.addAll(factionOwned);
    }

    private static String getShortNpcType(AWNPCEntities.NpcDeclaration dec) {
        return dec.getNpcType().substring("faction.".length());
    }

    private static ItemStack getStackForNpcType(String type, String npcSubtype) {
        return getStackForNpcType(type, npcSubtype, "");
    }

    private static ItemStack getStackForNpcType(String type, String npcSubtype, String faction) {
        ItemStack stack = new ItemStack(AWNPCItems.NPC_SPAWNER);
        stack.getOrCreateTag().put(NPC_TYPE_TAG, StringTag.valueOf(type));
        stack.getOrCreateTag().put(NPC_SUBTYPE_TAG, StringTag.valueOf(npcSubtype));
        if (!faction.isEmpty()) {
            stack.getOrCreateTag().put(FACTION_TAG, StringTag.valueOf(faction));
        }
        return stack;
    }

    @Nullable
    public static String getNpcType(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(NPC_TYPE_TAG)) {
            return stack.getTag().getString(NPC_TYPE_TAG);
        }
        return null;
    }

    private static String getNpcSubtype(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(NPC_SUBTYPE_TAG)) {
            return stack.getTag().getString(NPC_SUBTYPE_TAG);
        }
        return "";
    }

    public static Optional<String> getFaction(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(FACTION_TAG)) {
            return Optional.of(stack.getTag().getString(FACTION_TAG));
        }
        return Optional.empty();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {

        final Map<String, ModelResourceLocation> modelLocations = Maps.newHashMap();

        AWNPCEntities.getNpcMap().values().stream().map(AWNPCEntities.NpcDeclaration::getItemModelVariants).flatMap(Collection::stream).distinct()
                .forEach(v -> {
                    modelLocations.put(v, getModelLocation(v));
                    LegacyModelLoader.registerItemVariants(this, modelLocations.get(v));
                });

        LegacyModelLoader.setCustomMeshDefinition(this, stack -> {
            String npcType = getNpcType(stack);
            if (npcType == null) {
                npcType = "worker";
            }
            AWNPCEntities.NpcDeclaration dec = AWNPCEntities.getNpcDeclaration(npcType);
            return dec == null ? null : modelLocations.get(dec.getItemModelVariant(getNpcSubtype(stack)));
        });
    }

    private ModelResourceLocation getModelLocation(String modelVariant) {
        return new ModelResourceLocation(new ResourceLocation(AncientWarfareCore.MOD_ID,
                "npc_spawner/" + modelVariant), "inventory");
    }
}
