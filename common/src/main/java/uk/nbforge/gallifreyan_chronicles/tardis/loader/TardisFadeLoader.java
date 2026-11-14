package uk.nbforge.gallifreyan_chronicles.tardis.loader;

import com.mojang.serialization.JsonOps;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.loader.GCJsonReloadListener;
import net.minecraft.server.MinecraftServer;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.exterior.fade.FadeTypeKindCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade.FadeType;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade.FadeTypes;

public class TardisFadeLoader extends GCJsonReloadListener<FadeType> {
    public TardisFadeLoader() {
        super(FadeTypeKindCodec.CODEC, "tardis_fade", "TARDIS Fade");
    }

    @Override
    protected void clearRegistry() {
        FadeTypes.clear();
    }

    @Override
    protected void register(FadeType value) {
        FadeTypes.register(value);
        Constants.LOG.info("Registered TARDIS fade {} ({})", value.id(), FadeTypeKindCodec.CODEC.encodeStart(
                JsonOps.INSTANCE, value).result().map(Object::toString).orElse("?"));
    }

    @Override
    protected void onReloadComplete() {
        FadeTypes.markReloaded();
    }

    @Override
    protected int registrySize() {
        return FadeTypes.loadedSize();
    }

    @Override
    protected void broadcastSync(MinecraftServer server) {
    }
}
