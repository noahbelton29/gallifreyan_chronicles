package com.noahtnt2009.gallifreyan_chronicles.compat.sodium;

import com.noahtnt2009.gallifreyan_chronicles.config.GCConfig;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ModOptionsBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.network.chat.Component;

/**
 * Puts the mod's settings in Sodium's video settings screen. Only Sodium loads this class, through the
 * "sodium:config_api_user" entrypoint, so it is never touched when Sodium is not installed.
 */
public final class GCSodiumConfig implements ConfigEntryPoint {
    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        OptionGroupBuilder group = builder.createOptionGroup()
                .addOption(builder.createIntegerOption(GCUtils.modIdentifier("model_render_distance"))
                        .setName(Component.translatable("options.gallifreyan_chronicles.model_render_distance"))
                        .setTooltip(Component.translatable("options.gallifreyan_chronicles.model_render_distance.tooltip"))
                        .setRange(GCConfig.MIN_MODEL_RENDER_DISTANCE, GCConfig.MAX_MODEL_RENDER_DISTANCE, 4)
                        .setDefaultValue(64)
                        .setValueFormatter(value -> Component.translatable(
                                "options.gallifreyan_chronicles.model_render_distance.value", value))
                        .setStorageHandler(GCConfig::save)
                        .setBinding(GCConfig::setModelRenderDistance, GCConfig::modelRenderDistance))
                .addOption(builder.createIntegerOption(GCUtils.modIdentifier("portal_render_distance"))
                        .setName(Component.translatable("options.gallifreyan_chronicles.portal_render_distance"))
                        .setTooltip(Component.translatable("options.gallifreyan_chronicles.portal_render_distance.tooltip"))
                        .setRange(GCConfig.MIN_PORTAL_RENDER_DISTANCE, GCConfig.MAX_PORTAL_RENDER_DISTANCE, 2)
                        .setDefaultValue(20)
                        .setValueFormatter(value -> Component.translatable(
                                "options.gallifreyan_chronicles.model_render_distance.value", value))
                        .setStorageHandler(GCConfig::save)
                        .setBinding(GCConfig::setPortalRenderDistance, GCConfig::portalRenderDistance))
                .addOption(builder.createIntegerOption(GCUtils.modIdentifier("max_rendered_portals"))
                        .setName(Component.translatable("options.gallifreyan_chronicles.max_rendered_portals"))
                        .setTooltip(Component.translatable("options.gallifreyan_chronicles.max_rendered_portals.tooltip"))
                        .setRange(GCConfig.MIN_RENDERED_PORTALS, GCConfig.MAX_RENDERED_PORTALS, 1)
                        .setDefaultValue(8)
                        .setValueFormatter(value -> Component.literal(Integer.toString(value)))
                        .setStorageHandler(GCConfig::save)
                        .setBinding(GCConfig::setMaxRenderedPortals, GCConfig::maxRenderedPortals));

        OptionPageBuilder page = builder.createOptionPage()
                .setName(Component.translatable("options.gallifreyan_chronicles.header"))
                .addOptionGroup(group);

        ModOptionsBuilder mod = builder.registerOwnModOptions();
        mod.addPage(page);
    }
}
