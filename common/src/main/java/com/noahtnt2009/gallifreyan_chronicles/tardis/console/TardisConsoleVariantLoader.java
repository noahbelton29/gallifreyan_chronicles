package com.noahtnt2009.gallifreyan_chronicles.tardis.console;

import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonReloadListener;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleSyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import net.minecraft.server.MinecraftServer;

public class TardisConsoleVariantLoader extends GCJsonReloadListener<TardisConsoleVariant> {
    public TardisConsoleVariantLoader() {
        super(TardisConsoleVariant.CODEC, "tardis_console_variant", "TARDIS Console Variant");
    }

    @Override
    protected void clearRegistry() {
        TardisConsoleRegistry.clearVariants();
    }

    @Override
    protected void register(TardisConsoleVariant value) {
        TardisConsoleRegistry.registerVariant(value);
    }

    @Override
    protected int registrySize() {
        return TardisConsoleRegistry.variantSize();
    }

    @Override
    protected void onReloadComplete() {
        TardisConsoleRegistry.validateVariants();
    }

    @Override
    protected void broadcastSync(MinecraftServer server) {
        Services.NETWORK.broadcastTardisConsoleSync(server, TardisConsoleSyncPayload.create());
    }
}
