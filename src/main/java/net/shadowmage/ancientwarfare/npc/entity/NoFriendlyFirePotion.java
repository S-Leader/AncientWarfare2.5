package net.shadowmage.ancientwarfare.npc.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Splash potion projectile which will not directly collide with friendly NPCs.
 */
public class NoFriendlyFirePotion extends ThrownPotion {
    public NoFriendlyFirePotion(Level level) {
        super(level, 0.0D, 0.0D, 0.0D);
    }

    public NoFriendlyFirePotion(Level level, LivingEntity thrower, ItemStack potion) {
        super(level, thrower);
        setItem(potion);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        Entity owner = getOwner();
        return (!(owner instanceof NpcBase npc) || npc.isHostileTowards(entity)) && super.canHitEntity(entity);
    }
}
