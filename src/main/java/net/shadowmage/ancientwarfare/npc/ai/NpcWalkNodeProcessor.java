package net.shadowmage.ancientwarfare.npc.ai;

import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * AW2's ground evaluator. Modern Minecraft's evaluator already contains the
 * fence-gate, diagonal, stepping and falling logic that this class copied in
 * 1.12, so only the NPC-specific door policy remains here.
 */
public class NpcWalkNodeProcessor extends WalkNodeEvaluator {

    public NpcWalkNodeProcessor() {
        setCanOpenDoors(true);
        setCanPassDoors(true);
    }
}
