package uk.nbforge.gallifreyan_chronicles.loader;

import com.mojang.serialization.Codec;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jspecify.annotations.NonNull;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;

public final class GCJsonReloadListener<T extends IdentifiableData<T>> extends SimpleJsonResourceReloadListener<T> {

    private static volatile MinecraftServer currentServer;

    private final String logLabel;
    private final Runnable clear;
    private final Consumer<T> register;
    private final IntSupplier size;
    private Runnable afterReload = () -> {};
    private Consumer<MinecraftServer> sync = _ -> {};

    public GCJsonReloadListener(
            Codec<T> codec,
            String datapackFolder,
            String logLabel,
            Runnable clear,
            Consumer<T> register,
            IntSupplier size) {
        super(codec, FileToIdConverter.json(datapackFolder));
        this.logLabel = logLabel;
        this.clear = clear;
        this.register = register;
        this.size = size;
    }

    public static void setCurrentServer(MinecraftServer server) {
        currentServer = server;
    }

    public static MinecraftServer getCurrentServer() {
        return currentServer;
    }

    public GCJsonReloadListener<T> afterReload(Runnable afterReload) {
        this.afterReload = afterReload;
        return this;
    }

    public GCJsonReloadListener<T> syncWith(Consumer<MinecraftServer> sync) {
        this.sync = sync;
        return this;
    }

    @Override
    public void apply(
            Map<Identifier, T> resources, @NonNull ResourceManager manager, @NonNull ProfilerFiller profiler) {
        clear.run();
        for (Map.Entry<Identifier, T> entry : resources.entrySet()) {
            Identifier fileId = entry.getKey();
            String id = fileId.getNamespace() + ":" + fileId.getPath();
            register.accept(entry.getValue().withId(id));
            GCLog.DATA.debug("Loaded {}: {}", logLabel, id);
        }

        afterReload.run();
        GCLog.DATA.info("Loaded {} {}(s)", size.getAsInt(), logLabel);

        MinecraftServer server = currentServer;
        if (server != null) {
            sync.accept(server);
        }
    }
}
