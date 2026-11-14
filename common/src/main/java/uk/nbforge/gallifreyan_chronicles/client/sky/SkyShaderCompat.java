package uk.nbforge.gallifreyan_chronicles.client.sky;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.sky.data.DimensionSky;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.DimensionSkyRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class SkyShaderCompat {
    public enum Phase {
        NONE,
        SKY,
        STARS,
        CUSTOM_SKY
    }

    private static boolean resolved;
    private static boolean available;
    private static @Nullable Method getPipelineManager;
    private static @Nullable Method getPipelineNullable;
    private static @Nullable Method setPhase;
    private static @Nullable Class<?> phaseClass;
    private static @Nullable Constructor<?> namespacedId;
    private static @Nullable Method namespacedIdNamespace;
    private static @Nullable Method namespacedIdName;
    private static boolean pipelinesAssigned;
    private static boolean packCheckResolved;
    private static @Nullable Object irisApi;
    private static @Nullable Method isShaderPackInUse;
    private static @Nullable Method isRenderingShadowPass;

    private SkyShaderCompat() {
    }

    private static void resolve() {
        if (resolved) return;
        resolved = true;
        try {
            Class<?> iris = Class.forName("net.irisshaders.iris.Iris");
            Class<?> manager = Class.forName("net.irisshaders.iris.pipeline.PipelineManager");
            Class<?> pipeline = Class.forName("net.irisshaders.iris.pipeline.WorldRenderingPipeline");
            phaseClass = Class.forName("net.irisshaders.iris.pipeline.WorldRenderingPhase");
            Class<?> id = Class.forName("net.irisshaders.iris.shaderpack.materialmap.NamespacedId");
            getPipelineManager = iris.getMethod("getPipelineManager");
            getPipelineNullable = manager.getMethod("getPipelineNullable");
            setPhase = pipeline.getMethod("setPhase", phaseClass);
            namespacedId = id.getConstructor(String.class, String.class);
            namespacedIdNamespace = id.getMethod("getNamespace");
            namespacedIdName = id.getMethod("getName");
            available = true;
        } catch (ReflectiveOperationException | LinkageError e) {
            available = false;
        }
    }

    public static boolean isShaderPackInUse() {
        if (!packCheckResolved) {
            packCheckResolved = true;
            try {
                Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                irisApi = api.getMethod("getInstance").invoke(null);
                isShaderPackInUse = api.getMethod("isShaderPackInUse");
                /* looked up on its own so an older iris without it still works */
                try {
                    isRenderingShadowPass = api.getMethod("isRenderingShadowPass");
                } catch (NoSuchMethodException e) {
                    isRenderingShadowPass = null;
                }
            } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
                irisApi = null;
                isShaderPackInUse = null;
            }
        }
        if (irisApi == null || isShaderPackInUse == null) return false;
        try {
            return (Boolean) isShaderPackInUse.invoke(irisApi);
        } catch (ReflectiveOperationException | RuntimeException e) {
            isShaderPackInUse = null;
            return false;
        }
    }

    /* iris draws the world a second time from the sun's view to make shadows. true while it's doing that */
    public static boolean isRenderingShadowPass() {
        if (!isShaderPackInUse() || isRenderingShadowPass == null) return false;
        try {
            return (Boolean) isRenderingShadowPass.invoke(irisApi);
        } catch (ReflectiveOperationException | RuntimeException e) {
            isRenderingShadowPass = null;
            return false;
        }
    }

    public static void assignPipelines() {
        if (pipelinesAssigned) return;
        pipelinesAssigned = true;
        try {
            Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Class<?> program = Class.forName("net.irisshaders.iris.api.v0.IrisProgram");
            Object instance = api.getMethod("getInstance").invoke(null);
            Method assign = api.getMethod("assignPipeline", RenderPipeline.class, program);
            Object skyTextured = enumConstant(program, "SKY_TEXTURED");
            assign.invoke(instance, SkyPipelines.BODY, skyTextured);
        } catch (ClassNotFoundException e) {
            return;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
            Constants.LOG.warn("Could not register custom sky pipelines with Iris", e);
        }
    }

    public static void setPhase(Phase phase) {
        resolve();
        if (!available) return;
        try {
            Object manager = getPipelineManager.invoke(null);
            Object pipeline = getPipelineNullable.invoke(manager);
            if (pipeline != null) {
                setPhase.invoke(pipeline, enumConstant(phaseClass, phase.name()));
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            available = false;
        }
    }

    public static @Nullable Object remapDimension(@Nullable Object current) {
        resolve();
        if (!available || current == null) return null;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;

        Identifier dimension = level.dimension().identifier();
        DimensionSky sky = DimensionSkyRegistry.getForDimension(dimension);
        if (sky == null || sky.shaderDimension().isEmpty()) return null;

        try {
            assert namespacedIdNamespace != null;
            String namespace = (String) namespacedIdNamespace.invoke(current);
            assert namespacedIdName != null;
            String name = (String) namespacedIdName.invoke(current);
            if (dimension.getNamespace().equals(namespace) && dimension.getPath().equals(name)) return null;
            assert namespacedId != null;
            return namespacedId.newInstance("minecraft", sky.shaderDimension().get().getSerializedName());
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object enumConstant(Class<?> type, String name) {
        return Enum.valueOf((Class) type, name);
    }
}
