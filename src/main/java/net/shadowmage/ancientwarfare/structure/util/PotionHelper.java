package net.shadowmage.ancientwarfare.structure.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.util.Constants;

import java.util.List;
import java.util.Optional;

public class PotionHelper {
    private static final String SHOW_PARTICLES_TAG = "ShowParticles";
    private static final String CURATIVE_ITEMS_TAG = "CurativeItems";

    private PotionHelper() {
    }

    public static CompoundTag writeCustomPotionEffectToNBT(MobEffectInstance potionEffect) {
        CompoundTag nbt = new CompoundTag();
        //noinspection ConstantConditions
        nbt.putString("RegistryName", ForgeRegistries.MOB_EFFECTS.getKey(potionEffect.getEffect()).toString());
        nbt.putByte("Amplifier", (byte) potionEffect.getAmplifier());
        nbt.putInt("Duration", potionEffect.getDuration());
        nbt.putBoolean("Ambient", potionEffect.isAmbient());
        nbt.putBoolean(SHOW_PARTICLES_TAG, potionEffect.isVisible());
        writeCurativeItems(potionEffect, nbt);
        return nbt;
    }

    private static void writeCurativeItems(MobEffectInstance potionEffect, CompoundTag nbt) {
        ListTag list = new ListTag();
        for (ItemStack stack : potionEffect.getCurativeItems()) {
            list.add(stack.save(new CompoundTag()));
        }
        nbt.put(CURATIVE_ITEMS_TAG, list);
    }

    public static Optional<MobEffectInstance> readCustomPotionEffectFromNBT(CompoundTag nbt) {
        String registryName = nbt.getString("RegistryName");
        MobEffect potion = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(registryName));

        if (potion == null) {
            return Optional.empty();
        } else {
            int j = nbt.getByte("Amplifier");
            int k = nbt.getInt("Duration");
            boolean flag = nbt.getBoolean("Ambient");
            boolean flag1 = true;

            if (nbt.contains(SHOW_PARTICLES_TAG, 1)) {
                flag1 = nbt.getBoolean(SHOW_PARTICLES_TAG);
            }

            return Optional.of(readCurativeItems(new MobEffectInstance(potion, k, j < 0 ? 0 : j, flag, flag1), nbt));
        }
    }

    private static MobEffectInstance readCurativeItems(MobEffectInstance effect, CompoundTag nbt) {
        if (nbt.contains(CURATIVE_ITEMS_TAG, Constants.NBT.TAG_LIST)) {
            List<ItemStack> items = new java.util.ArrayList<>();
            ListTag list = nbt.getList(CURATIVE_ITEMS_TAG, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                items.add(ItemStack.of(list.getCompound(i)));
            }
            effect.setCurativeItems(items);
        }

        return effect;
    }
}
