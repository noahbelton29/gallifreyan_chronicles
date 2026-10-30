package com.noahtnt2009.gallifreyan_chronicles.client.watermark;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class HudVisibility {
    private static final String[] FIELD_NAMES = {"hideGui", "guiHidden", "hudHidden", "hideHud", "hidden", "hiddenByUser"};
    private static final String[] METHOD_NAMES = {"isHidden", "isGuiHidden", "isHudHidden", "hideGui", "isHudVisible", "isVisible"};
    private static final String[] INVERTED = {"isHudVisible", "isVisible"};

    private static boolean resolved;
    private static BooleanSupplier hidden;

    private HudVisibility() {
    }

    public static boolean isHidden() {
        if (!resolved) {
            resolved = true;
            try {
                hidden = resolve();
            } catch (Throwable t) {
                Constants.LOG.error("[GC] Could not look for the HUD hidden flag", t);
            }
        }
        if (hidden == null) return false;
        try {
            return hidden.getAsBoolean();
        } catch (Throwable t) {
            return false;
        }
    }

    private static BooleanSupplier resolve() {
        Minecraft minecraft = Minecraft.getInstance();
        List<Object> roots = new ArrayList<>();
        roots.add(minecraft.gui);
        Object hud = minecraft.gui.hud;
        roots.add(hud);
        roots.add(minecraft.options);

        for (Object root : roots) {
            for (String name : FIELD_NAMES) {
                Field field = findField(root.getClass(), name);
                if (field != null && field.getType() == boolean.class) {
                    field.setAccessible(true);
                    Constants.LOG.info("[GC] HUD hidden flag found: {}.{}", root.getClass().getName(), name);
                    return () -> {
                        try {
                            return field.getBoolean(Modifier.isStatic(field.getModifiers()) ? null : root);
                        } catch (IllegalAccessException e) {
                            return false;
                        }
                    };
                }
            }
            for (String name : METHOD_NAMES) {
                Method method = findMethod(root.getClass(), name);
                if (method != null && method.getReturnType() == boolean.class) {
                    method.setAccessible(true);
                    boolean invert = false;
                    for (String inverted : INVERTED) invert |= inverted.equals(name);
                    final boolean flip = invert;
                    Constants.LOG.info("[GC] HUD hidden flag found: {}.{}()", root.getClass().getName(), name);
                    return () -> {
                        try {
                            return flip != (boolean) method.invoke(root);
                        } catch (ReflectiveOperationException e) {
                            return false;
                        }
                    };
                }
            }
        }

        for (Object root : roots) {
            StringBuilder sb = new StringBuilder();
            for (Class<?> c = root.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
                for (Field f : c.getDeclaredFields()) {
                    if (f.getType() == boolean.class) sb.append(" field:").append(f.getName());
                }
                for (Method m : c.getDeclaredMethods()) {
                    if (m.getParameterCount() == 0 && m.getReturnType() == boolean.class) {
                        sb.append(" method:").append(m.getName()).append("()");
                    }
                }
            }
            Constants.LOG.info("[GC] HUD flag candidates on {}:{}", root.getClass().getName(), sb);
        }
        return null;
    }

    private static Field findField(Class<?> type, String name) {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    private static Method findMethod(Class<?> type, String name) {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                return c.getDeclaredMethod(name);
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }
}
