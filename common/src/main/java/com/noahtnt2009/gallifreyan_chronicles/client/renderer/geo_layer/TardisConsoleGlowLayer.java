package com.noahtnt2009.gallifreyan_chronicles.client.renderer.geo_layer;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.TardisConsoleBlockRenderer;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.GlowGroup;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Single glow layer for every TARDIS console, driven entirely by the console's
 * {@code glow_groups} data. Nothing here is per-console: adding a group to the json
 * (plus its textures) is all that's needed.
 *
 * <p>For each group and frame this draws {@code <variant texture>_<prefix><frame>.png}
 * with its alpha set to the frame's current weight (see {@link GlowGroup#weight}).
 *
 * <p>If a console has no glow groups, or none of its group textures exist for the
 * current variant, it falls back to a single steady {@code <variant texture>_glowmask.png}
 * (if present). Missing textures are always skipped rather than drawn as the
 * purple/black missing texture.
 */
public class TardisConsoleGlowLayer<T extends GeoAnimatable, O, R extends GeoRenderState>
        extends AutoGlowingGeoLayer<T, O, R> {

    private static final float MIN_WEIGHT = 0.01f;
    private static final String STATIC_SUFFIX = "glowmask";

    /** The glowmask texture currently being submitted; read back by {@link #getTextureResource}. */
    private static final DataTicket<Identifier> ACTIVE_TEXTURE =
            DataTicket.create("console_glow_active_texture", Identifier.class);

    // Texture existence cache. Entries expire so that F3+T / pack changes are picked up
    // without needing a per-loader reload listener.
    private static final long RECHECK_NANOS = 5_000_000_000L;
    private static final Map<Identifier, CachedExists> EXISTS = new HashMap<>();

    private record CachedExists(boolean exists, long checkedAt) {
    }

    public TardisConsoleGlowLayer(GeoRenderer<T, O, R> renderer) {
        super(renderer);
    }

    @Override
    protected @NonNull Identifier getTextureResource(@NonNull R renderState) {
        Identifier fallback = maskTexture(this.renderer.getTextureLocation(renderState), STATIC_SUFFIX);

        // The fallback is only used if something asks for our texture outside submitRenderTask.
        return renderState.getOrDefaultGeckolibData(ACTIVE_TEXTURE, fallback);
    }

    @Override
    public void submitRenderTask(
            @NonNull RenderPassInfo<R> info,
            @NonNull SubmitNodeCollector tasks) {

        R state = info.renderState();
        Identifier base = this.renderer.getTextureLocation(state);
        List<GlowGroup> groups = groupsFor(state);

        int originalColor = info.renderColor();

        try {
            if (hasGroupTextures(base, groups)) {
                float time = state.getOrDefaultGeckolibData(TardisConsoleBlockRenderer.GLOW_TIME, 0f);

                for (GlowGroup group : groups) {
                    for (int frame = 0; frame < group.frames(); frame++) {
                        float weight = group.weight(time, frame);
                        if (weight < MIN_WEIGHT)
                            continue;

                        Identifier texture = maskTexture(base, group.prefix() + frame);
                        if (textureExists(texture))
                            submitMask(info, tasks, texture, weight);
                    }
                }
            } else {
                Identifier texture = maskTexture(base, STATIC_SUFFIX);
                if (textureExists(texture))
                    submitMask(info, tasks, texture, 1f);
            }
        } finally {
            state.addGeckolibData(DataTickets.RENDER_COLOR, originalColor);
            state.addGeckolibData(ACTIVE_TEXTURE, maskTexture(base, STATIC_SUFFIX));
        }
    }

    private void submitMask(RenderPassInfo<R> info, SubmitNodeCollector tasks, Identifier texture, float weight) {
        R state = info.renderState();

        // Keep RGB white and fade through alpha.
        int alpha = Math.round(Math.clamp(weight, 0f, 1f) * 255f);
        int argb = (alpha << 24) | 0x00FFFFFF;

        state.addGeckolibData(ACTIVE_TEXTURE, texture);
        state.addGeckolibData(DataTickets.RENDER_COLOR, argb);

        super.submitRenderTask(info, tasks);
    }

    private static List<GlowGroup> groupsFor(GeoRenderState state) {
        String consoleId = state.getOrDefaultGeckolibData(TardisConsoleBlockRenderer.CONSOLE_ID, "");

        TardisConsole console = TardisConsoleRegistry.contains(consoleId)
                ? TardisConsoleRegistry.get(consoleId)
                : TardisConsoleRegistry.getDefault();

        return console == null ? List.of() : console.glowGroups();
    }

    /** True if at least one group has its first frame texture for this variant. */
    private static boolean hasGroupTextures(Identifier base, List<GlowGroup> groups) {
        for (GlowGroup group : groups) {
            if (textureExists(maskTexture(base, group.prefix() + 0)))
                return true;
        }
        return false;
    }

    /** {@code textures/.../x.png} + {@code glowmask_lights_0} -> {@code textures/.../x_glowmask_lights_0.png}. */
    private static Identifier maskTexture(Identifier base, String suffix) {
        String path = base.getPath();
        if (path.endsWith(".png"))
            path = path.substring(0, path.length() - ".png".length());

        return Identifier.fromNamespaceAndPath(base.getNamespace(), path + "_" + suffix + ".png");
    }

    private static boolean textureExists(Identifier texture) {
        long now = System.nanoTime();
        CachedExists cached = EXISTS.get(texture);

        if (cached != null && now - cached.checkedAt < RECHECK_NANOS)
            return cached.exists;

        boolean exists = Minecraft.getInstance().getResourceManager().getResource(texture).isPresent();
        EXISTS.put(texture, new CachedExists(exists, now));
        return exists;
    }
}
