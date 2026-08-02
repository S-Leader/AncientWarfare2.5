package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.entityrules;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.util.BlockTools;

/**
 * Template rule for paintings and item frames. Hanging entities persist their anchor in
 * TileX/TileY/TileZ, so rotating those NBT coordinates avoids accessing removed private fields.
 */
public class TemplateRuleEntityHanging extends TemplateRuleEntity<HangingEntity> {
    public static final String PLUGIN_NAME = "vanillaHangingEntity";
    private Direction direction = Direction.SOUTH;
    private BlockPos hangingOffset = BlockPos.ZERO;

    public TemplateRuleEntityHanging(Level level, HangingEntity entity, int turns, int x, int y, int z) {
        super(level, entity, turns, x, y, z);
        CompoundTag entityTag = entity.saveWithoutId(new CompoundTag());
        BlockPos anchor = new BlockPos(entityTag.getInt("TileX"), entityTag.getInt("TileY"), entityTag.getInt("TileZ"));
        this.hangingOffset = BlockTools.rotateHorizontal(anchor.offset(-x, -y, -z), turns);
        this.direction = Direction.from2DDataValue((entity.getDirection().get2DDataValue() + turns) % 4);
    }

    public TemplateRuleEntityHanging() {
        super();
    }

    @Override
    protected CompoundTag getEntityNBT(BlockPos pos, int turns) {
        CompoundTag tag = super.getEntityNBT(pos, turns).copy();
        Direction rotatedDirection = Direction.from2DDataValue((direction.get2DDataValue() + turns) % 4);
        BlockPos anchor = pos.offset(BlockTools.rotateHorizontal(hangingOffset, turns));
        tag.putByte("Facing", (byte) rotatedDirection.get2DDataValue());
        tag.putInt("TileX", anchor.getX());
        tag.putInt("TileY", anchor.getY());
        tag.putInt("TileZ", anchor.getZ());
        return tag;
    }

    @Override
    public void writeRuleData(CompoundTag tag) {
        super.writeRuleData(tag);
        tag.putByte("direction", (byte) direction.get2DDataValue());
        tag.putLong("hangingOffset", hangingOffset.asLong());
    }

    @Override
    public void parseRule(CompoundTag tag) {
        super.parseRule(tag);
        direction = Direction.from2DDataValue(tag.getByte("direction") % 4);
        hangingOffset = BlockPos.of(tag.getLong("hangingOffset"));
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }
}
