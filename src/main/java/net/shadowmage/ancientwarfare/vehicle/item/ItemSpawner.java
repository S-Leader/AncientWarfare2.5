package net.shadowmage.ancientwarfare.vehicle.item;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelLoader;
import net.shadowmage.ancientwarfare.vehicle.AncientWarfareVehicles;
import net.shadowmage.ancientwarfare.vehicle.config.AWVehicleStatics;
import net.shadowmage.ancientwarfare.vehicle.entity.IVehicleType;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;
import net.shadowmage.ancientwarfare.vehicle.entity.types.VehicleType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemSpawner extends ItemBaseVehicle {
    private static final String LEVEL_TAG = "level";
    private static final String HEALTH_TAG = "health";
    private static final String SPAWN_DATA_TAG = "spawnData";

    public ItemSpawner() {
        super("spawner");
        setHasSubtypes(true);
        setMaxStackSize(1);
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (world.isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }

        if (stack.isEmpty()) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }

        //noinspection ConstantConditions
        if (stack.hasTag() && stack.getTag().contains(SPAWN_DATA_TAG)) {
            if (rayTraceAndSpawnVehicle(world, player, hand, stack))
                return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        AncientWarfareVehicles.LOG.error("Vehicle spawner item was missing NBT data, something may have corrupted this item");
        return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
    }

    private boolean rayTraceAndSpawnVehicle(Level world, Player player, InteractionHand hand, ItemStack stack) {
        //noinspection ConstantConditions
        CompoundTag tag = stack.getTag().getCompound(SPAWN_DATA_TAG);
        int level = tag.getInt(LEVEL_TAG);
        Optional<VehicleBase> v = VehicleType.getVehicleForType(world, stack.getDamageValue(), level);
        if (!v.isPresent()) {
            return true;
        }
        VehicleBase vehicle = v.get();
        if (tag.contains(HEALTH_TAG)) {
            vehicle.setHealth(tag.getFloat(HEALTH_TAG));
        }
        BlockHitResult rayTrace = getPlayerPOVHitResult(world, player, ClipContext.Fluid.ANY);
        if (rayTrace.getType() != HitResult.Type.BLOCK) {
            return true;
        }
        spawnVehicle(world, player, vehicle, rayTrace);
        updateSpawnerStackCount(player, hand, stack);
        return false;
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
        //noinspection ConstantConditions
        if (stack.hasTag() && stack.getTag().contains(SPAWN_DATA_TAG)) {
            CompoundTag tag = stack.getTag().getCompound(SPAWN_DATA_TAG);
            int level = tag.getInt(LEVEL_TAG);
            tooltip.add(I18n.get("gui.ancientwarfarevehicle.tooltip.material_level", level));
            if (tag.contains(HEALTH_TAG)) {
                tooltip.add(I18n.get("gui.ancientwarfarevehicle.tooltip.health", tag.getFloat(HEALTH_TAG)));
            }

            //Read the tooltip from the vehicle TYPE directly — constructing an entity here
            //ran every frame and blew up outside the EntityType factory.
            IVehicleType type = VehicleType.getVehicleType(stack.getDamageValue());
            if (type == null) {
                return;
            }
            tooltip.addAll(type.getDisplayTooltip().stream()
                    .filter(java.util.Objects::nonNull)
                    .map(I18n::get)
                    .collect(Collectors.toSet()));
        }
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        int type = stack.getDamageValue();
        IVehicleType vehicle = type >= 0 && type < VehicleType.vehicleTypes.length ? VehicleType.vehicleTypes[type] : null;
        return vehicle == null || vehicle.getDisplayName() == null ? "item.spawner" : vehicle.getDisplayName();
    }

    public void getSubItems(CreativeModeTab tab, NonNullList<ItemStack> items) {
        items.addAll(VehicleType.getCreativeDisplayItems());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        String modelPath = "vehicle/%s";

        LegacyModelLoader.setCustomMeshDefinition(this, stack -> {
            if (stack.hasTag()) {
                //noinspection ConstantConditions
                int level = stack.getTag().getCompound(SPAWN_DATA_TAG).getInt(LEVEL_TAG);
                IVehicleType type = VehicleType.getVehicleType(stack.getDamageValue());
                String variant = type == null ? "catapult_stand_0" : type.getConfigName() + "_" + level;
                return new ModelResourceLocation(new ResourceLocation(AncientWarfareCore.MOD_ID, String.format(modelPath, variant)), "inventory");
            }
            return new ModelResourceLocation(new ResourceLocation(AncientWarfareCore.MOD_ID,
                    String.format(modelPath, "catapult_stand_0")), "inventory");
        });

        for (IVehicleType type : VehicleType.vehicleTypes) {
            if (type == null || type.getMaterialType() == null || !type.isEnabled()) {
                continue;
            }
            for (int level = 0; level < type.getMaterialType().getNumOfLevels(); level++) {
                LegacyModelLoader.registerItemVariants(this,
                        new ModelResourceLocation(new ResourceLocation(AncientWarfareCore.MOD_ID,
                                String.format(modelPath, type.getConfigName() + "_" + level)), "inventory"));
            }
        }
    }
}
