package net.shadowmage.ancientwarfare.automation.item;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.automation.block.BlockWarehouseStockLinker;
import net.shadowmage.ancientwarfare.automation.init.AWAutomationBlocks;
import net.shadowmage.ancientwarfare.core.item.ItemBlockOwnedRotatable;
import net.shadowmage.ancientwarfare.core.util.RayTraceUtils;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockWarehouseStockLinker extends ItemBlockOwnedRotatable {
    public static final String WAREHOUSE_POS_TAG = "warehousePosTag";

    public ItemBlockWarehouseStockLinker(BlockWarehouseStockLinker block) {
        super(block);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<String> tooltip, TooltipFlag flagIn) {
        if (stack.hasTag()) {
            //noinspection ConstantConditions
            tooltip.add(I18n.get("tile.warehouse_stock_linker.tooltip", formatPos(NbtUtils.readBlockPos(stack.getTag().getCompound(WAREHOUSE_POS_TAG)))));
        }
    }

    private void addMessage(Player player, BlockPos pos) {
        player.sendSystemMessage(Component.translatable("guistrings.automation.warehouse_set", formatPos(pos)));
    }

    private String formatPos(BlockPos pos) {
        return String.format("x: %d y: %d z: %d", pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public InteractionResult onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (player.isShiftKeyDown() && world.isClientSide) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown() && !world.isClientSide) {
            HitResult hit = RayTraceUtils.getPlayerTarget(player, 5, 0);
            if (hit instanceof BlockHitResult blockHit && world.getBlockState(blockHit.getBlockPos()).getBlock() == AWAutomationBlocks.WAREHOUSE_CONTROL) {
                ItemStack stack = player.getItemInHand(hand);
                stack.getOrCreateTag().put(WAREHOUSE_POS_TAG, NbtUtils.writeBlockPos(blockHit.getBlockPos()));
                addMessage(player, blockHit.getBlockPos());
                return InteractionResult.SUCCESS;
            }
        }
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }
}
