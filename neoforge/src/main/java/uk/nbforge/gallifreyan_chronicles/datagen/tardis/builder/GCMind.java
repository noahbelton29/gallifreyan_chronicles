package uk.nbforge.gallifreyan_chronicles.datagen.tardis.builder;

import com.google.gson.JsonObject;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.SparksAction;
import net.minecraft.ChatFormatting;

import java.util.Locale;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJson;

/**
 * Things a TARDIS can do in reaction to events. Statically import this class and pass them to
 * {@code whenUnwilling(...)}, {@code whenHit(...)} and friends on a personality or trait.
 */
public final class GCMind {
    private GCMind() {
    }

    private static JsonObject action(String type, Object... fields) {
        JsonObject out = GCJson.obj(fields);
        JsonObject typed = new JsonObject();
        typed.addProperty("type", type);
        out.asMap().forEach(typed::add);
        return typed;
    }

    /** Fights back, adding this much stress. */
    public static JsonObject fight(double stress) {
        return action("fight", "stress", stress);
    }

    public static JsonObject slamDoors(double volume, double shake) {
        return action("slam_doors", "volume", volume, "shake", shake);
    }

    public static JsonObject slamDoors(double volume, double shake, boolean lock) {
        return action("slam_doors", "volume", volume, "shake", shake, "lock", lock);
    }

    /** Lets the player notice the trait/personality. */
    public static JsonObject reveal() {
        return action("reveal");
    }

    /** Only reveals if an earlier action actually did something. */
    public static JsonObject revealIfItReacted() {
        return action("reveal", "if_it_reacted", true);
    }

    public static JsonObject sparks(SparksAction.Size size, double height) {
        return action("sparks", "size", size.name().toLowerCase(Locale.ROOT), "height", height);
    }

    public static JsonObject shock(double range, double damage, double push, double lift) {
        return action("shock", "range", range, "damage", damage, "push", push, "lift", lift);
    }

    public static JsonObject message(String key, ChatFormatting colour) {
        return action("message", "key", key, "colour", colour.name().toLowerCase(Locale.ROOT));
    }

    public static JsonObject hitBack(double minDamage, double maxDamage, double push) {
        return action("hit_back", "min_damage", minDamage, "max_damage", maxDamage, "push", push);
    }

    public static JsonObject savage(double minDamage, double maxDamage, double burnSeconds) {
        return action("savage", "min_damage", minDamage, "max_damage", maxDamage, "burn_seconds", burnSeconds);
    }

    public static JsonObject lashOut(double fromSeverity, double minDamage, double maxDamage, double push, double lift) {
        return action("lash_out", "from_severity", fromSeverity, "min_damage", minDamage, "max_damage", maxDamage,
                "push", push, "lift", lift);
    }

    public static JsonObject addStress(double amount) {
        return action("add_stress", "amount", amount);
    }

    public static JsonObject refuse() {
        return action("refuse");
    }

    public static JsonObject shield(double radius, double push) {
        return action("shield", "radius", radius, "push", push);
    }

    public static JsonObject lockDoors() {
        return action("lock_doors");
    }

    /** Plays a sound; "name" without a namespace means this mod's sound. */
    public static JsonObject sound(String name, double volume, double pitch) {
        String id = name.contains(":") ? name : uk.nbforge.gallifreyan_chronicles.Constants.MOD_ID + ":" + name;
        return action("sound", "sound", id, "volume", volume, "pitch", pitch);
    }

    public static JsonObject balk() {
        return action("balk");
    }
}
