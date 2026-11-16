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
 * Describes a TARDIS console, every method is optional.
 * Control ids come from GCTardisControls so a typo is a compile error and not a control that quietly never spawns.
 * Chain the methods, for example defaultVariant then standardControls then interiorDoorControl.
 */
public final class GCConsoleBuilder {
    // Usual cooldown in seconds
    public static final double INTERIOR_DOOR_COOLDOWN = 3.35;

    private final JsonObject json;
    private final JsonArray glowGroups = new JsonArray();
    private final List<Control> controls = new ArrayList<>();

    public GCConsoleBuilder(JsonObject json) {
        this.json = json;
    }

    /**
     * The variant used when none is chosen.
     */
    public GCConsoleBuilder defaultVariant(String variant) {
        json.addProperty("default_variant", variant);
        return this;
    }

    /**
     * Glow groups.
     */
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

    // Controls

    public GCConsoleBuilder control(String id, double cooldownSeconds) {
        return control(id, cooldownSeconds, details -> {
        });
    }

    /**
     * Lets you set where it is, how big it is or turn its animation off.
     */
    public GCConsoleBuilder control(String id, double cooldownSeconds, Consumer<Control> details) {
        return insert(controls.size(), id, cooldownSeconds, details);
    }

    /**
     * Placed straight after another control in the list.
     */
    public GCConsoleBuilder controlAfter(String after, String id, double cooldownSeconds, Consumer<Control> details) {
        return insert(indexOf(after) + 1, id, cooldownSeconds, details);
    }

    /**
     * Changes a control that is already in the list, for example one from standardControls.
     */
    public GCConsoleBuilder adjust(String id, Consumer<Control> details) {
        details.accept(controls.get(indexOf(id)));
        return sync();
    }

    /**
     * Every control a console normally has, in the usual order.
     */
    public GCConsoleBuilder standardControls() {
        return control(GCTardisControls.COORDINATE_STEP_CONTROL, 0.75)
                .control(GCTardisControls.CLOAKING_DEVICE_CONTROL, 1.5)
                .control(GCTardisControls.DIMENSIONAL_CONTROL, 0.5, Control::noAnimation)
                .control(GCTardisControls.DOOR_CONTROL, 0.5)
                .control(GCTardisControls.DOOR_LOCK_CONTROL, 0.5)
                .control(GCTardisControls.SPACE_TIME_THROTTLE, 1.5)
                .control(GCTardisControls.TIME_ROTOR_HANDBRAKE, 1.5)
                .control(GCTardisControls.POWER_CONTROL, 1.5)
                .control(GCTardisControls.X_INCREMENT_CONTROL, 1)
                .control(GCTardisControls.Y_INCREMENT_CONTROL, 1)
                .control(GCTardisControls.Z_INCREMENT_CONTROL, 1)
                .control(GCTardisControls.ISOMORPHIC_CONTROL, 0.5)
                .control(GCTardisControls.GYROSCOPIC_STABILIZERS, 0.5)
                .control(GCTardisControls.DIRECTIONAL_CONTROL, 0.5, Control::noAnimation);
    }

    /**
     * Goes right after the door control, needs standardControls first.
     */
    public void interiorDoorControl() {
        interiorDoorControl(details -> {
        });
    }

    public void interiorDoorControl(Consumer<Control> details) {
        controlAfter(GCTardisControls.DOOR_CONTROL, GCTardisControls.INTERIOR_DOOR_CONTROL,
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
     * Anything left unset is taken from the console model.
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
         * Where it is, in blocks from the console.
         */
        public Control at(double x, double y, double z) {
            this.position = new double[]{x, y, z};
            return this;
        }

        /**
         * The size of the hitbox, width then height then depth.
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
         * It moves but plays no animation.
         */
        public void noAnimation() {
            this.animated = false;
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

        /**
         * A whole number is written as 1 and not 1.0 to match how the files have always read.
         */
        private static Number number(double value) {
            // Not a ternary, mixing Long and Double there would turn both into double and bring the .0 back
            if (value == Math.rint(value)) return Long.valueOf((long) value);
            return Double.valueOf(value);
        }
    }
}
