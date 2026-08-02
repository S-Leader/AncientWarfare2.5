package net.shadowmage.ancientwarfare.core.container;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

/*
 * Created by Olivier on 05/02/2015.
 */
public class ContainerTileBase<T extends BlockEntity> extends ContainerBase {
    public final T tileEntity;
    private final BlockPos tilePos;

    @SuppressWarnings("unchecked")
    public ContainerTileBase(Player player, int x, int y, int z) {
        super(player);

        this.tilePos = new BlockPos(x, y, z);

        BlockEntity blockEntity = player.level().getBlockEntity(this.tilePos);
        if (blockEntity == null) {
            throw new IllegalArgumentException("No block entity found at " + this.tilePos);
        }

        this.tileEntity = (T) blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        Level level = this.tileEntity.getLevel();

        if (level == null || this.tileEntity.isRemoved()) {
            return false;
        }

        if (level.getBlockEntity(this.tilePos) != this.tileEntity) {
            return false;
        }

        return player.distanceToSqr(this.tilePos.getX() + 0.5D, this.tilePos.getY() + 0.5D, this.tilePos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public final boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof ContainerTileBase<?> other)) {
            return false;
        }

        return Objects.equals(this.tileEntity, other.tileEntity);
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(this.tileEntity);
    }
}