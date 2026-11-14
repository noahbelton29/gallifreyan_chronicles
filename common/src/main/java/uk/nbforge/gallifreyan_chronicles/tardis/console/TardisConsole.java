package uk.nbforge.gallifreyan_chronicles.tardis.console;

import uk.nbforge.gallifreyan_chronicles.Constants;


import uk.nbforge.gallifreyan_chronicles.core.data.IdentifiableData;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisConsoleRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.console.TardisConsoleCodec;

public record TardisConsole(String id, List<ControlEntry> controls, String defaultVariant,
                            List<GlowGroup> glowGroups) implements IdentifiableData<TardisConsole> {
    public record ControlEntry(String id, Vec3 offset, float width, float height, float depth, float cooldown,
                               ControlAnimationSetting animation) {
        public static final float DEFAULT_COOLDOWN_SECONDS = 1.5F;
        /**
         * Position placeholder for controls placed by their model's hitbox bone; resolved in withId.
         */
        public static final Vec3 FROM_HITBOX = new Vec3(Double.NaN, Double.NaN, Double.NaN);

    }

    public TardisConsole(String id) {
        this(id, List.of(), "", List.of());
    }

    public TardisConsole(String id, List<ControlEntry> controls) {
        this(id, controls, "", List.of());
    }

    private static List<ControlEntry> resolveHitboxes(String consoleId, List<ControlEntry> entries) {
        if (entries.stream().noneMatch(e -> e.offset() == ControlEntry.FROM_HITBOX)) return entries;
        Map<String, ConsoleHitboxBones.Box> boxes = ConsoleHitboxBones.read(consoleId);
        List<ControlEntry> out = new ArrayList<>(entries.size());
        for (ControlEntry e : entries) {
            if (e.offset() != ControlEntry.FROM_HITBOX) {
                out.add(e);
                continue;
            }
            ConsoleHitboxBones.Box box = boxes.get(e.id());
            if (box == null) {
                Constants.LOG.warn("Console {} control '{}' has no position and no '{}{}' bone",
                        consoleId, e.id(), e.id(), ConsoleHitboxBones.SUFFIX);
                continue;
            }
            boolean sized = !Float.isNaN(e.width());
            out.add(new ControlEntry(e.id(), box.center(),
                    sized ? e.width() : box.width(), sized ? e.height() : box.height(), sized ? e.depth() : box.depth(),
                    e.cooldown(), e.animation()));
        }
        return List.copyOf(out);
    }

    public @Nullable ControlEntry controlEntry(String controlId) {
        for (ControlEntry entry : controls) {
            if (entry.id().equals(controlId)) return entry;
        }
        return null;
    }

    public List<TardisConsoleVariant> variants() {
        return TardisConsoleRegistry.variantsOf(id);
    }

    public List<String> variantIds() {
        return variants().stream().map(TardisConsoleVariant::localId).toList();
    }

    public boolean hasVariants() {
        return !variants().isEmpty();
    }

    public boolean hasVariant(String variantId) {
        return exactVariant(variantId) != null;
    }

    public String resolvedDefaultVariant() {
        if (!defaultVariant.isEmpty() && hasVariant(defaultVariant)) return defaultVariant;
        List<TardisConsoleVariant> all = variants();
        return all.isEmpty() ? "" : all.getFirst().localId();
    }

    private @Nullable TardisConsoleVariant exactVariant(String variantId) {
        TardisConsoleVariant variant = TardisConsoleRegistry.getVariant(variantId);
        return variant != null && variant.parent().equals(id) ? variant : null;
    }

    public @Nullable TardisConsoleVariant variant(String variantId) {
        TardisConsoleVariant variant = exactVariant(variantId);
        return variant != null ? variant : exactVariant(resolvedDefaultVariant());
    }

    public String textureIdFor(String variantId) {
        TardisConsoleVariant variant = variant(variantId);
        return variant != null ? variant.texture() : id;
    }

    @Override
    public TardisConsole withId(String id) {
        return new TardisConsole(id, resolveHitboxes(id, controls), defaultVariant, glowGroups);
    }
}
