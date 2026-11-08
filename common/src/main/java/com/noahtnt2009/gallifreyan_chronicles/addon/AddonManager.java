package com.noahtnt2009.gallifreyan_chronicles.addon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.addon.api.AddonApi;
import com.noahtnt2009.gallifreyan_chronicles.addon.api.AddonContext;
import com.noahtnt2009.gallifreyan_chronicles.addon.api.GCAddon;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisEvents;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControlRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControlTypes;
import com.noahtnt2009.gallifreyan_chronicles.util.GCScheduler;
import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class AddonManager {
    public static final String FOLDER_NAME = "gc_addons";
    public static final String DESCRIPTOR = "addon.json";
    private static final String STATE_FILE = Constants.MOD_ID + "_addons.json";
    private static final int MAX_ICON_BYTES = 512 * 1024;
    private static final int MAX_LISTENER_FAILURES = 5;
    private static final Pattern VALID_ID = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern CONTROL_ID = Pattern.compile("[a-z0-9_]+");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static volatile List<AddonInfo> addons = List.of();
    private static volatile Map<String, AddonProblem> problems = Map.of();
    private static final Set<String> DISABLED = Collections.synchronizedSet(new LinkedHashSet<>());
    private static final Map<String, String> APPROVED = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final Map<String, String> FAILURES = new HashMap<>();
    private static final Map<String, LoadedJava> LOADED = new LinkedHashMap<>();
    private static boolean scanned;
    private static boolean initialised;

    private AddonManager() {
    }

    private record LoadedJava(GCAddon addon, URLClassLoader loader) {
    }

    public static Path folder() {
        return Services.PLATFORM.getConfigDir().resolveSibling(FOLDER_NAME);
    }

    private static Path stateFile() {
        return Services.PLATFORM.getConfigDir().resolve(STATE_FILE);
    }

    private static synchronized void ensureScanned() {
        if (scanned) return;
        scanned = true;
        loadState();
        scan();
    }

    public static synchronized void init() {
        if (initialised) return;
        initialised = true;
        ensureScanned();
        loadJava();
        logReport();
        Runtime.getRuntime().addShutdownHook(new Thread(AddonManager::shutdown, "GC addon shutdown"));
    }

    public static List<AddonInfo> all() {
        ensureScanned();
        return addons;
    }

    public static String modVersion() {
        return Services.PLATFORM.getModVersion(Constants.MOD_ID);
    }

    public static boolean isEnabled(String id) {
        ensureScanned();
        return !DISABLED.contains(id);
    }

    public static @Nullable AddonProblem problem(AddonInfo info) {
        ensureScanned();
        return problems.get(info.id());
    }

    public static boolean isActive(AddonInfo info) {
        return isEnabled(info.id()) && problem(info) == null;
    }

    public static List<AddonInfo> enabledData() {
        ensureScanned();
        List<AddonInfo> result = new ArrayList<>();
        for (AddonInfo info : addons) {
            if (info.hasData() && isActive(info)) result.add(info);
        }
        return result;
    }

    public static synchronized boolean restartRequired() {
        for (AddonInfo info : addons) {
            if (info.hasJava() && isActive(info) != LOADED.containsKey(info.id())
                    && !FAILURES.containsKey(info.id())) {
                return true;
            }
        }
        for (String id : LOADED.keySet()) {
            if (addons.stream().noneMatch(info -> info.id().equals(id))) return true;
        }
        return false;
    }

    public static synchronized boolean isRunning(String id) {
        return LOADED.containsKey(id);
    }

    public static void setEnabled(String id, boolean enabled) {
        if (enabled) DISABLED.remove(id);
        else DISABLED.add(id);
        saveState();
        refreshProblems();
    }

    public static void approve(AddonInfo info) {
        if (info.sha256() != null) APPROVED.put(info.id(), info.sha256());
        DISABLED.remove(info.id());
        saveState();
        refreshProblems();
    }

    private static boolean isApproved(AddonInfo info) {
        return !info.hasJava() || (info.sha256() != null && info.sha256().equals(APPROVED.get(info.id())));
    }

    private static void loadState() {
        DISABLED.clear();
        APPROVED.clear();
        Path file = stateFile();
        if (!Files.exists(file)) return;
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (root.has("disabled")) {
                for (JsonElement element : root.getAsJsonArray("disabled")) DISABLED.add(element.getAsString());
            }
            if (root.has("approved")) {
                for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("approved").entrySet()) {
                    APPROVED.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
        } catch (IOException | RuntimeException e) {
            Constants.LOG.warn("Could not read {}, every addon will start enabled and Java addons need approving again", file, e);
        }
    }

    private static void saveState() {
        JsonObject root = new JsonObject();
        JsonArray disabled = new JsonArray();
        synchronized (DISABLED) {
            DISABLED.forEach(disabled::add);
        }
        root.add("disabled", disabled);
        JsonObject approved = new JsonObject();
        synchronized (APPROVED) {
            APPROVED.forEach(approved::addProperty);
        }
        root.add("approved", approved);
        try {
            Files.createDirectories(stateFile().getParent());
            Files.writeString(stateFile(), GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Constants.LOG.warn("Could not save {}", stateFile(), e);
        }
    }

    private static synchronized void refreshProblems() {
        Map<String, AddonProblem> found = new HashMap<>();
        for (AddonInfo info : addons) {
            AddonProblem problem = check(info);
            if (problem != null) found.put(info.id(), problem);
        }
        problems = Map.copyOf(found);
    }

    private static @Nullable AddonProblem check(AddonInfo info) {
        if (info.isInvalid()) return new AddonProblem(AddonProblem.Kind.INVALID, info.error());
        if (info.apiVersion() > AddonApi.VERSION) {
            return new AddonProblem(AddonProblem.Kind.INCOMPATIBLE_API, info.apiVersion() + "|" + AddonApi.VERSION);
        }

        for (Map.Entry<String, String> requirement : info.requires().entrySet()) {
            String target = requirement.getKey();
            VersionRange range;
            try {
                range = VersionRange.parse(requirement.getValue());
            } catch (IllegalArgumentException e) {
                return new AddonProblem(AddonProblem.Kind.INVALID, "requires." + target + ": " + e.getMessage());
            }

            String found;
            if (target.equals(Constants.MOD_ID)) {
                found = modVersion();
                if (!matches(range, found)) {
                    return new AddonProblem(AddonProblem.Kind.INCOMPATIBLE_MOD, range + "|" + found);
                }
                continue;
            }
            if (Services.PLATFORM.isModLoaded(target)) {
                found = Services.PLATFORM.getModVersion(target);
            } else {
                AddonInfo other = addons.stream().filter(candidate -> candidate.id().equals(target)).findFirst().orElse(null);
                if (other == null || !isEnabled(other.id())) {
                    return new AddonProblem(AddonProblem.Kind.MISSING_REQUIREMENT, target + "|" + range);
                }
                found = other.version();
            }
            if (!matches(range, found)) {
                return new AddonProblem(AddonProblem.Kind.WRONG_REQUIREMENT_VERSION, target + "|" + range + "|" + found);
            }
        }

        if (!isApproved(info)) return new AddonProblem(AddonProblem.Kind.NEEDS_APPROVAL, "");
        String failure = FAILURES.get(info.id());
        if (failure != null) return new AddonProblem(AddonProblem.Kind.FAILED_TO_START, failure);
        return null;
    }

    private static boolean matches(VersionRange range, String version) {
        try {
            return range.matches(Version.parse(version));
        } catch (IllegalArgumentException e) {
            return range.toString().equals("*");
        }
    }

    public static synchronized void scan() {
        Path folder = folder();
        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            Constants.LOG.warn("Could not create the addons folder {}", folder, e);
            addons = List.of();
            refreshProblems();
            return;
        }

        List<Path> entries;
        try (Stream<Path> stream = Files.list(folder)) {
            entries = stream.sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase())).toList();
        } catch (IOException e) {
            Constants.LOG.warn("Could not list the addons folder {}", folder, e);
            addons = List.of();
            refreshProblems();
            return;
        }

        List<AddonInfo> found = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (Path entry : entries) {
            String fileName = entry.getFileName().toString();
            if (fileName.startsWith(".")) continue;
            boolean folderAddon = Files.isDirectory(entry);
            String lower = fileName.toLowerCase();
            if (!folderAddon && !lower.endsWith(".zip") && !lower.endsWith(".jar")) continue;

            AddonInfo info;
            try {
                info = describe(entry);
            } catch (IOException | RuntimeException e) {
                // show it in the list with the reason, instead of silently hiding it
                Constants.LOG.warn("Addon {} could not be read: {}", fileName, e.getMessage());
                info = broken(entry, String.valueOf(e.getMessage()));
            }
            if (info == null) continue;
            if (!ids.add(info.id())) {
                Constants.LOG.warn("Skipping {}: another addon already uses the id '{}'", fileName, info.id());
                continue;
            }
            found.add(info);
        }
        addons = List.copyOf(found);
        refreshProblems();
        Constants.LOG.info("Found {} addon(s) in {}", found.size(), folder);
    }

    private static AddonInfo broken(Path path, String error) {
        String fileName = path.getFileName().toString();
        String base = fileName.contains(".") && !Files.isDirectory(path) ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
        return new AddonInfo(base.toLowerCase().replaceAll("[^a-z0-9_.-]+", "_"), base, "", "", List.of(), null, path,
                false, null, null, 1, Map.of(), null, error);
    }

    private static @Nullable AddonInfo describe(Path path) throws IOException {
        byte[] descriptor = read(path, DESCRIPTOR);
        boolean hasData = read(path, "pack.mcmeta") != null;
        String fileName = path.getFileName().toString();
        String baseName = fileName.contains(".") && !Files.isDirectory(path)
                ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;

        if (descriptor == null && !hasData) {
            if (fileName.toLowerCase().endsWith(".zip") || Files.isDirectory(path)) {
                return broken(path, "no addon.json or pack.mcmeta found");
            }
            return null;
        }

        JsonObject json;
        try {
            json = descriptor != null
                    ? JsonParser.parseString(new String(descriptor, StandardCharsets.UTF_8)).getAsJsonObject()
                    : new JsonObject();
        } catch (RuntimeException e) {
            throw new IOException("addon.json is not valid JSON: " + e.getMessage(), e);
        }

        String id = string(json, "id", baseName.toLowerCase().replaceAll("[^a-z0-9_.-]+", "_"));
        if (!VALID_ID.matcher(id).matches()) {
            throw new IOException("id '" + id + "' may only use a-z, 0-9, _ . and -");
        }
        String name = string(json, "name", baseName);
        String version = string(json, "version", "");
        String description = string(json, "description", "");
        String website = json.has("website") ? string(json, "website", "") : null;

        List<String> authors = new ArrayList<>();
        if (json.has("authors") && json.get("authors").isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray("authors")) authors.add(element.getAsString());
        } else if (json.has("authors")) {
            authors.add(json.get("authors").getAsString());
        } else if (json.has("author")) {
            authors.add(json.get("author").getAsString());
        }

        int api = 1;
        if (json.has("api")) {
            try {
                api = json.get("api").getAsInt();
            } catch (RuntimeException e) {
                throw new IOException("\"api\" must be a whole number");
            }
        }

        Map<String, String> requires = new LinkedHashMap<>();
        if (json.has("requires")) {
            if (!json.get("requires").isJsonObject())
                throw new IOException("\"requires\" must be an object like {\"gallifreyan_chronicles\": \">=1.0.0\"}");
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("requires").entrySet()) {
                requires.put(entry.getKey(), entry.getValue().getAsString());
            }
        }

        String javaMain = null;
        String sha256 = null;
        if (json.has("java") && json.get("java").isJsonObject() && json.getAsJsonObject("java").has("main")) {
            javaMain = json.getAsJsonObject("java").get("main").getAsString();
            if (!fileName.toLowerCase().endsWith(".jar")) {
                throw new IOException("a Java addon has to be a .jar file");
            }
            sha256 = sha256(path);
        }

        byte[] icon = read(path, string(json, "icon", "icon.png"));
        if (icon != null && icon.length > MAX_ICON_BYTES) {
            Constants.LOG.warn("Addon '{}' has an icon over {} KB, ignoring it", id, MAX_ICON_BYTES / 1024);
            icon = null;
        }

        return new AddonInfo(id, name, version, description, List.copyOf(authors), website, path, hasData, javaMain,
                icon, api, Map.copyOf(requires), sha256, null);
    }

    private static String sha256(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) > 0) digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 is not available", e);
        }
    }

    private static String string(JsonObject json, String key, String fallback) {
        return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsString() : fallback;
    }

    private static byte @Nullable [] read(Path container, String entry) throws IOException {
        if (Files.isDirectory(container)) {
            Path file = container.resolve(entry).normalize();
            if (!file.startsWith(container) || !Files.isRegularFile(file)) return null;
            return Files.readAllBytes(file);
        }
        try (ZipFile zip = new ZipFile(container.toFile())) {
            ZipEntry zipEntry = zip.getEntry(entry);
            if (zipEntry == null || zipEntry.isDirectory()) return null;
            try (InputStream in = zip.getInputStream(zipEntry)) {
                return in.readNBytes(MAX_ICON_BYTES * 8);
            }
        }
    }

    public static long folderSignature() {
        long signature = 17;
        try (Stream<Path> stream = Files.list(folder())) {
            for (Path path : (Iterable<Path>) stream::iterator) {
                String name = path.getFileName().toString();
                if (name.startsWith(".")) continue;
                signature += name.hashCode() * 31L + Files.size(path) * 7L + Files.getLastModifiedTime(path).toMillis();
            }
        } catch (IOException | RuntimeException ignored) {
            // an unreadable or missing folder just gives a different fingerprint
        }
        return signature;
    }

    public static synchronized List<String> install(List<Path> dropped) {
        List<String> added = new ArrayList<>();
        Path folder = folder();
        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            Constants.LOG.warn("Could not create the addons folder {}", folder, e);
            return added;
        }
        for (Path source : dropped) {
            String name = source.getFileName().toString();
            String lower = name.toLowerCase();
            boolean directory = Files.isDirectory(source);
            if (!directory && !lower.endsWith(".zip") && !lower.endsWith(".jar")) continue;
            Path target = folder.resolve(name);
            if (Files.exists(target)) {
                Constants.LOG.warn("An addon called {} is already installed, not overwriting it", name);
                continue;
            }
            try {
                if (directory) copyTree(source, target);
                else Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
                added.add(name);
            } catch (IOException e) {
                Constants.LOG.warn("Could not install addon {}", name, e);
            }
        }
        if (!added.isEmpty()) scan();
        return added;
    }

    private static void copyTree(Path source, Path target) throws IOException {
        try (Stream<Path> walk = Files.walk(source)) {
            for (Path path : (Iterable<Path>) walk::iterator) {
                Path destination = target.resolve(source.relativize(path).toString());
                if (Files.isDirectory(path)) Files.createDirectories(destination);
                else Files.copy(path, destination);
            }
        }
    }

    // Java addons
    private static synchronized void loadJava() {
        for (AddonInfo info : addons) {
            if (!info.hasJava() || !isActive(info)) continue;
            URLClassLoader loader = null;
            try {
                URL url = info.path().toUri().toURL();
                loader = new URLClassLoader(new URL[]{url}, AddonManager.class.getClassLoader());
                Class<?> type = Class.forName(info.javaMain(), true, loader);
                if (!GCAddon.class.isAssignableFrom(type)) {
                    throw new IllegalStateException(info.javaMain() + " does not implement GCAddon");
                }
                GCAddon addon = (GCAddon) type.getDeclaredConstructor().newInstance();
                addon.onInitialize(new Context(info));
                LOADED.put(info.id(), new LoadedJava(addon, loader));
                Constants.LOG.info("Started Java addon '{}' {}", info.id(), info.version());
            } catch (Throwable t) {
                if (t instanceof VirtualMachineError error) throw error;
                Throwable cause = t instanceof java.lang.reflect.InvocationTargetException && t.getCause() != null ? t.getCause() : t;
                Constants.LOG.error("Java addon '{}' failed to start and was skipped", info.id(), cause);
                FAILURES.put(info.id(), cause.getClass().getSimpleName() + ": " + cause.getMessage());
                if (loader != null) {
                    try {
                        loader.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
        refreshProblems();
    }

    private static synchronized void shutdown() {
        for (Map.Entry<String, LoadedJava> entry : LOADED.entrySet()) {
            try {
                entry.getValue().addon().onShutdown();
            } catch (Throwable t) {
                Constants.LOG.warn("Java addon '{}' failed during shutdown", entry.getKey(), t);
            }
        }
    }

    private static void logReport() {
        for (AddonInfo info : addons) {
            AddonProblem problem = problem(info);
            String state = problem != null ? "NOT LOADED (" + problem.kind() + (problem.detail().isEmpty() ? "" : ": " + problem.detail()) + ")"
                    : isEnabled(info.id()) ? "enabled" : "disabled";
            Constants.LOG.info("Addon '{}' {} [{}{}]: {}", info.id(), info.version(), info.hasData() ? "data" : "",
                    info.hasJava() ? (info.hasData() ? "+java" : "java") : "", state);
        }
    }

    private static final class Context implements AddonContext {
        private final AddonInfo info;
        private final Logger logger;
        private final String controlPrefix;

        private Context(AddonInfo info) {
            this.info = info;
            this.logger = LoggerFactory.getLogger("GC Addon/" + info.id());
            this.controlPrefix = info.id().replace('.', '_').replace('-', '_') + "_";
        }

        @Override
        public String addonId() {
            return info.id();
        }

        @Override
        public Logger logger() {
            return logger;
        }

        @Override
        public String modVersion() {
            return AddonManager.modVersion();
        }

        @Override
        public boolean isModLoaded(String modId) {
            return Services.PLATFORM.isModLoaded(modId);
        }

        @Override
        public Path dataFolder() {
            Path path = Services.PLATFORM.getConfigDir().resolve(Constants.MOD_ID).resolve("addons").resolve(info.id());
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                logger.warn("Could not create {}", path, e);
            }
            return path;
        }

        @Override
        public JsonObject loadConfig() {
            Path file = dataFolder().resolve("config.json");
            if (!Files.exists(file)) return new JsonObject();
            try (Reader reader = Files.newBufferedReader(file)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            } catch (IOException | RuntimeException e) {
                logger.warn("Could not read {}, using an empty config", file, e);
                return new JsonObject();
            }
        }

        @Override
        public void saveConfig(JsonObject config) {
            Path file = dataFolder().resolve("config.json");
            try {
                Files.writeString(file, GSON.toJson(config), StandardCharsets.UTF_8);
            } catch (IOException e) {
                logger.warn("Could not save {}", file, e);
            }
        }

        @Override
        public <T extends TardisControl> void registerControl(String id, MapCodec<T> codec, T control) {
            if (!CONTROL_ID.matcher(id).matches() || !id.startsWith(controlPrefix) || id.length() == controlPrefix.length()) {
                throw new IllegalArgumentException("Control id '" + id + "' must be lower case letters, digits and underscores, "
                        + "and start with '" + controlPrefix + "'");
            }
            if (TardisControlRegistry.isRegistered(id)) {
                throw new IllegalStateException("A control called '" + id + "' is already registered");
            }
            TardisControlTypes.register(id, codec);
            TardisControlRegistry.register(id, control);
        }

        @Override
        public void onTakeOff(Consumer<Tardis> listener) {
            TardisEvents.TAKE_OFF.register(guard("take off", listener, TardisEvents.TAKE_OFF));
        }

        @Override
        public void onLand(Consumer<Tardis> listener) {
            TardisEvents.LAND.register(guard("land", listener, TardisEvents.LAND));
        }

        @Override
        public void runLater(MinecraftServer server, int delayTicks, Runnable action) {
            GCScheduler.runLater(server, delayTicks, () -> {
                try {
                    action.run();
                } catch (Throwable t) {
                    if (t instanceof VirtualMachineError error) throw error;
                    logger.error("A delayed task failed", t);
                }
            });
        }

        private Consumer<Tardis> guard(String eventName, Consumer<Tardis> listener, TardisEvents.Event event) {
            return new Consumer<>() {
                private int failures;

                @Override
                public void accept(Tardis tardis) {
                    try {
                        listener.accept(tardis);
                    } catch (Throwable t) {
                        if (t instanceof VirtualMachineError error) throw error;
                        logger.error("A {} listener failed", eventName, t);
                        if (++failures >= MAX_LISTENER_FAILURES) {
                            event.unregister(this);
                            logger.warn("Removed a {} listener after {} failures", eventName, MAX_LISTENER_FAILURES);
                        }
                    }
                }
            };
        }
    }
}
