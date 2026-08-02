package net.shadowmage.ancientwarfare.npc.ai.owned;

import net.minecraft.core.BlockPos;
import net.shadowmage.ancientwarfare.npc.ai.NpcAI;
import net.shadowmage.ancientwarfare.npc.entity.NpcPlayerOwned;
import net.shadowmage.ancientwarfare.npc.npc_command.NpcCommand.Command;
import net.shadowmage.ancientwarfare.npc.npc_command.NpcCommand.CommandMovement;

public class NpcAIPlayerOwnedFollowCommand extends NpcAI<NpcPlayerOwned> {

    private final CommandMovement movement = new CommandMovement() {
        @Override
        public void moveTo(BlockPos position, double squaredDistance) {
            moveToPosition(position, squaredDistance);
        }

        @Override
        public void moveTo(net.minecraft.world.entity.Entity entity, double squaredDistance) {
            moveToEntity(entity, squaredDistance);
        }
    };

    public NpcAIPlayerOwnedFollowCommand(NpcPlayerOwned npc) {
        super(npc);
        setMutexBits(ATTACK | MOVE);
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        Command cmd = npc.getCurrentCommand();
        return cmd != Command.NONE && (!cmd.type.prioritizesCombat() || npc.getTarget() == null);
    }

    @Override
    public void stop() {
        Command cmd = npc.getCurrentCommand();
        if (cmd != Command.NONE && (npc.getTarget() == null || !cmd.type.isPersistent())) {
            npc.handlePlayerCommand(Command.NONE);
        }
    }

    @Override
    public void start() {
        //noop
    }

    @Override
    public void tick() {
        Command cmd = npc.getCurrentCommand();
        cmd.type.tick(npc, cmd, movement);
    }
}
