package com.noahtnt2009.gallifreyan_chronicles.tardis.console;

import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonReloadListener;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleSyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
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
        TardisConsoleRegistry.setDefault(TardisConsoleRegistry.get(TardisConsoleRegistry.DEFAULT_ID));
    }

    @Override
    protected void broadcastSync(MinecraftServer server) {
        Services.NETWORK.broadcastTardisConsoleSync(server, TardisConsoleSyncPayload.create());
    }
}
