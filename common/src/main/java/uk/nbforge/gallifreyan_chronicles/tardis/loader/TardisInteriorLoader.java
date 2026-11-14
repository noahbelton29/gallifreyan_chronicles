package uk.nbforge.gallifreyan_chronicles.tardis.loader;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.loader.GCJsonReloadListener;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisInteriorSyncPayload;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import net.minecraft.server.MinecraftServer;

import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisInteriorRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.interior.TardisInteriorCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisInterior;

public class TardisInteriorLoader extends GCJsonReloadListener<TardisInterior> {
    public TardisInteriorLoader() {
        super(TardisInteriorCodec.CODEC, "tardis_interior", "TARDIS Interior");
    }

    @Override
    protected void clearRegistry() {
        TardisInteriorRegistry.clear();
    }

    @Override
    protected void register(TardisInterior value) {
        TardisInteriorRegistry.register(value);
    }

    @Override
    protected int registrySize() {
        return TardisInteriorRegistry.size();
    }

    @Override
    protected void onReloadComplete() {
        if (!TardisInteriorRegistry.contains(TardisInteriorRegistry.defaultId())) {
            Constants.LOG.warn("Default TARDIS interior {} not found; expected data/{}/tardis_interior/first_doctors_interior.json",
                    TardisInteriorRegistry.defaultId(), Constants.MOD_ID);
        }
    }

    @Override
    protected void broadcastSync(MinecraftServer server) {
        Services.NETWORK.broadcastTardisInteriorSync(server, TardisInteriorSyncPayload.create());
    }
}
