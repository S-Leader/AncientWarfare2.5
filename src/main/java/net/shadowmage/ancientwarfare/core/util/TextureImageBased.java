package net.shadowmage.ancientwarfare.core.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.awt.image.BufferedImage;

/**
 * Dynamic texture backed by an externally loaded image.
 */
public class TextureImageBased extends DynamicTexture {
    public TextureImageBased(ResourceLocation ignoredLocation, BufferedImage image) {
        super(toNativeImage(image));
    }

    public void reUploadImage() {
        upload();
    }

    private static NativeImage toNativeImage(BufferedImage image) {
        NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), true);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int a = argb >>> 24;
                int r = argb >> 16 & 0xFF;
                int g = argb >> 8 & 0xFF;
                int b = argb & 0xFF;
                nativeImage.setPixelRGBA(x, y, a << 24 | b << 16 | g << 8 | r);
            }
        }
        return nativeImage;
    }
}
