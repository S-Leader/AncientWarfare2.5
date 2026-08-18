package net.shadowmage.ancientwarfare.npc.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.input.IItemKeyInterface;
import net.shadowmage.ancientwarfare.core.input.IScrollableItem;
import net.shadowmage.ancientwarfare.core.input.InputHandler;
import net.shadowmage.ancientwarfare.core.util.RayTraceUtils;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.npc.npc_command.NpcCommand;
import net.shadowmage.ancientwarfare.npc.npc_command.NpcCommand.CommandType;

import javax.annotation.Nullable;
import java.util.*;

public class ItemCommandBaton extends ItemBaseNPC implements IItemKeyInterface, IScrollableItem {
    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("b397e70e-3cf8-4f6e-a644-596b26ba7fa0");
    private static final UUID ATTACK_SPEED_UUID = UUID.fromString("66d62a38-2cd0-4c48-bb1f-597d3fc27333");
    private final Tier material;
    private final Multimap<Attribute, AttributeModifier> attributes;
    private final int range = 120;

    public ItemCommandBaton(String name, Tier material) {
        super(name, propertiesFor(material));
        this.material = material;
        double attackDamage = 4 + material.getAttackDamageBonus();
        attributes = ImmutableMultimap.of(
                Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_UUID, "Weapon modifier", attackDamage, AttributeModifier.Operation.ADDITION),
                Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_UUID, "Weapon modifier", -2.3D, AttributeModifier.Operation.ADDITION));
    }

    private static Item.Properties propertiesFor(Tier material) {
        Item.Properties properties = new Item.Properties().stacksTo(1).durability(material.getUses());
        return material == net.minecraft.world.item.Tiers.NETHERITE ? properties.fireResistant() : properties;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (!stack.hasTag() || !stack.getOrCreateTag().contains("mode", Tag.TAG_STRING)) {
            stack.getOrCreateTag().putString("mode", BatonMode.getDefault().getName());
        }
        tooltip.add(Component.translatable("guistrings.npc.baton.add_remove").copy().append(" (RMB)"));
        tooltip.add(Component.literal(InputHandler.ALT_ITEM_USE_1.getTranslatedKeyMessage().getString() + " = Execute Command: ")
                .append(Component.translatable(getMode(stack).getTranslationKey())));
        tooltip.add(Component.literal("Use scroll wheel to change mode when sneaking"));
    }

    @Override
    public int getEnchantmentValue() {
        return material.getEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return material.getRepairIngredient().test(repair) || super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity attacked, LivingEntity wielder) {
        stack.hurtAndBreak(1, wielder, entity -> entity.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity wielder) {
        if (state.getDestroySpeed(level, pos) != 0) {
            stack.hurtAndBreak(2, wielder, entity -> entity.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? attributes : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && !player.isShiftKeyDown()) {
            HitResult hit = RayTraceUtils.getPlayerTarget(player, range, 0);
            if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof NpcBase npc
                    && npc.hasCommandPermissions(player.getUUID(), player.getName().getString())) {
                onNpcClicked(npc, stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void onKeyAction(Player player, ItemStack stack, ItemAltFunction altFunction) {
    }

    @Override
    public boolean onKeyActionClient(Player player, ItemStack stack, ItemAltFunction altFunction) {
        if (altFunction != ItemAltFunction.ALT_FUNCTION_1) {
            return false;
        }
        HitResult hit;
        switch (getMode(stack)) {
            case CLEAR_COMMAND -> NpcCommand.handleCommandClient(CommandType.CLEAR_COMMAND, null);
            case ATTACK -> {
                hit = RayTraceUtils.getPlayerTarget(player, range, 0);
                if (hit != null)
                    NpcCommand.handleCommandClient(hit.getType() == HitResult.Type.ENTITY ? CommandType.ATTACK : CommandType.ATTACK_AREA, hit);
            }
            case MOVE -> {
                hit = RayTraceUtils.getPlayerTarget(player, range, 0);
                if (hit != null)
                    NpcCommand.handleCommandClient(hit.getType() == HitResult.Type.ENTITY ? CommandType.GUARD : CommandType.MOVE, hit);
            }
            case SET_HOME -> sendBlockCommand(player, CommandType.SET_HOME);
            case CLEAR_HOME -> NpcCommand.handleCommandClient(CommandType.CLEAR_HOME, null);
            case SET_UPKEEP -> sendBlockCommand(player, CommandType.SET_UPKEEP);
            case CLEAR_UPKEEP -> NpcCommand.handleCommandClient(CommandType.CLEAR_UPKEEP, null);
        }
        return false;
    }

    private void sendBlockCommand(Player player, CommandType command) {
        HitResult hit = RayTraceUtils.getPlayerTarget(player, range, 0);
        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            NpcCommand.handleCommandClient(command, hit);
        }
    }

    private void onNpcClicked(NpcBase npc, ItemStack stack) {
        CommandSet.loadFromStack(stack).onNpcClicked(npc, stack);
    }

    public static List<Entity> getCommandedEntities(Level level, ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemCommandBaton)) {
            return new ArrayList<>();
        }
        return CommandSet.loadFromStack(stack).getEntities(level);
    }

    @Override
    public boolean onScrollUp(Level level, Player player, ItemStack stack) {
        if (!level.isClientSide) changeMode(getMode(stack).next(), player, stack);
        return true;
    }

    @Override
    public boolean onScrollDown(Level level, Player player, ItemStack stack) {
        if (!level.isClientSide) changeMode(getMode(stack).previous(), player, stack);
        return true;
    }

    private static class CommandSet {
        private final Set<UUID> ids = new HashSet<>();

        static CommandSet loadFromStack(ItemStack stack) {
            CommandSet set = new CommandSet();
            if (stack.hasTag() && stack.getTag().contains("entityList", Tag.TAG_COMPOUND)) {
                set.readFromNBT(stack.getTag().getCompound("entityList"));
            }
            return set;
        }

        private void writeToStack(ItemStack stack) {
            stack.getOrCreateTag().put("entityList", writeToNBT());
        }

        private void readFromNBT(CompoundTag tag) {
            ListTag entryList = tag.getList("entryList", Tag.TAG_COMPOUND);
            for (int i = 0; i < entryList.size(); i++) {
                CompoundTag idTag = entryList.getCompound(i);
                if (idTag.hasUUID("uuid")) ids.add(idTag.getUUID("uuid"));
            }
        }

        private CompoundTag writeToNBT() {
            CompoundTag tag = new CompoundTag();
            ListTag entryList = new ListTag();
            for (UUID id : ids) {
                CompoundTag idTag = new CompoundTag();
                idTag.putUUID("uuid", id);
                entryList.add(idTag);
            }
            tag.put("entryList", entryList);
            return tag;
        }

        void onNpcClicked(NpcBase npc, ItemStack stack) {
            UUID id = npc.getUUID();
            if (!ids.remove(id)) ids.add(id);
            validateEntities(npc.level());
            writeToStack(stack);
        }

        List<Entity> getEntities(Level level) {
            List<Entity> result = Lists.newArrayList();
            if (level instanceof ServerLevel serverLevel) {
                for (UUID id : ids) {
                    Entity entity = serverLevel.getEntity(id);
                    if (entity != null) result.add(entity);
                }
            } else if (level instanceof ClientLevel clientLevel) {
                for (Entity entity : clientLevel.entitiesForRendering()) {
                    if (ids.contains(entity.getUUID())) result.add(entity);
                }
            }
            return result;
        }

        private void validateEntities(Level level) {
            if (level instanceof ServerLevel serverLevel) {
                Iterator<UUID> it = ids.iterator();
                while (it.hasNext()) {
                    UUID id = it.next();
                    if (serverLevel.getEntity(id) == null) it.remove();
                }
            }
        }
    }

    private void changeMode(BatonMode mode, Player player, ItemStack stack) {
        player.displayClientMessage(Component.translatable(mode.getTranslationKey()), true);
        stack.getOrCreateTag().putString("mode", mode.getName());
    }

    private BatonMode getMode(ItemStack stack) {
        return stack.hasTag() ? BatonMode.fromName(stack.getTag().getString("mode")) : BatonMode.CLEAR_COMMAND;
    }

    public enum BatonMode {
        CLEAR_COMMAND("clear", "guistrings.npc.baton.clear"), ATTACK("attack", "guistrings.npc.baton.attack"),
        MOVE("move", "guistrings.npc.baton.move"), SET_HOME("setHome", "guistrings.npc.baton.set_home"),
        CLEAR_HOME("clearHome", "guistrings.npc.baton.clear_home"), SET_UPKEEP("setUpkeep", "guistrings.npc.baton.set_upkeep"),
        CLEAR_UPKEEP("clearUpkeep", "guistrings.npc.baton.clear_upkeep");

        private final String name;
        private final String key;
        private static final ImmutableMap<String, BatonMode> NAME_TO_MODE;

        BatonMode(String name, String key) {
            this.name = name;
            this.key = key;
        }

        public String getTranslationKey() {
            return key;
        }

        public static BatonMode getDefault() {
            return CLEAR_COMMAND;
        }

        public BatonMode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public BatonMode previous() {
            return values()[(ordinal() - 1 + values().length) % values().length];
        }

        public static BatonMode fromName(String name) {
            return NAME_TO_MODE.getOrDefault(name, CLEAR_COMMAND);
        }

        public String getName() {
            return name;
        }

        static {
            ImmutableMap.Builder<String, BatonMode> builder = ImmutableMap.builder();
            for (BatonMode mode : values()) builder.put(mode.name, mode);
            NAME_TO_MODE = builder.build();
        }
    }
}
