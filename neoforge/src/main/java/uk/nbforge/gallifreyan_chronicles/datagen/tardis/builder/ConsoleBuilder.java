package uk.nbforge.gallifreyan_chronicles.datagen.tardis.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJson;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * every method is optional. control ids come from GCTardisControls so a typo is a compile error and not a control that
 * quietly never spawns
 */
public final class ConsoleBuilder {
    public static final double INTERIOR_DOOR_COOLDOWN = 3.35;

    private final JsonObject json;
    private final JsonArray glowGroups = new JsonArray();
    private final List<Control> controls = new ArrayList<>();

    public ConsoleBuilder(JsonObject json) {
        this.json = json;
    }

    public ConsoleBuilder defaultVariant(String variant) {
        json.addProperty("default_variant", variant);
        return this;
    }

    public ConsoleBuilder sortOrder(int order) {
        json.addProperty("sort_order", order);
        return this;
    }

    public ConsoleBuilder glowGroup(String name, int frames, int holdTicks, int fadeTicks) {
        glowGroups.add(GCJson.obj("name", name, "frames", frames, "hold_ticks", holdTicks, "fade_ticks", fadeTicks));
        json.add("glow_groups", glowGroups);
        return this;
    }

    public ConsoleBuilder standardGlowGroups() {
        return glowGroup("lights", 4, 30, 8).glowGroup("buttons", 4, 60, 8).glowGroup("rotor", 4, 50, 8);
    }

    public ConsoleBuilder control(String id, double cooldownSeconds) {
        return control(id, cooldownSeconds, details -> {
        });
    }

    public ConsoleBuilder control(String id, double cooldownSeconds, Consumer<Control> details) {
        return insert(controls.size(), id, cooldownSeconds, details);
    }

    public ConsoleBuilder controlAfter(String after, String id, double cooldownSeconds, Consumer<Control> details) {
        return insert(indexOf(after) + 1, id, cooldownSeconds, details);
    }

    public ConsoleBuilder adjust(String id, Consumer<Control> details) {
        details.accept(controls.get(indexOf(id)));
        return sync();
    }

    public ConsoleBuilder standardControls() {
        return control(GCTardisControls.COORDINATE_STEP_CONTROL, 0.75)
            .control(GCTardisControls.CLOAKING_DEVICE_CONTROL, 1.5)
            .control(GCTardisControls.DIMENSIONAL_CONTROL, 0.5, Control::noAnimation)
            .control(GCTardisControls.DOOR_CONTROL, 0.5)
            .control(GCTardisControls.DOOR_LOCK_CONTROL, 0.5)
            .control(GCTardisControls.SPACE_TIME_THROTTLE, 1.5)
            .control(GCTardisControls.TIME_ROTOR_HANDBRAKE, 1.5)
            .control(GCTardisControls.POWER_CONTROL, 1.5)
            .control(GCTardisControls.X_INCREMENT, 1)
            .control(GCTardisControls.Y_INCREMENT, 1)
            .control(GCTardisControls.Z_INCREMENT, 1)
            .control(GCTardisControls.ISOMORPHIC_CONTROL, 0.5)
            .control(GCTardisControls.GYROSCOPIC_STABILIZERS, 0.5)
            .control(GCTardisControls.DIRECTIONAL_CONTROL, 0.5, Control::noAnimation)
            .control(GCTardisControls.CLOISTER_BELL_CONTROL, 0.5)
            .control(GCTardisControls.NAVIGATIONAL_LOCK_CONTROL, 0.5)
            .control(GCTardisControls.NAVCOM_CONTROL, 0.5);
    }

    /**
     * goes right after the door control, needs standardControls first
     */
    public void interiorDoorControl() {
        interiorDoorControl(details -> {
        });
    }

    public void interiorDoorControl(Consumer<Control> details) {
        controlAfter(
            GCTardisControls.DOOR_CONTROL, GCTardisControls.INTERIOR_DOOR_CONTROL, INTERIOR_DOOR_COOLDOWN, details);
    }

    private ConsoleBuilder insert(int index, String id, double cooldownSeconds, Consumer<Control> details) {
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

    private ConsoleBuilder sync() {
        JsonArray array = new JsonArray();
        for (Control control : controls) array.add(control.toJson());
        json.add("controls", array);
        return this;
    }

    /**
     * anything left unset comes from the console model
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

        private static JsonArray triple(double[] values) {
            return GCJson.arr(values[0], values[1], values[2]);
        }

        /**
         * whole numbers get written as 1 not 1.0 to match how the files have always looked
         */
        private static Number number(double value) {
            if (value == Math.rint(value)) return Long.valueOf((long) value);
            return Double.valueOf(value);
        }

        /**
         * in blocks from the console
         */
        public Control at(double x, double y, double z) {
            this.position = new double[]{x, y, z};
            return this;
        }

        public Control size(double width, double height, double depth) {
            this.size = new double[]{width, height, depth};
            return this;
        }

        public Control cooldown(double seconds) {
            this.cooldown = seconds;
            return this;
        }

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
    }
}
