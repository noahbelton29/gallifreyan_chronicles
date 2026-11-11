package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data;

import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonReloadListener;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisTrait;
import net.minecraft.server.MinecraftServer;

public class TardisTraitLoader extends GCJsonReloadListener<TardisTrait> {
    public TardisTraitLoader() {
        super(TardisTrait.CODEC, "tardis_trait", "TARDIS Trait");
    }

    @Override
    protected void clearRegistry() {
        TardisTraits.clear();
    }

    @Override
    protected void register(TardisTrait value) {
        TardisTraits.register(value);
    }

    @Override
    protected int registrySize() {
        return TardisTraits.size();
    }

    @Override
    protected void broadcastSync(MinecraftServer server) {
    }
}
