package net.shadowmage.ancientwarfare.structure.render;

import codechicken.lib.model.PerspectiveModelState;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.TransformUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.EnumMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class RenderItemAdvancedLootChest implements IItemRenderer {
    private static final ChestBlockEntity CHEST_TE = new ChestBlockEntity(BlockPos.ZERO, Blocks.CHEST.defaultBlockState());

    private static final PerspectiveModelState TRANSFORMS;

    static {
        Map<ItemDisplayContext, Transformation> map;
        Transformation thirdPerson;

        map = new EnumMap<>(ItemDisplayContext.class);
        thirdPerson = TransformUtils.create(0F, 2.5F, 0F, 75F, 45F, 0F, 0.375F);
        map.put(ItemDisplayContext.GUI, TransformUtils.create(0F, 0F, 0F, 30F, 45F, 0F, 0.625F));
        map.put(ItemDisplayContext.GROUND, TransformUtils.create(0F, 3F, 0F, 0F, 0F, 0F, 0.25F));
        map.put(ItemDisplayContext.FIXED, TransformUtils.create(0F, 0F, 0F, 0F, 0F, 0F, 0.5F));
        map.put(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, thirdPerson);
        map.put(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, TransformUtils.flipLeft(thirdPerson));
        map.put(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, TransformUtils.create(0F, 0F, 0F, 0F, 45F, 0F, 0.4F));
        map.put(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, TransformUtils.create(0F, 0F, 0F, 0F, 225F, 0F, 0.4F));
        TRANSFORMS = new PerspectiveModelState(map);
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(CHEST_TE, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    @Override
    public PerspectiveModelState getModelState() {
        return TRANSFORMS;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }
}
