package com.noahtnt2009.gallifreyan_chronicles.tardis.console;

import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonReloadListener;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleSyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ConsoleLinkSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

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
        TardisConsoleRegistry.setDefault(TardisConsoleRegistry.get(TardisConsoleRegistry.DEFAULT_ID));
        respawnAllLiveConsoles();
    }

    private void respawnAllLiveConsoles() {
        MinecraftServer server = getCurrentServer();
        if (server == null) return;

        TardisManager manager = TardisManager.get(server);
        for (ServerLevel level : server.getAllLevels()) {
            manager.getAllIds().forEach(tardisId ->
                    ConsoleLinkSystem.resolve(level, tardisId)
                            .ifPresent(ConsoleLinkSystem::spawnDefaultControls)
            );
        }
    }

    @Override
    protected void broadcastSync(MinecraftServer server) {
        Services.NETWORK.broadcastTardisConsoleSync(server, TardisConsoleSyncPayload.create());
    }
}