package uk.nbforge.gallifreyan_chronicles.client.gui.options;

import uk.nbforge.gallifreyan_chronicles.client.sky.StarDensity;
import uk.nbforge.gallifreyan_chronicles.config.GCConfig;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.function.Consumer;
import uk.nbforge.gallifreyan_chronicles.codec.client.sky.StarDensityCodec;

public final class GCOptions {
    public static final Component HEADER = Component.translatable("options.gallifreyan_chronicles.header");

    private GCOptions() {
    }

    public static OptionInstance<Double> tardisHum() {
        return percentSlider("options.gallifreyan_chronicles.tardis_hum", null,
                GCConfig.tardisHumVolume(), GCConfig::setTardisHumVolume);
    }

    public static OptionInstance<Double> cameraShake() {
        return percentSlider("options.gallifreyan_chronicles.camera_shake",
                Component.translatable("options.gallifreyan_chronicles.camera_shake.tooltip"),
                GCConfig.cameraShakeIntensity(), GCConfig::setCameraShakeIntensity);
    }

    public static OptionInstance<Double> powerDarkness() {
        return percentSlider("options.gallifreyan_chronicles.power_darkness",
                Component.translatable("options.gallifreyan_chronicles.power_darkness.tooltip"),
                GCConfig.powerDarknessStrength(), GCConfig::setPowerDarknessStrength);
    }

    public static OptionInstance<Boolean> immersivePortals() {
        return OptionInstance.createBoolean(
                "options.gallifreyan_chronicles.immersive_portals",
                OptionInstance.cachedConstantTooltip(Component.translatable("options.gallifreyan_chronicles.immersive_portals.tooltip")),
                GCConfig.immersivePortals(),
                value -> {
                    GCConfig.setImmersivePortals(value);
                    GCConfig.saveSoon();
                });
    }

    public static OptionInstance<Integer> modelRenderDistance() {
        return new OptionInstance<>(
                "options.gallifreyan_chronicles.model_render_distance",
                OptionInstance.cachedConstantTooltip(Component.translatable("options.gallifreyan_chronicles.model_render_distance.tooltip")),
                (caption, value) -> Options.genericValueLabel(caption,
                        Component.translatable("options.gallifreyan_chronicles.model_render_distance.value", value)),
                new OptionInstance.IntRange(GCConfig.MIN_MODEL_RENDER_DISTANCE, GCConfig.MAX_MODEL_RENDER_DISTANCE),
                GCConfig.modelRenderDistance(),
                value -> {
                    GCConfig.setModelRenderDistance(value);
                    GCConfig.saveSoon();
                });
    }

    public static OptionInstance<Integer> portalRenderDistance() {
        return new OptionInstance<>(
                "options.gallifreyan_chronicles.portal_render_distance",
                OptionInstance.cachedConstantTooltip(Component.translatable("options.gallifreyan_chronicles.portal_render_distance.tooltip")),
                (caption, value) -> Options.genericValueLabel(caption,
                        Component.translatable("options.gallifreyan_chronicles.model_render_distance.value", value)),
                new OptionInstance.IntRange(GCConfig.MIN_PORTAL_RENDER_DISTANCE, GCConfig.MAX_PORTAL_RENDER_DISTANCE),
                GCConfig.portalRenderDistance(),
                value -> {
                    GCConfig.setPortalRenderDistance(value);
                    GCConfig.saveSoon();
                });
    }

    public static OptionInstance<Integer> maxRenderedPortals() {
        return new OptionInstance<>(
                "options.gallifreyan_chronicles.max_rendered_portals",
                OptionInstance.cachedConstantTooltip(Component.translatable("options.gallifreyan_chronicles.max_rendered_portals.tooltip")),
                (caption, value) -> Options.genericValueLabel(caption, Component.literal(Integer.toString(value))),
                new OptionInstance.IntRange(GCConfig.MIN_RENDERED_PORTALS, GCConfig.MAX_RENDERED_PORTALS),
                GCConfig.maxRenderedPortals(),
                value -> {
                    GCConfig.setMaxRenderedPortals(value);
                    GCConfig.saveSoon();
                });
    }

    public static OptionInstance<StarDensity> starDensity() {
        return new OptionInstance<>(
                "options.gallifreyan_chronicles.star_density",
                OptionInstance.cachedConstantTooltip(Component.translatable("options.gallifreyan_chronicles.star_density.tooltip")),
                (caption, value) -> Options.genericValueLabel(caption, Component.translatable(value.translationKey())),
                new OptionInstance.Enum<>(Arrays.asList(StarDensity.values()), StarDensityCodec.CODEC),
                StarDensity.byName(GCConfig.starDensity()),
                value -> {
                    GCConfig.setStarDensity(value.getSerializedName());
                    GCConfig.saveSoon();
                });
    }

    private static OptionInstance<Double> percentSlider(String key, Component tooltip, double initial, Consumer<Double> setter) {
        return new OptionInstance<>(
                key,
                tooltip == null ? OptionInstance.noTooltip() : OptionInstance.cachedConstantTooltip(tooltip),
                GCOptions::percentLabel,
                OptionInstance.UnitDouble.INSTANCE,
                initial,
                value -> {
                    setter.accept(value);
                    GCConfig.saveSoon();
                });
    }

    private static Component percentLabel(Component caption, Double value) {
        if (value == 0.0) return Options.genericValueLabel(caption, CommonComponents.OPTION_OFF);
        return Component.translatable("options.percent_value", caption, (int) (value * 100.0));
    }
}
