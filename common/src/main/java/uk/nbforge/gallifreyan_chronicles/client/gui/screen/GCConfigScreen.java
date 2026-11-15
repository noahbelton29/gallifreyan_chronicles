package uk.nbforge.gallifreyan_chronicles.client.gui.screen;

import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import uk.nbforge.gallifreyan_chronicles.client.gui.options.GCOptions;
import uk.nbforge.gallifreyan_chronicles.config.GCConfig;

public class GCConfigScreen extends OptionsSubScreen {
    public GCConfigScreen(Screen lastScreen, Options options) {
        super(lastScreen, options, Component.translatable("menu.gallifreyan_chronicles.settings"));
    }

    @Override
    protected void addOptions() {
        assert this.list != null;
        this.list.addHeader(Component.translatable("options.gallifreyan_chronicles.header.audio_visual"));
        this.list.addSmall(GCOptions.tardisHum(), GCOptions.cameraShake());
        this.list.addSmall(GCOptions.powerDarkness(), GCOptions.starDensity());

        this.list.addHeader(Component.translatable("options.gallifreyan_chronicles.header.messages"));
        this.list.addSmall(GCOptions.overlayMessages(), GCOptions.flightMessages());
        this.list.addBig(GCOptions.versionHud());

        this.list.addHeader(Component.translatable("options.gallifreyan_chronicles.header.rendering"));
        this.list.addBig(GCOptions.immersivePortals());
        this.list.addBig(GCOptions.modelRenderDistance());
        this.list.addBig(GCOptions.portalRenderDistance());
        this.list.addBig(GCOptions.maxRenderedPortals());
    }

    @Override
    public void removed() {
        super.removed();
        GCConfig.flushPending(true);
    }
}
