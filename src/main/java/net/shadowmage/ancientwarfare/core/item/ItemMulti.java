package net.shadowmage.ancientwarfare.core.item;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.proxy.CommonProxyBase;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;
import net.shadowmage.ancientwarfare.core.util.LegacyItemStack;
import net.shadowmage.ancientwarfare.core.util.LegacyOreDictionary;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/*
 * Handle subtypes through ItemStack damage values
 */
public class ItemMulti extends ItemBase implements IClientRegister {

    private final HashMap<Integer, String> subItems = new HashMap<>();

    public ItemMulti(String modID, String regName) {
        super(modID, regName);
        this.setHasSubtypes(true);
    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        //1.12 getShareTag() returned false - nbt is not synced to clients
        return null;
    }

    @Override
    public String getDescriptionId(ItemStack par1ItemStack) {
        return super.getDescriptionId(par1ItemStack) + "." + par1ItemStack.getDamageValue();
    }

    public void getSubItems(CreativeModeTab tab, NonNullList<ItemStack> items) {
        for (Integer num : subItems.keySet()) {
            items.add(LegacyItemStack.of(this, 1, num));
        }
    }

    public void addSubItem(int num, String modelName) {
        if (!subItems.containsKey(num))
            subItems.put(num, modelName);
    }

    public void addSubItem(int num, String modelName, String ore) {
        addSubItem(num, modelName);
        LegacyOreDictionary.registerOre(ore, LegacyItemStack.of(this, 1, num));
    }

    public ItemStack getSubItem(int num) {
        return LegacyItemStack.of(this, 1, num);
    }

    public ItemMulti listenToProxy(CommonProxyBase proxy) {
        proxy.addClientRegister(this);

        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        for (Map.Entry<Integer, String> entry : subItems.entrySet()) {
            ModelLoaderHelper.registerItem(this, entry.getKey(), entry.getValue());
        }
    }
}
