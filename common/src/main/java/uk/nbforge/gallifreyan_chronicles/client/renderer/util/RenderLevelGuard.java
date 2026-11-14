package uk.nbforge.gallifreyan_chronicles.client.renderer.util;

import com.geckolib.constant.dataticket.DataTicket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import uk.nbforge.gallifreyan_chronicles.client.renderer.block.TardisExteriorBlockRenderState;

public final class RenderLevelGuard {
    public static final DataTicket<Integer> LEVEL_ID = DataTicket.create("gc_render_level_id", Integer.class);
    public static final DataTicket<Boolean> FOREIGN = DataTicket.create("gc_render_foreign", Boolean.class);

    private RenderLevelGuard() {
    }

    public static boolean isForeign(Level level) {
        ClientLevel current = Minecraft.getInstance().level;
        return level != null && current != null && level != current;
    }

    public static void tag(TardisExteriorBlockRenderState renderState, Level level) {
        renderState.addGeckolibData(FOREIGN, isForeign(level));
        if (level != null) renderState.addGeckolibData(LEVEL_ID, System.identityHashCode(level));
    }

    public static boolean belongsToOtherLevel(TardisExteriorBlockRenderState renderState) {
        if (renderState.getOrDefaultGeckolibData(FOREIGN, false)) return true;
        Integer levelId = renderState.getGeckolibData(LEVEL_ID);
        ClientLevel current = Minecraft.getInstance().level;
        return levelId != null && current != null && levelId != System.identityHashCode(current);
    }
}
