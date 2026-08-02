package net.shadowmage.ancientwarfare.core.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TextureFileBased extends SimpleTexture {

    private File file;

    public TextureFileBased(ResourceLocation par1ResourceLocation, File file) {
        super(par1ResourceLocation);
        this.file = file;
    }

    @Override
    public void load(ResourceManager par1ResourceManager) {
        try (InputStream in = new FileInputStream(file)) {
            NativeImage image = NativeImage.read(in);
            if (!RenderSystem.isOnRenderThreadOrInit()) {
                RenderSystem.recordRenderCall(() -> uploadImage(image));
            } else {
                uploadImage(image);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadImage(NativeImage image) {
        TextureUtil.prepareImage(getId(), image.getWidth(), image.getHeight());
        image.upload(0, 0, 0, true);
    }

}
