package uk.nbforge.gallifreyan_chronicles.tardis.loader;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.loader.GCJsonReloadListener;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisExteriorSyncPayload;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import net.minecraft.server.MinecraftServer;

import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisExteriorRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.exterior.TardisExteriorCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisExterior;

public class TardisExteriorLoader extends GCJsonReloadListener<TardisExterior> {
    public TardisExteriorLoader() {
        super(TardisExteriorCodec.CODEC, "tardis_exterior", "TARDIS Exterior");
    }

    @Override
    protected void clearRegistry() {
        TardisExteriorRegistry.clear();
    }

    @Override
    protected void register(TardisExterior value) {
        TardisExteriorRegistry.register(value);
    }

    @Override
    protected int registrySize() {
        return TardisExteriorRegistry.size();
    }

    @Override
    protected void onReloadComplete() {
        TardisExterior def = TardisExteriorRegistry.get(TardisExteriorRegistry.defaultId());
        if (def == null) {
            Constants.LOG.warn("No TARDIS exteriors loaded! Expected e.g. data/{}/tardis_exterior/first_doctors_exterior.json", Constants.MOD_ID);
            return;
        }
        TardisExteriorRegistry.setDefault(def);
    }

    @Override
    protected void broadcastSync(MinecraftServer server) {
        Services.NETWORK.broadcastTardisExteriorSync(server, TardisExteriorSyncPayload.create());
    }
}
