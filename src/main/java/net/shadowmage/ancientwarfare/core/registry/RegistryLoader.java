package net.shadowmage.ancientwarfare.core.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.util.JsonUtils;
import net.shadowmage.ancientwarfare.core.util.ModResourceHelper;
import net.shadowmage.ancientwarfare.core.util.parsing.JsonHelper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class RegistryLoader {
    private RegistryLoader() {
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, IRegistryDataParser> parsers = new HashMap<>();

    public static void registerParser(IRegistryDataParser parser) {
        parsers.put(parser.getName(), parser);
    }

    private static final Map<ResourceLocation, String> loadedRegistries = new HashMap<>();

    private static final List<DependentFile> loadLater = new ArrayList<>();

    public static void load() {
        load(p -> true);
    }

    public static void reload(String type) {
        loadedRegistries.entrySet().removeIf(entry -> entry.getValue().equals(type));
        load(p -> p.getName().equals(type));
    }

    public static void load(Predicate<IRegistryDataParser> include) {
        String modId = AncientWarfareCore.MOD_ID;
        Path registryOverridesFolder = new File(AWCoreStatics.configPathForFiles + "registry").toPath();
        if (registryOverridesFolder.toFile().exists()) {
            loadRegistries(modId, registryOverridesFolder, include);
        }
        Path bundledRegistries = ModResourceHelper.findModResource(modId, "assets", modId, "registry");
        if (bundledRegistries != null) {
            loadRegistries(modId, bundledRegistries, include);
        } else {
            File source = ModResourceHelper.getSource(AncientWarfareCore.class);
            loadRegistries(modId, source, "assets/" + modId + "/registry", include);
        }
    }

    private static void loadRegistries(String modId, File source, String base, Predicate<IRegistryDataParser> include) {
        if (!(source.isDirectory() || source.isFile())) {
            return;
        }

        FileSystem fs = null;
        try {
            Path root;
            if (source.isFile()) {
                fs = FileSystems.newFileSystem(source.toPath(), (ClassLoader) null);
                root = fs.getPath("/" + base);
            } else {
                root = source.toPath().resolve(base);
            }

            loadRegistries(modId, root, include);
        } catch (IOException e) {
            AncientWarfareCore.LOG.error("Error loading FileSystem from jar: ", e);
        } finally {
            IOUtils.closeQuietly(fs);
        }
    }

    @SuppressWarnings("squid:S3725") //ZipPath doesn't have toFile support
    private static void loadRegistries(String modId, Path root, Predicate<IRegistryDataParser> include) {
        if (!Files.exists(root)) {
            return;
        }

        Iterator<Path> itr;
        try {
            itr = Files.walk(root).iterator();
        } catch (IOException e) {
            AncientWarfareCore.LOG.error("Error iterating filesystem for: {}", root, e);
            return;
        }
        while (itr.hasNext()) {
            loadFile(modId, root, itr.next(), include);
        }

        loadDependents(modId, include);
    }

    private static void loadDependents(String modId, Predicate<IRegistryDataParser> include) {
        int lastCountLoadLater = loadLater.size();
        while (!loadLater.isEmpty()) {
            Iterator<DependentFile> iterator = loadLater.iterator();
            while (iterator.hasNext()) {
                DependentFile dependentFile = iterator.next();
                if (areDependenciesLoaded(dependentFile.getDependencies())) {
                    loadFile(modId, dependentFile.getPath(), include, false, dependentFile.getName());
                    iterator.remove();
                }
            }
            if (lastCountLoadLater <= loadLater.size()) {
                logIncorrectDependencies();
                break;
            }
            lastCountLoadLater = loadLater.size();
        }
    }

    private static void logIncorrectDependencies() {
        for (DependentFile dependentFile : loadLater) {
            AncientWarfareCore.LOG.error("Non existent or circular load after dependencies in {} - {}", dependentFile.getPath().toString(), String.join(",", dependentFile.getDependencies()));
        }
    }

    private static void loadFile(String modId, Path root, Path file, Predicate<IRegistryDataParser> include) {

        if (!"json".equals(FilenameUtils.getExtension(file.toString()))) {
            return;
        }

        String relative = root.relativize(file).toString();
        String name = FilenameUtils.removeExtension(relative).replaceAll("\\\\", "/");

        loadFile(modId, file, include, true, name);
    }

    private static void loadFile(String modId, Path file, Predicate<IRegistryDataParser> include, boolean checkDependencies, String name) {
        String shortName = name.substring(name.lastIndexOf('/') + 1);

        ResourceLocation registryName = new ResourceLocation(modId, name);

        BufferedReader reader = null;
        try {
            reader = Files.newBufferedReader(file);
            JsonObject json = JsonUtils.fromJson(GSON, reader, JsonObject.class);

            if (json == null) {
                return;
            }


            Optional<IRegistryDataParser> parser = getParser(shortName, json);

            if (!parser.isPresent()) {
                AncientWarfareCore.LOG.error("No parser defined for file name {}", shortName);
                return;
            }

            if (checkDependencies && json.has("load_after")) {
                Set<String> dependencies = JsonHelper.setFromJson(json.get("load_after"), e -> JsonUtils.getString(e, ""));
                if (!areDependenciesLoaded(dependencies)) {
                    loadLater.add(new DependentFile(name, file, dependencies));
                    return;
                }
            }

            if (loadedRegistries.containsKey(registryName)) {
                AncientWarfareCore.LOG.info("Registry {} has already been loaded in overrides, skipping...", registryName.toString());
                return;
            }
            loadedRegistries.put(registryName, parser.get().getName());

            if (!include.test(parser.get()) || isDisabled(json) || !isModLoaded(json)) {
                return;
            }

            parser.get().parse(json);
        } catch (JsonParseException e) {
            AncientWarfareCore.LOG.error("Parsing error loading registry {}", registryName, e);
        } catch (MissingResourceException e) {
            AncientWarfareCore.LOG.error(e.getMessage());
        } catch (IOException e) {
            AncientWarfareCore.LOG.error("Couldn't read registry {} from {}", registryName, file, e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    private static boolean areDependenciesLoaded(Set<String> dependencies) {
        for (String dependency : dependencies) {
            if (!loadedRegistries.containsValue(dependency)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isModLoaded(JsonObject json) {
        return !JsonUtils.hasField(json, "mod") || ModList.get().isLoaded(JsonUtils.getString(json, "mod"));
    }

    private static boolean isDisabled(JsonObject json) {
        return json.has("disabled") && JsonUtils.getBoolean(json, "disabled");
    }

    private static Optional<IRegistryDataParser> getParser(String fileName, JsonObject json) {
        String parserName = fileName;
        if (json.has("type")) {
            parserName = JsonUtils.getString(json, "type");
        }
        return parsers.containsKey(parserName) ? Optional.of(parsers.get(parserName)) : Optional.empty();
    }

    private static class DependentFile {
        private final String name;
        private final Path path;
        private final Set<String> dependencies;

        private DependentFile(String name, Path path, Set<String> dependencies) {
            this.name = name;
            this.path = path;
            this.dependencies = dependencies;
        }

        public Set<String> getDependencies() {
            return dependencies;
        }

        public Path getPath() {
            return path;
        }

        public String getName() {
            return name;
        }
    }
}
