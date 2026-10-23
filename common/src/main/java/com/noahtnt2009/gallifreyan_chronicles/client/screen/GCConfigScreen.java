package com.noahtnt2009.gallifreyan_chronicles.client.screen;

import com.noahtnt2009.gallifreyan_chronicles.config.GCConfig;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GCConfigScreen extends Screen {
    private static final int BOX_WIDTH = 260;
    private static final int BOX_HEIGHT = 20;
    private static final int LABEL_GAP = 12;
    private static final int ROW_SPACING = 44;

    private record Row(String labelKey, int y) {
    }

    private final Screen parent;
    private final List<Row> rows = new ArrayList<>();

    public GCConfigScreen(Screen parent) {
        super(Component.translatable("menu.gallifreyan_chronicles.config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        rows.clear();
        int x = this.width / 2 - BOX_WIDTH / 2;
        int y = this.height / 4 - 4;

        addRow(x, y, "menu.gallifreyan_chronicles.config.exterior",
                GCConfig::defaultExterior, GCConfig::setDefaultExterior);
        addRow(x, y + ROW_SPACING, "menu.gallifreyan_chronicles.config.console",
                GCConfig::defaultConsole, GCConfig::setDefaultConsole);
        addRow(x, y + ROW_SPACING * 2, "menu.gallifreyan_chronicles.config.interior",
                GCConfig::defaultInterior, GCConfig::setDefaultInterior);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), _ -> this.onClose())
                .bounds(x, y + ROW_SPACING * 3 + 6, BOX_WIDTH, BOX_HEIGHT)
                .build());
    }

    private void addRow(int x, int y, String labelKey, Supplier<String> getter, Consumer<String> setter) {
        rows.add(new Row(labelKey, y));
        EditBox box = new EditBox(this.font, x, y + LABEL_GAP, BOX_WIDTH, BOX_HEIGHT, Component.translatable(labelKey));
        box.setMaxLength(128);
        box.setValue(getter.get());
        box.setResponder(value -> {
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                setter.accept(trimmed);
            }
        });
        this.addRenderableWidget(box);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        drawCentered(graphics, this.title.getString(), 20, 0xFFFFFFFF);

        int x = this.width / 2 - BOX_WIDTH / 2;
        for (Row row : rows) {
            graphics.text(this.font, Component.translatable(row.labelKey()).getString(), x, row.y(), 0xFFFFFFFF);
        }

        int hintY = this.height / 4 - 4 + ROW_SPACING * 3 + BOX_HEIGHT + 12;
        drawCentered(graphics, Component.translatable("menu.gallifreyan_chronicles.config.hint").getString(),
                hintY, 0xFFAAAAAA);
    }

    private void drawCentered(GuiGraphicsExtractor graphics, String text, int y, int color) {
        graphics.text(this.font, text, this.width / 2 - this.font.width(text) / 2, y, color);
    }

    @Override
    public void onClose() {
        GCConfig.save();
        applyToLoadedRegistries();
        this.minecraft.gui.setScreen(parent);
    }

    private static void applyToLoadedRegistries() {
        if (TardisExteriorRegistry.contains(GCConfig.defaultExterior())) {
            TardisExteriorRegistry.setDefault(TardisExteriorRegistry.get(GCConfig.defaultExterior()));
        }
        if (TardisConsoleRegistry.contains(GCConfig.defaultConsole())) {
            TardisConsoleRegistry.setDefault(TardisConsoleRegistry.get(GCConfig.defaultConsole()));
        }
    }
}