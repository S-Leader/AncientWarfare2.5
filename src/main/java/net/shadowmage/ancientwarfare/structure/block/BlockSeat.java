package net.shadowmage.ancientwarfare.structure.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.entity.AWEntityRegistry;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.entity.EntitySeat;
import net.shadowmage.ancientwarfare.structure.util.RotationLimit;

import java.util.List;

public abstract class BlockSeat extends BlockBaseStructure {
    private static final Vec3 DEFAULT_SEAT_OFFSET = new Vec3(0.5, 0.5, 0.5);

    public BlockSeat(LegacyMaterial material, String regName) {
        super(material, regName);
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player playerIn, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (!world.isClientSide && !isOccupied(world, pos)) {
            EntitySeat seatEntity = AWEntityRegistry.createEntity(
                    new ResourceLocation(AncientWarfareStructure.MOD_ID, AWEntityRegistry.SEAT),
                    world, EntitySeat.class);
            if (seatEntity != null) {
                seatEntity.configure(Vec3.atLowerCornerOf(pos).add(getSeatOffset()), pos);
                world.addFreshEntity(seatEntity);
                playerIn.startRiding(seatEntity);
            }
        }
        return true;
    }

    protected Vec3 getSeatOffset() {
        return DEFAULT_SEAT_OFFSET;
    }

    private boolean isOccupied(Level world, BlockPos pos) {
        List<EntitySeat> seats = world.getEntitiesOfClass(EntitySeat.class, new AABB(pos, pos.offset(1, 1, 1)).inflate(1));
        for (EntitySeat seat : seats) {
            if (seat.getSeatPos().equals(pos)) {
                return seat.isVehicle();
            }
        }
        return false;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(BlockState state) {
        return false;
    }

    public abstract RotationLimit getRotationLimit(Level world, BlockPos seatPos, BlockState state);
}
