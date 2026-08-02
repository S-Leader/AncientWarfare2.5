package net.shadowmage.ancientwarfare.structure.item;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.item.ItemBlockBase;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.tile.TileColored;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ItemBlockColored extends ItemBlockBase {
    private static final String DYE_COLOR_TAG = "dyeColor";
    private static final int WHITE = 16383998;
    private static final String COLOR_TAG = "color";
    private static final String CUSTOM_DATA_TAG = "customData";
    private final Set<CompoundTag> customItemTags = new HashSet<>();

    public ItemBlockColored(Block block) {
        super(block);
    }

    @SuppressWarnings("ConstantConditions")
    @OnlyIn(Dist.CLIENT)
    public int getColor(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag.contains(COLOR_TAG)) {
                return tag.getInt(COLOR_TAG);
            }
            Optional<DyeColor> dyeColor = getDyeColor(stack);
            if (dyeColor.isPresent()) {
                float[] diffuse = dyeColor.get().getTextureDiffuseColors();
                return ((int) (diffuse[0] * 255.0F) << 16) | ((int) (diffuse[1] * 255.0F) << 8) | (int) (diffuse[2] * 255.0F);
            }
        }
        return WHITE;
    }

    public void addCustomItemTag(CompoundTag tag) {
        customItemTags.add(tag);
    }

    @SuppressWarnings("ConstantConditions")
    private Optional<DyeColor> getDyeColor(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(DYE_COLOR_TAG)) {
            return Optional.of(DyeColor.byId(15 - stack.getTag().getInt(DYE_COLOR_TAG)));
        }

        return Optional.empty();
    }

    public void getSubItems(CreativeModeTab tab, NonNullList<ItemStack> items) {
        for (int dyeDamage = 0; dyeDamage < 16; ++dyeDamage) {
            ItemStack item = new ItemStack(this, 1);
            item.getOrCreateTag().put(DYE_COLOR_TAG, IntTag.valueOf(dyeDamage));
            items.add(item);
        }

        for (CompoundTag tag : customItemTags) {
            ItemStack item = new ItemStack(this, 1);
            item.setTag(tag.copy());
            items.add(item);
        }
    }

    @Override
    protected void onBlockPlaced(BlockPlaceContext context, BlockState placedState) {
        if (placedState.getBlock() == block) {
            WorldTools.getTile(context.getLevel(), context.getClickedPos(), TileColored.class)
                    .ifPresent(tile -> tile.setFromStack(context.getItemInHand()));
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (!stack.hasTag()) {
            return getUnlocalizedNameInefficiently(stack);
        }

        CompoundTag tag = stack.getTag();

        if (tag.contains(CUSTOM_DATA_TAG)) {
            return I18n.get((getUnlocalizedNameInefficiently(stack) + "." + tag.getString("unlocalizedNamePart") + ".name").trim(),
                    StringUtils.capitalize(tag.getString(CUSTOM_DATA_TAG)));
        }

        String color = I18n.get(getDyeColor(stack).map(DyeColor::getName).orElse("white"));
        return I18n.get((getUnlocalizedNameInefficiently(stack) + ".name").trim(), color);
    }

}
