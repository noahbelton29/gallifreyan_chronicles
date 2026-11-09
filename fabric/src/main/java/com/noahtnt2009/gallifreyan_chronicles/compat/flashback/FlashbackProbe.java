package com.noahtnt2009.gallifreyan_chronicles.compat.flashback;

import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.playback.ReplayPlayer;
import com.moulberry.flashback.playback.ReplayServer;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.config.GCConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import qouteall.imm_ptl.core.ClientWorldLoader;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.render.context_management.RenderStates;

/* debug_replay in the config: while a replay plays, logs once a second what each
   loaded world has (chunks, entities, portals) and how many portals were drawn.
   a line is only written when something changed. off by default */
final class FlashbackProbe {
    private static String last = "";
    private static int ticks;

    private FlashbackProbe() {
    }

    static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!GCConfig.debugReplay() || client.level == null || !FlashbackChunksImpl.inReplay()) return;
            if (++ticks % 20 != 0) return;
            StringBuilder sb = new StringBuilder();
            sb.append("main=").append(name(client.level))
                    .append(" cam=").append(client.gameRenderer.mainCamera().blockPosition().toShortString())
                    .append(" drawnPortals=").append(RenderStates.getRenderedPortalNum())
                    .append(" player=").append(client.player != null ? name((ClientLevel) client.player.level()) : "none")
                    .append(" serverDim=").append(ReplayVirtualDimension.serverDim())
                    .append(" away=").append(ReplayVirtualDimension.isAway())
                    .append(" viewer=").append(viewerPos());
            for (ClientLevel world : ClientWorldLoader.getClientWorlds()) {
                int portals = 0;
                StringBuilder p = new StringBuilder();
                for (Entity e : world.entitiesForRendering()) {
                    if (!(e instanceof Portal portal)) continue;
                    portals++;
                    p.append(" [").append(portal.getId())
                            .append(portal.isVisible() ? " vis" : " hid")
                            .append(portal.getAxisW() == null ? " noshape" : "")
                            .append(portal.isRemoved() ? " removed" : "")
                            .append(']');
                }
                sb.append(" | ").append(name(world))
                        .append(" chunks=").append(world.getChunkSource().getLoadedChunksCount())
                        .append(" entities=").append(world.getEntityCount())
                        .append(" portals=").append(portals).append(p);
            }
            String line = sb.toString();
            if (!line.equals(last)) {
                last = line;
                Constants.LOG.info("[replay debug] tick {} {}", ticks, line);
            }
        });
    }

    /* where the replay server has the camera, it sends chunks around this */
    private static String viewerPos() {
        ReplayServer server = Flashback.getReplayServer();
        if (server == null) return "none";
        for (ReplayPlayer viewer : server.getReplayViewers()) return viewer.blockPosition().toShortString();
        return "none";
    }

    private static String name(ClientLevel level) {
        String n = level.dimension().identifier().getPath();
        return n.startsWith("tardis/") ? "tardis" : n;
    }
}
