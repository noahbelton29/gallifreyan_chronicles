package uk.nbforge.gallifreyan_chronicles.tardis.loader;

import uk.nbforge.gallifreyan_chronicles.loader.GCJsonReloadListener;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisConsoleSyncPayload;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import net.minecraft.server.MinecraftServer;

import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisConsoleRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.console.TardisConsoleVariantCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsoleVariant;

public class TardisConsoleVariantLoader extends GCJsonReloadListener<TardisConsoleVariant> {
    public TardisConsoleVariantLoader() {
        super(TardisConsoleVariantCodec.CODEC, "tardis_console_variant", "TARDIS Console Variant");
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
