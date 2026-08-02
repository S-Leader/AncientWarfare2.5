package net.shadowmage.ancientwarfare.structure.render;

import codechicken.lib.model.PerspectiveModelState;
import codechicken.lib.render.item.IItemRenderer;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelBase;
import net.shadowmage.ancientwarfare.core.compat.client.LegacyBlockEntityRenderer;
import net.shadowmage.ancientwarfare.structure.block.BlockFlag;
import net.shadowmage.ancientwarfare.structure.tile.TileFlag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static codechicken.lib.util.TransformUtils.create;
import static codechicken.lib.util.TransformUtils.flipLeft;

public class ProtectionFlagRenderer extends LegacyBlockEntityRenderer<TileFlag> implements IItemRenderer {
    private final LegacyBannerModel bannerModel = new LegacyBannerModel();
    private final LegacyHumanoidHeadModel humanoidHead = new LegacyHumanoidHeadModel();

    private static final PerspectiveModelState TRANSFORMS;

    static {
        Map<ItemDisplayContext, Transformation> map = new HashMap<>();
        Transformation thirdPerson = create(0F, 2.5F, 0F, 75F, -45F, 0F, 0.6F);
        map.put(ItemDisplayContext.GUI, create(0F, -3F, 0F, 30F, 45F, 0F, 0.525F));
        map.put(ItemDisplayContext.GROUND, create(0F, 3F, 0F, 0F, 0F, 0F, 0.25F));
        map.put(ItemDisplayContext.FIXED, create(0F, 0F, 0F, 0F, 0F, 0F, 0.5F));
        map.put(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, thirdPerson);
        map.put(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, flipLeft(thirdPerson));
        map.put(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, create(0F, 0F, 0F, 0F, -45F, 0F, 0.4F));
        map.put(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, create(0F, 0F, 0F, 0F, 225F, 0F, 0.4F));
        TRANSFORMS = new PerspectiveModelState(map);
    }

    @Override
    public void render(TileFlag te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        boolean flag = te.getWorld() != null;
        int angle = flag ? getRotation(te) : 0;
        long worldTime = flag ? te.getWorld().getGameTime() : 0L;
        String faction = te.getName();
        BlockPos pos = te.getPos();

        /*
         * A modern BlockEntityRenderer receives a PoseStack that is already translated
         * into block-local coordinates. Rebuilding a second PoseStack and subtracting
         * the camera position applies the view transform twice, which makes the flag
         * appear to drift with the player's camera. Use the active render context
         * supplied by LegacyBlockEntityRenderer instead.
         */
        PoseStack poseStack = getActivePoseStack();
        MultiBufferSource buffer = getActiveBufferSource();
        int packedLight = getActivePackedLight();

        render(poseStack, buffer, packedLight, (float) x, (float) y, (float) z, partialTicks, alpha, angle, worldTime, faction, pos);
        if (te.isPlayerOwned()) {
            renderPlayerHead(poseStack, buffer, packedLight, te.getPlayerProfile(), (float) x, (float) y, (float) z, partialTicks, angle);
        }
    }

    private int getRotation(TileFlag te) {
        BlockState state = te.getWorld().getBlockState(te.getPos());
        return state.hasProperty(BlockFlag.ROTATION) ? state.getValue(BlockFlag.ROTATION) : 0;
    }

    private void renderPlayerHead(PoseStack poseStack, MultiBufferSource buffer, int packedLight, GameProfile profile, float x, float y, float z, float animateTicks, int rotation) {
        ResourceLocation resourcelocation;

        Minecraft minecraft = Minecraft.getInstance();
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(profile);

        if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
            resourcelocation = minecraft.getSkinManager().registerTexture(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
        } else {
            UUID uuid = UUIDUtil.getOrCreatePlayerUUID(profile);
            resourcelocation = DefaultPlayerSkin.getDefaultSkin(uuid);
        }

        poseStack.pushPose();

        poseStack.translate(x + 0.5F, y + 2F, z + 0.5F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);

        VertexConsumer vertices = buffer.getBuffer(RenderType.entityTranslucent(resourcelocation));
        LegacyModelBase.renderWithContext(poseStack, vertices, packedLight, OverlayTexture.NO_OVERLAY,
                () -> humanoidHead.render(null, animateTicks, 0.0F, 0.0F, 360 * (rotation / 16.0F) - 180, 0.0F, 0.0625F));
        poseStack.popPose();
    }

    private void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, float x, float y, float z, float partialTicks, float alpha, int rotation, float worldTime, String faction, BlockPos pos) {
        poseStack.pushPose();

        poseStack.translate(x + 0.5F, y + 0.5F, z + 0.5F);
        float f1 = (float) (rotation * 360) / 16.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(-f1));
        bannerModel.bannerStand.showModel = true;

        float f3 = (float) (pos.getX() * 7 + pos.getY() * 9 + pos.getZ() * 13) + worldTime + partialTicks;
        bannerModel.bannerSlate.rotateAngleX = (-0.0125F + 0.01F * Mth.cos(f3 * (float) Math.PI * 0.02F)) * (float) Math.PI;
        ResourceLocation resourcelocation = getBannerResourceLocation(faction);

        poseStack.pushPose();
        poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
        VertexConsumer vertices = buffer.getBuffer(RenderType.entityCutoutNoCull(resourcelocation));
        LegacyModelBase.renderWithContext(poseStack, vertices, packedLight, OverlayTexture.NO_OVERLAY, bannerModel::renderBanner);
        poseStack.popPose();

        poseStack.popPose();
    }

    private ResourceLocation getBannerResourceLocation(String faction) {
        return new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/entity/structure/banner/" + faction + ".png");
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!stack.hasTag()) {
            return;
        }

        poseStack.pushPose();

        CompoundTag tag = stack.getTag();
        //noinspection ConstantConditions
        render(poseStack, buffer, packedLight, 0, 0, 0, 0, 1, 0, 0, tag.getString("name"), BlockPos.ZERO);

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
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }
}
