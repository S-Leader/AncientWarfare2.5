package net.shadowmage.ancientwarfare.structure.render;

import codechicken.lib.model.PerspectiveModelState;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.TransformUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
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
import net.shadowmage.ancientwarfare.structure.block.BlockWoodenCoffin;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.item.ItemBlockWoodenCoffin;
import net.shadowmage.ancientwarfare.structure.model.ModelCoffin;
import net.shadowmage.ancientwarfare.structure.tile.TileWoodenCoffin;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class WoodenCoffinRenderer extends RenderLootInfo<TileWoodenCoffin> implements IItemRenderer {
    private static final ModelCoffin COFFIN_MODEL = new ModelCoffin();

    private static final Map<BlockCoffin.IVariant, ResourceLocation> TEXTURES = new HashMap<>();

    static {
        for (BlockWoodenCoffin.Variant variant : BlockWoodenCoffin.Variant.values()) {
            TEXTURES.put(variant, new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/structure/coffin_" + variant.getName() + ".png"));
        }
    }

    private static final PerspectiveModelState TRANSFORMS;

    static {
        Map<ItemDisplayContext, Transformation> map;
        Transformation thirdPerson;

        map = new EnumMap<>(ItemDisplayContext.class);
        thirdPerson = TransformUtils.create(0F, 3F, 5F, 75F, 180F, 180F, 0.015F);
        map.put(ItemDisplayContext.GUI, TransformUtils.create(6F, 5F, 0F, 60F, 225F, 200F, 0.035F));
        map.put(ItemDisplayContext.GROUND, TransformUtils.create(0F, 8F, 0F, 0F, 0F, 180F, 0.025F));
        map.put(ItemDisplayContext.FIXED, TransformUtils.create(0F, -4F, -12F, 90F, 180F, 0F, 0.035F));
        map.put(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, thirdPerson);
        map.put(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, TransformUtils.flipLeft(thirdPerson));
        map.put(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, TransformUtils.create(25F, -15F, -10F, 50F, 170F, 170F, 0.08F));
        map.put(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, TransformUtils.create(25F, -15F, -10F, 50F, 170F, 170F, 0.08F));
        TRANSFORMS = new PerspectiveModelState(map);
    }

    @Override
    public void render(TileWoodenCoffin te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te == null || te.isRemoved() || te.getLevel() == null
                || te.getLevel().getBlockEntity(te.getPos()) != te
                || !te.getLevel().getBlockState(te.getPos()).is(AWStructureBlocks.WOODEN_COFFIN.get())) {
            return;
        }
        BlockState state = te.getBlockState();
        if (!state.is(AWStructureBlocks.WOODEN_COFFIN.get())
                || !state.hasProperty(BlockMulti.INVISIBLE)
                || Boolean.TRUE.equals(state.getValue(BlockMulti.INVISIBLE))) {
            return;
        }
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);
        float rotation = te.getDirection().getRotationAngle();
        boolean upright = te.getUpright();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5F, y + 2.16F, z + 0.5F);
        GlStateManager.rotate(-rotation, 0, 1, 0); //passing in negative value because of the flipping of the model below

        GlStateManager.pushMatrix();
        if (upright) {
            if ((rotation % 90) == 0) {
                GlStateManager.translate(0, -1.25, 1.85);
            } else {
                GlStateManager.translate(0, -1.25, 2.1);
            }
        } else {
            GlStateManager.translate(0, 0, -0.22f);
        }
        GlStateManager.rotate(upright ? 265 : 180, 1, 0, 0);
        GlStateManager.scale(0.09f, 0.09f, 0.09f);
        ResourceLocation texture = TEXTURES.getOrDefault(te.getVariant(), TEXTURES.get(BlockWoodenCoffin.Variant.OAK));
        VertexConsumer vertices = getActiveBufferSource().getBuffer(RenderType.entityCutoutNoCull(texture));
        float lidAngle = te.getPrevLidAngle() + (te.getLidAngle() - te.getPrevLidAngle()) * partialTicks;
        LegacyModelBase.renderWithContext(getActivePoseStack(), vertices, getActivePackedLight(),
                getActivePackedOverlay(), () -> COFFIN_MODEL.renderAll((float) (-lidAngle / 180F * Math.PI)));
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    @Override
    protected double getNameplateOffsetZ(TileWoodenCoffin te, double z) {
        if (!te.getUpright()) {
            return super.getNameplateOffsetZ(te, z);

        }

        double offSetZ = Math.max(Math.min(Minecraft.getInstance().player.getZ() - te.getPos().getZ(), 1), -1);
        return z + offSetZ;
    }

    @Override
    protected double getNameplateOffsetX(TileWoodenCoffin te, double x) {
        if (!te.getUpright()) {
            return super.getNameplateOffsetX(te, x);

        }

        double offSetX = Math.max(Math.min(Minecraft.getInstance().player.getX() - te.getPos().getX(), 1), -1);
        return x + offSetX;
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockWoodenCoffin.Variant variant = ItemBlockWoodenCoffin.getVariant(stack);

        poseStack.pushPose();
        ResourceLocation texture = TEXTURES.getOrDefault(variant, TEXTURES.get(BlockWoodenCoffin.Variant.OAK));
        VertexConsumer vertices = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        LegacyModelBase.renderWithContext(poseStack, vertices, packedLight, packedOverlay, COFFIN_MODEL::renderAll);
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
