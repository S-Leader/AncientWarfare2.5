package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.util.LegacyItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TemplateRuleFlowerPot extends TemplateRuleVanillaBlocks {
    public static final String PLUGIN_NAME = "vanillaFlowerPot";
    private static final String ITEM_NAME_TAG = "itemName";
    private Item item = Items.AIR;
    private int itemMeta = 0;

    public TemplateRuleFlowerPot(Level world, BlockPos pos, BlockState state, int turns) {
        super(world, pos, state, turns);
        if (state.getBlock() instanceof FlowerPotBlock pot) {
            Block content = pot.getContent();
            if (content != Blocks.AIR) {
                item = content.asItem();
            }
        }
    }

    public TemplateRuleFlowerPot() {
        super();
    }

    @Override
    protected Optional<ItemStack> getStack() {
        return Optional.of(new ItemStack(Items.FLOWER_POT));
    }

    @Override
    public List<ItemStack> getResources() {
        ArrayList<ItemStack> resources = new ArrayList<>(super.getResources());
        if (item != Items.AIR) {
            resources.add(LegacyItemStack.of(item, 1, itemMeta));
        }
        return resources;
    }

    @Override
    public boolean shouldReuseRule(Level world, BlockState state, int turns, BlockPos pos) {
        return false;
    }

    @Override
    public void writeRuleData(CompoundTag tag) {
        super.writeRuleData(tag);
        ResourceLocation itemName = ForgeRegistries.ITEMS.getKey(item);
        if (item != Items.AIR && itemName != null) {
            tag.putString(ITEM_NAME_TAG, itemName.toString());
        }
        tag.putInt("itemMeta", itemMeta);
    }

    @Override
    public void parseRule(CompoundTag tag) {
        super.parseRule(tag);
        if (tag.contains(ITEM_NAME_TAG)) {
            ResourceLocation registryName = ResourceLocation.tryParse(tag.getString(ITEM_NAME_TAG));
            if (registryName != null && ForgeRegistries.ITEMS.containsKey(registryName)) {
                item = ForgeRegistries.ITEMS.getValue(registryName);
            }
        }
        itemMeta = tag.getInt("itemMeta");
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Nullable
    @Override
    public BlockEntity getTileEntity(int turns) {
        return null;
    }
}
