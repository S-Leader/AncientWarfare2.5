package net.shadowmage.ancientwarfare.npc.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import net.shadowmage.ancientwarfare.core.util.BlockTools;

import java.util.UUID;

public class ItemScythe extends ItemBaseNPC {
    private static final int HARVEST_RADIUS = 2;
    private static final UUID DAMAGE_UUID = UUID.fromString("67add96e-d32b-47cd-9335-9ad3b48fa069");
    private static final UUID SPEED_UUID = UUID.fromString("19bb340f-f8e5-4112-b316-49262f1bc03d");
    private final Tier material;
    private final Multimap<Attribute, AttributeModifier> attributes;

    public ItemScythe(Tier material, String registryName, float attackOffset, float attackSpeed) {
        super(registryName, new Item.Properties().stacksTo(1).durability(material.getUses()));
        this.material = material;
        attributes = ImmutableMultimap.of(
                Attributes.ATTACK_DAMAGE, new AttributeModifier(DAMAGE_UUID, "Weapon modifier", material.getAttackDamageBonus() + attackOffset, AttributeModifier.Operation.ADDITION),
                Attributes.ATTACK_SPEED, new AttributeModifier(SPEED_UUID, "Weapon modifier", attackSpeed, AttributeModifier.Operation.ADDITION));
    }

    @Override
    public int getEnchantmentValue() {
        return material.getEnchantmentValue();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? attributes : super.getDefaultAttributeModifiers(slot);
    }

    protected void applyPotionEffect(LivingEntity target) {
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        applyPotionEffect(target);
        stack.hurtAndBreak(1, attacker, entity -> entity.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        return true;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        Level level = player.level();
        boolean harvested = false;
        for (BlockPos current : BlockPos.betweenClosed(pos.offset(-HARVEST_RADIUS, -HARVEST_RADIUS, -HARVEST_RADIUS), pos.offset(HARVEST_RADIUS, HARVEST_RADIUS, HARVEST_RADIUS))) {
            BlockState state = level.getBlockState(current);
            if (state.is(BlockTags.CROPS) || state.getBlock() instanceof IPlantable) {
                harvested |= BlockTools.breakBlockAndDrop(level, current);
                level.levelEvent(player, 2001, current, Block.getId(state));
            }
        }
        if (harvested) stack.hurtAndBreak(2, player, entity -> entity.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        return harvested;
    }
}
