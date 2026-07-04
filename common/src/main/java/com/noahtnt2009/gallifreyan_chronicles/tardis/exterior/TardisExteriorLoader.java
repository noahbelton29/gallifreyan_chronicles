package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisExteriorSyncFactory;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class TardisExteriorLoader extends SimpleJsonResourceReloadListener<TardisExterior> {
    private static volatile MinecraftServer currentServer;

    public TardisExteriorLoader() {
        super(TardisExterior.CODEC, FileToIdConverter.json("tardis_exteriors"));
    }

    public static void setCurrentServer(MinecraftServer server) {
        currentServer = server;
    }

    @Override
    protected void apply(Map<Identifier, TardisExterior> resources, ResourceManager manager, ProfilerFiller profiler) {
        Constants.LOG.info("TardisExteriorLoader.apply() called, resources size = {}", resources.size());
        TardisExteriorRegistry.clear();
        for (Map.Entry<Identifier, TardisExterior> entry : resources.entrySet()) {
            Identifier fileId = entry.getKey();
            String id = fileId.getNamespace() + ":" + fileId.getPath();
            TardisExterior exterior = entry.getValue().withId(id);
            TardisExteriorRegistry.register(exterior);
            Constants.LOG.debug("Loaded TARDIS exterior: {}", id);
        }
        TardisExteriorRegistry.setDefault(TardisExteriorRegistry.get(TardisExteriorRegistry.DEFAULT_ID));
        Constants.LOG.info("Loaded {} TARDIS exterior(s)", TardisExteriorRegistry.size());

        MinecraftServer server = currentServer;
        if (server != null) {
            Services.NETWORK.broadcastTardisExteriorSync(server, TardisExteriorSyncFactory.create());
        }
    }
}