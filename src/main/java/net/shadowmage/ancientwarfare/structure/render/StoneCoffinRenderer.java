package net.shadowmage.ancientwarfare.structure.render;

import codechicken.lib.model.PerspectiveModelState;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.TransformUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelBase;
import net.shadowmage.ancientwarfare.core.compat.client.GlStateManager;
import net.shadowmage.ancientwarfare.structure.block.BlockCoffin;
import net.shadowmage.ancientwarfare.structure.block.BlockMulti;
import net.shadowmage.ancientwarfare.structure.block.BlockStoneCoffin;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.item.ItemBlockStoneCoffin;
import net.shadowmage.ancientwarfare.structure.model.ModelStoneCoffin;
import net.shadowmage.ancientwarfare.structure.tile.TileStoneCoffin;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class StoneCoffinRenderer extends RenderLootInfo<TileStoneCoffin> implements IItemRenderer {
    private static final ModelStoneCoffin STONE_COFFIN_MODEL = new ModelStoneCoffin();

    private static final Map<BlockCoffin.IVariant, ResourceLocation> TEXTURES = new HashMap<>();

    static {
        for (BlockStoneCoffin.Variant variant : BlockStoneCoffin.Variant.values()) {
            TEXTURES.put(variant, new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/structure/stone_coffin_" + variant.getName() + ".png"));
        }
    }

    private static final PerspectiveModelState TRANSFORMS;

    static {
        Map<ItemDisplayContext, Transformation> map;
        Transformation thirdPerson;

        map = new EnumMap<>(ItemDisplayContext.class);
        thirdPerson = TransformUtils.create(0F, 3F, 5F, 75F, 180F, 180F, 0.015F);
        map.put(ItemDisplayContext.GUI, TransformUtils.create(1F, 4F, 0F, 60F, 225F, 200F, 0.019F));
        map.put(ItemDisplayContext.GROUND, TransformUtils.create(0F, 8F, 0F, 0F, 0F, 180F, 0.017F));
        map.put(ItemDisplayContext.FIXED, TransformUtils.create(0F, -4F, -12F, 90F, 180F, 0F, 0.035F));
        map.put(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, thirdPerson);
        map.put(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, TransformUtils.flipLeft(thirdPerson));
        map.put(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, TransformUtils.create(25F, -15F, -10F, 50F, 170F, 170F, 0.08F));
        map.put(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, TransformUtils.create(25F, -15F, -10F, 50F, 170F, 170F, 0.08F));
        TRANSFORMS = new PerspectiveModelState(map);
    }

    @Override
    public void render(TileStoneCoffin te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te == null || te.isRemoved() || te.getLevel() == null
                || te.getLevel().getBlockEntity(te.getPos()) != te
                || !te.getLevel().getBlockState(te.getPos()).is(AWStructureBlocks.STONE_COFFIN)) {
            return;
        }
        BlockState state = te.getBlockState();
        if (!state.is(AWStructureBlocks.STONE_COFFIN)
                || !state.hasProperty(BlockMulti.INVISIBLE)
                || Boolean.TRUE.equals(state.getValue(BlockMulti.INVISIBLE))) {
            return;
        }
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);
        float rotation = te.getDirection().getRotationAngle();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + 1.78F, z);
        GlStateManager.rotate(-rotation, 0, 1, 0); //passing in negative value because of the flipping of the model below
        GlStateManager.pushMatrix();

        switch ((int) rotation) {
            case 0: // north
                GlStateManager.translate(0, 0, -1F);
                break;
            case 90: // east
                GlStateManager.translate(0, 0, -2F);
                break;
            case 180: //south
                GlStateManager.translate(-1F, 0, -2F);
                break;
            case 270: // west
            default:
                GlStateManager.translate(-1F, 0, -1F);
                break;
        }

        GlStateManager.rotate(180, 1, 0, 0);
        GlStateManager.scale(0.074f, 0.074f, 0.074f);
        ResourceLocation texture = TEXTURES.getOrDefault(te.getVariant(), TEXTURES.get(BlockStoneCoffin.Variant.STONE));
        VertexConsumer vertices = getActiveBufferSource().getBuffer(RenderType.entityCutoutNoCull(texture));
        float lidAngle = te.getPrevLidAngle() + (te.getLidAngle() - te.getPrevLidAngle()) * partialTicks;
        LegacyModelBase.renderWithContext(getActivePoseStack(), vertices, getActivePackedLight(),
                getActivePackedOverlay(), () -> STONE_COFFIN_MODEL.renderAll((float) (-lidAngle / 180F * Math.PI)));
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockStoneCoffin.Variant variant = ItemBlockStoneCoffin.getVariant(stack);

        poseStack.pushPose();
        ResourceLocation texture = TEXTURES.getOrDefault(variant, TEXTURES.get(BlockStoneCoffin.Variant.STONE));
        VertexConsumer vertices = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        LegacyModelBase.renderWithContext(poseStack, vertices, packedLight, packedOverlay, STONE_COFFIN_MODEL::renderAll);
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
