package com.noahtnt2009.gallifreyan_chronicles.client.renderer.geo_layer;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.TardisConsoleBlockRenderer;
import com.noahtnt2009.gallifreyan_chronicles.client.tardis.TardisConsoleAssets;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.GlowGroup;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TardisConsoleGlowLayer<T extends GeoAnimatable, O, R extends GeoRenderState>
        extends AutoGlowingGeoLayer<T, O, R> {

    private static final float MIN_WEIGHT = 0.01f;
    private static final String STATIC_SUFFIX = "glowmask";

    private static final DataTicket<Identifier> ACTIVE_TEXTURE =
            DataTicket.create("console_glow_active_texture", Identifier.class);

    private static final long RECHECK_NANOS = 5_000_000_000L;
    private static final Map<Identifier, CachedExists> EXISTS = new HashMap<>();

    private static final Map<Identifier, RenderType> GLOW_TYPES = new HashMap<>();

    private static RenderType glowType(Identifier texture) {
        return GLOW_TYPES.computeIfAbsent(texture, t -> RenderType.create(
                "gc_console_glow",
                RenderSetup.builder(RenderPipelines.EYES)
                        .withTexture("Sampler0", t)
                        .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                        .sortOnUpload()
                        .createRenderSetup()));
    }

    private record CachedExists(boolean exists, long checkedAt) {
    }

    public TardisConsoleGlowLayer(GeoRenderer<T, O, R> renderer) {
        super(renderer);
    }

    @Override
    protected @NonNull Identifier getTextureResource(@NonNull R renderState) {
        Identifier fallback = maskTexture(this.renderer.getTextureLocation(renderState), STATIC_SUFFIX);

        return renderState.getOrDefaultGeckolibData(ACTIVE_TEXTURE, fallback);
    }

    @Override
    protected @Nullable RenderType getRenderType(@NonNull R renderState) {
        return glowType(getTextureResource(renderState));
    }

    @Override
    public void submitRenderTask(
            @NonNull RenderPassInfo<R> info,
            @NonNull SubmitNodeCollector tasks) {

        R state = info.renderState();
        Identifier base = this.renderer.getTextureLocation(state);
        TardisConsole console = consoleFor(state);
        List<GlowGroup> groups = console == null ? List.of() : console.glowGroups();
        TardisConsoleVariant variant = console == null ? null : console.variant(
                state.getOrDefaultGeckolibData(TardisConsoleBlockRenderer.CONSOLE_VARIANT, ""));

        int originalColor = info.renderColor();

        try {
            if (hasGroupTextures(base, variant, groups)) {
                float time = state.getOrDefaultGeckolibData(TardisConsoleBlockRenderer.GLOW_TIME, 0f);

                for (GlowGroup group : groups) {
                    Identifier groupBase = groupBase(base, variant, group);

                    for (int frame = 0; frame < group.frames(); frame++) {
                        float weight = group.weight(time, frame);
                        if (weight < MIN_WEIGHT)
                            continue;

                        Identifier texture = maskTexture(groupBase, group.prefix() + frame);
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

        int alpha = Math.round(Math.clamp(weight, 0f, 1f) * 255f);
        int argb = (alpha << 24) | 0x00FFFFFF;

        state.addGeckolibData(ACTIVE_TEXTURE, texture);
        state.addGeckolibData(DataTickets.RENDER_COLOR, argb);

        super.submitRenderTask(info, tasks);
    }

    private static @Nullable TardisConsole consoleFor(GeoRenderState state) {
        String consoleId = state.getOrDefaultGeckolibData(TardisConsoleBlockRenderer.CONSOLE_ID, "");

        return TardisConsoleRegistry.contains(consoleId)
                ? TardisConsoleRegistry.get(consoleId)
                : TardisConsoleRegistry.getDefault();
    }

    private static Identifier groupBase(Identifier variantBase, @Nullable TardisConsoleVariant variant, GlowGroup group) {
        String override = variant == null ? null : variant.glowTexture(group.name());
        return override == null ? variantBase : TardisConsoleAssets.texture(override);
    }

    private static boolean hasGroupTextures(Identifier variantBase, @Nullable TardisConsoleVariant variant, List<GlowGroup> groups) {
        for (GlowGroup group : groups) {
            if (textureExists(maskTexture(groupBase(variantBase, variant, group), group.prefix() + 0)))
                return true;
        }
        return false;
    }

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