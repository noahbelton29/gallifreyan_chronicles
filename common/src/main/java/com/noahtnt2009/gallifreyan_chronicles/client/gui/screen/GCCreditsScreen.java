package com.noahtnt2009.gallifreyan_chronicles.client.gui.screen;

import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class GCCreditsScreen extends Screen {
    private static final Identifier LOGO = GCUtils.modIdentifier("textures/gui/title/logo.png");
    private static final Identifier VIGNETTE = Identifier.withDefaultNamespace("textures/misc/credits_vignette.png");
    private static final Component SECTION_HEADING = Component.literal("============").withStyle(ChatFormatting.WHITE);
    private static final String NAME_PREFIX = "           ";
    private static final String AUTHOR = "NoahTNT2009";
    private static final String CODER = "Misper_YT";
    private static final String TITLE_THEME = "mr_ChameleonCircuit on YouTube";

    private static final int LOGO_WIDTH = 256;
    private static final int LOGO_HEIGHT = LOGO_WIDTH * 399 / 2027;
    private static final int LOGO_OFFSET = 50;
    private static final int LINES_OFFSET = 100;
    private static final int LINE_HEIGHT = 12;
    private static final int COLUMN_HALF_WIDTH = 128;
    private static final int END_PADDING = 24;
    private static final int KEY_SPACE = 32;
    private static final int KEY_LEFT_CONTROL = 1073742048;
    private static final int KEY_RIGHT_CONTROL = 1073742052;
    private static final float BASE_SPEED = 0.8F;
    private static final float SPEEDUP = 5.0F;
    private static final float SPEEDUP_FAST = 15.0F;

    private final Screen parent;
    private final List<FormattedCharSequence> lines = new ArrayList<>();
    private final IntSet centeredLines = new IntOpenHashSet();
    private final IntSet heldModifiers = new IntOpenHashSet();
    private float scroll;
    private float scrollSpeed = BASE_SPEED;
    private int direction = 1;
    private boolean speedup;
    private int totalScrollLength;

    public GCCreditsScreen(Screen parent) {
        super(Component.translatable("menu.gallifreyan_chronicles.credits"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (!this.lines.isEmpty()) return;

        this.addTitle("menu.gallifreyan_chronicles.credits.coding", AUTHOR, CODER);
        this.addTitle("menu.gallifreyan_chronicles.credits.models", AUTHOR);
        this.addTitle("menu.gallifreyan_chronicles.credits.textures", AUTHOR);
        this.addTitle("menu.gallifreyan_chronicles.credits.interiors", "Fredbear1983");
        this.addTitle("menu.gallifreyan_chronicles.credits.title_theme", TITLE_THEME);

        this.addSection(Component.translatable("menu.gallifreyan_chronicles.credits.special_thanks"));
        this.addTitle("menu.gallifreyan_chronicles.credits.texturing_tips", "westankrang");
        this.addTitle("menu.gallifreyan_chronicles.credits.suggestions", "SpartanGamezFTB");

        this.addSection(Component.translatable("menu.gallifreyan_chronicles.credits.disclaimer"));
        this.addNotice("menu.gallifreyan_chronicles.credits.disclaimer.ownership",
                "menu.gallifreyan_chronicles.credits.disclaimer.credit",
                "menu.gallifreyan_chronicles.credits.disclaimer.unofficial");

        this.totalScrollLength = this.lines.size() * LINE_HEIGHT;
    }

    private void addSection(Component name) {
        this.addLine(SECTION_HEADING, true);
        this.addLine(name.copy().withStyle(ChatFormatting.YELLOW), true);
        this.addLine(SECTION_HEADING, true);
        this.addLine(Component.empty(), false);
        this.addLine(Component.empty(), false);
    }

    private void addTitle(String titleKey, String... names) {
        this.addLine(Component.translatable(titleKey).withStyle(ChatFormatting.GRAY), false);
        for (String name : names) {
            this.addLine(Component.literal(NAME_PREFIX).append(name).withStyle(ChatFormatting.WHITE), false);
        }
        this.addLine(Component.empty(), false);
        this.addLine(Component.empty(), false);
    }

    // centred paragraphs, one line each, for notices that aren't a name under a heading
    private void addNotice(String... lineKeys) {
        for (String key : lineKeys) {
            this.addLine(Component.translatable(key).withStyle(ChatFormatting.WHITE), true);
            this.addLine(Component.empty(), false);
        }
    }

    private void addLine(Component line, boolean centered) {
        if (centered) this.centeredLines.add(this.lines.size());
        this.lines.add(line.getVisualOrderText());
    }

    private void updateSpeed() {
        float multiplier = this.speedup ? SPEEDUP + this.heldModifiers.size() * SPEEDUP_FAST : 1.0F;
        this.scrollSpeed = BASE_SPEED * multiplier * this.direction;
    }

    @Override
    public void tick() {
        if (this.scroll > this.totalScrollLength + this.height * 2 + END_PADDING) {
            this.onClose();
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isUp()) {
            this.direction = -1;
        } else if (event.shortcutKey() == KEY_LEFT_CONTROL || event.shortcutKey() == KEY_RIGHT_CONTROL) {
            this.heldModifiers.add(event.shortcutKey());
        } else if (event.shortcutKey() == KEY_SPACE) {
            this.speedup = true;
        }
        this.updateSpeed();
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (event.isUp()) {
            this.direction = 1;
        }
        if (event.shortcutKey() == KEY_SPACE) {
            this.speedup = false;
        } else if (event.shortcutKey() == KEY_LEFT_CONTROL || event.shortcutKey() == KEY_RIGHT_CONTROL) {
            this.heldModifiers.remove(event.shortcutKey());
        }
        this.updateSpeed();
        return super.keyReleased(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.VIGNETTE, VIGNETTE, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
        this.scroll = Math.max(0.0F, this.scroll + partialTick * this.scrollSpeed);

        int columnX = this.width / 2 - COLUMN_HALF_WIDTH;
        int logoY = this.height + LOGO_OFFSET;
        float offset = -this.scroll;

        graphics.pose().pushMatrix();
        graphics.pose().translate(0.0F, offset);
        graphics.nextStratum();
        graphics.blit(RenderPipelines.GUI_TEXTURED, LOGO, this.width / 2 - LOGO_WIDTH / 2, logoY, 0.0F, 0.0F,
                LOGO_WIDTH, LOGO_HEIGHT, LOGO_WIDTH, LOGO_HEIGHT, ARGB.white(1.0F));

        int y = logoY + LINES_OFFSET;
        for (int i = 0; i < this.lines.size(); i++) {
            if (i == this.lines.size() - 1) {
                float overshoot = y + offset - (this.height / 2.0F - 6.0F);
                if (overshoot < 0.0F) {
                    graphics.pose().translate(0.0F, -overshoot);
                }
            }
            if (y + offset + LINE_HEIGHT + 8.0F > 0.0F && y + offset < this.height) {
                FormattedCharSequence line = this.lines.get(i);
                if (this.centeredLines.contains(i)) {
                    graphics.centeredText(this.font, line, columnX + COLUMN_HALF_WIDTH, y, -1);
                } else {
                    graphics.text(this.font, line, columnX, y, -1);
                }
            }
            y += LINE_HEIGHT;
        }
        graphics.pose().popMatrix();
    }

    @Override
    protected void extractMenuBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        Screen.extractMenuBackgroundTexture(graphics, Screen.MENU_BACKGROUND, 0, 0, 0.0F, this.scroll * 0.5F, width, height);
    }

    @Override
    public Music getBackgroundMusic() {
        return Musics.CREDITS;
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
