package net.shadowmage.ancientwarfare.npc.entity;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.EntityType;
import com.google.common.base.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.item.ItemHammer;
import net.shadowmage.ancientwarfare.npc.ai.*;
import net.shadowmage.ancientwarfare.npc.ai.owned.*;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.item.ItemCombatOrder;
import net.shadowmage.ancientwarfare.npc.item.ItemCommandBaton;


@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2160"})
public class NpcCombat extends NpcPlayerOwned implements RangedAttackMob {
    private static final String PATROL_AI_TAG = "patrolAI";
    private final NpcAIAttackMeleeLongRange meleeAI;
    private final Goal arrowAI;
    private final NpcAIPlayerOwnedPatrol patrolAI;

    private NpcBase distressedTarget;

    @SuppressWarnings("squid:S4738")
    public NpcCombat(EntityType<? extends PathfinderMob> type, Level par1World) {
        super(type, par1World);
        meleeAI = new NpcAIAttackMeleeLongRange(this);
        arrowAI = new NpcAIPlayerOwnedAttackRanged(this);
        horseAI = new NpcAIPlayerOwnedRideHorse(this);
        patrolAI = new NpcAIPlayerOwnedPatrol(this);

        //noinspection Guava
        Predicate<Entity> selector = this::isHostileTowards;

        tasks.addTask(0, new FloatGoal(this));
        tasks.addTask(0, new NpcAIRestrictOpenDoor(this));
        tasks.addTask(0, new NpcAIDoor(this, true));
        tasks.addTask(0, horseAI);
        tasks.addTask(1, new NpcAIPlayerOwnedFollowCommand(this));
        tasks.addTask(2, new NpcAIPlayerOwnedGetFood(this));
        tasks.addTask(3, new NpcAIPlayerOwnedIdleWhenHungry(this));

        //3==NpcAIShieldBlock
        tasks.addTask(5, new NpcAIFollowPlayer(this));
        tasks.addTask(6, new NpcAIPlayerOwnedAlarmResponse(this));

        //6--empty....
        //7==combat task, inserted from onweaponinventoryupdated
        tasks.addTask(8, new NpcAIMedicBase<NpcCombat>(this));
        tasks.addTask(8, new NpcAIDistressResponse(this));
        tasks.addTask(9, patrolAI);

        tasks.addTask(10, new NpcAIMoveHome(this, 50F, 5F, 20F, 5F));

        //post-100 -- used by delayed shared tasks (look at random stuff, wander)
        tasks.addTask(101, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        tasks.addTask(102, new NpcAIWander(this));
        tasks.addTask(103, new NpcAIWatchClosest(this, Mob.class, 8.0F));

        targetTasks.addTask(0, new NpcAIPlayerOwnedCommander(this));
        targetTasks.addTask(1, new NpcAIOwnerHurtByTarget(this));
        targetTasks.addTask(2, new NpcAIOwnerHurtTarget(this));
        targetTasks.addTask(3, new NpcAIHurt(this));
        targetTasks.addTask(4, new NpcAIAttackNearest(this, selector));

        setCanPickUpLoot(true);
    }

    @Override
    public boolean canHoldItem(ItemStack stack) {
        return getMainHandItem().isEmpty() || getMainHandItem().getItem() == stack.getItem();
    }

    @Override
    public boolean isValidOrdersStack(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemCombatOrder;
    }

    @Override
    public void onOrdersInventoryChanged() {
        patrolAI.onOrdersInventoryChanged();
    }

    @Override
    public void onWeaponInventoryChanged() {
        super.onWeaponInventoryChanged();
        if (!world.isClientSide) {
            tasks.removeTask(arrowAI);
            tasks.removeTask(meleeAI);
            ItemStack stack = getMainHandItem();
            if (isBow(stack.getItem())) {
                tasks.addTask(4, arrowAI);
            } else {
                tasks.addTask(4, meleeAI);
            }
            meleeAI.setAttackReachFromWeapon(getMainHandItem());
        }
    }

    @Override
    public boolean canAttackClass(Class claz) {
        return (isBow(getMainHandItem().getItem())) || super.canAttackClass(claz);
    }

    @Override
    public boolean worksInRain() {
        return true;
    }

    @Override
    public boolean isPassive() {
        return false;
    }

    @Override
    public boolean shouldSleep() {
        return false;
    }

    @Override
    public String getNpcSubType() {
        return getSubtypeFromEquipment();
    }

    private String getSubtypeFromEquipment() {
        ItemStack stack = getMainHandItem();
        if (!stack.isEmpty()) {
            Item item = stack.getItem();
            ResourceLocation itemName = ForgeRegistries.ITEMS.getKey(item);
            if (AWCoreStatics.medicItems.contains(itemName)) {
                return "medic";
            } else if (item instanceof ItemHammer) {
                return "engineer";
            }
            if (isBow(item)) {
                return "archer";
            } else if (item instanceof ItemCommandBaton) {
                return "commander";
            } else if (stack.isEnchantable()) {
                return "soldier";
            }
            System.out.println("[AW2t] WARNING: item does not match any Combat NPC subtype! item=" + item.toString());
        }
        return "";
    }

    @Override
    public String getNpcType() {
        return "combat";
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        onWeaponInventoryChanged();
        onOffhandInventoryChanged();
        if (tag.contains(PATROL_AI_TAG)) {
            patrolAI.readFromNBT(tag.getCompound(PATROL_AI_TAG));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put(PATROL_AI_TAG, patrolAI.writeToNBT(new CompoundTag()));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float force) {
        // minimum inaccuracy = 3.0f, slowly reaches 0 (or close to it) as the NPC reaches max level
        float inaccuracy = 3.0f - ((float) Math.sqrt(getLevelingStats().getBaseLevel()) / (float) Math.sqrt(AWNPCStatics.maxNpcCombatLevel) * 3.0f);
        RangeAttackHelper.doRangedAttack(this, target, force, inaccuracy);
    }

    @Override
    public boolean requiresUpkeep() {
        // Tweak config option for combat NPCs to never need food
        if (AWCoreStatics.combatNPCsRequireFood) {
            return super.requiresUpkeep();
        } else {
            return false;
        }
    }

    public void respondToDistress(NpcBase source) {
        distressedTarget = source;
        LivingEntity threat = source.getTarget();
        if (threat == null || !threat.isAlive() || !canTarget(threat) || !isHostileTowards(threat)) {
            return;
        }

        LivingEntity currentTarget = getTarget();
        if (currentTarget == null || !currentTarget.isAlive() || distanceToSqr(threat) < distanceToSqr(currentTarget)) {
            setTarget(threat);
        }
    }

    public NpcBase getDistressedTarget() {
        return distressedTarget;
    }

    public void clearDistress() {
        distressedTarget = null;
    }
}
