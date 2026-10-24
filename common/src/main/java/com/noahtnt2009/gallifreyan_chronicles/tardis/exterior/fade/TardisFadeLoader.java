package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonReloadListener;
import net.minecraft.server.MinecraftServer;

public class TardisFadeLoader extends GCJsonReloadListener<FadeType> {
    public TardisFadeLoader() {
        super(FadeTypeKinds.CODEC, "tardis_fade", "TARDIS Fade");
    }

    @Override
    protected void clearRegistry() {
        FadeTypes.clear();
    }

    @Override
    protected void register(FadeType value) {
        FadeTypes.register(value);
        Constants.LOG.info("Registered TARDIS fade {} ({})", value.id(), FadeTypeKinds.CODEC.encodeStart(
                com.mojang.serialization.JsonOps.INSTANCE, value).result().map(Object::toString).orElse("?"));
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
