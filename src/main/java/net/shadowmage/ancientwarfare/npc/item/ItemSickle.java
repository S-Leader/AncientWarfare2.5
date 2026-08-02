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

public class ItemSickle extends ItemBaseNPC {
    private static final UUID DAMAGE_UUID = UUID.fromString("ae806d12-f62a-4751-9ef3-d37b544fb12e");
    private static final UUID SPEED_UUID = UUID.fromString("dc128288-b8b2-4864-8dd0-3933cc7c65ee");
    private final Tier material;
    private final Multimap<Attribute, AttributeModifier> attributes;

    public ItemSickle(Tier material, double attackSpeed) {
        super("sickle", new Item.Properties().stacksTo(1).durability(material.getUses()));
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
