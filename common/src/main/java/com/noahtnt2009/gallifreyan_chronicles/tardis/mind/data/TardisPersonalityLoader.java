package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data;

import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonReloadListener;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisPersonality;
import net.minecraft.server.MinecraftServer;

public class TardisPersonalityLoader extends GCJsonReloadListener<TardisPersonality> {
    public TardisPersonalityLoader() {
        super(TardisPersonality.CODEC, "tardis_personality", "TARDIS Personality");
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
