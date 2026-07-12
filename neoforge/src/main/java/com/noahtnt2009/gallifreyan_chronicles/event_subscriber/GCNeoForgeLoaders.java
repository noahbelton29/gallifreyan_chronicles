package com.noahtnt2009.gallifreyan_chronicles.event_subscriber;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.data.DimensionSkyLoader;
import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonReloadListener;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleLoader;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorLoader;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;


@EventBusSubscriber(modid = Constants.MOD_ID)
public final class GCNeoForgeLoaders {
    private static final Map<Identifier, Supplier<GCJsonReloadListener<?>>> LOADERS =
            new LinkedHashMap<>();

    private GCNeoForgeLoaders() {}

    public static void register(String path, Supplier<GCJsonReloadListener<?>> factory) {
        LOADERS.put(GCUtils.of(path), factory);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        for (Map.Entry<Identifier, Supplier<GCJsonReloadListener<?>>> entry : LOADERS.entrySet()) {
            event.addListener(entry.getKey(), entry.getValue().get());
            Constants.LOG.info("Registered GC NeoForge Reload Listener: {}", entry.getKey());
        }
    }
}
