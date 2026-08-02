package net.shadowmage.ancientwarfare.structure.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class LayeredCustomColorMaskTexture extends AbstractTexture {
    /**
     * Access to the Logger, for all your logging needs.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * The location of the texture.
     */
    private final ResourceLocation textureLocation;
    private final List<String> textures;
    private final List<Integer> colors;

    public LayeredCustomColorMaskTexture(ResourceLocation textureLocation, List<String> textures, List<Integer> colors) {
        this.textureLocation = textureLocation;
        this.textures = textures;
        this.colors = colors;
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException {
        releaseId();
        BufferedImage bufferedimage;
        try {
            Optional<Resource> resource = resourceManager.getResource(textureLocation);
            if (resource.isEmpty()) {
                throw new IOException("Missing resource " + textureLocation);
            }
            BufferedImage bufferedimage1;
            try (InputStream stream = resource.get().open()) {
                bufferedimage1 = ImageIO.read(stream);
            }
            int i = bufferedimage1.getType();

            if (i == 0) {
                i = 6;
            }

            bufferedimage = new BufferedImage(bufferedimage1.getWidth(), bufferedimage1.getHeight(), i);
            Graphics graphics = bufferedimage.getGraphics();
            graphics.drawImage(bufferedimage1, 0, 0, null);
            int j = 0;

            while (j < 17 && j < textures.size() && j < colors.size()) {
                String texture = textures.get(j);
                int color = colors.get(j);

                overlayTexture(resourceManager, bufferedimage, bufferedimage1, texture, color);

                ++j;
            }
            uploadTextureImage(getId(), bufferedimage);
        } catch (IOException ioexception) {
            LOGGER.error("Couldn't load layered image", (Throwable) ioexception);
        }
    }

    private void overlayTexture(ResourceManager resourceManager, BufferedImage bufferedimage, BufferedImage bufferedimage1, String textureName, int color)
            throws IOException {
        if (textureName == null) {
            return;
        }
        Optional<Resource> iresource1 = resourceManager.getResource(new ResourceLocation(textureName));
        if (iresource1.isEmpty()) {
            return;
        }
        BufferedImage bufferedimage2;
        try (InputStream stream = iresource1.get().open()) {
            bufferedimage2 = ImageIO.read(stream);
        }

        if (bufferedimage2.getWidth() == bufferedimage.getWidth() && bufferedimage2.getHeight() == bufferedimage.getHeight() && bufferedimage2.getType() == 6) {
            for (int l = 0; l < bufferedimage2.getHeight(); ++l) {
                for (int i1 = 0; i1 < bufferedimage2.getWidth(); ++i1) {
                    int j1 = bufferedimage2.getRGB(i1, l);

                    if ((j1 & -16777216) != 0) {
                        int k1 = (j1 & 16711680) << 8 & -16777216;
                        int l1 = bufferedimage1.getRGB(i1, l);
                        int i2 = multiplyColor(l1, color) & 16777215;
                        bufferedimage2.setRGB(i1, l, k1 | i2);
                    }
                }
            }

            bufferedimage.getGraphics().drawImage(bufferedimage2, 0, 0, null);
        }
    }

    /**
     * Reimplementation of the removed 1.12 MathHelper.multiplyColor helper.
     */
    private static int multiplyColor(int first, int second) {
        int i = (first & 16711680) >> 16;
        int j = (second & 16711680) >> 16;
        int k = (first & 65280) >> 8;
        int l = (second & 65280) >> 8;
        int i1 = first & 255;
        int j1 = second & 255;
        int k1 = (int) ((float) i * (float) j / 255.0F);
        int l1 = (int) ((float) k * (float) l / 255.0F);
        int i2 = (int) ((float) i1 * (float) j1 / 255.0F);
        return first & -16777216 | k1 << 16 | l1 << 8 | i2;
    }

    /**
     * Reimplementation of the removed 1.12 TextureUtil.uploadTextureImage using NativeImage.
     */
    private static void uploadTextureImage(int textureId, BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        try (NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, width, height, false)) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int argb = image.getRGB(x, y);
                    int abgr = (argb & 0xFF00FF00) | ((argb & 0x00FF0000) >> 16) | ((argb & 0x000000FF) << 16);
                    nativeImage.setPixelRGBA(x, y, abgr);
                }
            }
            TextureUtil.prepareImage(textureId, width, height);
            nativeImage.upload(0, 0, 0, false);
        }
    }
}
