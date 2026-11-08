package com.noahtnt2009.gallifreyan_chronicles.client.gui.screen;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.addon.AddonInfo;
import com.noahtnt2009.gallifreyan_chronicles.addon.AddonManager;
import com.noahtnt2009.gallifreyan_chronicles.addon.AddonProblem;
import com.noahtnt2009.gallifreyan_chronicles.client.gui.title.GCButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GCAddonsScreen extends Screen {
    private static final int ROW_HEIGHT = 40;
    private static final int ICON_SIZE = 32;
    private static final int LIST_TOP = 40;
    private static final int FOOTER_HEIGHT = 64;
    private static final int TOGGLE_WIDTH = 50;
    private static final int TOGGLE_HEIGHT = 20;
    private static final int ROW_BACKGROUND = 0x80000000;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFAAAAAA;
    private static final int DARK_GRAY = 0xFF777777;
    private static final int GREEN = 0xFF55FF55;
    private static final int RED = 0xFFFF5555;
    private static final int ORANGE = 0xFFFFAA00;

    private final Screen parent;
    private List<AddonInfo> addons = List.of();
    private int page;
    private int perPage = 1;
    private boolean dataChanged;
    private long folderSignature = AddonManager.folderSignature();
    private int ticksSinceCheck;
    private Component status = Component.empty();

    public GCAddonsScreen(Screen parent) {
        super(Component.translatable("addons.gallifreyan_chronicles.title"));
        this.parent = parent;
    }

    private int panelWidth() {
        return Math.min(this.width - 24, 380);
    }

    private int panelLeft() {
        return (this.width - panelWidth()) / 2;
    }

    @Override
    protected void init() {
        addons = AddonManager.all();
        perPage = Math.max(1, (this.height - LIST_TOP - FOOTER_HEIGHT) / ROW_HEIGHT);
        page = Math.max(0, Math.min(page, pageCount() - 1));

        int left = panelLeft();
        int right = left + panelWidth();
        int start = page * perPage;
        int end = Math.min(addons.size(), start + perPage);
        for (int i = start; i < end; i++) {
            AddonInfo info = addons.get(i);
            int rowY = LIST_TOP + (i - start) * ROW_HEIGHT;
            GCButton toggle = new GCButton(right - TOGGLE_WIDTH - 6, rowY + (ROW_HEIGHT - TOGGLE_HEIGHT) / 2,
                    TOGGLE_WIDTH, TOGGLE_HEIGHT, toggleLabel(info), button -> onToggle(info));
            AddonProblem problem = AddonManager.problem(info);
            // an addon that cannot work here cannot be switched on; one that is merely waiting for approval can
            toggle.active = problem == null || !problem.kind().cannotBeSwitchedOn();
            this.addRenderableWidget(toggle);
        }

        int footerY = this.height - FOOTER_HEIGHT + 6;
        if (pageCount() > 1) {
            this.addRenderableWidget(new GCButton(left, footerY, 20, 20, Component.literal("<"), button -> changePage(-1)));
            this.addRenderableWidget(new GCButton(right - 20, footerY, 20, 20, Component.literal(">"), button -> changePage(1)));
        }

        int half = (panelWidth() - 4) / 2;
        int buttonsY = this.height - 30;
        this.addRenderableWidget(new GCButton(left, buttonsY, half, 20,
                Component.translatable("addons.gallifreyan_chronicles.open_folder"),
                button -> openFolder(AddonManager.folder())));
        this.addRenderableWidget(new GCButton(left + panelWidth() - half, buttonsY, half, 20,
                Component.translatable("gui.done"), button -> this.onClose()));
    }

    private void onToggle(AddonInfo info) {
        if (AddonManager.isActive(info)) {
            AddonManager.setEnabled(info.id(), false);
            if (info.hasData()) dataChanged = true;
            this.rebuildWidgets();
            return;
        }
        AddonProblem problem = AddonManager.problem(info);
        if (problem != null && problem.kind() == AddonProblem.Kind.NEEDS_APPROVAL) {
            String shortHash = info.sha256() == null ? "" : info.sha256().substring(0, 12);
            this.minecraft.gui.setScreen(new ConfirmScreen(approved -> {
                if (approved) {
                    AddonManager.approve(info);
                    if (info.hasData()) dataChanged = true;
                }
                this.minecraft.gui.setScreen(this);
            }, Component.translatable("addons.gallifreyan_chronicles.approve.title", info.name()),
                    Component.translatable("addons.gallifreyan_chronicles.approve.message", shortHash)));
            return;
        }
        AddonManager.setEnabled(info.id(), true);
        if (info.hasData()) dataChanged = true;
        this.rebuildWidgets();
    }

    private static Component toggleLabel(AddonInfo info) {
        return AddonManager.isActive(info)
                ? Component.translatable("options.on").withColor(GREEN)
                : Component.translatable("options.off").withColor(RED);
    }

    private int pageCount() {
        return Math.max(1, (addons.size() + perPage - 1) / perPage);
    }

    private void changePage(int delta) {
        int next = Math.max(0, Math.min(pageCount() - 1, page + delta));
        if (next == page) return;
        page = next;
        this.rebuildWidgets();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0.0 && pageCount() > 1) {
            changePage(scrollY < 0 ? 1 : -1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void tick() {
        super.tick();
        // pick up addons added to or removed from the folder while this screen is open, for example from Explorer
        if (++ticksSinceCheck < 20) return;
        ticksSinceCheck = 0;
        long signature = AddonManager.folderSignature();
        if (signature == folderSignature) return;
        folderSignature = signature;
        AddonManager.scan();
        dataChanged = true;
        this.rebuildWidgets();
    }

    @Override
    public void onFilesDrop(@NonNull List<Path> files) {
        List<String> added = AddonManager.install(files);
        if (added.isEmpty()) {
            status = Component.translatable("addons.gallifreyan_chronicles.install_none").withColor(ORANGE);
        } else {
            status = Component.translatable("addons.gallifreyan_chronicles.installed", added.size()).withColor(GREEN);
            dataChanged = true;
        }
        folderSignature = AddonManager.folderSignature();
        this.rebuildWidgets();
    }

    private static void openFolder(Path folder) {
        try {
            Files.createDirectories(folder);
            String path = folder.toAbsolutePath().toString();
            ProcessBuilder builder = switch (Util.getPlatform()) {
                case WINDOWS -> new ProcessBuilder("explorer.exe", path);
                case OSX -> new ProcessBuilder("open", path);
                default -> new ProcessBuilder("xdg-open", path);
            };
            builder.start();
        } catch (IOException e) {
            Constants.LOG.warn("Could not open the addons folder {}", folder, e);
        }
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.text(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, 12, WHITE);

        Component hint = Component.translatable("addons.gallifreyan_chronicles.drop_hint");
        graphics.text(this.font, hint, this.width / 2 - this.font.width(hint) / 2, 24, GRAY);

        int left = panelLeft();
        int right = left + panelWidth();

        if (addons.isEmpty()) {
            Component empty = Component.translatable("addons.gallifreyan_chronicles.empty");
            graphics.text(this.font, empty, this.width / 2 - this.font.width(empty) / 2, LIST_TOP + 20, GRAY);
        }

        int start = page * perPage;
        int end = Math.min(addons.size(), start + perPage);
        for (int i = start; i < end; i++) {
            drawRow(graphics, addons.get(i), left, right, LIST_TOP + (i - start) * ROW_HEIGHT);
        }

        int footerY = this.height - FOOTER_HEIGHT + 6;
        if (pageCount() > 1) {
            Component pageLabel = Component.translatable("addons.gallifreyan_chronicles.page", page + 1, pageCount());
            graphics.text(this.font, pageLabel, this.width / 2 - this.font.width(pageLabel) / 2, footerY + 6, GRAY);
        }

        Component note = AddonManager.restartRequired()
                ? Component.translatable("addons.gallifreyan_chronicles.restart").withColor(ORANGE)
                : status;
        if (!note.getString().isEmpty()) {
            graphics.text(this.font, note, this.width / 2 - this.font.width(note) / 2, footerY + 24, WHITE);
        }
    }

    private void drawRow(GuiGraphicsExtractor graphics, AddonInfo info, int left, int right, int y) {
        boolean enabled = AddonManager.isActive(info);
        AddonProblem problem = AddonManager.problem(info);
        graphics.fill(left, y + 1, right, y + ROW_HEIGHT - 1, ROW_BACKGROUND);

        AddonIcons.Icon icon = AddonIcons.get(info);
        int iconX = left + 4;
        int iconY = y + (ROW_HEIGHT - ICON_SIZE) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, icon.texture(), iconX, iconY, 0.0F, 0.0F,
                ICON_SIZE, ICON_SIZE, icon.width(), icon.height());
        if (!enabled) graphics.fill(iconX, iconY, iconX + ICON_SIZE, iconY + ICON_SIZE, 0x99000000);

        int textX = iconX + ICON_SIZE + 6;
        int textWidth = right - TOGGLE_WIDTH - 12 - textX;
        int nameColor = enabled ? WHITE : DARK_GRAY;

        String name = trim(info.name(), textWidth);
        graphics.text(this.font, name, textX, y + 4, nameColor);
        if (!info.version().isEmpty()) {
            int versionX = textX + this.font.width(name) + 4;
            if (versionX + this.font.width(info.version()) <= textX + textWidth) {
                graphics.text(this.font, info.version(), versionX, y + 4, DARK_GRAY);
            }
        }

        graphics.text(this.font, trim(info.description(), textWidth), textX, y + 15, enabled ? GRAY : DARK_GRAY);

        StringBuilder meta = new StringBuilder();
        if (!info.authors().isEmpty()) {
            meta.append(Component.translatable("addons.gallifreyan_chronicles.by", String.join(", ", info.authors())).getString());
        }
        if (info.hasData()) append(meta, Component.translatable("addons.gallifreyan_chronicles.type.data").getString());
        if (info.hasJava()) append(meta, Component.translatable("addons.gallifreyan_chronicles.type.java").getString());
        if (problem != null) {
            int color = problem.kind() == AddonProblem.Kind.NEEDS_APPROVAL ? ORANGE : RED;
            graphics.text(this.font, trim(problemText(problem).getString(), textWidth), textX, y + 26, color);
        } else {
            graphics.text(this.font, trim(meta.toString(), textWidth), textX, y + 26, DARK_GRAY);
        }

        if (info.hasJava() && problem == null && AddonManager.isRunning(info.id()) != enabled) {
            Component restart = Component.translatable("addons.gallifreyan_chronicles.restart_tag").withColor(ORANGE);
            graphics.text(this.font, restart, right - TOGGLE_WIDTH - 6 + TOGGLE_WIDTH / 2 - this.font.width(restart) / 2,
                    y + ROW_HEIGHT - 11, ORANGE);
        }
    }

    private static Component problemText(AddonProblem problem) {
        String[] parts = problem.detail().split("\\|", -1);
        String key = "addons.gallifreyan_chronicles.problem." + problem.kind().name().toLowerCase();
        return switch (problem.kind()) {
            case INVALID, FAILED_TO_START -> Component.translatable(key, problem.detail());
            case INCOMPATIBLE_API, INCOMPATIBLE_MOD, MISSING_REQUIREMENT -> Component.translatable(key, parts[0], parts.length > 1 ? parts[1] : "");
            case WRONG_REQUIREMENT_VERSION -> Component.translatable(key, parts[0], parts.length > 1 ? parts[1] : "", parts.length > 2 ? parts[2] : "");
            case NEEDS_APPROVAL -> Component.translatable(key);
        };
    }

    private static void append(StringBuilder builder, String part) {
        if (!builder.isEmpty()) builder.append("  ·  ");
        builder.append(part);
    }

    private String trim(String text, int maxWidth) {
        if (maxWidth <= 0) return "";
        if (this.font.width(text) <= maxWidth) return text;
        String ellipsis = "...";
        int length = text.length();
        while (length > 0 && this.font.width(text.substring(0, length)) + this.font.width(ellipsis) > maxWidth) length--;
        return text.substring(0, length) + ellipsis;
    }

    @Override
    public void onClose() {
        if (dataChanged) {
            this.minecraft.getResourcePackRepository().reload();
            this.minecraft.reloadResourcePacks();
        }
        this.minecraft.gui.setScreen(parent);
    }
}
