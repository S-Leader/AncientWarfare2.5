package net.shadowmage.ancientwarfare.npc.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.ai.AIHelper;
import net.shadowmage.ancientwarfare.npc.ai.owned.NpcAIPlayerOwnedRideHorse;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.entity.faction.NpcFaction;
import net.shadowmage.ancientwarfare.npc.init.AWNPCSounds;
import net.shadowmage.ancientwarfare.npc.npc_command.NpcCommand.Command;
import net.shadowmage.ancientwarfare.npc.orders.UpkeepOrder;
import net.shadowmage.ancientwarfare.npc.registry.NpcDefault;
import net.shadowmage.ancientwarfare.npc.registry.NpcDefaultsRegistry;
import net.shadowmage.ancientwarfare.npc.tile.TileTownHall;

import javax.annotation.Nullable;
import java.util.Optional;

import static net.shadowmage.ancientwarfare.npc.config.AWNPCStatics.npcKeepEquipment;

@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2160"})
public abstract class NpcPlayerOwned extends NpcBase implements IKeepFood, Npc {
    private static final String COMMAND_TAG = "command";
    private static final String TOWN_HALL_TAG = "townHall";
    private static final String UPKEEP_POS_TAG = "upkeepPos";
    private static final String FOOD_VALUE_TAG = "foodValue";

    public boolean isAlarmed;

    private Command playerIssuedCommand = Command.NONE;
    private int foodValueRemaining;
    NpcAIPlayerOwnedRideHorse horseAI;
    private BlockPos townHallPosition;
    private BlockPos upkeepAutoBlock;



    public NpcPlayerOwned(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        initializeOwnedNpc();
    }

    private void initializeOwnedNpc() {
        NpcDefault npcDefault = NpcDefaultsRegistry.getOwnedNpcDefault(this);
        if (getNavigation() instanceof GroundPathNavigation navigation) {
            npcDefault.applyPathSettings(navigation);
        }
        npcDefault.applyAttributes(this);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            setDropChance(slot, 1.0F);
        }
    }

    @Override
    public NpcDefault getNpcDefault() {
        return NpcDefaultsRegistry.getOwnedNpcDefault(this);
    }

    @Override
    public int getMaxFallDistance() {
        return Math.max(0, super.getMaxFallDistance() - 1);
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return isFemale() || getSkinSettings().isAlexModel()
                ? AWNPCSounds.HUMAN_FEMALE_HURT
                : AWNPCSounds.HUMAN_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return isFemale() || getSkinSettings().isAlexModel()
                ? AWNPCSounds.HUMAN_FEMALE_DEATH
                : AWNPCSounds.HUMAN_HURT;
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide) {
            if (horseAI != null) {
                horseAI.onKilled();
            }
            validateTownHallPosition();
            getTownHall().ifPresent(townHall -> townHall.handleNpcDeath(this, source));
        }
        super.die(source);
    }

    @Override
    protected void onRepack() {
        super.onRepack();
        if (horseAI != null) {
            horseAI.onKilled();
        }
    }

    @Override
    public final int getArmorValueOverride() {
        return -1;
    }

    @Override
    public final int getAttackDamageOverride() {
        return -1;
    }

    private void setTownHallPosition(@Nullable BlockPos position) {
        townHallPosition = position;
    }

    @Override
    public Optional<BlockPos> getTownHallPosition() {
        return Optional.ofNullable(townHallPosition);
    }

    public Optional<TileTownHall> getTownHall() {
        return getTownHallPosition().flatMap(position -> WorldTools.getTile(level(), position, TileTownHall.class));
    }

    public void handleTownHallBroadcast(BlockPos position) {
        validateTownHallPosition();
        Optional<BlockPos> currentTownHall = getTownHallPosition();

        if (currentTownHall.isPresent()) {
            BlockPos currentPosition = currentTownHall.get();
            double currentDistance = distanceToSqr(
                    currentPosition.getX() + 0.5D,
                    currentPosition.getY(),
                    currentPosition.getZ() + 0.5D
            );
            double newDistance = distanceToSqr(
                    position.getX() + 0.5D,
                    position.getY(),
                    position.getZ() + 0.5D
            );

            if (newDistance < currentDistance) {
                setTownHallPosition(position);
                if (upkeepAutoBlock == null || upkeepAutoBlock.equals(currentPosition)) {
                    upkeepAutoBlock = position;
                }
            }
        } else {
            setTownHallPosition(position);
            if (upkeepAutoBlock == null) {
                upkeepAutoBlock = position;
            }
        }

        isAlarmed = getTownHall().map(townHall -> townHall.alarmActive).orElse(false);
    }

    private void validateTownHallPosition() {
        Optional<BlockPos> currentTownHall = getTownHallPosition();
        if (currentTownHall.isEmpty()) {
            return;
        }

        BlockPos position = currentTownHall.get();
        if (!level().hasChunkAt(position)) {
            return;
        }

        Optional<TileTownHall> townHall = WorldTools.getTile(level(), position, TileTownHall.class);
        if (townHall.isPresent() && hasCommandPermissions(townHall.get().getOwner())) {
            return;
        }

        setTownHallPosition(null);
    }

    public Command getCurrentCommand() {
        return playerIssuedCommand;
    }

    public void handlePlayerCommand(Command command) {
        if (command == null || command.type == null) {
            setPlayerCommand(Command.NONE);
            return;
        }
        command.type.onIssued(this, command);
    }

    public void setPlayerCommand(Command command) {
        playerIssuedCommand = command == null ? Command.NONE : command;
    }

    @Override
    public boolean isHostileTowards(Entity target) {
        if (target instanceof NpcPlayerOwned || target instanceof Player) {
            return !getOwner().isOwnerOrSameTeamOrFriend(target);
        }
        if (target instanceof NpcFaction faction) {
            return faction.isHostileTowards(this);
        }
        return NpcDefaultsRegistry.getOwnedNpcDefault(this).isTarget(target)
                || AIHelper.isAdditionalEntityToTarget(target);
    }

    @Override
    public boolean canTarget(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }

        double targetingRange = getAttributeValue(Attributes.FOLLOW_RANGE);

        if (entity.isInvisible()) {
            targetingRange *= AWCoreStatics.invisibilityFollowRangePenalty;
        }

        if (entity.isShiftKeyDown()) {
            targetingRange *= AWCoreStatics.sneakingFollowRangePenalty;
        }

        if (!getSensing().hasLineOfSight(livingEntity)) {
            targetingRange *= AWCoreStatics.obscuredFollowRangePenalty;
        }

        if (distanceTo(entity) > targetingRange) {
            return false;
        }

        return !getOwner().isOwnerOrSameTeamOrFriend(entity);
    }

    @Override
    public boolean canBeAttackedBy(Entity entity) {
        return !getOwner().isOwnerOrSameTeamOrFriend(entity);
    }

    @Override
    public int getFoodRemaining() {
        return foodValueRemaining;
    }

    @Override
    public void setFoodRemaining(int food) {
        foodValueRemaining = Math.max(0, food);
    }

    @Override
    public Optional<BlockPos> getUpkeepPoint() {
        Optional<BlockPos> configuredPosition = UpkeepOrder.getUpkeepOrder(upkeepStack)
                .flatMap(UpkeepOrder::getUpkeepPosition);
        return configuredPosition.isPresent()
                ? configuredPosition
                : Optional.ofNullable(upkeepAutoBlock);
    }

    @Override
    public void setUpkeepAutoPosition(@Nullable BlockPos position) {
        upkeepAutoBlock = position;
    }

    @Override
    public Direction getUpkeepBlockSide() {
        return UpkeepOrder.getUpkeepOrder(upkeepStack)
                .map(UpkeepOrder::getUpkeepBlockSide)
                .orElse(Direction.DOWN);
    }

    @Override
    public int getUpkeepDimensionId() {
        return UpkeepOrder.getUpkeepOrder(upkeepStack)
                .map(UpkeepOrder::getUpkeepDimension)
                .orElseGet(this::getCurrentDimensionId);
    }

    private int getCurrentDimensionId() {
        if (Level.OVERWORLD.equals(level().dimension())) {
            return 0;
        }
        if (Level.NETHER.equals(level().dimension())) {
            return -1;
        }
        if (Level.END.equals(level().dimension())) {
            return 1;
        }
        return level().dimension().location().toString().hashCode();
    }

    @Override
    public int getUpkeepAmount() {
        return UpkeepOrder.getUpkeepOrder(upkeepStack)
                .map(UpkeepOrder::getUpkeepAmount)
                .orElse(AWNPCStatics.npcDefaultUpkeepWithdraw);
    }

    @Override
    protected boolean tryCommand(Player player) {
        return hasCommandPermissions(player.getUUID(), player.getGameProfile().getName())
                && super.tryCommand(player);
    }

    @Override
    protected void dropEquipment() {
        if (!npcKeepEquipment) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    spawnAtLocation(stack.copy(), 0.0F);
                    setItemSlot(slot, ItemStack.EMPTY);
                }
            }
        }

        if (!AWNPCStatics.persistOrdersOnDeath) {
            if (!ordersStack.isEmpty()) {
                spawnAtLocation(ordersStack.copy(), 0.0F);
            }
            if (!upkeepStack.isEmpty()) {
                spawnAtLocation(upkeepStack.copy(), 0.0F);
            }
            ordersStack = ItemStack.EMPTY;
            upkeepStack = ItemStack.EMPTY;
        }
    }

    public boolean withdrawFood(IItemHandler handler) {
        int requiredAmount = getUpkeepAmount() - getFoodRemaining();
        if (requiredAmount <= 0) {
            return true;
        }

        int eaten = 0;

        for (int slot = 0; slot < handler.getSlots() && eaten < requiredAmount; slot++) {
            while (eaten < requiredAmount) {
                ItemStack simulated = handler.extractItem(slot, 1, true);
                if (simulated.isEmpty()) {
                    break;
                }

                int foodValue = AncientWarfareNPC.statics.getFoodValue(simulated);
                if (foodValue <= 0) {
                    break;
                }

                ItemStack extracted = handler.extractItem(slot, 1, false);
                if (extracted.isEmpty()) {
                    break;
                }

                eaten += foodValue;
            }
        }

        setFoodRemaining(getFoodRemaining() + eaten);
        return getFoodRemaining() >= getUpkeepAmount();
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);

        if (getFoodRemaining() < getUpkeepAmount()) {
            int foodValue = AncientWarfareNPC.statics.getFoodValue(heldStack);
            if (foodValue > 0) {
                if (!level().isClientSide) {
                    heldStack.shrink(1);
                    setFoodRemaining(getFoodRemaining() + foodValue);
                }
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && foodValueRemaining > 0 && !isSleeping()) {
            foodValueRemaining--;
        }
    }

    @Nullable
    @Override
    public Entity changeDimension(ServerLevel destination) {
        townHallPosition = null;
        upkeepAutoBlock = null;
        return super.changeDimension(destination);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        foodValueRemaining = Math.max(0, tag.getInt(FOOD_VALUE_TAG));

        if (tag.contains(COMMAND_TAG, Tag.TAG_COMPOUND)) {
            playerIssuedCommand = new Command(tag.getCompound(COMMAND_TAG));
        } else {
            playerIssuedCommand = Command.NONE;
        }

        townHallPosition = tag.contains(TOWN_HALL_TAG, Tag.TAG_LONG)
                ? BlockPos.of(tag.getLong(TOWN_HALL_TAG))
                : null;

        upkeepAutoBlock = tag.contains(UPKEEP_POS_TAG, Tag.TAG_LONG)
                ? BlockPos.of(tag.getLong(UPKEEP_POS_TAG))
                : null;

        onOrdersInventoryChanged();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putInt(FOOD_VALUE_TAG, foodValueRemaining);

        if (playerIssuedCommand != null && playerIssuedCommand != Command.NONE) {
            tag.put(COMMAND_TAG, playerIssuedCommand.writeToNBT(new CompoundTag()));
        }

        if (townHallPosition != null) {
            tag.putLong(TOWN_HALL_TAG, townHallPosition.asLong());
        }

        if (upkeepAutoBlock != null) {
            tag.putLong(UPKEEP_POS_TAG, upkeepAutoBlock.asLong());
        }
    }
}
