package net.shadowmage.ancientwarfare.npc.entity.faction;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.npc.ai.*;
import net.shadowmage.ancientwarfare.npc.ai.faction.NpcAIFactionHurt;
import net.shadowmage.ancientwarfare.npc.ai.faction.NpcAIFactionPriest;
import net.shadowmage.ancientwarfare.npc.ai.faction.NpcAIFactionRangedAttack;
import net.shadowmage.ancientwarfare.npc.entity.NoFriendlyFirePotion;

public class NpcFactionPriest extends NpcFaction implements RangedAttackMob {
    public NpcFactionPriest(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        addAI();
    }


    private void addAI() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(0, new NpcAIRestrictOpenDoor(this));
        goalSelector.addGoal(0, new NpcAIDoor(this, true));
        goalSelector.addGoal(1, new NpcAIFollowPlayer(this));
        goalSelector.addGoal(2, new NpcAIMoveHome(this, 50F, 5F, 30F, 5F));
        goalSelector.addGoal(3, new NpcAIFactionPriest(this));
        goalSelector.addGoal(16, new NpcAIFactionRangedAttack(this, 0.8D, 20, 80));
        goalSelector.addGoal(101, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        goalSelector.addGoal(102, new NpcAIWander(this));
        goalSelector.addGoal(103, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        targetSelector.addGoal(1, new NpcAIFactionHurt(this, this::isHostileTowards));
        targetSelector.addGoal(15, new NpcAIAttackNearest(this, entity -> {
            if (!isHostileTowards(entity)) return false;
            if (hasRestriction()) {
                BlockPos home = getRestrictCenter();
                return entity.distanceToSqr(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D) <= 900.0D;
            }
            return true;
        }));
    }

    @Override
    public String getNpcType() {
        return "priest";
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        double targetY = target.getY() + target.getEyeHeight() - 1.1D;
        double dx = target.getX() + target.getDeltaMovement().x - getX();
        double dy = targetY - getY();
        double dz = target.getZ() + target.getDeltaMovement().z - getZ();
        float horizontal = Mth.sqrt((float) (dx * dx + dz * dz));
        Potion potion = Potions.HARMING;
        if (horizontal >= 8.0F && !target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) potion = Potions.SLOWNESS;
        else if (target.getHealth() >= 8.0F && !target.hasEffect(MobEffects.POISON)) potion = Potions.POISON;
        else if (horizontal <= 3.0F && !target.hasEffect(MobEffects.WEAKNESS) && getRandom().nextFloat() < 0.25F)
            potion = Potions.WEAKNESS;

        ThrownPotion projectile = getPotion(potion);
        projectile.setXRot(projectile.getXRot() + 20.0F);
        projectile.shoot(dx, dy + horizontal * 0.2D, dz, 0.75F, 8.0F);
        playSound(SoundEvents.WITCH_THROW, 1.0F, 0.8F + getRandom().nextFloat() * 0.4F);
        level().addFreshEntity(projectile);
    }

    private ThrownPotion getPotion(Potion potion) {
        return new NoFriendlyFirePotion(level(), this, PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), potion));
    }

    @Override
    public boolean canAttackClass(Class type) {
        return true;
    }

    @Override
    public boolean isPassive() {
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return effect.getEffect() != MobEffects.POISON && super.canBeAffected(effect);
    }
}
