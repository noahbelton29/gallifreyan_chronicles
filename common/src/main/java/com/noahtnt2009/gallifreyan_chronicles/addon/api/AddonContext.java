package com.noahtnt2009.gallifreyan_chronicles.addon.api;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControl;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Everything a Java addon is given to work with. This is the supported way in: anything an addon registers through it
 * is tied to the addon, errors in it are caught and logged against the addon's id, and it keeps working across updates
 * as long as {@link AddonApi#VERSION} stays the same.
 *
 * <p>For reading and controlling TARDISes use {@code TardisApi} and {@code Tardis}; they are public and documented too.
 */
public interface AddonContext {
    /** The addon's id, from its {@code addon.json}. */
    String addonId();

    /** A logger named after the addon, so its lines are easy to find in the game log. */
    Logger logger();

    /** The version of Gallifreyan Chronicles that is running, for example {@code "1.0.0"}. */
    String modVersion();

    /** Whether another mod is loaded. Use it before touching that mod's classes. */
    boolean isModLoaded(String modId);

    /** A folder only this addon uses for its own files. It is created on first use. */
    Path dataFolder();

    /** Reads the addon's {@code config.json} from {@link #dataFolder()}. Returns an empty object when there is none yet. */
    JsonObject loadConfig();

    /** Writes the addon's {@code config.json} into {@link #dataFolder()}. */
    void saveConfig(JsonObject config);

    /**
     * Registers a new console control. The id must be lower case letters, digits and underscores and start with the
     * addon id followed by an underscore (addon {@code my_addon}: {@code my_addon_warp_lever}), which keeps addons from
     * clashing with the mod or each other. It can then be used from a console variant's JSON as
     * {@code { "type": "my_addon_warp_lever" }}.
     *
     * @throws IllegalArgumentException when the id is not allowed
     * @throws IllegalStateException    when the id is already taken
     */
    <T extends TardisControl> void registerControl(String id, MapCodec<T> codec, T control);

    /**
     * Runs when any TARDIS starts taking off, on the server thread. If the listener throws five times it is removed
     * and a warning is logged, so a broken addon cannot spam or slow the game.
     */
    void onTakeOff(Consumer<Tardis> listener);

    /** Runs when any TARDIS lands, on the server thread. Removed after five failures, like {@link #onTakeOff}. */
    void onLand(Consumer<Tardis> listener);

    /** Runs {@code action} on the server thread after {@code delayTicks} game ticks (20 ticks is one second). */
    void runLater(MinecraftServer server, int delayTicks, Runnable action);
}
