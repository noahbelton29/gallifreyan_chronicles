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

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final class Data {
        String default_exterior = BUILT_IN_EXTERIOR;
        String default_console = BUILT_IN_CONSOLE;
        String default_interior = BUILT_IN_INTERIOR;
    }

    private static Data data = new Data();

    private GCConfig() {
    }

    private static Path file() {
        return Services.PLATFORM.getConfigDir().resolve(Constants.MOD_ID + ".json");
    }

    public static void load() {
        Path file = file();
        Data loaded = null;
        if (Files.exists(file)) {
            try {
                loaded = GSON.fromJson(Files.readString(file), Data.class);
            } catch (Exception e) {
                Constants.LOG.error("Could not read {}, using defaults", file, e);
            }
        }
        data = loaded != null ? loaded : new Data();
        if (blank(data.default_exterior)) data.default_exterior = BUILT_IN_EXTERIOR;
        if (blank(data.default_console)) data.default_console = BUILT_IN_CONSOLE;
        if (blank(data.default_interior)) data.default_interior = BUILT_IN_INTERIOR;
        save();
    }

    public static void save() {
        Path file = file();
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(data));
        } catch (IOException e) {
            Constants.LOG.error("Could not write {}", file, e);
        }
    }

    private static boolean blank(String s) {
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

    public static void setDefaultExterior(String id) {
        data.default_exterior = id;
    }

    public static void setDefaultConsole(String id) {
        data.default_console = id;
    }

    public static void setDefaultInterior(String id) {
        data.default_interior = id;
    }
}
