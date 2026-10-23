package com.noahtnt2009.gallifreyan_chronicles.tardis.door;

import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonReloadListener;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleSyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import net.minecraft.server.MinecraftServer;

public class TardisDoorVariantLoader extends GCJsonReloadListener<TardisDoorVariant> {
    public TardisDoorVariantLoader() {
        super(TardisDoorVariant.CODEC, "tardis_door_variant", "TARDIS Door Variant");
    }

    @Override
    protected void clearRegistry() {
        TardisDoorRegistry.clear();
    }

    @Override
    protected void register(TardisDoorVariant value) {
        TardisDoorRegistry.register(value);
    }

    @Override
    protected int registrySize() {
        return TardisDoorRegistry.size();
    }

    @Override
    protected void onReloadComplete() {
        TardisDoorRegistry.validate();
    }

    @Override
    protected void broadcastSync(MinecraftServer server) {
        Services.NETWORK.broadcastTardisConsoleSync(server, TardisConsoleSyncPayload.create());
    }
}
