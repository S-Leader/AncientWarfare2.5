// The entity-based gate implementation still requires three-part collision geometry.
 /*
 Copyright 2015 Olivier Sylvain (aka GotoLink)
 This software is distributed under the terms of the GNU General Public License.
 Please see COPYING for precise license information.

 This file is part of Ancient Warfare.

 Ancient Warfare is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Ancient Warfare is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Ancient Warfare.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.shadowmage.ancientwarfare.structure.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class DualBoundingBox extends AABB {

    private double yOffset;

    public DualBoundingBox(BlockPos min, BlockPos max) {
        super(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
    }

    public AABB getMin() {
        return new AABB(minX, minY - yOffset, minZ, minX + 1, maxY, minZ + 1);
    }

    public AABB getMax() {
        return new AABB(maxX - 1, minY - yOffset, maxZ - 1, maxX, maxY, maxZ);
    }

    public AABB getTop() {
        return new AABB(minX, maxY - 1, minZ, maxX, maxY, maxZ);
    }

    private DualBoundingBox(double mX, double mY, double mZ, double MX, double MY, double MZ) {
        super(mX, mY, mZ, MX, MY, MZ);
    }

    public DualBoundingBox setBB(AABB bb) {
        DualBoundingBox box = new DualBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
        box.yOffset = this.yOffset;
        return box;
    }

    @Override
    public AABB contract(double varX, double varY, double varZ) {
        return setBB(super.contract(varX, varY, varZ));
    }

    @Override
    public AABB move(double xOff, double yOff, double zOff) {
        return setBB(super.move(xOff, yOff, zOff));
    }

    @Override
    public AABB minmax(AABB other) {
        return setBB(super.minmax(other));
    }

    @Override
    public AABB inflate(double varX, double varY, double varZ) {
        return setBB(super.inflate(varX, varY, varZ));
    }

    @Override
    public AABB expandTowards(double varX, double varY, double varZ) {
        return setBB(super.expandTowards(varX, varY, varZ));
    }

    @Override
    public boolean intersects(double x1, double y1, double z1, double x2, double y2, double z2) {
        return intersects(getMin(), x1, y1, z1, x2, y2, z2) || intersects(getMax(), x1, y1, z1, x2, y2, z2) || intersects(getTop(), x1, y1, z1, x2, y2, z2);
    }

    private boolean intersects(AABB aabb, double x1, double y1, double z1, double x2, double y2, double z2) {
        return aabb.minX < x2 && aabb.maxX > x1 && aabb.minY < y2 && aabb.maxY > y1 && aabb.minZ < z2 && aabb.maxZ > z1;
    }

    @Override
    public boolean contains(Vec3 vec3) {
        return getMin().contains(vec3) || getMax().contains(vec3) || getTop().contains(vec3);
    }

    @Override
    public Optional<Vec3> clip(Vec3 vec3_1, Vec3 vec3_2) {
        return new AABB(minX, minY - yOffset, minZ, maxX, maxY, maxZ).clip(vec3_1, vec3_2);
    }
}
