package net.shadowmage.ancientwarfare.vehicle.item;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.vehicle.config.AWVehicleStatics;
import net.shadowmage.ancientwarfare.vehicle.entity.IVehicleType;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;
import net.shadowmage.ancientwarfare.vehicle.entity.types.VehicleType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Placement item for one concrete vehicle type. Material level and preserved health remain
 * per-stack state, but the vehicle identity is now the registered item itself.
 */
public class ItemSpawner extends ItemBaseVehicle {
    private static final String LEVEL_TAG = "level";
    private static final String HEALTH_TAG = "health";
    private static final String SPAWN_DATA_TAG = "spawnData";

    private final IVehicleType vehicleType;

    public ItemSpawner(IVehicleType vehicleType) {
        super(vehicleType.getConfigName());
        this.vehicleType = vehicleType;
        setHasSubtypes(true);
        setMaxStackSize(1);
    }

    public IVehicleType getVehicleType() {
        return vehicleType;
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        if (stack.isEmpty() || !vehicleType.isEnabled()) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }
        return rayTraceAndSpawnVehicle(world, player, hand, stack)
                ? new InteractionResultHolder<>(InteractionResult.SUCCESS, stack)
                : new InteractionResultHolder<>(InteractionResult.FAIL, stack);
    }

    private boolean rayTraceAndSpawnVehicle(Level world, Player player, InteractionHand hand, ItemStack stack) {
        CompoundTag tag = getSpawnData(stack);
        int level = getMaterialLevel(stack);
        Optional<VehicleBase> vehicleOptional = VehicleType.getVehicleForType(world, vehicleType.getGlobalVehicleType(), level);
        if (vehicleOptional.isEmpty()) {
            return false;
        }
        VehicleBase vehicle = vehicleOptional.get();
        if (tag.contains(HEALTH_TAG)) {
            vehicle.setHealth(tag.getFloat(HEALTH_TAG));
        }
        BlockHitResult rayTrace = getPlayerPOVHitResult(world, player, ClipContext.Fluid.ANY);
        if (rayTrace.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        spawnVehicle(world, player, vehicle, rayTrace);
        updateSpawnerStackCount(player, hand, stack);
        return true;
    }

    private CompoundTag getSpawnData(ItemStack stack) {
        return stack.getOrCreateTag().getCompound(SPAWN_DATA_TAG);
    }

    public int getMaterialLevel(ItemStack stack) {
        int maxLevel = Math.max(0, vehicleType.getMaterialType().getNumOfLevels() - 1);
        return Mth.clamp(getSpawnData(stack).getInt(LEVEL_TAG), 0, maxLevel);
    }

    private void updateSpawnerStackCount(Player player, InteractionHand hand, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
            if (stack.getCount() <= 0) {
                player.setItemInHand(hand, ItemStack.EMPTY);
            }
        }
    }

    private void spawnVehicle(Level world, Player player, VehicleBase vehicle, BlockHitResult rayTrace) {
        Vec3 hitVec = rayTrace.getLocation();
        if (rayTrace.getDirection().getAxis().isHorizontal()) {
            Vec3i dirVec = rayTrace.getDirection().getNormal();
            float halfWidth = vehicle.getBbWidth() / 2f;
            hitVec = hitVec.add(dirVec.getX() * halfWidth, 0, dirVec.getZ() * halfWidth);
        }

        vehicle.setPos(hitVec.x, hitVec.y, hitVec.z);
        vehicle.setYRot(-player.getYRot() + 180);
        vehicle.yRotO = vehicle.getYRot();
        vehicle.localTurretDestRot = vehicle.localTurretRotation = vehicle.localTurretRotationHome = vehicle.getYRot();
        if (AWVehicleStatics.generalSettings.useVehicleSetupTime) {
            vehicle.setSetupState(true, 100);
        }
        world.addFreshEntity(vehicle);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level world, List<String> tooltip, TooltipFlag flagIn) {
        super.addInformation(stack, world, tooltip, flagIn);
        CompoundTag tag = getSpawnData(stack);
        tooltip.add(I18n.get("gui.ancientwarfarevehicle.tooltip.material_level", getMaterialLevel(stack)));
        if (tag.contains(HEALTH_TAG)) {
            tooltip.add(I18n.get("gui.ancientwarfarevehicle.tooltip.health", tag.getFloat(HEALTH_TAG)));
        }
        tooltip.addAll(vehicleType.getDisplayTooltip().stream()
                .filter(java.util.Objects::nonNull)
                .map(I18n::get)
                .collect(Collectors.toSet()));
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return vehicleType.getDisplayName() == null ? super.getDescriptionId(stack) : vehicleType.getDisplayName();
    }

    public void getSubItems(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (!vehicleType.isEnabled()) {
            return;
        }
        for (int level = 0; level < vehicleType.getMaterialType().getNumOfLevels(); level++) {
            items.add(createStack(level));
        }
    }

    public ItemStack createStack(int level) {
        ItemStack stack = new ItemStack(this);
        CompoundTag spawnData = new CompoundTag();
        spawnData.putInt(LEVEL_TAG, Mth.clamp(level, 0, Math.max(0, vehicleType.getMaterialType().getNumOfLevels() - 1)));
        stack.getOrCreateTag().put(SPAWN_DATA_TAG, spawnData);
        return stack;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        // Models are loaded normally from 1.20 blockstates/models JSON.
    }
}
