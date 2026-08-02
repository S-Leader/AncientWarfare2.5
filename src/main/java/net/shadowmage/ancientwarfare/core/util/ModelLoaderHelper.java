package net.shadowmage.ancientwarfare.core.util;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelLoader;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class ModelLoaderHelper {

    private ModelLoaderHelper() {
    }

    public static void registerItem(Item item, String prefix) {
        registerItem(item, prefix, "inventory");
    }

    public static void registerItem(Item item, String prefix, boolean metaSuffix) {
        registerItem(item, prefix, metaSuffix, "inventory");
    }

    public static void registerItem(Block block, String prefix, String variant, boolean metaSuffix) {
        registerItem(block.asItem(), prefix, metaSuffix, variant);
    }

    public static void registerItem(Block block, String prefix, String variant) {
        registerItem(block.asItem(), prefix, variant);
    }

    public static void registerItem(Item item, String prefix, String variant) {
        registerItem(item, prefix, true, variant);
    }

    public static void registerItem(Item item, String prefix, boolean metaSuffix, String variant) {
        registerItem(item, prefix, metaSuffix, meta -> variant);
    }

    public static void registerItem(Item item, String prefix, boolean metaSuffix, Function<Integer, String> getVariant) {
        boolean multipleVariants = LegacyCreativeTabContents.stacksFor(item).stream()
                .map(ItemStack::getDamageValue).distinct().limit(2).count() > 1;
        registerItem(item, (it, meta) -> {
            String modelName = AncientWarfareCore.MOD_ID + ":" + (prefix.isEmpty() ? "" : prefix + "/")
                    + RegistryTools.getRegistryName(it).getPath();
            String suffix = multipleVariants && metaSuffix ? "_" + meta : "";
            return new ModelResourceLocation(new ResourceLocation(modelName + suffix), getVariant.apply(meta));
        });
    }

    public static void registerItem(Block block, ModelResourceLocation modelLocation) {
        registerItem(block.asItem(), (i, m) -> modelLocation);
    }

    public static void registerItem(Item item, Function2<Item, Integer, ModelResourceLocation> getModelLocation) {
        for (ItemStack variant : LegacyCreativeTabContents.stacksFor(item)) {
            int metadata = variant.getDamageValue();
            LegacyModelLoader.setCustomModelResourceLocation(item, metadata, getModelLocation.apply(item, metadata));
        }
    }

    public static void registerItem(Item item, int meta, String modelVariantName) {
        ResourceLocation itemName = RegistryTools.getRegistryName(item);
        String namespace = itemName == null ? AncientWarfareCore.MOD_ID : itemName.getNamespace();
        String full = modelVariantName.indexOf(':') >= 0 ? modelVariantName : namespace + ":" + modelVariantName;
        int hashIndex = full.indexOf('#');
        String location = hashIndex >= 0 ? full.substring(0, hashIndex) : full;
        String variant = hashIndex >= 0 ? full.substring(hashIndex + 1).toLowerCase() : "normal";
        LegacyModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(new ResourceLocation(location), variant));
    }

    public static void registerItem(Item item, int meta, String modelName, String variant) {
        LegacyModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(new ResourceLocation(modelName), variant));
    }

}
