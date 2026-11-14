package uk.nbforge.gallifreyan_chronicles.datagen.tardis.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJson;

/**
 * Fluent way to describe a TARDIS console; every method is optional. Control ids are the ones in
 * {@link GCTardisControls}, so a typo is a compile error instead of a control that quietly never spawns.
 *
 * <pre>{@code
 * console("brachacki_console")
 *         .defaultVariant("first_doctors_console")
 *         .standardGlowGroups()
 *         .standardControls()
 *         .interiorDoorControl()
 *         .adjust(GCTardisControls.CLOAK_CONTROL, c -> c.at(-0.525, 0.6875, -0.5).size(0.0625, 0.1875, 0.0625));
 * }</pre>
 */
public final class GCConsoleBuilder {
    /**
     * The usual cooldown, in seconds, of the interior door control.
     */
    public static final double INTERIOR_DOOR_COOLDOWN = 3.35;

    private final JsonObject json;
    private final JsonArray glowGroups = new JsonArray();
    private final List<Control> controls = new ArrayList<>();

    public GCConsoleBuilder(JsonObject json) {
        this.json = json;
    }

    /**
     * The console model used when none is chosen.
     */
    public GCConsoleBuilder defaultVariant(String variant) {
        json.addProperty("default_variant", variant);
        return this;
    }

    // glow groups
    public GCConsoleBuilder glowGroup(String name, int frames, int holdTicks, int fadeTicks) {
        glowGroups.add(GCJson.obj("name", name, "frames", frames, "hold_ticks", holdTicks, "fade_ticks", fadeTicks));
        json.add("glow_groups", glowGroups);
        return this;
    }

    /**
     * The lights, buttons and rotor groups every console uses.
     */
    public GCConsoleBuilder standardGlowGroups() {
        return glowGroup("lights", 4, 30, 8)
                .glowGroup("buttons", 4, 60, 8)
                .glowGroup("rotor", 4, 50, 8);
    }

    // controls

    /**
     * Adds a control with its usual cooldown.
     */
    public GCConsoleBuilder control(String id, double cooldownSeconds) {
        return control(id, cooldownSeconds, details -> {
        });
    }

    /**
     * Adds a control and lets you set where it is, how big it is, or turn its animation off.
     */
    public GCConsoleBuilder control(String id, double cooldownSeconds, Consumer<Control> details) {
        return insert(controls.size(), id, cooldownSeconds, details);
    }

    /**
     * Like {@link #control(String, double, Consumer)}, but placed straight after {@code after} in the list.
     */
    public GCConsoleBuilder controlAfter(String after, String id, double cooldownSeconds, Consumer<Control> details) {
        return insert(indexOf(after) + 1, id, cooldownSeconds, details);
    }

    /**
     * Changes a control that is already in the list, e.g. one added by {@link #standardControls()}.
     */
    public GCConsoleBuilder adjust(String id, Consumer<Control> details) {
        details.accept(controls.get(indexOf(id)));
        return sync();
    }

    /**
     * Every control a console normally has, with its usual cooldown, in the usual order.
     */
    public GCConsoleBuilder standardControls() {
        return control(GCTardisControls.CHANGE_INCREMENT, 0.75)
                .control(GCTardisControls.CLOAK_CONTROL, 1.5)
                .control(GCTardisControls.DIMENSION_SELECTOR, 0.5, Control::noAnimation)
                .control(GCTardisControls.DOOR_CONTROL, 0.5)
                .control(GCTardisControls.DOOR_LOCK_CONTROL, 0.5)
                .control(GCTardisControls.FLIGHT_LEVER, 1.5)
                .control(GCTardisControls.HANDBRAKE, 1.5)
                .control(GCTardisControls.POWER_LEVER, 1.5)
                .control(GCTardisControls.X_INCREMENT_CONTROL, 1)
                .control(GCTardisControls.Y_INCREMENT_CONTROL, 1)
                .control(GCTardisControls.Z_INCREMENT_CONTROL, 1)
                .control(GCTardisControls.ISOMORPHIC_CONTROL, 0.5)
                .control(GCTardisControls.DIRECTION_CONTROL, 0.5, Control::noAnimation);
    }

    /**
     * The interior door control, right after the door control. Needs {@link #standardControls()} first.
     */
    public GCConsoleBuilder interiorDoorControl() {
        return interiorDoorControl(details -> {
        });
    }

    public GCConsoleBuilder interiorDoorControl(Consumer<Control> details) {
        return controlAfter(GCTardisControls.DOOR_CONTROL, GCTardisControls.INTERIOR_DOOR_CONTROL,
                INTERIOR_DOOR_COOLDOWN, details);
    }

    private GCConsoleBuilder insert(int index, String id, double cooldownSeconds, Consumer<Control> details) {
        for (Control existing : controls) {
            if (existing.id.equals(id)) throw new IllegalStateException("Console already has control " + id);
        }
        Control control = new Control(id, cooldownSeconds);
        details.accept(control);
        controls.add(index, control);
        return sync();
    }

    private int indexOf(String id) {
        for (int i = 0; i < controls.size(); i++) {
            if (controls.get(i).id.equals(id)) return i;
        }
        throw new IllegalStateException("Console has no control " + id + " yet");
    }

    private GCConsoleBuilder sync() {
        JsonArray array = new JsonArray();
        for (Control control : controls) array.add(control.toJson());
        json.add("controls", array);
        return this;
    }

    /**
     * One control entry. Anything left unset is taken from the console model.
     */
    public static final class Control {
        private final String id;
        private double cooldown;
        private double[] position;
        private double[] size;
        private Boolean animated;

        private Control(String id, double cooldown) {
            this.id = id;
            this.cooldown = cooldown;
        }

        /**
         * Where the control is, in blocks from the console.
         */
        public Control at(double x, double y, double z) {
            this.position = new double[]{x, y, z};
            return this;
        }

        /**
         * How big its hitbox is: width, height, depth.
         */
        public Control size(double width, double height, double depth) {
            this.size = new double[]{width, height, depth};
            return this;
        }

        public Control cooldown(double seconds) {
            this.cooldown = seconds;
            return this;
        }

        /**
         * The control moves, but plays no animation.
         */
        public Control noAnimation() {
            this.animated = false;
            return this;
        }

        private JsonObject toJson() {
            JsonObject out = new JsonObject();
            out.addProperty("id", id);
            if (position != null) out.add("position", triple(position));
            if (size != null) out.add("size", triple(size));
            out.add("cooldown", new JsonPrimitive(number(cooldown)));
            if (animated != null) out.addProperty("animation", animated);
            return out;
        }

        private static JsonArray triple(double[] values) {
            return GCJson.arr(values[0], values[1], values[2]);
        }

        /* a whole number is written as 1, not 1.0, to match how the files have always read */
        private static Number number(double value) {
            /* not a ternary: mixing Long and Double there would turn both into double and bring the .0 back */
            if (value == Math.rint(value)) return Long.valueOf((long) value);
            return Double.valueOf(value);
        }
    }
}
