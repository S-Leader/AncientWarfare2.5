package net.shadowmage.ancientwarfare.core.util;

import net.minecraft.resources.ResourceLocation;

/**
 * Transitional contract for blocks/items that keep a 1.12-style registry name
 * but do not extend the AW BlockBase hierarchy (e.g. vanilla-class subclasses).
 */
public interface ILegacyRegistryName {
    ResourceLocation getRegistryName();
}
