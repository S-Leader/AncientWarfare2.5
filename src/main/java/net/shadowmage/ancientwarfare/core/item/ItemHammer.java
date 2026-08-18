package net.shadowmage.ancientwarfare.core.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.shadowmage.ancientwarfare.core.block.BlockBase;
import net.shadowmage.ancientwarfare.core.input.IItemKeyInterface;
import net.shadowmage.ancientwarfare.core.input.InputHandler;
import net.shadowmage.ancientwarfare.core.interfaces.IWorkSite;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ItemHammer extends ItemBaseCore implements IItemKeyInterface {
    public static final String WORK_MODE_TAG = "workMode";

    private final Tier material;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public ItemHammer(String regName, Tier material) {
        //durability() already forces max stack 1; stacksTo() throws once durability is set.
        super(regName, propertiesFor(material));
        this.material = material;
        double attackDamage = 4.0D + material.getAttackDamageBonus();
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", attackDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -3.1D, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    private static Item.Properties propertiesFor(Tier material) {
        Item.Properties properties = new Item.Properties().durability(material.getUses());
        return material == net.minecraft.world.item.Tiers.NETHERITE ? properties.fireResistant() : properties;
    }

    public Tier getMaterial() {
        return material;
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
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, entity -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(2, miner, entity -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String key = InputHandler.ALT_ITEM_USE_1.getTranslatedKeyMessage().getString();
        tooltip.add(Component.translatable("guistrings.core.hammer.use_primary_item_key", key));
        tooltip.add(Component.translatable(stack.getOrCreateTag().getBoolean(WORK_MODE_TAG)
                ? "guistrings.core.hammer.work_mode"
                : "guistrings.core.hammer.rotate_mode"));
    }

    @Override
    public boolean onKeyActionClient(Player player, ItemStack stack, ItemAltFunction altFunction) {
        return altFunction == ItemAltFunction.ALT_FUNCTION_1;
    }

    @Override
    public void onKeyAction(Player player, ItemStack stack, ItemAltFunction altFunction) {
        if (player.level().isClientSide) {
            return;
        }
        boolean workMode = !stack.getOrCreateTag().getBoolean(WORK_MODE_TAG);
        stack.getOrCreateTag().putBoolean(WORK_MODE_TAG, workMode);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.3F, 0.6F);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        BlockPos pos = hit.getBlockPos();
        if (stack.getOrCreateTag().getBoolean(WORK_MODE_TAG)) {
            Optional<IWorkSite> worksite = WorldTools.getTile(level, pos, IWorkSite.class).filter(IWorkSite::hasWork);
            if (worksite.isPresent()) {
                worksite.get().addEnergyFromPlayer(player);
                playSound(level, pos, SoundEvents.PISTON_CONTRACT);
            } else if (!level.isEmptyBlock(pos)) {
                playBlockSound(level, pos, level.getBlockState(pos));
            }
        } else if (!level.isEmptyBlock(pos)) {
            BlockState state = level.getBlockState(pos);
            if (rotateBlock(level, pos, state, hit.getDirection())) {
                playSound(level, pos, SoundEvents.PISTON_EXTEND);
            } else {
                playBlockSound(level, pos, state);
            }
        }
        return InteractionResultHolder.success(stack);
    }

    private boolean rotateBlock(Level level, BlockPos pos, BlockState state, Direction face) {
        if (state.getBlock() instanceof BlockBase awBlock) {
            return awBlock.rotateBlock(level, pos, face);
        }
        BlockState rotated = state.rotate(level, pos, Rotation.CLOCKWISE_90);
        if (rotated != state) {
            level.setBlock(pos, rotated, 3);
            return true;
        }
        return false;
    }

    private void playSound(Level level, BlockPos pos, SoundEvent sound) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 0.2F, level.getRandom().nextFloat() * 0.15F + 0.6F);
    }

    private void playBlockSound(Level level, BlockPos pos, BlockState state) {
        SoundType sound = state.getSoundType(level, pos, null);
        level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS, sound.getVolume() * 0.5F, sound.getPitch() * 0.8F);
    }
}
