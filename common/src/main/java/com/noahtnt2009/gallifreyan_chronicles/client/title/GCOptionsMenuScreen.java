package com.noahtnt2009.gallifreyan_chronicles.client.title;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;

public class GCOptionsMenuScreen extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_STEP = 24;
    private static final int DONE_GAP = 12;
    private static final int TITLE_GAP = 20;
    private static final int TITLE_COLOR = 0xFFFFFFFF;

    private final Screen parent;

    public GCOptionsMenuScreen(Screen parent) {
        super(Component.translatable("menu.gallifreyan_chronicles.options"));
        this.parent = parent;
    }

    private int top() {
        return (this.height - (BUTTON_STEP * 3 + DONE_GAP + BUTTON_HEIGHT)) / 2;
    }

    @Override
    protected void init() {
        int x = (this.width - BUTTON_WIDTH) / 2;
        int y = this.top();

        this.addRenderableWidget(new GCButton(x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.translatable("menu.gallifreyan_chronicles.game_options"),
                button -> this.minecraft.gui.setScreen(new OptionsScreen(this, this.minecraft.options))));
        y += BUTTON_STEP;

        this.addRenderableWidget(new GCButton(x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.translatable("options.language"),
                button -> this.minecraft.gui.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager()))));
        y += BUTTON_STEP;

        this.addRenderableWidget(new GCButton(x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.translatable("options.accessibility"),
                button -> this.minecraft.gui.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options))));
        y += BUTTON_STEP + DONE_GAP;

        this.addRenderableWidget(new GCButton(x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.translatable("gui.done"), button -> this.onClose()));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, this.width / 2, this.top() - TITLE_GAP, TITLE_COLOR);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
