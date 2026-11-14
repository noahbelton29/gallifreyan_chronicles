package uk.nbforge.gallifreyan_chronicles.client.gui.screen;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.addon.AddonInfo;
import uk.nbforge.gallifreyan_chronicles.addon.AddonManager;
import uk.nbforge.gallifreyan_chronicles.addon.AddonProblem;
import uk.nbforge.gallifreyan_chronicles.client.gui.title.GCButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GCAddonsScreen extends Screen {
    private static final int ROW_HEIGHT = 36;
    private static final int ICON_SIZE = 28;
    private static final int LIST_TOP = 40;
    private static final int FOOTER_HEIGHT = 48;
    private static final int GAP = 6;
    private static final int TOGGLE_WIDTH = 40;
    private static final int TOGGLE_HEIGHT = 18;
    private static final int PADDING = 8;
    private static final int LINE = 10;

    private static final int PANEL = 0x66000000;
    private static final int ROW_HOVER = 0x22FFFFFF;
    private static final int ROW_SELECTED = 0x33FFFFFF;
    private static final int DIVIDER = 0x33FFFFFF;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFAAAAAA;
    private static final int DARK_GRAY = 0xFF777777;
    private static final int GREEN = 0xFF55FF55;
    private static final int RED = 0xFFFF5555;
    private static final int ORANGE = 0xFFFFAA00;

    private final Screen parent;
    private List<AddonInfo> addons = List.of();
    private @Nullable String selectedId;
    private boolean detailsOpen;
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

    private int totalWidth() {
        return Math.min(this.width - 24, 560);
    }

    private int left() {
        return (this.width - totalWidth()) / 2;
    }

    private boolean wide() {
        return this.width >= 440;
    }

    private boolean sidebarShown() {
        return detailsOpen && selected() != null;
    }

    private boolean listShown() {
        return !sidebarShown() || wide();
    }

    private int sidebarWidth() {
        if (!sidebarShown()) return 0;
        return wide() ? Math.max(170, totalWidth() * 40 / 100) : totalWidth();
    }

    private int listWidth() {
        if (!listShown()) return 0;
        return totalWidth() - (sidebarShown() ? sidebarWidth() + GAP : 0);
    }

    private int listBottom() {
        return this.height - FOOTER_HEIGHT;
    }

    private int sidebarLeft() {
        return wide() ? left() + totalWidth() - sidebarWidth() : left();
    }

    private int rowY(int indexOnPage) {
        return LIST_TOP + indexOnPage * ROW_HEIGHT;
    }

    @Override
    protected void init() {
        addons = AddonManager.all();
        if (selectedId != null && addons.stream().noneMatch(info -> info.id().equals(selectedId))) {
            selectedId = null;
            detailsOpen = false;
        }
        perPage = Math.max(1, (listBottom() - LIST_TOP) / ROW_HEIGHT);
        page = Math.max(0, Math.min(page, pageCount() - 1));

        if (listShown()) {
            int listLeft = left();
            int listRight = listLeft + listWidth();
            int start = page * perPage;
            int end = Math.min(addons.size(), start + perPage);
            for (int i = start; i < end; i++) {
                AddonInfo info = addons.get(i);
                this.addRenderableWidget(new RowButton(listLeft, rowY(i - start), listWidth() - TOGGLE_WIDTH - 12, ROW_HEIGHT,
                        button -> toggleDetails(info)));
                GCButton toggle = new GCButton(listRight - TOGGLE_WIDTH - 6, rowY(i - start) + (ROW_HEIGHT - TOGGLE_HEIGHT) / 2,
                        TOGGLE_WIDTH, TOGGLE_HEIGHT, toggleLabel(info), button -> onToggle(info));
                toggle.active = canSwitch(info);
                this.addRenderableWidget(toggle);
            }

            int pagingY = this.height - FOOTER_HEIGHT + 6;
            if (pageCount() > 1) {
                this.addRenderableWidget(new GCButton(listRight - 16, pagingY, 16, 14, Component.literal(">"), button -> changePage(1)));
                this.addRenderableWidget(new GCButton(listRight - 16 - 40, pagingY, 16, 14, Component.literal("<"), button -> changePage(-1)));
            }
        }

        AddonInfo open = sidebarShown() ? selected() : null;
        if (open != null) {
            int sx = sidebarLeft();
            int sw = sidebarWidth();
            this.addRenderableWidget(new GCButton(sx + sw - PADDING + 2 - 14, LIST_TOP + 4, 14, 14, Component.literal("x"), button -> closeDetails()));
            GCButton toggle = new GCButton(sx + PADDING, listBottom() - PADDING - TOGGLE_HEIGHT, sw - PADDING * 2, TOGGLE_HEIGHT,
                    toggleLabel(open), button -> onToggle(open));
            toggle.active = canSwitch(open);
            this.addRenderableWidget(toggle);
        }

        int buttonsY = this.height - 26;
        int half = (totalWidth() - 4) / 2;
        this.addRenderableWidget(new GCButton(left(), buttonsY, half, 20,
                Component.translatable("addons.gallifreyan_chronicles.open_folder"),
                button -> openFolder(AddonManager.folder())));
        this.addRenderableWidget(new GCButton(left() + totalWidth() - half, buttonsY, half, 20,
                Component.translatable("gui.done"), button -> this.onClose()));
    }

    private static boolean canSwitch(AddonInfo info) {
        AddonProblem problem = AddonManager.problem(info);
        return problem == null || !problem.kind().cannotBeSwitchedOn();
    }

    private void closeDetails() {
        detailsOpen = false;
        this.rebuildWidgets();
    }

    private void onToggle(AddonInfo info) {
        AddonManager.setEnabled(info, !AddonManager.isActive(info));
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

    private void toggleDetails(AddonInfo info) {
        detailsOpen = !(detailsOpen && info.id().equals(selectedId));
        selectedId = info.id();
        this.rebuildWidgets();
    }

    private static final class RowButton extends Button {
        private RowButton(int x, int y, int width, int height, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            // the screen draws the row itself
        }
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

        graphics.text(this.font, this.title, left(), 12, WHITE);
        Component hint = Component.translatable("addons.gallifreyan_chronicles.drop_hint");
        graphics.text(this.font, hint, left(), 24, DARK_GRAY);

        int listLeft = left();
        int listRight = listLeft + listWidth();

        if (addons.isEmpty() && listShown()) {
            Component empty = Component.translatable("addons.gallifreyan_chronicles.empty");
            graphics.text(this.font, empty, listLeft + PADDING, LIST_TOP + PADDING, GRAY);
        }

        if (listShown()) {
            int start = page * perPage;
            int end = Math.min(addons.size(), start + perPage);
            for (int i = start; i < end; i++) {
                drawRow(graphics, addons.get(i), listLeft, listRight, rowY(i - start), mouseX, mouseY);
            }
        }

        int footerY = this.height - FOOTER_HEIGHT + 9;
        if (pageCount() > 1 && listShown()) {
            Component pageLabel = Component.translatable("addons.gallifreyan_chronicles.page", page + 1, pageCount());
            graphics.text(this.font, pageLabel, listRight - 36 - this.font.width(pageLabel) / 2, footerY - 2, GRAY);
        }
        Component note = AddonManager.restartRequired()
                ? Component.translatable("addons.gallifreyan_chronicles.restart").withColor(ORANGE)
                : status;
        if (!note.getString().isEmpty()) {
            int room = (listShown() ? listWidth() : totalWidth()) - (pageCount() > 1 && listShown() ? 70 : 0);
            graphics.text(this.font, trim(note.getString(), room), listLeft, footerY - 2, noteColor(note));
        }

        if (sidebarShown()) drawSidebar(graphics);
    }

    private int noteColor(Component note) {
        return note.getStyle().getColor() != null ? 0xFF000000 | note.getStyle().getColor().getValue() : GRAY;
    }

    private void drawRow(GuiGraphicsExtractor graphics, AddonInfo info, int left, int right, int y, int mouseX, int mouseY) {
        boolean active = AddonManager.isActive(info);
        AddonProblem problem = AddonManager.problem(info);
        boolean selected = detailsOpen && info.id().equals(selectedId);
        boolean hovered = mouseX >= left && mouseX < right && mouseY >= y && mouseY < y + ROW_HEIGHT;

        graphics.fill(left, y + 1, right, y + ROW_HEIGHT - 1, selected ? ROW_SELECTED : hovered ? ROW_HOVER : PANEL);
        graphics.fill(left, y + 1, left + 2, y + ROW_HEIGHT - 1, stateColor(info, active, problem));

        AddonIcons.Icon icon = AddonIcons.get(info);
        int iconX = left + 8;
        int iconY = y + (ROW_HEIGHT - ICON_SIZE) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, icon.texture(), iconX, iconY, 0.0F, 0.0F,
                ICON_SIZE, ICON_SIZE, icon.width(), icon.height());
        if (!active) graphics.fill(iconX, iconY, iconX + ICON_SIZE, iconY + ICON_SIZE, 0x99000000);

        int textX = iconX + ICON_SIZE + 7;
        int textWidth = right - TOGGLE_WIDTH - 12 - textX;

        String name = trim(info.name(), textWidth);
        graphics.text(this.font, name, textX, y + 7, active ? WHITE : DARK_GRAY);
        if (!info.version().isEmpty()) {
            int versionX = textX + this.font.width(name) + 5;
            if (versionX + this.font.width(info.version()) <= textX + textWidth) {
                graphics.text(this.font, info.version(), versionX, y + 7, DARK_GRAY);
            }
        }

        if (problem != null) {
            int color = RED;
            graphics.text(this.font, trim(problemText(problem).getString(), textWidth), textX, y + 20, color);
        } else {
            graphics.text(this.font, trim(firstLine(info.description()), textWidth), textX, y + 20, active ? GRAY : DARK_GRAY);
        }
    }

    private static int stateColor(AddonInfo info, boolean active, @Nullable AddonProblem problem) {
        if (problem != null) return RED;
        if (info.hasJava() && AddonManager.isRunning(info.id()) != active) return ORANGE;
        return active ? GREEN : 0xFF444444;
    }

    private void drawSidebar(GuiGraphicsExtractor graphics) {
        AddonInfo info = selected();
        if (info == null) return;
        int sx = sidebarLeft();
        int sw = sidebarWidth();
        int sy = LIST_TOP;
        int bottom = listBottom();
        graphics.fill(sx, sy, sx + sw, bottom, PANEL);

        int x = sx + PADDING;
        int inner = sw - PADDING * 2;
        boolean active = AddonManager.isActive(info);
        AddonProblem problem = AddonManager.problem(info);

        AddonIcons.Icon icon = AddonIcons.get(info);
        int headerY = sy + PADDING;
        graphics.blit(RenderPipelines.GUI_TEXTURED, icon.texture(), x, headerY, 0.0F, 0.0F, 32, 32, icon.width(), icon.height());
        int headerX = x + 32 + 7;
        int headerWidth = inner - 39 - 16;
        graphics.text(this.font, trim(info.name(), headerWidth), headerX, headerY, WHITE);
        if (!info.version().isEmpty())
            graphics.text(this.font, trim(info.version(), inner - 39), headerX, headerY + 11, GRAY);
        if (!info.authors().isEmpty()) {
            String by = Component.translatable("addons.gallifreyan_chronicles.by", String.join(", ", info.authors())).getString();
            graphics.text(this.font, trim(by, inner - 39), headerX, headerY + 22, DARK_GRAY);
        }

        int toggleTop = bottom - PADDING - TOGGLE_HEIGHT;
        List<FormattedCharSequence> factLines = new ArrayList<>();
        for (String fact : facts(info)) factLines.addAll(this.font.split(Component.literal(fact), inner));
        int maxFactLines = Math.max(2, (bottom - sy) / 3 / LINE);
        if (factLines.size() > maxFactLines) factLines = factLines.subList(0, maxFactLines);
        int factsTop = toggleTop - 6 - factLines.size() * LINE;

        int y = headerY + 32 + 8;
        int limit = factsTop - 6;
        if (problem != null) {
            int color = RED;
            y = drawWrapped(graphics, problemText(problem).getString(), x, y, inner, color, limit);
            y = drawWrapped(graphics, Component.translatable("addons.gallifreyan_chronicles.hint." + problem.kind().name().toLowerCase()).getString(),
                    x, y + 2, inner, DARK_GRAY, limit);
        } else if (info.hasJava() && AddonManager.isRunning(info.id()) != active) {
            String key = active ? "state.restart_on" : "state.restart_off";
            y = drawWrapped(graphics, Component.translatable("addons.gallifreyan_chronicles." + key).getString(), x, y, inner, ORANGE, limit);
        } else {
            String key = active ? "state.active" : "state.off";
            graphics.text(this.font, Component.translatable("addons.gallifreyan_chronicles." + key), x, y, active ? GREEN : GRAY);
            y += LINE;
        }

        graphics.fill(x, factsTop - 4, x + inner, factsTop - 3, DIVIDER);
        for (int i = 0; i < factLines.size(); i++) {
            graphics.text(this.font, factLines.get(i), x, factsTop + i * LINE, DARK_GRAY);
        }

        String description = info.description().isEmpty()
                ? Component.translatable("addons.gallifreyan_chronicles.details.no_description").getString()
                : info.description();
        drawWrapped(graphics, description, x, y + 8, inner, info.description().isEmpty() ? DARK_GRAY : GRAY, factsTop - 8);
    }

    private List<String> facts(AddonInfo info) {
        List<String> facts = new ArrayList<>();
        StringBuilder type = new StringBuilder();
        if (info.hasData()) type.append(Component.translatable("addons.gallifreyan_chronicles.type.data").getString());
        if (info.hasJava()) {
            if (!type.isEmpty()) type.append(" + ");
            type.append(Component.translatable("addons.gallifreyan_chronicles.type.java").getString());
        }
        if (!type.isEmpty()) facts.add(type.toString());
        facts.add(Component.translatable("addons.gallifreyan_chronicles.details.id", info.id()).getString());
        for (Map.Entry<String, String> requirement : info.requires().entrySet()) {
            facts.add(Component.translatable("addons.gallifreyan_chronicles.details.requires", requirement.getKey(), requirement.getValue()).getString());
        }
        facts.add(Component.translatable("addons.gallifreyan_chronicles.details.file", info.path().getFileName().toString()).getString());
        return facts;
    }

    private @Nullable AddonInfo selected() {
        for (AddonInfo info : addons) {
            if (info.id().equals(selectedId)) return info;
        }
        return null;
    }

    private int drawWrapped(GuiGraphicsExtractor graphics, String text, int x, int y, int width, int color, int maxY) {
        for (var line : this.font.split(Component.literal(text), width)) {
            if (y + LINE - 1 > maxY) break;
            graphics.text(this.font, line, x, y, color);
            y += LINE;
        }
        return y;
    }

    private static Component problemText(AddonProblem problem) {
        String[] parts = problem.detail().split("\\|", -1);
        String key = "addons.gallifreyan_chronicles.problem." + problem.kind().name().toLowerCase();
        return switch (problem.kind()) {
            case INVALID, FAILED_TO_START -> Component.translatable(key, problem.detail());
            case INCOMPATIBLE_API, INCOMPATIBLE_MOD, MISSING_REQUIREMENT ->
                    Component.translatable(key, parts[0], parts.length > 1 ? parts[1] : "");
            case WRONG_REQUIREMENT_VERSION ->
                    Component.translatable(key, parts[0], parts.length > 1 ? parts[1] : "", parts.length > 2 ? parts[2] : "");
        };
    }

    private static String firstLine(String text) {
        int newline = text.indexOf('\n');
        return newline < 0 ? text : text.substring(0, newline);
    }

    private String trim(String text, int maxWidth) {
        if (maxWidth <= 0) return "";
        if (this.font.width(text) <= maxWidth) return text;
        String ellipsis = "...";
        int length = text.length();
        while (length > 0 && this.font.width(text.substring(0, length)) + this.font.width(ellipsis) > maxWidth)
            length--;
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
