package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.MathUtils;
import net.shadowmage.ancientwarfare.core.util.NBTHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EntityStatueInfo {
    private static final String ENTITY_NAME_TAG = "entityName";
    private static final String STATUE_ENTITY_NAME_TAG = "statueEntityName";
    private static final String OVERALL_TRANSFORM_TAG = "overallTransform";
    private static final String PART_TRANSFORMS_TAG = "partTransforms";

    private RenderType renderType = RenderType.ENTITY;

    private Entity entity = null;
    private ResourceLocation entityName = null;
    private boolean entityOnFire = false;

    private String statueEntityName = "Zombie";
    private Transform overallTransform = new Transform();
    private Map<String, Transform> partTransforms = new HashMap<>();

    Optional<Entity> getRenderEntity(Level world) {
        if (entity != null) {
            return Optional.of(entity);
        }
        if (entityName != null && world.isClientSide) {
            EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityName);
            if (entityType == null) {
                entityName = null;
                return Optional.empty();
            }
            entity = entityType.create(world);
            return Optional.ofNullable(entity);
        }
        return Optional.empty();
    }

    boolean isEntityOnFire() {
        return entityOnFire;
    }

    void setEntityOnFire(boolean entityOnFire) {
        this.entityOnFire = entityOnFire;
    }

    void resetEntityName() {
        entityName = null;
        entity = null;
    }

    @Nullable
    public ResourceLocation getEntityName() {
        return entityName;
    }

    void setEntityName(@Nullable ResourceLocation entityName) {
        this.entityName = entityName;
        entity = null;
    }

    public void deserializeNBT(CompoundTag tag) {
        renderType = RenderType.byName(tag.getString("renderType"));
        if (tag.contains(STATUE_ENTITY_NAME_TAG)) {
            statueEntityName = tag.getString(STATUE_ENTITY_NAME_TAG);
            if (tag.contains(OVERALL_TRANSFORM_TAG)) {
                overallTransform = new Transform();
                overallTransform.deserializeNBT(tag.getCompound(OVERALL_TRANSFORM_TAG));
            }
            if (tag.contains(PART_TRANSFORMS_TAG)) {
                partTransforms = NBTHelper.getMap(tag.getList(PART_TRANSFORMS_TAG, Constants.NBT.TAG_COMPOUND), t -> t.getString("name"), t -> {
                    Transform transform = new Transform();
                    transform.deserializeNBT(t.getCompound("transform"));
                    return transform;
                });
            }
        } else {
            if (tag.contains(ENTITY_NAME_TAG)) {
                setEntityName(new ResourceLocation(tag.getString(ENTITY_NAME_TAG)));
                setEntityOnFire(tag.getBoolean("entityOnFire"));
            } else {
                setEntityName(null);
            }
        }

    }

    public CompoundTag serializeNBT(CompoundTag tag) {
        tag.putString("renderType", renderType.getSerializedName());
        if (renderType == RenderType.ENTITY) {
            if (getEntityName() != null) {
                tag.putString(ENTITY_NAME_TAG, getEntityName().toString());
                tag.putBoolean("entityOnFire", isEntityOnFire());
            }
        } else {
            tag.putString(STATUE_ENTITY_NAME_TAG, statueEntityName);
            tag.put(OVERALL_TRANSFORM_TAG, overallTransform.serializeNBT());
            tag.put(PART_TRANSFORMS_TAG, NBTHelper.mapToCompoundList(partTransforms, (t, name) -> t.putString("name", name),
                    (t, transform) -> t.put("transform", transform.serializeNBT())));
        }
        return tag;
    }

    public void setRenderType(RenderType renderType) {
        this.renderType = renderType;
    }

    public void setPartTransform(String partName, Transform partTransform) {
        partTransforms.put(partName, partTransform);
    }

    public RenderType getRenderType() {
        return renderType;
    }

    public String getStatueEntityName() {
        return statueEntityName;
    }

    public Map<String, Transform> getPartTransforms() {
        return partTransforms;
    }

    public Transform getOverallTransform() {
        return overallTransform;
    }

    public void setOverallTransform(Transform transform) {
        overallTransform = transform;
    }

    public void setStatueEntityName(String name) {
        statueEntityName = name;
    }

    public enum RenderType implements StringRepresentable {
        ENTITY("entity"),
        MODEL("model");

        private static final Map<String, RenderType> NAME_TYPE = new HashMap<>();

        static {
            for (RenderType type : values()) {
                NAME_TYPE.put(type.getSerializedName(), type);
            }
        }

        private String name;

        RenderType(String name) {
            this.name = name;
        }

        public static RenderType byName(String name) {
            return NAME_TYPE.getOrDefault(name, ENTITY);
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static class Transform implements INBTSerializable<CompoundTag> {
        private float offsetX = 0;
        private float offsetY = 0;
        private float offsetZ = 0;
        private float rotationX = 0;
        private float rotationY = 0;
        private float rotationZ = 0;
        private float scale = 1;

        public Transform() {
        }

        Transform(float offsetX, float offsetY, float offsetZ, float rotationX, float rotationY, float rotationZ, float scale) {
            this(offsetX, offsetY, offsetZ, rotationX, rotationY, rotationZ);
            this.scale = scale;
        }

        Transform(float offsetX, float offsetY, float offsetZ, float rotationX, float rotationY, float rotationZ) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.rotationX = rotationX;
            this.rotationY = rotationY;
            this.rotationZ = rotationZ;
        }

        public float getOffsetX() {
            return offsetX;
        }

        public void setOffsetX(float offset) {
            offsetX = offset;
        }

        public float getOffsetY() {
            return offsetY;
        }

        public void setOffsetY(float offsetY) {
            this.offsetY = offsetY;
        }

        public float getOffsetZ() {
            return offsetZ;
        }

        public void setOffsetZ(float offsetZ) {
            this.offsetZ = offsetZ;
        }

        public float getRotationX() {
            return rotationX;
        }

        public void setRotationX(float rotationX) {
            this.rotationX = rotationX;
        }

        public float getRotationY() {
            return rotationY;
        }

        public void setRotationY(float rotationY) {
            this.rotationY = rotationY;
        }

        public float getRotationZ() {
            return rotationZ;
        }

        public void setRotationZ(float rotationZ) {
            this.rotationZ = rotationZ;
        }

        public float getScale() {
            return scale;
        }

        public void setScale(float scale) {
            this.scale = scale;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            setNonDefaultValue(tag, "offsetX", offsetX);
            setNonDefaultValue(tag, "offsetY", offsetY);
            setNonDefaultValue(tag, "offsetZ", offsetZ);

            setNonDefaultValue(tag, "rotationX", rotationX);
            setNonDefaultValue(tag, "rotationY", rotationY);
            setNonDefaultValue(tag, "rotationZ", rotationZ);

            setNonDefaultValue(tag, "scale", scale, 1);

            return tag;
        }

        private void setNonDefaultValue(CompoundTag tag, String tagName, float value) {
            setNonDefaultValue(tag, tagName, value, 0);
        }

        private void setNonDefaultValue(CompoundTag tag, String tagName, float value, float defaultValue) {
            if (!MathUtils.epsilonEquals(value, defaultValue)) {
                tag.putFloat(tagName, value);
            }
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            offsetX = loadValue(nbt, "offsetX").orElse(0f);
            offsetY = loadValue(nbt, "offsetY").orElse(0f);
            offsetZ = loadValue(nbt, "offsetZ").orElse(0f);

            rotationX = loadValue(nbt, "rotationX").orElse(0f);
            rotationY = loadValue(nbt, "rotationY").orElse(0f);
            rotationZ = loadValue(nbt, "rotationZ").orElse(0f);

            scale = loadValue(nbt, "scale").orElse(1f);
        }

        private Optional<Float> loadValue(CompoundTag nbt, String name) {
            if (!nbt.contains(name)) {
                return Optional.empty();
            }

            return Optional.of(nbt.getFloat(name));
        }
    }
}