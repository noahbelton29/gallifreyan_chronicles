package uk.nbforge.gallifreyan_chronicles.tardis.loader;

import uk.nbforge.gallifreyan_chronicles.loader.GCJsonReloadListener;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisPersonality;
import net.minecraft.server.MinecraftServer;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.mind.TardisPersonalityCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.TardisPersonalities;

public class TardisPersonalityLoader extends GCJsonReloadListener<TardisPersonality> {
    public TardisPersonalityLoader() {
        super(TardisPersonalityCodec.CODEC, "tardis_personality", "TARDIS Personality");
    }

    @Override
    protected void clearRegistry() {
        TardisPersonalities.clear();
    }

    @Override
    protected void register(TardisPersonality value) {
        TardisPersonalities.register(value);
    }

    @Override
    protected int registrySize() {
        return TardisPersonalities.size();
    }

    @Override
    protected void broadcastSync(MinecraftServer server) {
    }
}
