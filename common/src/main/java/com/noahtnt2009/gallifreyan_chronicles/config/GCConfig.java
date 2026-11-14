package com.noahtnt2009.gallifreyan_chronicles.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GCConfig {
    public static final String BUILT_IN_EXTERIOR = Constants.MOD_ID + ":first_doctors_exterior";
    public static final String BUILT_IN_CONSOLE = Constants.MOD_ID + ":brachacki_console";
    public static final String BUILT_IN_INTERIOR = Constants.MOD_ID + ":first_doctors_interior";
    private static final long SAVE_DELAY_NANOS = 1_000_000_000L;
    private static boolean dirty;
    private static boolean shutdownHookAdded;
    private static long dirtyAt;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final class Data {
        String default_exterior = BUILT_IN_EXTERIOR;
        String default_console = BUILT_IN_CONSOLE;
        String default_interior = BUILT_IN_INTERIOR;
        boolean show_version_hud = true;
        double tardis_hum_volume = 1.0;
        double camera_shake_intensity = 1.0;
        double power_darkness_strength = 1.0;
        String star_density = "normal";
        int model_render_distance = 64;
        int portal_render_distance = 20;
        int max_rendered_portals = 8;
        boolean immersive_portals = true;
        /* logs replay state every second while a flashback replay plays. for finding replay bugs */
        boolean debug_replay = false;
    }

    private static Data data = new Data();

    private GCConfig() {
    }

    private static Path configFile() {
        return Services.PLATFORM.getConfigDir().resolve(Constants.MOD_ID + ".json");
    }

    public static void load() {
        Path file = configFile();
        Data loaded = null;
        if (Files.exists(file)) {
            try {
                loaded = GSON.fromJson(Files.readString(file), Data.class);
            } catch (Exception e) {
                Constants.LOG.error("Could not read {}, using defaults", file, e);
            }
        }
        data = loaded != null ? loaded : new Data();
        if (isBlank(data.default_exterior)) data.default_exterior = BUILT_IN_EXTERIOR;
        if (isBlank(data.default_console)) data.default_console = BUILT_IN_CONSOLE;
        if (isBlank(data.default_interior)) data.default_interior = BUILT_IN_INTERIOR;
        data.tardis_hum_volume = clampVolume(data.tardis_hum_volume);
        data.camera_shake_intensity = clampVolume(data.camera_shake_intensity);
        data.power_darkness_strength = clampVolume(data.power_darkness_strength);
        if (isBlank(data.star_density)) data.star_density = "normal";
        data.model_render_distance = clampRenderDistance(data.model_render_distance);
        data.portal_render_distance = clampPortalDistance(data.portal_render_distance);
        data.max_rendered_portals = clampMaxPortals(data.max_rendered_portals);
        save();
    }

    public static synchronized void saveSoon() {
        if (!shutdownHookAdded) {
            shutdownHookAdded = true;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> flushPending(true), "gc-config-save"));
        }
        dirty = true;
        dirtyAt = System.nanoTime();
    }

    public static synchronized void flushPending(boolean force) {
        if (!dirty) return;
        if (!force && System.nanoTime() - dirtyAt < SAVE_DELAY_NANOS) return;
        save();
    }

    public static synchronized void save() {
        dirty = false;
        Path file = configFile();
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(data));
        } catch (IOException e) {
            Constants.LOG.error("Could not write {}", file, e);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public static String defaultExterior() {
        return data.default_exterior;
    }

    public static String defaultConsole() {
        return data.default_console;
    }

    public static String defaultInterior() {
        return data.default_interior;
    }

    public static boolean showVersionHud() {
        return data.show_version_hud;
    }

    public static void setDefaultExterior(String id) {
        data.default_exterior = id;
    }

    public static void setDefaultConsole(String id) {
        data.default_console = id;
    }

    public static void setDefaultInterior(String id) {
        data.default_interior = id;
    }

    public static double tardisHumVolume() {
        return data.tardis_hum_volume;
    }

    public static void setTardisHumVolume(double volume) {
        data.tardis_hum_volume = clampVolume(volume);
    }

    public static double cameraShakeIntensity() {
        return data.camera_shake_intensity;
    }

    public static void setCameraShakeIntensity(double intensity) {
        data.camera_shake_intensity = clampVolume(intensity);
    }

    public static double powerDarknessStrength() {
        return data.power_darkness_strength;
    }

    public static void setPowerDarknessStrength(double strength) {
        data.power_darkness_strength = clampVolume(strength);
    }

    public static final int MIN_MODEL_RENDER_DISTANCE = 16;
    public static final int MAX_MODEL_RENDER_DISTANCE = 256;

    private static int clampRenderDistance(int distance) {
        if (distance <= 0) return 64;
        return Math.clamp(distance, MIN_MODEL_RENDER_DISTANCE, MAX_MODEL_RENDER_DISTANCE);
    }

    public static final int MIN_PORTAL_RENDER_DISTANCE = 4;
    public static final int MAX_PORTAL_RENDER_DISTANCE = 128;

    private static int clampPortalDistance(int distance) {
        if (distance <= 0) return 20;

        return Math.clamp(distance, MIN_PORTAL_RENDER_DISTANCE, MAX_PORTAL_RENDER_DISTANCE);
    }

    public static final int MIN_RENDERED_PORTALS = 1;
    public static final int MAX_RENDERED_PORTALS = 24;

    private static int clampMaxPortals(int count) {
        if (count <= 0) return 8;
        return Math.clamp(count, MIN_RENDERED_PORTALS, MAX_RENDERED_PORTALS);
    }

    public static boolean debugReplay() {
        return data.debug_replay;
    }

    public static boolean immersivePortals() {
        return data.immersive_portals;
    }

    public static void setImmersivePortals(boolean enabled) {
        data.immersive_portals = enabled;
    }

    public static int maxRenderedPortals() {
        return data.max_rendered_portals;
    }

    public static void setMaxRenderedPortals(int count) {
        data.max_rendered_portals = clampMaxPortals(count);
    }

    public static int portalRenderDistance() {
        return data.portal_render_distance;
    }

    public static void setPortalRenderDistance(int distance) {
        data.portal_render_distance = clampPortalDistance(distance);
    }

    public static int modelRenderDistance() {
        return data.model_render_distance;
    }

    public static void setModelRenderDistance(int distance) {
        data.model_render_distance = clampRenderDistance(distance);
    }

    public static String starDensity() {
        return data.star_density;
    }

    public static void setStarDensity(String density) {
        data.star_density = isBlank(density) ? "normal" : density;
    }

    private static double clampVolume(double volume) {
        return Double.isNaN(volume) ? 1.0 : Math.clamp(volume, 0.0, 1.0);
    }

    public static void setShowVersionHud(boolean show) {
        data.show_version_hud = show;
    }
}
