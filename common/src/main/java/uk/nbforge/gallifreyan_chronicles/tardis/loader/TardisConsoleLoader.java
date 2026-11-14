package uk.nbforge.gallifreyan_chronicles.tardis.loader;

import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.loader.GCJsonReloadListener;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisConsoleSyncPayload;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.ConsoleLinkSystem;
import net.minecraft.server.MinecraftServer;

import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisConsoleRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.console.TardisConsoleCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsole;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsoleValidator;

public class TardisConsoleLoader extends GCJsonReloadListener<TardisConsole> {
    public TardisConsoleLoader() {
        super(TardisConsoleCodec.CODEC, "tardis_console", "TARDIS Console");
    }

    @Override
    protected void clearRegistry() {
        TardisConsoleRegistry.clear();
    }

    @Override
    protected void register(TardisConsole value) {
        TardisConsoleRegistry.register(value);
    }

    @Override
    protected int registrySize() {
        return TardisConsoleRegistry.size();
    }

    @Override
    protected void onReloadComplete() {
        if (TardisConsoleRegistry.size() == 0) {
            Constants.LOG.error("No TARDIS consoles loaded; console features are disabled until the data errors are fixed");
            TardisConsoleRegistry.setDefault(null);
            return;
        }

        TardisConsoleRegistry.setDefault(TardisConsoleRegistry.get(TardisConsoleRegistry.defaultId()));
        TardisConsoleRegistry.validateVariants();
        TardisConsoleValidator.validate(TardisConsoleRegistry.getAll());
        respawnAllLiveConsoles();
    }

    private void respawnAllLiveConsoles() {
        MinecraftServer server = getCurrentServer();
        if (server == null) return;

        for (Tardis tardis : TardisApi.all(server)) {
            tardis.consoles().forEach(ConsoleLinkSystem::spawnDefaultControls);
        }
    }

    @Override
    protected void broadcastSync(MinecraftServer server) {
        Services.NETWORK.broadcastTardisConsoleSync(server, TardisConsoleSyncPayload.create());
    }
}