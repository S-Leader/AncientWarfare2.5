package net.shadowmage.ancientwarfare.structure.gates.types;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.shadowmage.ancientwarfare.core.entity.AWEntityRegistry;
import net.shadowmage.ancientwarfare.core.owner.Owner;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.LegacyItemStack;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.config.AWStructureStatics;
import net.shadowmage.ancientwarfare.structure.entity.DualBoundingBox;
import net.shadowmage.ancientwarfare.structure.entity.EntityGate;
import net.shadowmage.ancientwarfare.structure.gates.IGateType;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.init.AWStructureItems;
import net.shadowmage.ancientwarfare.structure.init.AWStructureSounds;
import net.shadowmage.ancientwarfare.structure.tile.TEGateProxy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class Gate implements IGateType {

    private static final Gate[] gateTypes = new Gate[16];

    private static final Gate basicWood = new Gate(0, "_wood_1.png", AWStructureSounds.WOODEN_GATE, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, AWStructureSounds.WOODEN_GATE_BREAK, AWStructureStatics.gateVerticalWoodenMaxHealth).setName("gateBasicWood").setVariant(Variant.WOOD_BASIC);
    private static final Gate basicIron = new Gate(1, "_iron_1.png", AWStructureSounds.IRON_GATE, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, AWStructureSounds.IRON_GATE_BREAK, AWStructureStatics.gateVerticalIronMaxHealth).setName("gateBasicIron").setVariant(Variant.IRON_BASIC).setModel(1);

    private static final Gate singleWood = new GateSingle(4, "_wood_1.png", AWStructureSounds.WOODEN_GATE, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, AWStructureSounds.WOODEN_GATE_BREAK, AWStructureStatics.gateSingleWoodMaxHealth).setName("gateSingleWood").setVariant(Variant.WOOD_SINGLE);
    private static final Gate singleIron = new GateSingle(5, "_iron_1.png", AWStructureSounds.IRON_GATE, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, AWStructureSounds.IRON_GATE_BREAK, AWStructureStatics.gateSingleIronMaxHealth).setName("gateSingleIron").setVariant(Variant.IRON_SINGLE).setModel(1);

    private static final Gate doubleWood = new GateDouble(8, "_wood_1.png", AWStructureSounds.WOODEN_GATE, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, AWStructureSounds.WOODEN_GATE_BREAK, AWStructureStatics.gateDoubleWoodMaxHealth).setName("gateDoubleWood").setVariant(Variant.WOOD_DOUBLE);
    private static final Gate doubleIron = new GateDouble(9, "_iron_1.png", AWStructureSounds.IRON_GATE, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, AWStructureSounds.IRON_GATE_BREAK, AWStructureStatics.gateDoubleIronMaxHealth).setName("gateDoubleIron").setVariant(Variant.IRON_DOUBLE).setModel(1);

    private static final Gate rotatingBridge = new GateRotatingBridge(12, "_bridge_wood_1.png", AWStructureSounds.WOODEN_GATE, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, AWStructureSounds.WOODEN_GATE_BREAK, AWStructureStatics.drawbridgeMaxHealth);

    private static final HashMap<String, Integer> gateIDByName = new HashMap<>();

    static {
        gateIDByName.put("gate.verticalWooden", 0);
        gateIDByName.put("gate.verticalIron", 1);
        gateIDByName.put("gate.singleWood", 4);
        gateIDByName.put("gate.singleIron", 5);
        gateIDByName.put("gate.doubleWood", 8);
        gateIDByName.put("gate.doubleIron", 9);
        gateIDByName.put("gate.drawbridge", 12);
    }

    public Variant getVariant() {
        return variant;
    }

    public boolean isWood(Variant variant) {
        switch (variant) {
            case WOOD_DOUBLE:
            case WOOD_ROTATING:
            case WOOD_SINGLE:
            case WOOD_BASIC:
                return true;
            default:
                return false;
        }
    }

    public enum Variant {
        WOOD_BASIC, IRON_BASIC, WOOD_SINGLE, IRON_SINGLE, WOOD_DOUBLE, IRON_DOUBLE, WOOD_ROTATING
    }

    private final int globalID;
    protected String displayName = "";
    protected String tooltip = "";
    protected Variant variant;
    protected int maxHealth = 80;
    private int modelType = 0;
    public SoundEvent moveSound;
    public SoundEvent hurtSound;
    public SoundEvent breakSound;

    protected boolean canSoldierInteract = true;

    protected float moveSpeed = 0.5f * 0.05f;
    /// 1/2 block / second

    private final ItemStack displayStack;

    private ResourceLocation textureLocation;
    private ResourceLocation textureLocationHurt;

    /*
     *
     */
    public Gate(int id, String textureLocation, SoundEvent moveSound, SoundEvent hurtSound, SoundEvent breakSound, int maxHealth) {
        globalID = id;
        tooltip = "item.gate." + id + ".tooltip";
        if (id >= 0 && id < gateTypes.length && gateTypes[id] == null) {
            gateTypes[id] = this;
        }
        displayStack = LegacyItemStack.of(AWStructureItems.GATE_SPAWNER, 1, id);
        this.textureLocation = new ResourceLocation("ancientwarfare:textures/model/structure/gate/gate" + textureLocation);
        textureLocationHurt = new ResourceLocation("ancientwarfare:textures/model/structure/gate/gate_wood_1_damaged_2.png");
        this.moveSound = moveSound;
        this.hurtSound = hurtSound;
        this.breakSound = breakSound;
        this.maxHealth = maxHealth;
    }

    protected final Gate setName(String name) {
        displayName = name;
        return this;
    }

    protected final Gate setVariant(Variant variant) {
        this.variant = variant;
        return this;
    }

    protected final Gate setModel(int type) {
        modelType = type;
        return this;
    }

    @Override
    public int getGlobalID() {
        return globalID;
    }

    @Override
    public int getModelType() {
        return modelType;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getTooltip() {
        return tooltip;
    }

    @Override
    public ItemStack getConstructingItem() {
        return LegacyItemStack.of(AWStructureItems.GATE_SPAWNER, 1, globalID);
    }

    @Override
    public ItemStack getDisplayStack() {
        return displayStack;
    }

    @Override
    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    public float getMoveSpeed() {
        return moveSpeed;
    }

    @Override
    public ResourceLocation getTexture() {
        return textureLocation;
    }

    @Override
    public boolean canActivate(EntityGate gate, boolean open) {
        return true;
    }

    @Override
    public boolean canSoldierActivate() {
        return canSoldierInteract;
    }

    public static String getGateNameFor(EntityGate gate) {
        int id = gate.getGateType().getGlobalID();
        return getGateNameFor(id);
    }

    private static String getGateNameFor(int id) {
        int gateID;
        for (Map.Entry<String, Integer> entry : gateIDByName.entrySet()) {
            gateID = entry.getValue();
            if (gateID == id) {
                return entry.getKey();
            }
        }
        return "gate.verticalWooden";
    }

    public static Gate getGateByName(String name) {
        if (gateIDByName.containsKey(name)) {
            return getGateByID(gateIDByName.get(name));
        }
        return basicWood;
    }

    public static Gate getGateByID(int id) {
        if (id >= 0 && id < gateTypes.length) {
            return gateTypes[id];
        }
        return basicWood;
    }

    public void updateRenderBoundingBox(EntityGate gate) {
        if (gate.getRenderBoundingBox().getSize() == 0) {
            BlockPos min = BlockTools.getMin(gate.pos1, gate.pos2);
            BlockPos max = BlockTools.getMax(gate.pos1, gate.pos2);
            gate.setRenderBoundingBox(getRenderBoundingBox(gate, min, max));
        }
    }

    protected AABB getRenderBoundingBox(EntityGate gate, BlockPos min, BlockPos max) {
        return new AABB(min, max.offset(1, 1, 1));
    }

    @Override
    public void setCollisionBoundingBox(EntityGate gate) {
        if (gate.pos1 == null || gate.pos2 == null) {
            return;
        }
        BlockPos min = BlockTools.getMin(gate.pos1, gate.pos2);
        BlockPos max = BlockTools.getMax(gate.pos1, gate.pos2);
        if (!(gate.getBoundingBox() instanceof DualBoundingBox)) {
            gate.setBoundingBox(new DualBoundingBox(min, max));
        }
        if (gate.edgePosition > 0) {
            gate.setBoundingBox(new AABB(min.getX(), max.getY() + 0.5d, min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1));
        } else {
            gate.setBoundingBox(new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1));
        }
    }

    @Override
    public boolean arePointsValidPair(BlockPos pos1, BlockPos pos2) {
        return pos1.getX() == pos2.getX() || pos1.getZ() == pos2.getZ();
    }

    @Override
    public void setInitialBounds(EntityGate gate, BlockPos pos1, BlockPos pos2) {
        BlockPos min = BlockTools.getMin(pos1, pos2);
        BlockPos max = BlockTools.getMax(pos1, pos2);
        boolean wideOnXAxis = min.getX() != max.getX();
        float width = wideOnXAxis ? max.getX() - min.getX() + 1 : max.getZ() - min.getZ() + 1;
        float xOffset = wideOnXAxis ? width * 0.5f : 0.5f;
        float zOffset = wideOnXAxis ? 0.5f : width * 0.5f;
        gate.setPositions(min, max);
        gate.setRenderBoundingBox(getRenderBoundingBox(gate, min, max));
        gate.edgeMax = (float) max.getY() - min.getY() + 1;
        gate.setPos(min.getX() + xOffset, min.getY(), min.getZ() + zOffset);
    }

    @Override
    public void onGateFinishOpen(EntityGate gate) {
        //overriden in subclass
    }

    @Override
    public void onGateStartClose(EntityGate gate) {
        //overriden in subclass
    }

    @Override
    public void onGateFinishClose(EntityGate gate) {
        if (gate.level().isClientSide) {
            return;
        }
        BlockPos min = BlockTools.getMin(gate.pos1, gate.pos2);
        BlockPos max = BlockTools.getMax(gate.pos1, gate.pos2);
        closeBetween(gate, min, max);
    }

    final void closeBetween(EntityGate gate, BlockPos min, BlockPos max) {
        BlockPos.betweenClosed(min, max).forEach(pos -> {
            placeProxyIfNotPresent(gate, pos);
            if (!gate.getRenderedTilePos().isPresent()) {
                WorldTools.getTile(gate.level(), pos, TEGateProxy.class).ifPresent(te -> {
                    te.setRender();
                    gate.setRenderedTilePos(pos.immutable());
                });
            }
        });
    }

    public void setRenderedTileIfNotPresent(EntityGate gate) {
        if (gate.getRenderedTilePos().isPresent()) {
            return;
        }
        BlockPos min = BlockTools.getMin(gate.pos1, gate.pos2);
        BlockPos max = BlockTools.getMax(gate.pos1, gate.pos2);
        BlockPos.betweenClosed(min, max).forEach(pos -> placeProxyIfNotPresent(gate, pos));
        Optional<TEGateProxy> renderedTe = StreamSupport.stream(BlockPos.betweenClosed(min, max).spliterator(), false)
                .map(pos -> WorldTools.getTile(gate.level(), pos, TEGateProxy.class)).filter(te -> te.isPresent() && te.get().doesRender()).map(Optional::get).findFirst();
        if (renderedTe.isPresent()) {
            gate.setRenderedTilePos(renderedTe.get().getPos());
        } else {
            WorldTools.getTile(gate.level(), min, TEGateProxy.class).ifPresent(te -> {
                te.setRender();
                gate.setRenderedTilePos(min);
            });
        }
    }

    private void placeProxyIfNotPresent(EntityGate gate, BlockPos pos) {
        BlockState state = gate.level().getBlockState(pos);
        Block block = state.getBlock();
        if (block != AWStructureBlocks.GATE_PROXY) {
            if (!gate.level().isEmptyBlock(pos)) {
                Block.dropResources(state, gate.level(), pos);
            }
            gate.level().setBlock(pos, AWStructureBlocks.GATE_PROXY.defaultBlockState(), 3);
        }
        WorldTools.getTile(gate.level(), pos, TEGateProxy.class).ifPresent(t -> {
            t.setOwner(gate);
        });
    }

    /*
     * @return a fully setup gate, or null if chosen spawn position is invalid (blocks in the way)
     */
    public static Optional<EntityGate> constructGate(Level world, BlockPos pos1, BlockPos pos2, Gate type, Direction facing, Owner owner) {
        BlockPos min = BlockTools.getMin(pos1, pos2);
        BlockPos max = BlockTools.getMax(pos1, pos2);
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!world.isEmptyBlock(pos)) {
                        AncientWarfareStructure.LOG.info("could not create gate for non-air block at: {},{},{} block: {}", x, y, z, world.getBlockState(pos).getBlock());
                        return Optional.empty();
                    }
                }
            }
        }

        Direction rotatedFacing = facing;
        if (pos1.getX() == pos2.getX()) {
            if (facing == Direction.SOUTH || facing == Direction.NORTH) {
                rotatedFacing = facing.getClockWise();
            }
        } else if (pos1.getZ() == pos2.getZ() && (facing == Direction.EAST || facing == Direction.WEST)) {
            rotatedFacing = facing.getClockWise();
        }

        EntityGate ent = AWEntityRegistry.createEntity(
                new ResourceLocation(AncientWarfareStructure.MOD_ID, AWEntityRegistry.AW_GATES),
                world, EntityGate.class);
        if (ent == null) {
            return Optional.empty();
        }
        ent.setGateType(type);
        ent.gateOrientation = rotatedFacing;
        type.setInitialBounds(ent, pos1, pos2);
        type.onGateFinishClose(ent);
        ent.setOwner(owner);
        return Optional.of(ent);
    }

    public static ItemStack getItemToConstruct(int type) {
        return getGateByID(type).getConstructingItem();
    }
}
