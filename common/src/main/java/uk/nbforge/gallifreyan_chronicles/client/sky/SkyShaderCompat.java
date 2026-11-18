package uk.nbforge.gallifreyan_chronicles.client.sky;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.DimensionSkyRegistry;
import uk.nbforge.gallifreyan_chronicles.sky.data.DimensionSky;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;

public final class SkyShaderCompat {
    public enum Phase {
        NONE,
        SKY,
        STARS,
        CUSTOM_SKY
    }

    private record Hooks(
            Method getPipelineManager,
            Method getPipelineNullable,
            Method setPhase,
            Class<?> phaseClass,
            Constructor<?> namespacedId,
            Method namespacedIdNamespace,
            Method namespacedIdName) {}

    private static boolean resolved;
    private static @Nullable Hooks hooks;
    private static boolean pipelinesAssigned;

    private SkyShaderCompat() {}

    private static @Nullable Hooks hooks() {
        if (!resolved) {
            resolved = true;
            hooks = lookUpHooks();
        }
        return hooks;
    }

    private static @Nullable Hooks lookUpHooks() {
        try {
            Class<?> iris = Class.forName("net.irisshaders.iris.Iris");
            Class<?> manager = Class.forName("net.irisshaders.iris.pipeline.PipelineManager");
            Class<?> pipeline = Class.forName("net.irisshaders.iris.pipeline.WorldRenderingPipeline");
            Class<?> phase = Class.forName("net.irisshaders.iris.pipeline.WorldRenderingPhase");
            Class<?> id = Class.forName("net.irisshaders.iris.shaderpack.materialmap.NamespacedId");
            return new Hooks(
                    iris.getMethod("getPipelineManager"),
                    manager.getMethod("getPipelineNullable"),
                    pipeline.getMethod("setPhase", phase),
                    phase,
                    id.getConstructor(String.class, String.class),
                    id.getMethod("getNamespace"),
                    id.getMethod("getName"));
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    public static void assignPipelines() {
        if (pipelinesAssigned) return;
        pipelinesAssigned = true;
        Class<?> api;
        Class<?> program;
        try {
            api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            program = Class.forName("net.irisshaders.iris.api.v0.IrisProgram");
        } catch (ClassNotFoundException e) {
            // no iris, nothing to assign
            return;
        }
        try {
            Object instance = api.getMethod("getInstance").invoke(null);
            // a MethodHandle cuz IrisProgram isnt on the compile classpath so a plain invoke cant be typed properly
            MethodHandle assign = MethodHandles.publicLookup()
                    .unreflect(api.getMethod("assignPipeline", RenderPipeline.class, program));
            assign.invoke(instance, SkyPipelines.BODY, enumConstant(program, "SKY_TEXTURED"));
        } catch (Throwable e) {
            GCLog.CLIENT.warn("Couldn't register custom sky pipelines with Iris", e);
        }
    }

    public static void setPhase(Phase phase) {
        Hooks iris = hooks();
        if (iris == null) return;
        try {
            Object manager = iris.getPipelineManager().invoke(null);
            Object pipeline = iris.getPipelineNullable().invoke(manager);
            if (pipeline != null) {
                iris.setPhase().invoke(pipeline, enumConstant(iris.phaseClass(), phase.name()));
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            hooks = null;
        }
    }

    public static @Nullable Object remapDimension(@Nullable Object current) {
        Hooks iris = hooks();
        if (iris == null || current == null) return null;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;

        Identifier dimension = level.dimension().identifier();
        DimensionSky sky = DimensionSkyRegistry.getForDimension(dimension);
        if (sky == null || sky.shaderDimension().isEmpty()) return null;

        try {
            String namespace = (String) iris.namespacedIdNamespace().invoke(current);
            String name = (String) iris.namespacedIdName().invoke(current);
            if (dimension.getNamespace().equals(namespace)
                    && dimension.getPath().equals(name)) return null;
            return iris.namespacedId()
                    .newInstance("minecraft", sky.shaderDimension().get().getSerializedName());
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object enumConstant(Class<?> type, String name) {
        return Enum.valueOf((Class) type, name);
    }
}
