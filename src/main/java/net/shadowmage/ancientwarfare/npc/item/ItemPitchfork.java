package net.shadowmage.ancientwarfare.npc.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

import java.util.UUID;

public class ItemPitchfork extends ItemBaseNPC {
    private static final UUID DAMAGE_UUID = UUID.fromString("860236a3-37a1-442d-84c1-29f481dd15db");
    private static final UUID SPEED_UUID = UUID.fromString("b83aa857-411e-4614-8074-c20181485370");
    private final Tier material;
    private final Multimap<Attribute, AttributeModifier> attributes;

    public ItemPitchfork(Tier material, float attackSpeed) {
        super("pitchfork", new Item.Properties().stacksTo(1).durability(material.getUses()));
        this.material = material;
        attributes = ImmutableMultimap.of(
                Attributes.ATTACK_DAMAGE, new AttributeModifier(DAMAGE_UUID, "Weapon modifier", material.getAttackDamageBonus(), AttributeModifier.Operation.ADDITION),
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
}
