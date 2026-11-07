package com.noahtnt2009.gallifreyan_chronicles.client.gui.title;

import com.noahtnt2009.gallifreyan_chronicles.compat.essential.EssentialMenuCompat;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class GCOptionsMenuScreen extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_STEP = 24;
    private static final int DONE_GAP = 12;
    private static final int TITLE_GAP = 20;
    private static final int TITLE_COLOR = 0xFFFFFFFF;
    private static final int SECTION_COLOR = 0xFFA0A0A0;
    private static final int SECTION_GAP = 22;
    private static final int COLUMN_GAP = 4;

    private final Screen parent;

    public GCOptionsMenuScreen(Screen parent) {
        super(Component.translatable("menu.gallifreyan_chronicles.options"));
        this.parent = parent;
    }

    private List<String> essentialIds() {
        return this.parent instanceof TitleScreen ? EssentialMenuCompat.titleMenuIds() : List.of();
    }

    private int essentialRows() {
        return (this.essentialIds().size() + 1) / 2;
    }

    private int essentialHeight() {
        int rows = this.essentialRows();
        return rows == 0 ? 0 : SECTION_GAP + rows * BUTTON_STEP;
    }

    private int mainRows() {
        return ReplayViewerHook.available() ? 4 : 3;
    }

    private int top() {
        return (this.height - (BUTTON_STEP * this.mainRows() + this.essentialHeight() + DONE_GAP + BUTTON_HEIGHT)) / 2;
    }

    private int essentialHeadingY() {
        return this.top() + BUTTON_STEP * this.mainRows() + SECTION_GAP - 12;
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
        y += BUTTON_STEP;

        if (ReplayViewerHook.available()) {
            this.addRenderableWidget(new GCButton(x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                    Component.translatableWithFallback("menu.gallifreyan_chronicles.replay_viewer", "Replay Viewer"),
                    button -> {
                        Runnable opener = ReplayViewerHook.opener;
                        if (opener != null) opener.run();
                    }));
            y += BUTTON_STEP;
        }

        List<String> ids = this.essentialIds();
        if (!ids.isEmpty()) {
            y += SECTION_GAP;
            int half = (BUTTON_WIDTH - COLUMN_GAP) / 2;
            for (int i = 0; i < ids.size(); i++) {
                String id = ids.get(i);
                int column = i % 2;
                int bx = column == 0 ? x : x + BUTTON_WIDTH - half;
                int width = column == 0 && i == ids.size() - 1 ? BUTTON_WIDTH : half;
                this.addRenderableWidget(new GCButton(bx, y, width, BUTTON_HEIGHT, EssentialMenuCompat.label(id),
                        button -> {
                            EssentialMenuCompat.requestOnTitleScreen(id);
                            this.minecraft.gui.setScreen(this.parent);
                        }));
                if (column == 1 || i == ids.size() - 1) y += BUTTON_STEP;
            }
        }
        y += DONE_GAP;

        this.addRenderableWidget(new GCButton(x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.translatable("gui.done"), button -> this.onClose()));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, this.width / 2, this.top() - TITLE_GAP, TITLE_COLOR);
        if (this.essentialRows() > 0) {
            graphics.centeredText(this.font, Component.translatableWithFallback("menu.gallifreyan_chronicles.essential", "Essential"),
                    this.width / 2, this.essentialHeadingY(), SECTION_COLOR);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
