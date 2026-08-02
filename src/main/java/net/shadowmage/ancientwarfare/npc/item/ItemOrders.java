package net.shadowmage.ancientwarfare.npc.item;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.input.IItemKeyInterface;
import net.shadowmage.ancientwarfare.core.input.InputHandler;

import javax.annotation.Nullable;
import java.util.List;

public abstract class ItemOrders extends ItemBaseNPC implements IItemKeyInterface {

    public ItemOrders(String regName) {
        super(regName);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<String> tooltip, TooltipFlag flagIn) {
        tooltip.add(I18n.get("guistrings.npc.orders.open_gui"));
        String key = InputHandler.ALT_ITEM_USE_1.getTranslatedKeyMessage().getString();
        tooltip.add(I18n.get("guistrings.npc.orders.add_position", key));
    }

    @Override
    public boolean onKeyActionClient(Player player, ItemStack stack, ItemAltFunction altFunction) {
        return altFunction == ItemAltFunction.ALT_FUNCTION_1;
    }

    public abstract List<BlockPos> getPositionsForRender(ItemStack stack);

    public void addMessage(Player player) {
        player.sendSystemMessage(Component.translatable("guistrings.npc.orders.position_added"));
    }
}
