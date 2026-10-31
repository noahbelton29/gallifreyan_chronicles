package com.noahtnt2009.gallifreyan_chronicles.compat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public final class IrisCompat {
    private IrisCompat() {
    }

    private static final class Holder {
        static final MethodHandle SHADER_PACK_IN_USE = lookup();

        private static MethodHandle lookup() {
            try {
                Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                Object instance = api.getMethod("getInstance").invoke(null);
                return MethodHandles.lookup()
                        .unreflect(api.getMethod("isShaderPackInUse"))
                        .bindTo(instance);
            } catch (Throwable ignored) {
                return null;
            }
        }
    }

    public static boolean shadersActive() {
        MethodHandle handle = Holder.SHADER_PACK_IN_USE;
        if (handle == null) return false;
        try {
            return (boolean) handle.invoke();
        } catch (Throwable ignored) {
            return false;
        }
    }
}
