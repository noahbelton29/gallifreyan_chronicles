package uk.nbforge.gallifreyan_chronicles.client.gui.title;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.GuiGraphicsExtractor.HoveredTextEffects;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

public class GCButton extends Button {
    private static final int BACKGROUND = 0x80000000;
    private static final int BACKGROUND_DISABLED = 0x40000000;
    private static final int WHITE = 0xFFFFFF;
    private static final float HOVER_WHITE_ALPHA = 48.0f;
    private static final float HOVER_MILLIS = 120.0f;

    private float hoverProgress;
    private long lastFrameMillis = -1L;

    public GCButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    private float updateHover() {
        long now = Util.getMillis();
        float delta = this.lastFrameMillis < 0L ? 0.0f : (now - this.lastFrameMillis) / HOVER_MILLIS;
        this.lastFrameMillis = now;
        float target = this.active && this.isHoveredOrFocused() ? 1.0f : 0.0f;
        this.hoverProgress = Mth.approach(this.hoverProgress, target, delta);
        float t = this.hoverProgress;
        return t * t * (3.0f - 2.0f * t);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = this.getX();
        int y = this.getY();
        int width = this.getWidth();
        int height = this.getHeight();
        float hover = this.updateHover();

        graphics.fill(x, y, x + width, y + height, this.active ? BACKGROUND : BACKGROUND_DISABLED);
        if (hover > 0.0f) {
            graphics.fill(x, y, x + width, y + height, ARGB.color(Math.round(hover * HOVER_WHITE_ALPHA), WHITE));
        }

        this.extractDefaultLabel(graphics.textRendererForWidget(this, HoveredTextEffects.NONE));
    }
}
