package uk.nbforge.gallifreyan_chronicles.tardis.loader;

import uk.nbforge.gallifreyan_chronicles.loader.GCJsonReloadListener;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisTrait;
import net.minecraft.server.MinecraftServer;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.mind.TardisTraitCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.TardisTraits;

public class TardisTraitLoader extends GCJsonReloadListener<TardisTrait> {
    public TardisTraitLoader() {
        super(TardisTraitCodec.CODEC, "tardis_trait", "TARDIS Trait");
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
