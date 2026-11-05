package com.noahtnt2009.gallifreyan_chronicles.client.gui.title;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.client.gui.screen.GCConfigScreen;
import com.noahtnt2009.gallifreyan_chronicles.client.gui.screen.GCCreditsScreen;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public final class GCTitleScreenLayout {
    private static final Identifier LOGO = GCUtils.modIdentifier("textures/gui/title/logo.png");
    private static final int LOGO_TEXTURE_WIDTH = 2027;
    private static final int LOGO_TEXTURE_HEIGHT = 399;

    private static final int PADDING = 10;
    private static final int LOGO_PADDING = 8;
    private static final int MIN_TOP = 8;
    private static final int CENTER_NUDGE = 4;
    private static final int LOGO_GAP = 16;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_STEP = 24;
    private static final int SECTION_GAP = 8;
    private static final int FOOTER_HEIGHT = 28;

    private static final int PANEL_SHADE = 0x66000000;
    private static final int EDGE_DARK = 0xBF000000;
    private static final int VERSION_COLOR = 0xFFFFFFFF;
    private static final int SUBTEXT_COLOR = 0xFFA0A0A0;
    private static final int GAME_VERSION_COLOR = 0x6EE65A;
    private static final int FABRIC_COLOR = 0xF0C08A;
    private static final int NEOFORGE_COLOR = 0xFF8080;
    private static final String DEV_MARKER = "-dev";

    private GCTitleScreenLayout() {
    }

    public static int panelWidth(int screenWidth) {
        return Mth.clamp(Math.round(screenWidth * 0.42F), 180, 240);
    }

    private static int logoWidth(int screenWidth) {
        return panelWidth(screenWidth) - LOGO_PADDING * 2;
    }

    private static int fullWidthRows() {
        return ModListScreens.available() ? 5 : 4;
    }

    private static int contentTop(int screenWidth, int screenHeight) {
        int block = logoHeight(screenWidth) + LOGO_GAP + fullWidthRows() * BUTTON_STEP + SECTION_GAP + BUTTON_HEIGHT;
        return Math.max(MIN_TOP, (screenHeight - FOOTER_HEIGHT - block) / 2 + CENTER_NUDGE);
    }

    private static int logoHeight(int screenWidth) {
        return logoWidth(screenWidth) * LOGO_TEXTURE_HEIGHT / LOGO_TEXTURE_WIDTH;
    }

    public static void build(Screen screen, Minecraft minecraft, Font font, int width, int height, Consumer<AbstractWidget> add) {
        int buttonWidth = panelWidth(width) - PADDING * 2;
        int halfWidth = (buttonWidth - 4) / 2;
        int y = contentTop(width, height) + logoHeight(width) + LOGO_GAP;

        add.accept(new GCButton(PADDING, y, buttonWidth, BUTTON_HEIGHT, Component.translatable("menu.singleplayer"),
                button -> minecraft.gui.setScreen(new SelectWorldScreen(screen))));
        y += BUTTON_STEP;

        GCButton multiplayer = new GCButton(PADDING, y, buttonWidth, BUTTON_HEIGHT, Component.translatable("menu.multiplayer"),
                button -> minecraft.gui.setScreen(minecraft.options.skipMultiplayerWarning
                        ? new JoinMultiplayerScreen(screen)
                        : new SafetyScreen(screen)));
        if (!minecraft.allowsMultiplayer()) {
            multiplayer.active = false;
            multiplayer.setTooltip(Tooltip.create(Component.translatable("title.multiplayer.disabled")));
        }
        add.accept(multiplayer);
        y += BUTTON_STEP;

        if (ModListScreens.available()) {
            add.accept(new GCButton(PADDING, y, buttonWidth, BUTTON_HEIGHT, Component.translatable("menu.gallifreyan_chronicles.mods"),
                    button -> {
                        Screen mods = ModListScreens.create(screen);
                        if (mods != null) minecraft.gui.setScreen(mods);
                    }));
            y += BUTTON_STEP;
        }

        add.accept(new GCButton(PADDING, y, buttonWidth, BUTTON_HEIGHT, Component.translatable("menu.gallifreyan_chronicles.settings"),
                button -> minecraft.gui.setScreen(new GCConfigScreen(screen))));
        y += BUTTON_STEP;

        add.accept(new GCButton(PADDING, y, buttonWidth, BUTTON_HEIGHT, Component.translatable("menu.gallifreyan_chronicles.credits"),
                button -> minecraft.gui.setScreen(new GCCreditsScreen(screen))));
        y += BUTTON_STEP + SECTION_GAP;

        add.accept(new GCButton(PADDING, y, halfWidth, BUTTON_HEIGHT, Component.translatable("menu.options"),
                button -> minecraft.gui.setScreen(new GCOptionsMenuScreen(screen))));
        add.accept(new GCButton(PADDING + buttonWidth - halfWidth, y, halfWidth, BUTTON_HEIGHT, Component.translatable("menu.quit"),
                button -> minecraft.stop()));

        Component copyright = Component.translatable("title.credits");
        int copyrightWidth = font.width(copyright);
        add.accept(new PlainTextButton(width - copyrightWidth - 2, height - 10, copyrightWidth, 10, copyright,
                button -> minecraft.gui.setScreen(new CreditsAndAttributionScreen(screen)), font));
    }

    public static void renderBackdrop(GuiGraphicsExtractor graphics, int width, int height) {
        int panel = panelWidth(width);
        VortexBackground.render(graphics, width, height, panel + (width - panel) / 2, height / 2);

        Screen.extractMenuBackgroundTexture(graphics, Screen.MENU_BACKGROUND, 0, 0, 0.0F, 0.0F, panel, height);
        graphics.fill(0, 0, panel, height, PANEL_SHADE);
        graphics.fill(panel, 0, panel + 1, height, EDGE_DARK);

        int logoWidth = logoWidth(width);
        int logoHeight = logoHeight(width);
        graphics.blit(RenderPipelines.GUI_TEXTURED, LOGO, LOGO_PADDING, contentTop(width, height), 0.0F, 0.0F,
                logoWidth, logoHeight, logoWidth, logoHeight, ARGB.white(1.0F));
    }

    public static void renderOverlay(GuiGraphicsExtractor graphics, Font font, int width, int height) {
        String version = Services.PLATFORM.getModVersion(Constants.MOD_ID);
        int devIndex = version.indexOf(DEV_MARKER);
        String shortVersion = devIndex < 0 ? version : version.substring(0, devIndex);
        Component modLine = Component.translatable("menu.gallifreyan_chronicles.version", shortVersion);
        String platform = Services.PLATFORM.getPlatformName();
        int platformColor = platform.equalsIgnoreCase("NeoForge") ? NEOFORGE_COLOR : FABRIC_COLOR;
        Component gameLine = Component.translatable("menu.gallifreyan_chronicles.game_version",
                Component.literal(Services.PLATFORM.getModVersion("minecraft")).withColor(GAME_VERSION_COLOR),
                Component.literal(platform).withColor(platformColor));
        graphics.text(font, modLine, PADDING, height - 22, VERSION_COLOR, true);
        graphics.text(font, gameLine, PADDING, height - 12, SUBTEXT_COLOR, true);
    }
}
