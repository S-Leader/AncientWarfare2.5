package net.shadowmage.ancientwarfare.npc.item;

import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.util.LegacyItemStack;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;

public class ItemBardInstrument extends ItemBaseNPC {

    private final String[] instrumentNames = new String[]{"lute", "flute", "harp", "drum"};

    public ItemBardInstrument(String regName) {
        super(regName);
        setHasSubtypes(true);
    }

    public void getSubItems(CreativeModeTab tab, NonNullList<ItemStack> items) {
        for (int i = 0; i < instrumentNames.length; i++) {
            items.add(LegacyItemStack.of(this, 1, i));
        }
    }

    @Override
    public String getDescriptionId(ItemStack par1ItemStack) {
        return super.getDescriptionId(par1ItemStack) + "." + instrumentNames[par1ItemStack.getDamageValue()];
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            int meta = stack.getDamageValue();
            SoundEvent s;
            s = SoundEvents.NOTE_BLOCK_BASEDRUM.value();
            if (meta == 0) {
                s = SoundEvents.NOTE_BLOCK_BASS.value();
            } else if (meta == 1) {
                s = SoundEvents.NOTE_BLOCK_FLUTE.value();
            } else if (meta == 2) {
                s = SoundEvents.NOTE_BLOCK_HARP.value();
            }
            world.playSound(null, player.getX() + 0.5, player.getY() + 0.5, player.getZ() + 0.5, s, SoundSource.PLAYERS, 2.0F, 1.0F);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity living) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        ModelLoaderHelper.registerItem(this, "npc", false, meta -> "variant=" + instrumentNames[meta]);
    }
}
