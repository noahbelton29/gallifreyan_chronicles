package com.noahtnt2009.gallifreyan_chronicles.tardis.door;

import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class DoorBlockJson {
    public static final String FOLDER = "/data/" + Constants.MOD_ID + "/tardis_door_variant";
    private static final String MARKER = "first_doctors_doors.json";
    private static final String DEFAULT_PARTICLE = Constants.MOD_ID + ":brachacki_wall_block";

    public record Entry(String block, String variant, String model, int priority, String particle) {
    }

    private static @Nullable List<TardisDoorVariant> variants;
    private static @Nullable List<Entry> entries;

    private DoorBlockJson() {
    }

    /* every door block to register, in priority order */
    public static List<Entry> entries() {
        if (entries != null) return entries;
        List<Entry> found = new ArrayList<>();
        for (TardisDoorVariant v : bundledVariants()) {
            if (v.interior() || v.block().isEmpty()) continue;
            String block = v.block().contains(":") ? v.block() : Constants.MOD_ID + ":" + v.block();
            if (!block.startsWith(Constants.MOD_ID + ":")) {
                Constants.LOG.warn("[GC] door variant {} has block {} outside {}, skipped", v.id(), block, Constants.MOD_ID);
                continue;
            }
            String particle = v.particle().isEmpty() ? DEFAULT_PARTICLE : v.particle();
            found.add(new Entry(local(block), v.localId(), local(v.parent()), v.priority(), particle));
        }
        return entries = List.copyOf(found);
    }

    /* every door variant json the mod ships, decoded with the codec, by priority then id */
    public static List<TardisDoorVariant> bundledVariants() {
        if (variants != null) return variants;
        List<TardisDoorVariant> found = new ArrayList<>();
        try {
            URL url = folderUrl();
            if (url == null) {
                Constants.LOG.warn("[GC] no door variant folder found, no door blocks registered");
                return variants = List.of();
            }
            URI uri = url.toURI();
            if ("jar".equals(uri.getScheme())) {
                FileSystem jar;
                boolean opened = false;
                try {
                    jar = FileSystems.newFileSystem(uri, Map.of());
                    opened = true;
                } catch (FileSystemAlreadyExistsException e) {
                    jar = FileSystems.getFileSystem(uri);
                }
                try {
                    read(jar.getPath(FOLDER), found);
                } finally {
                    if (opened) jar.close();
                }
            } else {
                read(Path.of(uri), found);
            }
        } catch (Exception e) {
            Constants.LOG.error("[GC] couldn't read the door variant jsons", e);
        }
        found.sort(Comparator.comparingInt(TardisDoorVariant::priority).thenComparing(TardisDoorVariant::localId));
        return variants = List.copyOf(found);
    }

    /* datagen can't look up folders, only files, so find a shipped file and use its folder */
    private static @Nullable URL folderUrl() throws Exception {
        URL url = DoorBlockJson.class.getResource(FOLDER);
        if (url != null) return url;
        URL marker = DoorBlockJson.class.getResource(FOLDER + "/" + MARKER);
        if (marker != null) {
            String text = marker.toString();
            return URI.create(text.substring(0, text.lastIndexOf('/'))).toURL();
        }
        return sourceFolderUrl();
    }

    /* last resort in dev: walk up from the run folder to common's resources */
    private static @Nullable URL sourceFolderUrl() throws Exception {
        for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
            Path folder = dir.resolve("common/src/main/resources" + FOLDER);
            if (Files.isDirectory(folder)) return folder.toUri().toURL();
        }
        return null;
    }

    private static void read(Path folder, List<TardisDoorVariant> out) throws Exception {
        try (Stream<Path> files = Files.list(folder)) {
            for (Path file : files.toList()) {
                String name = file.getFileName().toString();
                if (!name.endsWith(".json")) continue;
                String id = Constants.MOD_ID + ":" + name.substring(0, name.length() - ".json".length());
                JsonElement json = JsonParser.parseString(Files.readString(file));
                TardisDoorVariant.CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(error -> Constants.LOG.error("[GC] bad door variant {}: {}", id, error))
                        .ifPresent(v -> out.add(v.withId(id)));
            }
        }
    }

    private static String local(String id) {
        return GCUtils.localPath(id);
    }
}
