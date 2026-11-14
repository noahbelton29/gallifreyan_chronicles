package uk.nbforge.gallifreyan_chronicles.client.gui.title;

import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

public final class VortexBackground {
    private static final int SHEET_COUNT = 8;
    private static final int FRAMES_PER_SHEET = 32;
    private static final int FRAME_COUNT = SHEET_COUNT * FRAMES_PER_SHEET;
    private static final int FRAME_SIZE = 128;
    private static final long LOOP_MILLIS = 16_000L;
    private static final int BACKDROP_COLOR = 0xFF040404;
    private static final Identifier[] SHEETS = new Identifier[SHEET_COUNT];
    private static long startMillis = -1L;

    static {
        for (int i = 0; i < SHEET_COUNT; i++) {
            SHEETS[i] = GCUtil.modIdentifier("textures/gui/title/vortex_sheet_" + i + ".png");
        }
    }

    private VortexBackground() {
    }

    public static void render(GuiGraphicsExtractor graphics, int width, int height, int centerX, int centerY) {
        graphics.fill(0, 0, width, height, BACKDROP_COLOR);

        long now = Util.getMillis();
        if (startMillis < 0L && Minecraft.getInstance().gui.overlay() == null) {
            startMillis = now;
        }
        long elapsed = startMillis < 0L ? 0L : now - startMillis;
        int frame = (int) ((elapsed % LOOP_MILLIS) * FRAME_COUNT / LOOP_MILLIS);
        Identifier sheet = SHEETS[frame / FRAMES_PER_SHEET];
        int row = frame % FRAMES_PER_SHEET;

        int reach = Math.max(Math.max(centerX, width - centerX), Math.max(centerY, height - centerY));
        int scale = Math.max(1, Mth.ceil(reach * 2.0F / FRAME_SIZE));
        int size = FRAME_SIZE * scale;
        int x = centerX - size / 2;
        int y = centerY - size / 2;

        graphics.blit(RenderPipelines.GUI_TEXTURED, sheet, x, y, 0.0F, (float) (row * FRAME_SIZE),
                size, size, FRAME_SIZE, FRAME_SIZE, FRAME_SIZE, FRAME_SIZE * FRAMES_PER_SHEET, ARGB.white(1.0F));
    }
}
