package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class TardisExteriorLoader extends SimpleJsonResourceReloadListener<TardisExterior> {
    public TardisExteriorLoader() {
        super(TardisExterior.CODEC, FileToIdConverter.json("tardis_exteriors"));
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
    }
}