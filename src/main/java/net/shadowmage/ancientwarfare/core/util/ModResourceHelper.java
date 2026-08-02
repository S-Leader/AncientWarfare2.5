package net.shadowmage.ancientwarfare.core.util;

import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Resolves the directory or jar that contains a mod entry-point class without
 * relying on the removed 1.12 Loader.activeModContainer() API.
 */
public final class ModResourceHelper {
    private ModResourceHelper() {
    }

    /**
     * Resolves a resource through Forge's mod file abstraction. Unlike a class
     * CodeSource this works for both normal jars and ModLauncher's union filesystem.
     */
    @Nullable
    public static Path findModResource(String modId, String... pathSegments) {
        try {
            var modFileInfo = ModList.get().getModFileById(modId);
            if (modFileInfo != null) {
                Path resource = modFileInfo.getFile().findResource(pathSegments);
                if (Files.exists(resource)) {
                    return resource;
                }
            }
        } catch (RuntimeException ignored) {
            // Loading can be invoked very early by development tools. Callers retain
            // their legacy file-system fallback for that case.
        }
        return null;
    }

    public static File getSource(Class<?> anchor) {
        try {
            URL location = anchor.getProtectionDomain().getCodeSource().getLocation();
            if (location != null) {
                URI uri = location.toURI();
                return new File(uri);
            }
        } catch (URISyntaxException | SecurityException | IllegalArgumentException ignored) {
            // Fall through to the working directory. This keeps data loading optional
            // instead of crashing startup when a custom class loader hides CodeSource,
            // or when the dev-time union filesystem yields a non-file URI scheme.
        }
        return new File(".");
    }
}
