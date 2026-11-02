package com.noahtnt2009.gallifreyan_chronicles.tardis.console;

import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonReloadListener;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleSyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ConsoleLinkSystem;
import net.minecraft.server.MinecraftServer;

public class TardisConsoleLoader extends GCJsonReloadListener<TardisConsole> {
    public TardisConsoleLoader() {
        super(TardisConsole.CODEC, "tardis_console", "TARDIS Console");
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