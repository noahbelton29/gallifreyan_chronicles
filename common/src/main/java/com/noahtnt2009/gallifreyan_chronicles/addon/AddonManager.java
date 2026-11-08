package com.noahtnt2009.gallifreyan_chronicles.addon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.addon.api.AddonApi;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/* Runs the addon system.
   Data addons: zip or folder in gc_addons, no code.
   Java addons: mod jars in mods, the loader loads them.
   Turning a java addon on or off needs a restart. */
public final class AddonManager {
    public static final String FOLDER_NAME = "gc_addons";
    public static final String DESCRIPTOR = "addon.json";
    private static final String DISABLED_SUFFIX = ".disabled";
    private static final String STATE_FILE = Constants.MOD_ID + "_addons.json";
    private static final int MAX_ICON_BYTES = 512 * 1024;
    private static final Pattern VALID_ID = Pattern.compile("[a-z0-9_.-]+");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static volatile List<AddonInfo> addons = List.of();
    private static volatile Map<String, AddonProblem> problems = Map.of();
    /* data addons the player turned off */
    private static final Set<String> DISABLED = Collections.synchronizedSet(new LinkedHashSet<>());
    /* java addons the player toggled, and what they picked */
    private static final Map<String, Boolean> MOD_STATE = Collections.synchronizedMap(new LinkedHashMap<>());
    private static boolean scanned;
    private static boolean initialised;

    private AddonManager() {
    }

    public static Path folder() {
        return Services.PLATFORM.getConfigDir().resolveSibling(FOLDER_NAME);
    }

    private static Path modsFolder() {
        return Services.PLATFORM.getConfigDir().resolveSibling("mods");
    }

    private static Path stateFile() {
        return Services.PLATFORM.getConfigDir().resolve(STATE_FILE);
    }

    /* load saved state and scan the folders, once */
    private static synchronized void ensureScanned() {
        if (scanned) return;
        scanned = true;
        loadState();
        scan();
    }

    /* start java addons, after our own stuff is registered */
    public static synchronized void init() {
        if (initialised) return;
        initialised = true;
        ensureScanned();
        ModAddonLoader.load();
        refreshProblems();
        logReport();
        Runtime.getRuntime().addShutdownHook(new Thread(AddonManager::applyModFileStates, "GC addon files"));
    }

    public static List<AddonInfo> all() {
        ensureScanned();
        return addons;
    }

    /* our mod's version */
    public static String modVersion() {
        return Services.PLATFORM.getModVersion(Constants.MOD_ID);
    }

    static @Nullable AddonInfo infoFor(String id) {
        ensureScanned();
        for (AddonInfo info : addons) {
            if (info.id().equals(id)) return info;
        }
        return null;
    }

    /* is it switched on? it can still have a problem */
    public static boolean isEnabled(AddonInfo info) {
        ensureScanned();
        if (info.hasJava()) {
            Boolean wanted = MOD_STATE.get(info.id());
            return wanted != null ? wanted : !info.path().getFileName().toString().endsWith(DISABLED_SUFFIX);
        }
        return !DISABLED.contains(info.id());
    }

    /* what's wrong, or null */
    public static @Nullable AddonProblem problem(AddonInfo info) {
        ensureScanned();
        return problems.get(info.id());
    }

    /* on and nothing wrong */
    public static boolean isActive(AddonInfo info) {
        return isEnabled(info) && problem(info) == null;
    }

    /* data addons to offer as packs */
    public static List<AddonInfo> enabledData() {
        ensureScanned();
        List<AddonInfo> result = new ArrayList<>();
        for (AddonInfo info : addons) {
            if (info.hasData() && !info.hasJava() && isActive(info)) result.add(info);
        }
        return result;
    }

    /* a java addon was toggled, needs a restart */
    public static boolean restartRequired() {
        ensureScanned();
        for (AddonInfo info : addons) {
            if (info.hasJava() && isActive(info) != ModAddonLoader.isRunning(info.id())) return true;
        }
        return false;
    }

    /* did the game start it? */
    public static boolean isRunning(String id) {
        return ModAddonLoader.isRunning(id);
    }

    public static void setEnabled(AddonInfo info, boolean enabled) {
        if (info.hasJava()) {
            MOD_STATE.put(info.id(), enabled);
        } else if (enabled) {
            DISABLED.remove(info.id());
        } else {
            DISABLED.add(info.id());
        }
        saveState();
        refreshProblems();
    }

    private static void loadState() {
        DISABLED.clear();
        MOD_STATE.clear();
        Path file = stateFile();
        if (!Files.exists(file)) return;
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (root.has("disabled")) {
                for (JsonElement element : root.getAsJsonArray("disabled")) DISABLED.add(element.getAsString());
            }
            if (root.has("java")) {
                for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("java").entrySet()) {
                    MOD_STATE.put(entry.getKey(), entry.getValue().getAsBoolean());
                }
            }
        } catch (IOException | RuntimeException e) {
            Constants.LOG.warn("Could not read {}, every addon will start as it is on disk", file, e);
        }
    }

    private static void saveState() {
        JsonObject root = new JsonObject();
        JsonArray disabled = new JsonArray();
        synchronized (DISABLED) {
            DISABLED.forEach(disabled::add);
        }
        root.add("disabled", disabled);
        JsonObject java = new JsonObject();
        synchronized (MOD_STATE) {
            MOD_STATE.forEach(java::addProperty);
        }
        root.add("java", java);
        try {
            Files.createDirectories(stateFile().getParent());
            Files.writeString(stateFile(), GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Constants.LOG.warn("Could not save {}", stateFile(), e);
        }
    }

    /* work out the problems for every addon */
    private static synchronized void refreshProblems() {
        Map<String, AddonProblem> found = new HashMap<>();
        for (AddonInfo info : addons) {
            AddonProblem problem = blockingProblem(info);
            if (problem == null && info.hasJava()) {
                String failure = ModAddonLoader.failure(info.id());
                if (failure != null) problem = new AddonProblem(AddonProblem.Kind.FAILED_TO_START, failure);
            }
            if (problem != null) found.put(info.id(), problem);
        }
        problems = Map.copyOf(found);
    }

    /* why it can't run */
    static @Nullable AddonProblem blockingProblem(AddonInfo info) {
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
                if (other == null || !isEnabled(other)) {
                    return new AddonProblem(AddonProblem.Kind.MISSING_REQUIREMENT, target + "|" + range);
                }
                found = other.version();
            }
            if (!matches(range, found)) {
                return new AddonProblem(AddonProblem.Kind.WRONG_REQUIREMENT_VERSION, target + "|" + range + "|" + found);
            }
        }
        return null;
    }

    private static boolean matches(VersionRange range, String version) {
        try {
            return range.matches(Version.parse(version));
        } catch (IllegalArgumentException e) {
            /* no numbers in the version, only * passes */
            return range.toString().equals("*");
        }
    }

    public static synchronized void scan() {
        List<AddonInfo> found = new ArrayList<>();
        Set<String> ids = new HashSet<>();

        scanFolder(folder(), false, found, ids);
        scanFolder(modsFolder(), true, found, ids);

        addons = List.copyOf(found);
        refreshProblems();
        Constants.LOG.info("Found {} addon(s)", found.size());
    }

    private static void scanFolder(Path folder, boolean modsFolder, List<AddonInfo> found, Set<String> ids) {
        try {
            Files.createDirectories(folder);
            if (!modsFolder) writeReadme(folder);
        } catch (IOException e) {
            Constants.LOG.warn("Could not create the folder {}", folder, e);
            return;
        }

        List<Path> entries;
        try (Stream<Path> stream = Files.list(folder)) {
            entries = stream.sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase())).toList();
        } catch (IOException e) {
            Constants.LOG.warn("Could not list the folder {}", folder, e);
            return;
        }

        for (Path entry : entries) {
            String fileName = entry.getFileName().toString();
            String lower = fileName.toLowerCase();
            if (fileName.startsWith(".")) continue;
            boolean folderAddon = Files.isDirectory(entry);
            boolean modJar = lower.endsWith(".jar") || lower.endsWith(".jar" + DISABLED_SUFFIX);
            if (modsFolder) {
                /* skip jars that aren't addons */
                if (!modJar) continue;
            } else if (!folderAddon && !lower.endsWith(".zip") && !lower.endsWith(".jar")) {
                continue;
            }

            AddonInfo info;
            try {
                info = describe(entry, modsFolder);
            } catch (IOException | RuntimeException e) {
                if (modsFolder) continue; /* not ours, skip it */
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
    }

    private static void writeReadme(Path folder) throws IOException {
        Path readme = folder.resolve("README.txt");
        if (Files.exists(readme)) return;
        Files.writeString(readme, """
                Gallifreyan Chronicles addons
                =============================
                Drag addon files onto the Addons screen on the title screen, or put them here yourself.
                Switch them on and off from that screen.

                Data addons (datapack style: .zip or folder) go in this folder. They have an addon.json and a pack.mcmeta.
                Java addons are real mods (.jar) and go in the mods folder, not here. The Addons screen puts them there for you.
                They can do anything a mod can, so only install ones you trust.

                The full guide is in docs/ADDONS.md in the mod's repository.
                """, StandardCharsets.UTF_8);
    }

    /* couldn't read it, show the error */
    private static AddonInfo broken(Path path, String error) {
        String fileName = path.getFileName().toString();
        String base = fileName.contains(".") && !Files.isDirectory(path) ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
        return new AddonInfo(base.toLowerCase().replaceAll("[^a-z0-9_.-]+", "_"), base, "", "", List.of(), null, path,
                false, null, null, 1, Map.of(), null, error);
    }

    private static @Nullable AddonInfo describe(Path path, boolean inModsFolder) throws IOException {
        byte[] descriptor = read(path, DESCRIPTOR);
        boolean hasData = read(path, "pack.mcmeta") != null;
        String fileName = path.getFileName().toString();
        String baseName = fileName.contains(".") && !Files.isDirectory(path)
                ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;

        if (descriptor == null && !hasData) {
            /* just another mod, or an empty zip */
            if (!inModsFolder && (fileName.toLowerCase().endsWith(".zip") || Files.isDirectory(path))) {
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

        String javaMain = null;
        if (json.has("java") && json.get("java").isJsonObject() && json.getAsJsonObject("java").has("main")) {
            javaMain = json.getAsJsonObject("java").get("main").getAsString();
        }
        if (inModsFolder && javaMain == null) return null; /* not our mod */
        if (!inModsFolder && javaMain != null) {
            throw new IOException("this is a Java addon, which belongs in the mods folder, not gc_addons");
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
            if (!json.get("requires").isJsonObject()) throw new IOException("\"requires\" must be an object like {\"gallifreyan_chronicles\": \">=1.0.0\"}");
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("requires").entrySet()) {
                requires.put(entry.getKey(), entry.getValue().getAsString());
            }
        }

        byte[] icon = read(path, string(json, "icon", "icon.png"));
        if (icon != null && icon.length > MAX_ICON_BYTES) {
            Constants.LOG.warn("Addon '{}' has an icon over {} KB, ignoring it", id, MAX_ICON_BYTES / 1024);
            icon = null;
        }

        /* java addon data is loaded by the loader */
        return new AddonInfo(id, name, version, description, List.copyOf(authors), website, path,
                hasData && javaMain == null, javaMain, icon, api, Map.copyOf(requires), null, null);
    }

    private static String string(JsonObject json, String key, String fallback) {
        return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsString() : fallback;
    }

    /* read one file from a zip, jar or folder */
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

    /* quick fingerprint of both folders */
    public static long folderSignature() {
        long signature = 17;
        for (Path folder : List.of(folder(), modsFolder())) {
            try (Stream<Path> stream = Files.list(folder)) {
                for (Path path : (Iterable<Path>) stream::iterator) {
                    String name = path.getFileName().toString();
                    if (name.startsWith(".")) continue;
                    signature += name.hashCode() * 31L + Files.size(path) * 7L + Files.getLastModifiedTime(path).toMillis();
                }
            } catch (IOException | RuntimeException ignored) {
                /* missing folder, so a different fingerprint */
            }
        }
        return signature;
    }

    /* copy dropped files in: java jars to mods, the rest to gc_addons */
    public static synchronized List<String> install(List<Path> dropped) {
        List<String> added = new ArrayList<>();
        for (Path source : dropped) {
            String name = source.getFileName().toString();
            String lower = name.toLowerCase();
            boolean directory = Files.isDirectory(source);
            if (!directory && !lower.endsWith(".zip") && !lower.endsWith(".jar")) continue;

            boolean javaAddon = false;
            if (!directory) {
                try {
                    byte[] descriptor = read(source, DESCRIPTOR);
                    if (descriptor != null) {
                        JsonObject json = JsonParser.parseString(new String(descriptor, StandardCharsets.UTF_8)).getAsJsonObject();
                        javaAddon = json.has("java");
                    }
                } catch (IOException | RuntimeException ignored) {
                    /* can't read it, so it goes to gc_addons */
                }
            }

            Path target = (javaAddon ? modsFolder() : folder()).resolve(name);
            if (Files.exists(target)) {
                Constants.LOG.warn("An addon called {} is already installed, not overwriting it", name);
                continue;
            }
            try {
                Files.createDirectories(target.getParent());
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

    /* open jars can't be renamed on Windows,
       so a helper renames them after the game closes.
       Might not work, rename by hand then. */
    private static void applyModFileStates() {
        List<String[]> moves = new ArrayList<>();
        for (AddonInfo info : addons) {
            if (!info.hasJava() || !MOD_STATE.containsKey(info.id())) continue;
            Path path = info.path();
            String name = path.getFileName().toString();
            boolean wanted = MOD_STATE.get(info.id());
            if (wanted && name.endsWith(DISABLED_SUFFIX)) {
                moves.add(new String[]{path.toString(), path.resolveSibling(name.substring(0, name.length() - DISABLED_SUFFIX.length())).toString()});
            } else if (!wanted && !name.endsWith(DISABLED_SUFFIX)) {
                moves.add(new String[]{path.toString(), path.resolveSibling(name + DISABLED_SUFFIX).toString()});
            }
        }
        if (moves.isEmpty()) return;
        try {
            boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
            if (windows) {
                StringBuilder script = new StringBuilder("@echo off\r\nping -n 4 127.0.0.1 >nul\r\n");
                for (String[] move : moves) script.append("move /y \"").append(move[0]).append("\" \"").append(move[1]).append("\" >nul\r\n");
                script.append("del \"%~f0\"\r\n");
                Path file = Files.createTempFile("gc_addon_files", ".bat");
                Files.writeString(file, script.toString(), StandardCharsets.UTF_8);
                new ProcessBuilder("cmd", "/c", "start", "/min", "", file.toString()).start();
            } else {
                StringBuilder script = new StringBuilder("sleep 3");
                for (String[] move : moves) script.append("; mv -f '").append(move[0].replace("'", "'\\''")).append("' '")
                        .append(move[1].replace("'", "'\\''")).append("'");
                new ProcessBuilder("sh", "-c", "nohup sh -c \"" + script.toString().replace("\"", "\\\"") + "\" >/dev/null 2>&1 &").start();
            }
        } catch (IOException e) {
            Constants.LOG.warn("Could not schedule renaming the Java addon jars; rename them by hand ({} .jar <-> .jar.disabled)", moves.size(), e);
        }
    }

    /* one summary in the log */
    private static void logReport() {
        for (AddonInfo info : addons) {
            AddonProblem problem = problem(info);
            String state = problem != null ? "NOT LOADED (" + problem.kind() + (problem.detail().isEmpty() ? "" : ": " + problem.detail()) + ")"
                    : isRunning(info.id()) ? "running" : isEnabled(info) ? "enabled" : "disabled";
            Constants.LOG.info("Addon '{}' {} [{}]: {}", info.id(), info.version(), info.hasJava() ? "java" : "data", state);
        }
    }
}
