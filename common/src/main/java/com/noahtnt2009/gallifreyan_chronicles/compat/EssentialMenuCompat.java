package com.noahtnt2009.gallifreyan_chronicles.compat;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.client.title.GCButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public final class EssentialMenuCompat {
    private static final String PROXY_CLASS = "gg.essential.gui.proxies.EssentialProxyElement";
    private static final String MENU_PROXY_CLASS = "gg.essential.gui.proxies.MenuButtonProxy";
    private static final String HANDLER_CLASS = "gg.essential.gui.proxies.ScreenWithProxiesHandler";
    private static final String MENU_DISPLAY_CLASS = "gg.essential.handlers.PauseMenuDisplay";
    private static final List<String> ORDER = List.of("invite_host", "social", "wardrobe", "pictures", "settings", "account");
    private static final Map<String, String> FALLBACK_LABELS = Map.of(
            "invite_host", "Host",
            "social", "Social",
            "wardrobe", "Wardrobe",
            "pictures", "Pictures",
            "settings", "Settings",
            "account", "Account"
    );

    private static final int BUTTON_WIDTH = 90;
    private static final int BUTTON_HEIGHT = 20;
    private static final int STEP = 22;
    private static final int MARGIN = 8;

    private static final int PENDING_TIMEOUT_FRAMES = 60;

    private static final Map<Screen, State> STATES = new WeakHashMap<>();
    private static List<String> titleIds = List.of();
    private static String pendingId;
    private static int pendingFrames;
    private static boolean loggedFailure;
    private static boolean loggedFound;
    private static final int OFFSCREEN = -10000;

    private EssentialMenuCompat() {
    }

    private static final class State {
        private GCButton toggle;
        private final Map<String, GCButton> entries = new LinkedHashMap<>();
        private final Map<String, List<AbstractWidget>> targets = new LinkedHashMap<>();
        private final Set<AbstractWidget> handled = Collections.newSetFromMap(new IdentityHashMap<>());
        private boolean open;
    }

    public static List<String> titleMenuIds() {
        return titleIds;
    }

    private static String baseId(String id) {
        return id.replaceFirst("_\\d+$", "");
    }

    public static Component label(String essentialId) {
        String id = baseId(essentialId);
        String fallback = FALLBACK_LABELS.get(id);
        if (fallback == null) {
            fallback = id.isEmpty() ? id : Character.toUpperCase(id.charAt(0)) + id.substring(1).replace('_', ' ');
        }
        return Component.translatableWithFallback("menu.gallifreyan_chronicles.essential." + id, fallback);
    }

    public static void requestOnTitleScreen(String id) {
        pendingId = baseId(id);
        pendingFrames = 0;
    }

    public static void initTitleProxies(Screen screen) {
        Field handlerField = null;
        for (Class<?> type = screen.getClass(); type != null && handlerField == null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (field.getType().getName().equals(HANDLER_CLASS)) {
                    handlerField = field;
                    break;
                }
            }
        }
        if (handlerField == null) return;

        try {
            ClassLoader loader = handlerField.getType().getClassLoader();
            Class<?> display = Class.forName(MENU_DISPLAY_CLASS, true, loader);
            if (!(Boolean) display.getMethod("isEnabled").invoke(null)) return;

            handlerField.setAccessible(true);
            Object handler = handlerField.get(screen);
            if (handler == null) {
                handler = handlerField.getType().getMethod("forMainMenu", Screen.class).invoke(null, screen);
                handlerField.set(screen, handler);
            }
            handler.getClass().getMethod("initGui").invoke(handler);
        } catch (ReflectiveOperationException | RuntimeException e) {
            logFailure(e);
        }
    }

    public static void sync(Screen screen, Consumer<AbstractWidget> add) {
        List<AbstractWidget> proxies = new ArrayList<>();
        for (GuiEventListener child : screen.children()) {
            if (child instanceof AbstractWidget widget && isA(widget.getClass(), PROXY_CLASS)) proxies.add(widget);
        }

        if (!proxies.isEmpty() && !loggedFound) {
            loggedFound = true;
            Constants.LOG.info("[GC] Essential menu: tucking {} Essential buttons away on {}", proxies.size(),
                    screen.getClass().getSimpleName());
        }

        if (screen instanceof TitleScreen) {
            syncTitle(proxies);
            return;
        }

        State state = STATES.get(screen);
        if (state != null && state.toggle != null && !screen.children().contains(state.toggle)) {
            STATES.remove(screen);
            state = null;
        }
        if (proxies.isEmpty() && state == null) return;
        if (state == null) {
            state = new State();
            STATES.put(screen, state);
        }

        for (AbstractWidget proxy : proxies) {
            tuck(proxy);
            if (!state.handled.add(proxy) || !isA(proxy.getClass(), MENU_PROXY_CLASS)) continue;

            String essentialId = essentialId(proxy);
            if (essentialId == null) continue;
            String id = baseId(essentialId);
            List<AbstractWidget> group = state.targets.computeIfAbsent(id, key -> new ArrayList<>());
            group.add(proxy);
            if (state.entries.containsKey(id)) continue;

            State owner = state;
            GCButton entry = new GCButton(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, label(id), button -> {
                owner.open = false;
                clickFirst(group);
            });
            entry.visible = false;
            state.entries.put(id, entry);
            add.accept(entry);
        }

        if (state.toggle == null && !state.entries.isEmpty()) {
            State owner = state;
            state.toggle = new GCButton(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT,
                    Component.translatableWithFallback("menu.gallifreyan_chronicles.essential", "Essential"),
                    button -> owner.open = !owner.open);
            add.accept(state.toggle);
        }

        layout(screen, state);
    }

    private static void syncTitle(List<AbstractWidget> proxies) {
        List<String> ids = new ArrayList<>();
        List<AbstractWidget> pending = new ArrayList<>();
        for (AbstractWidget proxy : proxies) {
            tuck(proxy);
            if (!isA(proxy.getClass(), MENU_PROXY_CLASS)) continue;
            String essentialId = essentialId(proxy);
            if (essentialId == null) continue;
            String id = baseId(essentialId);
            if (id.equals(pendingId)) pending.add(proxy);
            if (!ids.contains(id)) ids.add(id);
        }
        ids.sort((a, b) -> Integer.compare(orderIndex(a), orderIndex(b)));
        if (!ids.isEmpty()) titleIds = List.copyOf(ids);

        if (pendingId == null) return;
        if (clickFirst(pending)) {
            pendingId = null;
        } else if (++pendingFrames > PENDING_TIMEOUT_FRAMES) {
            pendingId = null;
        }
    }

    private static void layout(Screen screen, State state) {
        if (state.toggle == null) return;
        int x = screen.width - MARGIN - BUTTON_WIDTH;
        state.toggle.setPosition(x, MARGIN);

        List<Map.Entry<String, GCButton>> sorted = new ArrayList<>(state.entries.entrySet());
        sorted.sort((a, b) -> Integer.compare(orderIndex(a.getKey()), orderIndex(b.getKey())));
        int y = MARGIN + STEP;
        for (Map.Entry<String, GCButton> entry : sorted) {
            GCButton button = entry.getValue();
            button.setPosition(x, y);
            button.visible = state.open;
            y += STEP;
        }
    }

    private static void tuck(AbstractWidget proxy) {
        proxy.setPosition(OFFSCREEN, OFFSCREEN);
        proxy.visible = false;
        proxy.active = false;
    }

    private static int orderIndex(String id) {
        int index = ORDER.indexOf(baseId(id));
        return index < 0 ? ORDER.size() : index;
    }

    private static boolean isA(Class<?> type, String className) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            if (current.getName().equals(className)) return true;
        }
        return false;
    }

    private static String essentialId(AbstractWidget proxy) {
        try {
            Object id = proxy.getClass().getMethod("getEssentialId").invoke(proxy);
            return id instanceof String string ? string : null;
        } catch (ReflectiveOperationException | RuntimeException e) {
            logFailure(e);
            return null;
        }
    }

    private static boolean clickFirst(List<AbstractWidget> proxies) {
        for (AbstractWidget proxy : proxies) {
            if (click(proxy)) return true;
        }
        return false;
    }

    private static boolean click(AbstractWidget proxy) {
        try {
            Object component = proxy.getClass().getMethod("getEssentialComponent").invoke(proxy);
            if (component == null) return false;
            for (Method method : proxy.getClass().getMethods()) {
                if (method.getName().equals("click") && method.getParameterCount() == 1
                        && method.getParameterTypes()[0].isInstance(component)) {
                    method.invoke(proxy, component);
                    return true;
                }
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            logFailure(e);
        }
        return false;
    }

    private static void logFailure(Exception e) {
        if (loggedFailure) return;
        loggedFailure = true;
        Constants.LOG.warn("[GC] Essential menu compatibility failed", e);
    }
}
