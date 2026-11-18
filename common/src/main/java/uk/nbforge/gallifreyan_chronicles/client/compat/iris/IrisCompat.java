package uk.nbforge.gallifreyan_chronicles.client.compat.iris;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import org.jspecify.annotations.Nullable;

public final class IrisCompat {
    private IrisCompat() {}

    // looked up the first time either check runs, both null without iris
    private static final class Api {
        static final @Nullable MethodHandle SHADER_PACK_IN_USE = lookUp("isShaderPackInUse");
        static final @Nullable MethodHandle RENDERING_SHADOW_PASS = lookUp("isRenderingShadowPass");

        private static @Nullable MethodHandle lookUp(String method) {
            try {
                Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                Object instance = api.getMethod("getInstance").invoke(null);
                return MethodHandles.publicLookup()
                        .unreflect(api.getMethod(method))
                        .bindTo(instance);
            } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
                return null;
            }
        }
    }

    public static boolean shaderPackInUse() {
        return call(Api.SHADER_PACK_IN_USE);
    }

    /**
     * iris draws the world a second time from the sun's view for shadows, true while its doing that. shadow pass was
     * drawing the custom sky twice and it looked awful
     */
    public static boolean renderingShadowPass() {
        return shaderPackInUse() && call(Api.RENDERING_SHADOW_PASS);
    }

    private static boolean call(@Nullable MethodHandle check) {
        if (check == null) return false;
        try {
            return (boolean) check.invoke();
        } catch (Throwable e) {
            return false;
        }
    }
}
