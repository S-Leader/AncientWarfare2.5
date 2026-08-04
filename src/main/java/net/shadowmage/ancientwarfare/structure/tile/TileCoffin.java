package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.interfaces.ITickable;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.EntityTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.block.BlockCoffin;
import net.shadowmage.ancientwarfare.structure.util.LootHelper;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class TileCoffin extends TileMulti implements ITickable, ISpecialLootContainer {
    protected BlockCoffin.CoffinDirection direction = BlockCoffin.CoffinDirection.NORTH;
    private boolean opening = false;
    private boolean open = false;
    private float prevLidAngle = 0;
    private float lidAngle = 0;
    private int openTime = 0;
    private LootSettings lootSettings = new LootSettings();
    private static final float OPEN_ANGLE = 15F;

    public abstract BlockCoffin.IVariant getVariant();

    @Override
    public void setPlacementDirection(Level world, BlockPos pos, BlockState state, Direction horizontalFacing, float rotationYaw) {
        setDirection(BlockCoffin.CoffinDirection.fromFacing(horizontalFacing));
    }

    @Override
    protected void readNBT(CompoundTag compound) {
        super.readNBT(compound);
        direction = BlockCoffin.CoffinDirection.fromName(compound.getString("direction"));
        opening = compound.getBoolean("opening");
        open = compound.getBoolean("open");
        if (open) {
            lidAngle = prevLidAngle = OPEN_ANGLE;
        }
        lootSettings = LootSettings.deserializeNBT(compound.getCompound("lootSettings"));
    }

    @Override
    protected void writeNBT(CompoundTag compound) {
        super.writeNBT(compound);
        compound.putString("direction", direction.getName());
        compound.putBoolean("opening", opening);
        compound.putBoolean("open", open);
        compound.put("lootSettings", lootSettings.serializeNBT());
    }

    public void setDirection(BlockCoffin.CoffinDirection direction) {
        BlockCoffin.CoffinDirection safeDirection = direction == null
                ? BlockCoffin.CoffinDirection.NORTH
                : direction;
        if (this.direction != safeDirection) {
            this.direction = safeDirection;
            setChanged();
        }
    }

    public BlockCoffin.CoffinDirection getDirection() {
        return direction;
    }

    public void open() {
        Optional<BlockPos> mainPos = getMainBlockPos();
        if (!mainPos.isPresent() || mainPos.get().equals(pos)) {
            if (!open && !opening) {
                playSound();
                opening = true;
                setChanged();
                BlockTools.notifyBlockUpdate(this);
            }
            return;
        }
        WorldTools.getTile(world, mainPos.get(), TileCoffin.class).ifPresent(TileCoffin::open);
    }

    protected abstract void playSound();

    private void dropLoot(@Nullable Player player) {
        if (world.isClientSide || isOpen()) {
            return;
        }
        Optional<BlockPos> mainPos = getMainBlockPos();
        if (!mainPos.isPresent() || mainPos.get().equals(pos)) {
            LootHelper.dropLoot(this, player);
            return;
        }
        WorldTools.getTile(world, mainPos.get(), TileCoffin.class).ifPresent(te -> te.dropLoot(player));
    }

    private boolean isOpen() {
        return getValueFromMain(TileCoffin.class, TileCoffin::isOpen, open, () -> true);
    }

    @Override
    public void onBlockBroken(BlockState state) {
        dropLoot(EntityTools.findClosestPlayer(world, pos, 100));
        super.onBlockBroken(state);
    }

    @Override
    public void update() {
        if (opening && !open) {
            prevLidAngle = lidAngle;
            openTime++;

            float halfAngle = OPEN_ANGLE / 2;
            float halfTime = (float) getTotalOpenTime() / 2;
            if (openTime > halfTime) {
                float ratio = (getTotalOpenTime() - openTime) / halfTime;
                lidAngle = OPEN_ANGLE - (halfAngle * ratio * ratio);
            } else {
                float ratio = openTime / halfTime;
                lidAngle = halfAngle * ratio * ratio;
            }
            if (lidAngle >= OPEN_ANGLE) {
                dropLoot(EntityTools.findClosestPlayer(world, pos, 100));
                prevLidAngle = lidAngle;
                open = true;
                opening = false;
                setChanged();
                if (!world.isClientSide) {
                    BlockTools.notifyBlockUpdate(this);
                }
            }
        }
    }

    protected abstract int getTotalOpenTime();

    public float getPrevLidAngle() {
        return prevLidAngle;
    }

    public float getLidAngle() {
        return lidAngle;
    }

    @Override
    public void setLootSettings(LootSettings settings) {
        this.lootSettings = settings;
    }

    @Override
    public LootSettings getLootSettings() {
        return getValueFromMain(TileCoffin.class, TileCoffin::getLootSettings, lootSettings, LootSettings::new);
    }
}
