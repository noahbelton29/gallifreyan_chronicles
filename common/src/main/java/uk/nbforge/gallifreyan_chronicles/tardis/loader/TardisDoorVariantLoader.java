package uk.nbforge.gallifreyan_chronicles.tardis.loader;

import uk.nbforge.gallifreyan_chronicles.loader.GCJsonReloadListener;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisConsoleSyncPayload;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import net.minecraft.server.MinecraftServer;

import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisDoorRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.door.TardisDoorVariantCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.door.TardisDoorVariant;

public class TardisDoorVariantLoader extends GCJsonReloadListener<TardisDoorVariant> {
    public TardisDoorVariantLoader() {
        super(TardisDoorVariantCodec.CODEC, "tardis_door_variant", "TARDIS Door Variant");
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
