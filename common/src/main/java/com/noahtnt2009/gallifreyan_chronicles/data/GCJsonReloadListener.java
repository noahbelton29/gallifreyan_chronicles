package com.noahtnt2009.gallifreyan_chronicles.data;

import com.mojang.serialization.Codec;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public abstract class GCJsonReloadListener<T extends IdentifiableData<T>>
        extends SimpleJsonResourceReloadListener<T> {

    private static volatile MinecraftServer currentServer;

    private final String logLabel;

    protected GCJsonReloadListener(Codec<T> codec, String datapackFolder, String logLabel) {
        super(codec, FileToIdConverter.json(datapackFolder));
        this.logLabel = logLabel;
    }

    public static void setCurrentServer(MinecraftServer server) {
        currentServer = server;
    }
//
//    public static MinecraftServer getCurrentServer() {
//        return currentServer;
//    }

    @Override
    protected final void apply(Map<Identifier, T> resources, @NonNull ResourceManager manager,
                                @NonNull ProfilerFiller profiler) {
        clearRegistry();
        for (Map.Entry<Identifier, T> entry : resources.entrySet()) {
            Identifier fileId = entry.getKey();
            String id = fileId.getNamespace() + ":" + fileId.getPath();
            T value = entry.getValue().withId(id);
            register(value);
            Constants.LOG.debug("Loaded {}: {}", logLabel, id);
        }

        onReloadComplete();
        Constants.LOG.info("Loaded {} {}(s)", registrySize(), logLabel);

        MinecraftServer server = currentServer;
        if (server != null) {
            broadcastSync(server);
        }
    }

    protected abstract void clearRegistry();
    protected abstract void register(T value);
    protected abstract int registrySize();
    protected abstract void broadcastSync(MinecraftServer server);
    protected void onReloadComplete() {}
}
