package net.shadowmage.ancientwarfare.structure.item;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.input.IItemKeyInterface;
import net.shadowmage.ancientwarfare.core.input.InputHandler;
import net.shadowmage.ancientwarfare.core.owner.Owner;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelLoader;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.structure.entity.EntityGate;
import net.shadowmage.ancientwarfare.structure.event.IBoxRenderer;
import net.shadowmage.ancientwarfare.structure.gates.types.Gate;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ItemGateSpawner extends ItemBaseStructure implements IItemKeyInterface, IBoxRenderer {
    private static final String AW_GATE_INFO_TAG = "AWGateInfo";
    private final int fixedGateId;

    /** Compatibility-only metadata container for old inventories/templates. */
    @Deprecated
    public ItemGateSpawner(String name) {
        this(name, -1);
        setHasSubtypes(true);
    }

    /** Modern 1.20 gate placer: one registry id represents exactly one gate type. */
    public ItemGateSpawner(String name, int fixedGateId) {
        super(name);
        this.fixedGateId = fixedGateId;
        setMaxStackSize(1);
    }

    private int getGateId(ItemStack stack) {
        return fixedGateId >= 0 ? fixedGateId : stack.getDamageValue();
    }

    private Gate getGate(ItemStack stack) {
        Gate gate = Gate.getGateByID(getGateId(stack));
        return gate != null ? gate : Gate.getGateByID(0);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level world, List<String> tooltip, TooltipFlag flag) {
        CompoundTag tag;
        //noinspection ConstantConditions
        if (stack.hasTag() && stack.getTag().contains(AW_GATE_INFO_TAG)) {
            tag = stack.getTag().getCompound(AW_GATE_INFO_TAG);
        } else {
            tag = new CompoundTag();
        }
        if (tag.contains("pos1") && tag.contains("pos2")) {
            tooltip.add(I18n.get("guistrings.gate.construct"));
        } else {
            String key = InputHandler.ALT_ITEM_USE_1.getTranslatedKeyMessage().getString();
            tooltip.add(I18n.get("guistrings.gate.use_primary_item_key", key));
        }
        tooltip.add(I18n.get("guistrings.gate.clear_item"));
    }

    public void getSubItems(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (fixedGateId >= 0) {
            items.add(new ItemStack(this));
            return;
        }
    }

    @Override
    public String getDescriptionId(ItemStack par1ItemStack) {
        return "item." + getGate(par1ItemStack).getDisplayName();
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (world.isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        CompoundTag tag;
        //noinspection ConstantConditions
        if (stack.hasTag() && stack.getTag().contains(AW_GATE_INFO_TAG)) {
            tag = stack.getTag().getCompound(AW_GATE_INFO_TAG);
        } else {
            tag = new CompoundTag();
        }
        if (player.isShiftKeyDown()) {
            tag = new CompoundTag();
            stack.setTag(tag);
        } else if (tag.contains("pos1") && tag.contains("pos2")) {
            BlockPos pos1 = BlockPos.of(tag.getLong("pos1"));
            BlockPos pos2 = BlockPos.of(tag.getLong("pos2"));
            BlockPos avg = BlockTools.getAverageOf(pos1, pos2);
            int max = 10;
            if (pos1.getX() - pos2.getX() > max)
                max = pos1.getX() - pos2.getX();
            else if (pos2.getX() - pos1.getX() > max)
                max = pos2.getX() - pos1.getX();
            if (pos1.getZ() - pos2.getZ() > max)
                max = pos1.getZ() - pos2.getZ();
            else if (pos2.getZ() - pos1.getZ() > max)
                max = pos2.getZ() - pos1.getZ();
            if (Math.sqrt(player.distanceToSqr(avg.getX() + 0.5, pos1.getY() + 0.5, avg.getZ() + 0.5)) > max && Math.sqrt(player.distanceToSqr(avg.getX() + 0.5, pos2.getY() + 0.5, avg.getZ() + 0.5)) > max) {
                player.sendSystemMessage(Component.translatable("guistrings.gate.too_far"));
                return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
            }
            if (!canSpawnGate(world, pos1, pos2)) {
                player.sendSystemMessage(Component.translatable("guistrings.gate.exists"));
                return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
            }
            Optional<EntityGate> entity = Gate.constructGate(world, pos1, pos2, getGate(stack), player.getDirection(), new Owner(player));
            if (entity.isPresent()) {
                world.addFreshEntity(entity.get());
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                tag.remove("pos1");
                tag.remove("pos2");
                stack.setTag(tag);
            } else {
                player.sendSystemMessage(Component.translatable("guistrings.gate.need_to_clear"));
            }
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    private boolean canSpawnGate(Level world, BlockPos pos1, BlockPos pos2) {
        BlockPos min = BlockTools.getMin(pos1, pos2);
        BlockPos max = BlockTools.getMax(pos1, pos2);
        AABB newGateBB = new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
        AABB oldGateBB;
        List<EntityGate> gates = world.getEntitiesOfClass(EntityGate.class, newGateBB);
        for (EntityGate gate : gates) {
            min = BlockTools.getMin(gate.pos1, gate.pos2);
            max = BlockTools.getMax(gate.pos1, gate.pos2);
            oldGateBB = new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
            if (oldGateBB.intersects(newGateBB)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onKeyActionClient(Player player, ItemStack stack, ItemAltFunction altFunction) {
        return altFunction == ItemAltFunction.ALT_FUNCTION_1;
    }

    @Override
    public void onKeyAction(Player player, ItemStack stack, ItemAltFunction altFunction) {
        BlockPos hit = BlockTools.getBlockClickedOn(player, player.level(), true);
        if (hit == null) {
            return;
        }
        CompoundTag tag;
        //noinspection ConstantConditions
        if (stack.hasTag() && stack.getTag().contains(AW_GATE_INFO_TAG)) {
            tag = stack.getTag().getCompound(AW_GATE_INFO_TAG);
        } else {
            tag = new CompoundTag();
        }
        if (!tag.contains("pos2")) {
            if (tag.contains("pos1")) {
                Gate g = getGate(stack);
                if (g.arePointsValidPair(BlockPos.of(tag.getLong("pos1")), hit)) {
                    tag.putLong("pos2", hit.asLong());
                    player.sendSystemMessage(Component.translatable("guistrings.gate.set_pos_two"));
                } else {
                    player.sendSystemMessage(Component.translatable("guistrings.gate.invalid_position"));
                }
            } else {
                tag.putLong("pos1", hit.asLong());
                player.sendSystemMessage(Component.translatable("guistrings.gate.set_pos_one"));
            }
        }
        stack.getOrCreateTag().put(AW_GATE_INFO_TAG, tag);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderBox(Player player, InteractionHand hand, ItemStack stack, float delta) {
        CompoundTag tag = stack.getTag();
        BlockPos p1;
        BlockPos p2;
        if (tag != null && tag.contains(AW_GATE_INFO_TAG)) {
            tag = tag.getCompound(AW_GATE_INFO_TAG);
            if (tag.contains("pos1")) {
                p1 = BlockPos.of(tag.getLong("pos1"));
                if (tag.contains("pos2")) {
                    p2 = BlockPos.of(tag.getLong("pos2"));
                } else {
                    p2 = BlockTools.getBlockClickedOn(player, player.level(), true);
                    if (p2 == null) {
                        return;
                    }
                }
            } else {
                p1 = BlockTools.getBlockClickedOn(player, player.level(), true);
                if (p1 == null) {
                    return;
                }
                p2 = p1;
            }
        } else {
            p1 = BlockTools.getBlockClickedOn(player, player.level(), true);
            if (p1 == null) {
                return;
            }
            p2 = p1;
        }
        Util.renderBoundingBox(player, BlockTools.getMin(p1, p2), BlockTools.getMax(p1, p2), delta);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        ResourceLocation basePath = new ResourceLocation(AncientWarfareCore.MOD_ID, "structure/gate_spawner");
        ModelResourceLocation ironBasic = new ModelResourceLocation(basePath, "variant=gate_iron_basic");
        ModelResourceLocation ironDouble = new ModelResourceLocation(basePath, "variant=gate_iron_double");
        ModelResourceLocation ironSingle = new ModelResourceLocation(basePath, "variant=gate_iron_single");
        ModelResourceLocation woodBasic = new ModelResourceLocation(basePath, "variant=gate_wood_basic");
        ModelResourceLocation woodDouble = new ModelResourceLocation(basePath, "variant=gate_wood_double");
        ModelResourceLocation woodRotating = new ModelResourceLocation(basePath, "variant=gate_wood_rotating");
        ModelResourceLocation woodSingle = new ModelResourceLocation(basePath, "variant=gate_wood_single");

        if (fixedGateId >= 0) {
            ModelResourceLocation model = modelForVariant(getGate(new ItemStack(this)).getVariant(),
                    ironBasic, ironDouble, ironSingle, woodBasic, woodDouble, woodRotating, woodSingle);
            LegacyModelLoader.setCustomModelResourceLocation(this, 0, model);
            LegacyModelLoader.registerItemVariants(this, model);
            return;
        }

        LegacyModelLoader.setCustomMeshDefinition(this, stack -> modelForVariant(getGate(stack).getVariant(),
                ironBasic, ironDouble, ironSingle, woodBasic, woodDouble, woodRotating, woodSingle));
        LegacyModelLoader.registerItemVariants(this, ironBasic, ironDouble, ironSingle, woodBasic, woodDouble, woodRotating, woodSingle);
    }
    private static ModelResourceLocation modelForVariant(Gate.Variant variant,
                                                         ModelResourceLocation ironBasic,
                                                         ModelResourceLocation ironDouble,
                                                         ModelResourceLocation ironSingle,
                                                         ModelResourceLocation woodBasic,
                                                         ModelResourceLocation woodDouble,
                                                         ModelResourceLocation woodRotating,
                                                         ModelResourceLocation woodSingle) {
        return switch (variant) {
            case IRON_BASIC -> ironBasic;
            case IRON_DOUBLE -> ironDouble;
            case IRON_SINGLE -> ironSingle;
            case WOOD_DOUBLE -> woodDouble;
            case WOOD_ROTATING -> woodRotating;
            case WOOD_SINGLE -> woodSingle;
            case WOOD_BASIC -> woodBasic;
        };
    }
}
