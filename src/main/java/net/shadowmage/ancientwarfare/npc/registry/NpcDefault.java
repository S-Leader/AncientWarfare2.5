package net.shadowmage.ancientwarfare.npc.registry;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

import javax.annotation.concurrent.Immutable;
import java.util.Map;

@Immutable
public abstract class NpcDefault {
    //legacy 1.12 attribute names (as used in npc defaults registry json) mapped onto 1.20 attributes
    private static final String LEGACY_MAX_HEALTH = "generic.maxHealth";
    private static final String LEGACY_ATTACK_DAMAGE = "generic.attackDamage";
    private static final Map<String, Attribute> LEGACY_ATTRIBUTES = ImmutableMap.<String, Attribute>builder()
            .put(LEGACY_MAX_HEALTH, Attributes.MAX_HEALTH)
            .put("generic.followRange", Attributes.FOLLOW_RANGE)
            .put("generic.knockbackResistance", Attributes.KNOCKBACK_RESISTANCE)
            .put("generic.movementSpeed", Attributes.MOVEMENT_SPEED)
            .put("generic.flyingSpeed", Attributes.FLYING_SPEED)
            .put(LEGACY_ATTACK_DAMAGE, Attributes.ATTACK_DAMAGE)
            .put("generic.attackSpeed", Attributes.ATTACK_SPEED)
            .put("generic.armor", Attributes.ARMOR)
            .put("generic.armorToughness", Attributes.ARMOR_TOUGHNESS)
            .put("generic.luck", Attributes.LUCK)
            .build();
    protected Map<String, Double> attributes;
    protected int experienceDrop;
    protected boolean canSwim;
    protected boolean canBreakDoors;
    protected Map<Integer, Item> equipment;

    public NpcDefault(Map<String, Double> attributes, int experienceDrop, boolean canSwim, boolean canBreakDoors, Map<Integer, Item> equipment) {
        this.attributes = attributes;
        this.experienceDrop = experienceDrop;
        this.canSwim = canSwim;
        this.canBreakDoors = canBreakDoors;
        this.equipment = equipment;
    }

    public abstract NpcDefault setExperienceDrop(int experienceDrop);

    public abstract NpcDefault setCanSwim(boolean canSwim);

    public abstract NpcDefault setCanBreakDoors(boolean canBreakDoors);

    public abstract NpcDefault setAttributes(Map<String, Double> additionalAttributes);

    public abstract NpcDefault setEquipment(Map<Integer, Item> additionalEquipment);

    public int getExperienceDrop() {
        return experienceDrop;
    }

    public double getBaseHealth() {
        return (attributes.containsKey(LEGACY_MAX_HEALTH)) ? attributes.get(LEGACY_MAX_HEALTH) : 0;
    }

    public double getBaseAttack() {
        return (attributes.containsKey(LEGACY_ATTACK_DAMAGE)) ? attributes.get(LEGACY_ATTACK_DAMAGE) : 0;
    }

    public void applyAttributes(NpcBase npc) {
        attributes.forEach((name, value) -> applyAttribute(npc, name, value));
    }

    private void applyAttribute(NpcBase npc, String attributeName, double baseValue) {
        Attribute attributeType = LEGACY_ATTRIBUTES.get(attributeName);
        if (attributeType == null) {
            return;
        }
        AttributeInstance attribute = npc.getAttributes().getInstance(attributeType);
        if (attribute != null) {
            attribute.setBaseValue(baseValue);

            if (attributeType == Attributes.MAX_HEALTH) {
                npc.setHealth((float) baseValue);
            }
        }
    }

    public void applyPathSettings(GroundPathNavigation navigator) {
        navigator.setCanFloat(canSwim);
        navigator.setCanOpenDoors(canBreakDoors);
    }

    public void applyEquipment(NpcBase npc) {
        equipment.forEach((slot, item) -> npc.setItemSlot(slot, new ItemStack(item)));
    }
}
