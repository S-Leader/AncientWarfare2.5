package net.shadowmage.ancientwarfare.core.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

@OnlyIn(Dist.CLIENT)
public class TextureUtils {
    private TextureUtils() {
    }

    public static ResourceLocation getTextureLocation(String path) {
        String overridePath = AWCoreStatics.configPathForFiles + path;
        ResourceLocation locationOverride = new ResourceLocation(AncientWarfareCore.MOD_ID, overridePath);
        if (textureLoaded(locationOverride)) {
            return locationOverride;
        }
        ResourceLocation locationMain = new ResourceLocation(AncientWarfareCore.MOD_ID, path);
        if (textureLoaded(locationMain)) {
            return locationMain;
        }

        if (loadTexture(locationOverride, overridePath)) {
            return locationOverride;
        }

        if (loadTextureFromAssets(locationMain, path)) {
            return locationMain;
        }

        return MissingTextureAtlasSprite.getLocation();
    }

    private static boolean textureLoaded(ResourceLocation loc) {
        //noinspection ConstantConditions - getTexture isn't marked as nullable but can return null
        return Minecraft.getInstance().getTextureManager().getTexture(loc) != null;
    }

    private static boolean loadTexture(ResourceLocation loc, String path) {
        File file = new File(path);
        return file.exists() && loadTexture(loc, file);
    }

    private static boolean loadTexture(ResourceLocation loc, File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            Minecraft.getInstance().getTextureManager().register(loc, new TextureImageBased(loc, image));

            return true;
        } catch (IOException e) {
            //noop
        }
        return false;
    }

    private static boolean loadTextureFromAssets(ResourceLocation loc, String path) {
        String[] relativeSegments = path.split("/");
        String[] resourceSegments = new String[relativeSegments.length + 2];
        resourceSegments[0] = "assets";
        resourceSegments[1] = AncientWarfareCore.MOD_ID;
        System.arraycopy(relativeSegments, 0, resourceSegments, 2, relativeSegments.length);
        Path bundledTexture = ModResourceHelper.findModResource(AncientWarfareCore.MOD_ID, resourceSegments);
        if (bundledTexture != null) {
            try (InputStream inputstream = Files.newInputStream(bundledTexture)) {
                Minecraft.getInstance().getTextureManager().register(loc, new TextureImageBased(loc, ImageIO.read(inputstream)));
                return true;
            } catch (IOException e) {
                // Fall through to the legacy CodeSource lookup.
            }
        }

        String fullPath = "assets/" + AncientWarfareCore.MOD_ID + "/" + path;
        File source = ModResourceHelper.getSource(AncientWarfareCore.class);
        if (source.isFile()) {
            try (FileSystem fs = FileSystems.newFileSystem(source.toPath(), (ClassLoader) null)) {
                InputStream inputstream = fs.provider().newInputStream(fs.getPath(fullPath));
                Minecraft.getInstance().getTextureManager().register(loc, new TextureImageBased(loc, ImageIO.read(inputstream)));
                return true;
            } catch (IOException e) {
                //noop
            }
        } else if (source.isDirectory()) {
            File file = source.toPath().resolve(fullPath).toFile();
            if (loadTexture(loc, file)) {
                return true;
            }
        }
        return false;
    }
}
