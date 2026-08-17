package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.entityrules;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.datafixes.ComponentItemFixer;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.api.IStructureBuilder;
import net.shadowmage.ancientwarfare.structure.api.TemplateRuleEntityBase;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static net.shadowmage.ancientwarfare.npc.event.EventHandler.NO_SPAWN_PREVENTION_TAG;

public class TemplateRuleEntity<T extends Entity> extends TemplateRuleEntityBase {
    public static final String PLUGIN_NAME = "entity";
    private CompoundTag tag;

    public ResourceLocation registryName;
    public float xOffset;
    public float zOffset;
    public float yOffset;
    public float rotation;

    public TemplateRuleEntity() {
        super();
    }

    public TemplateRuleEntity(Level world, T entity, int turns, int x, int y, int z) {
        super();

        registryName = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());

        rotation = (entity.getYRot() + 90.f * turns) % 360.f;
        float x1 = (float) (entity.getX() % 1.d);
        float z1 = (float) (entity.getZ() % 1.d);
        if (x1 < 0) {
            x1++;
        }
        if (z1 < 0) {
            z1++;
        }
        xOffset = BlockTools.rotateFloatX(x1, z1, turns);
        zOffset = BlockTools.rotateFloatZ(x1, z1, turns);
        yOffset = (float) (entity.getY() % 1d);

        tag = entity.saveWithoutId(new CompoundTag());
        tag.remove("UUIDMost");
        tag.remove("UUIDLeast");
    }

    @Override
    public void handlePlacement(Level world, int turns, BlockPos pos, IStructureBuilder builder) {
        try {
            createEntity(world, turns, pos).ifPresent(entity -> {
                if (!world.addFreshEntity(entity)) {
                    AncientWarfareStructure.LOG.warn("Entity {} from structure rule could not be added at {}", registryName, pos);
                }
            });
        } catch (RuntimeException | LinkageError exception) {
            /*
             * Entity rules are the final structure pass. A broken legacy NBT blob
             * or an optional-mod entity must not make the already-built town piece
             * count as a failed structure and silently lose every later entity.
             */
            AncientWarfareStructure.LOG.error("Unable to place structure entity {} at {}; skipping this entity",
                    registryName, pos, exception);
        }
    }

    protected Optional<T> createEntity(Level world, int turns, BlockPos pos) {
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(registryName);
        //noinspection unchecked
        T e = entityType == null ? null : (T) entityType.create(world);
        if (e == null) {
            AncientWarfareStructure.LOG.debug("Could not create entity for name: {} Entity skipped during structure creation.", registryName);
            return Optional.empty();
        }
        CompoundTag entityNBT = ComponentItemFixer.fixRecursively(getEntityNBT(pos, turns).copy());
        // Structure templates may be placed more than once. Never reuse an entity UUID.
        entityNBT.remove("UUID");
        entityNBT.remove("UUIDMost");
        entityNBT.remove("UUIDLeast");
        removeNonExistentAttributes(e, entityNBT);

        e.load(entityNBT);
        updateEntityOnPlacement(turns, pos, e);
        addNoSpawnPreventionTag(e);
        return Optional.of(e);
    }

    private void removeNonExistentAttributes(T e, CompoundTag entityNBT) {
        if (e instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) e;
            ListTag attributes = entityNBT.getList("Attributes", Constants.NBT.TAG_COMPOUND);
            Iterator<Tag> it = attributes.iterator();
            while (it.hasNext()) {
                Tag nbt = it.next();
                if (nbt.getId() == Constants.NBT.TAG_COMPOUND) {
                    CompoundTag attribute = (CompoundTag) nbt;
                    ResourceLocation attributeId = ResourceLocation.tryParse(attribute.getString("Name"));
                    if (attributeId == null || BuiltInRegistries.ATTRIBUTE.getOptional(attributeId).map(living::getAttribute).orElse(null) == null) {
                        it.remove();
                    }
                }
            }
        }
    }

    private void addNoSpawnPreventionTag(T e) {
        if (e instanceof Enemy) {
            e.getTags().add(NO_SPAWN_PREVENTION_TAG);
        }
    }

    @SuppressWarnings("unused") //parameters used in overrides
    protected CompoundTag getEntityNBT(BlockPos pos, int turns) {
        return tag;
    }

    protected void updateEntityOnPlacement(int turns, BlockPos pos, T e) {
        e.moveTo(pos.getX() + BlockTools.rotateFloatX(xOffset, zOffset, turns), pos.getY() + yOffset,
                pos.getZ() + BlockTools.rotateFloatZ(xOffset, zOffset, turns), (rotation + 90.0F * turns) % 360.0F, 0.0F);
    }

    @Override
    public void writeRuleData(CompoundTag tag) {
        super.writeRuleData(tag);
        tag.putString("mobID", registryName.toString());
        tag.putFloat("xOffset", xOffset);
        tag.putFloat("yOffset", yOffset);
        tag.putFloat("zOffset", zOffset);
        tag.putFloat("rotation", rotation);
        tag.put("entityData", this.tag);
    }

    @Override
    public void parseRule(CompoundTag tag) {
        super.parseRule(tag);
        registryName = new ResourceLocation(tag.getString("mobID"));
        xOffset = tag.getFloat("xOffset");
        yOffset = tag.getFloat("yOffset");
        zOffset = tag.getFloat("zOffset");
        rotation = tag.getFloat("rotation");
        this.tag = tag.getCompound("entityData");
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public boolean shouldPlaceOnBuildPass(Level world, int turns, BlockPos pos, int buildPass) {
        return buildPass == 3;
    }

    @Override
    public List<ItemStack> getResources() {
        return Collections.emptyList();
    }
}
