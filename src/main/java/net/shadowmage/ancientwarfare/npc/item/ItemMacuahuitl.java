package net.shadowmage.ancientwarfare.npc.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;

import javax.annotation.Nullable;
import java.util.List;

public class ItemMacuahuitl extends SwordItem implements IClientRegister {
    public ItemMacuahuitl(Tier material, String registryName) {
        super(material, 3, -2.4F, new Item.Properties().durability(Math.max(1, material.getUses() - 100)).rarity(Rarity.EPIC));
        AncientWarfareNPC.proxy.addClientRegister(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        // Models are loaded normally from 1.20 blockstates/models JSON.
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.macuahuitl.tooltip"));
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(Items.OBSIDIAN) || super.isValidRepairItem(toRepair, repair);
    }
}
