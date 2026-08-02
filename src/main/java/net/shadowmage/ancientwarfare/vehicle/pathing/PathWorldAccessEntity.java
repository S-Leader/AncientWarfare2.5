/**
 * Copyright 2012 John Cummens (aka Shadowmage, Shadowmage4513)
 * This software is distributed under the terms of the GNU General Public License.
 * Please see COPYING for precise license information.
 * <p>
 * This file is part of Ancient Warfare.
 * <p>
 * Ancient Warfare is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Ancient Warfare is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Ancient Warfare.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.shadowmage.ancientwarfare.vehicle.pathing;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * a wrapper for a world-obj that will do additional validation of nodes to see if the entity can walk
 * on the node, and fit in the area (used more for vehicles than soldiers)
 *
 * @author Shadowmage
 */
public class PathWorldAccessEntity extends PathWorldAccess {
    private final Level entityLevel;
    private final Entity entity;

    /**
     * @param world
     */
    public PathWorldAccessEntity(Level world, Entity entity) {
        super(world);
        this.entityLevel = world;
        this.entity = entity;
    }

    @Override
    public boolean isWalkable(int x, int y, int z) {
        int radius = Math.max(0, Mth.ceil((entity.getBbWidth() - 1.0F) / 2.0F));
        int clearance = Math.max(2, Mth.ceil(entity.getBbHeight()));
        for (int dx = x - radius; dx <= x + radius; dx++) {
            for (int dz = z - radius; dz <= z + radius; dz++) {
                if (!super.isWalkable(dx, y, dz)) {
                    return false;
                }
                for (int dy = 2; dy < clearance; dy++) {
                    if (!super.checkBlockBounds(dx, y + dy, dz)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean isRemote() {
        return entityLevel.isClientSide;
    }
}
