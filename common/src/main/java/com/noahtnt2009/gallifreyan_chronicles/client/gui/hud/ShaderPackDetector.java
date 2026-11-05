package com.noahtnt2009.gallifreyan_chronicles.client.gui.hud;

import java.lang.reflect.Method;

public final class ShaderPackDetector {
    private static boolean resolved;
    private static Object api;
    private static Method inUse;

    private ShaderPackDetector() {
    }

    public static boolean isShaderPackInUse() {
        if (!resolved) {
            resolved = true;
            try {
                Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                api = apiClass.getMethod("getInstance").invoke(null);
                inUse = apiClass.getMethod("isShaderPackInUse");
            } catch (ReflectiveOperationException | LinkageError e) {
                api = null;
                inUse = null;
            }
        }
        if (api == null) return false;
        try {
            return (boolean) inUse.invoke(api);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }
}
