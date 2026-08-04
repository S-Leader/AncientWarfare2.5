package net.shadowmage.ancientwarfare.automation.render;

import codechicken.lib.vec.uv.IconTransformation;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.automation.block.TorqueTier;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueBase;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;

import java.util.Map;

public abstract class TorqueTieredRenderer<T extends TileTorqueBase> extends BaseTorqueRenderer<T> {
    public Map<TorqueTier, TextureAtlasSprite> sprites = Maps.newHashMap();
    public Map<TorqueTier, IconTransformation> iconTransforms = Maps.newHashMap();

    protected TorqueTieredRenderer(String modelPath) {
        super(modelPath);
    }

    public void setSprite(TorqueTier torqueTier, TextureAtlasSprite sprite) {
        sprites.put(torqueTier, sprite);
        iconTransforms.put(torqueTier, new IconTransformation(sprite));
    }

    @Override
    protected IconTransformation getIconTransform(LegacyModelState state) {
        return iconTransforms.get(state.getValue(AutomationProperties.TIER));
    }

    @Override
    protected IconTransformation getIconTransform(ItemStack stack) {
        return iconTransforms.get(TorqueTier.fromItemStack(stack));
    }

    @Override
    protected TextureAtlasSprite getQuadSprite(LegacyModelState state) {
        return sprites.get(state.getValue(AutomationProperties.TIER));
    }

    @Override
    protected TextureAtlasSprite getQuadSprite(ItemStack stack) {
        return sprites.get(TorqueTier.fromItemStack(stack));
    }

    public TextureAtlasSprite getSprite(TorqueTier torqueTier) {
        return sprites.get(torqueTier);
    }
}
