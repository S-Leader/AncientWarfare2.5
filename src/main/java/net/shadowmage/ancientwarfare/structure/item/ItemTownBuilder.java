package net.shadowmage.ancientwarfare.structure.item;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.input.IItemKeyInterface;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.gui.GuiTownSelection;
import net.shadowmage.ancientwarfare.structure.town.TownBoundingArea;
import net.shadowmage.ancientwarfare.structure.town.TownTemplate;
import net.shadowmage.ancientwarfare.structure.town.TownTemplateManager;
import net.shadowmage.ancientwarfare.structure.town.WorldTownGenerator;

import java.util.Optional;

public class ItemTownBuilder extends ItemBaseStructure implements IItemKeyInterface {

    public ItemTownBuilder(String name) {
        super(name);
        setMaxStackSize(1);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
        return false;
    }

    @Override
    public boolean onKeyActionClient(Player player, ItemStack stack, ItemAltFunction altFunction) {
        return altFunction == ItemAltFunction.ALT_FUNCTION_1;
    }

    @Override
    public void onKeyAction(Player player, ItemStack stack, ItemAltFunction altFunction) {
        if (player.level().isClientSide) {
            return;
        }

        BlockHitResult rayTraceResult = getPlayerPOVHitResult(player.level(), player, ClipContext.Fluid.NONE);
        if (rayTraceResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        Optional<TownTemplate> template = TownTemplateManager.INSTANCE.getTemplate(getTownName(stack));
        if (!template.isPresent()) {
            player.sendSystemMessage(Component.literal("No town template set"));
            return;
        }

        long t1 = System.nanoTime();
        WorldTownGenerator.INSTANCE.generate(player.level(), getTownArea(rayTraceResult.getBlockPos(), player.getDirection(), getLength(stack), getWidth(stack)), template.get());
        long t2 = System.nanoTime();
        AncientWarfareStructure.LOG.debug("Total Town gen nanos (incl. validation): {}", t2 - t1);
    }

    private TownBoundingArea getTownArea(BlockPos pos, Direction horizontalFacing, int chunkLength, int chunkWidth) {
        int minY = Math.max(pos.getY() - 3, 0);
        int maxY = Math.min(minY + 40, 255);

        if (horizontalFacing.getAxis() == Direction.Axis.X) {
            int chunkMinX = (pos.getX() >> 4) - (horizontalFacing.getStepX() < 0 ? chunkLength : 0);
            int chunkMaxX = (pos.getX() >> 4) + (horizontalFacing.getStepX() > 0 ? chunkLength : 0);
            int chunkMinZ = (pos.getZ() >> 4) - (chunkWidth / 2);
            int chunkMaxZ = chunkMinZ + chunkWidth;
            return new TownBoundingArea(chunkMinX, chunkMinZ, chunkMaxX, chunkMaxZ, minY, maxY);
        }

        int chunkMinX = (pos.getX() >> 4) - (chunkWidth / 2);
        int chunkMaxX = chunkMinX + chunkWidth;
        int chunkMinZ = (pos.getZ() >> 4) - (horizontalFacing.getStepZ() < 0 ? chunkLength : 0);
        int chunkMaxZ = (pos.getZ() >> 4) + (horizontalFacing.getStepZ() > 0 ? chunkLength : 0);
        return new TownBoundingArea(chunkMinX, chunkMinZ, chunkMaxX, chunkMaxZ, minY, maxY);
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        if (!player.level().isClientSide && !player.isShiftKeyDown()) {
            AWMenuTypes.open(player, NetworkHandler.GUI_TOWN_BUILDER, 0, 0, 0);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    public static String getTownName(ItemStack townBuilder) {
        return townBuilder.getTag() == null ? "" : townBuilder.getTag().getString("townName");
    }

    public static void setTownName(ItemStack townBuilder, String structName) {
        townBuilder.getOrCreateTag().put("townName", StringTag.valueOf(structName));
    }

    public static void setWidth(ItemStack townBuilder, int width) {
        townBuilder.getOrCreateTag().put("width", IntTag.valueOf(width));
    }

    public static void setLength(ItemStack townBuilder, int length) {
        townBuilder.getOrCreateTag().put("length", IntTag.valueOf(length));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        super.registerClient();
    }

    public static int getWidth(ItemStack townBuilder) {
        //noinspection ConstantConditions
        return townBuilder.hasTag() ? townBuilder.getTag().getInt("width") : 0;
    }

    public static int getLength(ItemStack townBuilder) {
        //noinspection ConstantConditions
        return townBuilder.hasTag() ? townBuilder.getTag().getInt("length") : 0;
    }
}
