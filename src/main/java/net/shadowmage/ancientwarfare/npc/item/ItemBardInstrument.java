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

/**
 * Bard instrument item. 1.20 recipes must distinguish the instruments by
 * registry id rather than legacy Damage/NBT, otherwise vanilla/JEI ingredient
 * matching treats every variant as the same item.
 *
 * <p>The no-argument legacy form is retained under bard_instrument so old
 * worlds/templates can still deserialize their 1.12 metadata stacks. New
 * recipes and creative inventory use the fixed-instrument registrations.</p>
 */
public class ItemBardInstrument extends ItemBaseNPC {

    public enum Instrument {
        LUTE(0, "lute"),
        FLUTE(1, "flute"),
        HARP(2, "harp"),
        DRUM(3, "drum");

        private final int legacyMeta;
        private final String name;

        Instrument(int legacyMeta, String name) {
            this.legacyMeta = legacyMeta;
            this.name = name;
        }

        public int legacyMeta() {
            return legacyMeta;
        }

        public String serializedName() {
            return name;
        }

        public static Instrument byLegacyMeta(int meta) {
            for (Instrument instrument : values()) {
                if (instrument.legacyMeta == meta) {
                    return instrument;
                }
            }
            return LUTE;
        }
    }

    private final Instrument fixedInstrument;

    public ItemBardInstrument(String regName, Instrument instrument) {
        super(regName);
        fixedInstrument = instrument;
    }

    private Instrument getInstrument(ItemStack stack) {
        return fixedInstrument != null ? fixedInstrument : Instrument.byLegacyMeta(stack.getDamageValue());
    }

    public void getSubItems(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (fixedInstrument != null) {
            items.add(new ItemStack(this));
            return;
        }
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return "item.ancientwarfarenpc.bard_instrument." + getInstrument(stack).serializedName();
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            SoundEvent sound = switch (getInstrument(stack)) {
                case LUTE -> SoundEvents.NOTE_BLOCK_BASS.value();
                case FLUTE -> SoundEvents.NOTE_BLOCK_FLUTE.value();
                case HARP -> SoundEvents.NOTE_BLOCK_HARP.value();
                case DRUM -> SoundEvents.NOTE_BLOCK_BASEDRUM.value();
            };
            world.playSound(null, player.getX() + 0.5, player.getY() + 0.5, player.getZ() + 0.5,
                    sound, SoundSource.PLAYERS, 2.0F, 1.0F);
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
        // Models are loaded normally from 1.20 blockstates/models JSON.
    }
}
