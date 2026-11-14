package uk.nbforge.gallifreyan_chronicles.client.sky;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import uk.nbforge.gallifreyan_chronicles.sky.data.SkyStars;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class SkyMeshes implements AutoCloseable {
    public static final int DISC_VERTICES = 10;
    private static final float DISC_RADIUS = 512.0F;
    private static final float STAR_DISTANCE = 100.0F;
    private static final int MAX_STARS = 40000;
    public static final int STAR_GROUPS = 16;
    public static final int BACKDROP_INDEX_COUNT = 36;
    private static final int SPHERE_STACKS = 24;
    private static final int SPHERE_SLICES = 48;
    public static final int SPHERE_INDEX_COUNT = SPHERE_STACKS * SPHERE_SLICES * 6;

    private final GpuBuffer topDisc;
    private final GpuBuffer bottomDisc;
    private final GpuBuffer quad;
    private final GpuBuffer backdrop;
    private final GpuBuffer sphere;
    private final @Nullable GpuBuffer stars;
    private final int starIndexCount;
    private final int[] groupFirstQuad;
    private final int[] groupQuadCount;

    private SkyMeshes(GpuBuffer topDisc, GpuBuffer bottomDisc, GpuBuffer quad, GpuBuffer backdrop, GpuBuffer sphere,
                      @Nullable GpuBuffer stars, int starIndexCount, int[] groupFirstQuad, int[] groupQuadCount) {
        this.topDisc = topDisc;
        this.bottomDisc = bottomDisc;
        this.quad = quad;
        this.backdrop = backdrop;
        this.sphere = sphere;
        this.stars = stars;
        this.starIndexCount = starIndexCount;
        this.groupFirstQuad = groupFirstQuad;
        this.groupQuadCount = groupQuadCount;
    }

    public static SkyMeshes build(@Nullable SkyStars starSettings, float densityMultiplier) {
        GpuBuffer stars = null;
        int starIndexCount = 0;
        int[] groupFirstQuad = new int[STAR_GROUPS];
        int[] groupQuadCount = new int[STAR_GROUPS];
        int starCount = starSettings == null ? 0 : Math.min(MAX_STARS, Math.round(starSettings.count() * densityMultiplier));
        if (starCount > 0) {
            try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(
                    DefaultVertexFormat.POSITION.getVertexSize() * starCount * 4)) {
                BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION);
                if (addStars(builder, starSettings, starCount, groupFirstQuad, groupQuadCount)) {
                    try (MeshData mesh = builder.buildOrThrow()) {
                        starIndexCount = mesh.drawState().indexCount();
                        stars = RenderSystem.getDevice().createBuffer(() -> "GC sky stars", 40, mesh.vertexBuffer());
                    }
                }
            }
        }
        return new SkyMeshes(buildDisc("GC sky top disc", 16.0F), buildDisc("GC sky bottom disc", -16.0F), buildQuad(),
                buildBackdrop(), buildSphere(), stars, starIndexCount, groupFirstQuad, groupQuadCount);
    }

    private static boolean addStars(BufferBuilder builder, SkyStars settings, int count,
                                    int[] groupFirstQuad, int[] groupQuadCount) {
        Random random = new Random(settings.seed());
        float minSize = Math.min(settings.minSize(), settings.maxSize());
        float maxSize = Math.max(settings.minSize(), settings.maxSize());
        List<List<Vector3f[]>> groups = new ArrayList<>(STAR_GROUPS);
        for (int i = 0; i < STAR_GROUPS; i++) groups.add(new ArrayList<>());
        int added = 0;
        int attempts = 0;
        while (added < count && attempts < count * 8) {
            attempts++;
            float x = random.nextFloat() * 2.0F - 1.0F;
            float y = random.nextFloat() * 2.0F - 1.0F;
            float z = random.nextFloat() * 2.0F - 1.0F;
            float lengthSq = Mth.lengthSquared(x, y, z);
            if (lengthSq <= 0.01F || lengthSq >= 1.0F) continue;

            float size = minSize + random.nextFloat() * (maxSize - minSize);

            Vector3f center = new Vector3f(x, y, z).normalize(STAR_DISTANCE);
            float spin = (float) (random.nextDouble() * Math.PI * 2.0);
            Matrix3f rotation = new Matrix3f()
                    .rotateTowards(new Vector3f(center).negate(), new Vector3f(0.0F, 1.0F, 0.0F))
                    .rotateZ(-spin);
            groups.get(random.nextInt(STAR_GROUPS)).add(new Vector3f[]{
                    new Vector3f(size, -size, 0.0F).mul(rotation).add(center),
                    new Vector3f(size, size, 0.0F).mul(rotation).add(center),
                    new Vector3f(-size, size, 0.0F).mul(rotation).add(center),
                    new Vector3f(-size, -size, 0.0F).mul(rotation).add(center)
            });
            added++;
        }

        int quad = 0;
        for (int group = 0; group < STAR_GROUPS; group++) {
            groupFirstQuad[group] = quad;
            groupQuadCount[group] = groups.get(group).size();
            for (Vector3f[] corners : groups.get(group)) {
                for (Vector3f corner : corners) builder.addVertex(corner);
            }
            quad += groupQuadCount[group];
        }
        return added > 0;
    }

    private static GpuBuffer buildDisc(String name, float y) {
        try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(DISC_VERTICES * DefaultVertexFormat.POSITION.getVertexSize())) {
            BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
            float x = Math.signum(y) * DISC_RADIUS;
            builder.addVertex(0.0F, y, 0.0F);
            for (int degrees = -180; degrees <= 180; degrees += 45) {
                float radians = (float) Math.toRadians(degrees);
                builder.addVertex(x * Mth.cos(radians), y, DISC_RADIUS * Mth.sin(radians));
            }
            try (MeshData mesh = builder.buildOrThrow()) {
                return RenderSystem.getDevice().createBuffer(() -> name, 32, mesh.vertexBuffer());
            }
        }
    }

    private static GpuBuffer buildQuad() {
        try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(4 * DefaultVertexFormat.POSITION_TEX.getVertexSize())) {
            BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.addVertex(-1.0F, 0.0F, -1.0F).setUv(0.0F, 1.0F);
            builder.addVertex(1.0F, 0.0F, -1.0F).setUv(1.0F, 1.0F);
            builder.addVertex(1.0F, 0.0F, 1.0F).setUv(1.0F, 0.0F);
            builder.addVertex(-1.0F, 0.0F, 1.0F).setUv(0.0F, 0.0F);
            try (MeshData mesh = builder.buildOrThrow()) {
                return RenderSystem.getDevice().createBuffer(() -> "GC sky body quad", 32, mesh.vertexBuffer());
            }
        }
    }

    private static GpuBuffer buildBackdrop() {
        float[][] faces = {
                {-1, -1, -1, 1, -1, -1, 1, 1, -1, -1, 1, -1},
                {-1, -1, 1, -1, 1, 1, 1, 1, 1, 1, -1, 1},
                {-1, -1, -1, -1, 1, -1, -1, 1, 1, -1, -1, 1},
                {1, -1, -1, 1, -1, 1, 1, 1, 1, 1, 1, -1},
                {-1, 1, -1, 1, 1, -1, 1, 1, 1, -1, 1, 1},
                {-1, -1, -1, -1, -1, 1, 1, -1, 1, 1, -1, -1}
        };
        try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(BACKDROP_INDEX_COUNT / 6 * 4 * DefaultVertexFormat.POSITION.getVertexSize())) {
            BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION);
            for (float[] face : faces) {
                for (int corner = 0; corner < 4; corner++) {
                    builder.addVertex(face[corner * 3], face[corner * 3 + 1], face[corner * 3 + 2]);
                }
            }
            try (MeshData mesh = builder.buildOrThrow()) {
                return RenderSystem.getDevice().createBuffer(() -> "GC sky overlay backdrop", 32, mesh.vertexBuffer());
            }
        }
    }

    private static GpuBuffer buildSphere() {
        try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(
                SPHERE_STACKS * SPHERE_SLICES * 4 * DefaultVertexFormat.POSITION.getVertexSize())) {
            BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION);
            for (int stack = 0; stack < SPHERE_STACKS; stack++) {
                float lat0 = Mth.PI * stack / SPHERE_STACKS - Mth.HALF_PI;
                float lat1 = Mth.PI * (stack + 1) / SPHERE_STACKS - Mth.HALF_PI;
                for (int slice = 0; slice < SPHERE_SLICES; slice++) {
                    float lon0 = Mth.TWO_PI * slice / SPHERE_SLICES;
                    float lon1 = Mth.TWO_PI * (slice + 1) / SPHERE_SLICES;
                    addSpherePoint(builder, lat0, lon0);
                    addSpherePoint(builder, lat0, lon1);
                    addSpherePoint(builder, lat1, lon1);
                    addSpherePoint(builder, lat1, lon0);
                }
            }
            try (MeshData mesh = builder.buildOrThrow()) {
                return RenderSystem.getDevice().createBuffer(() -> "GC sky overlay fog sphere", 32, mesh.vertexBuffer());
            }
        }
    }

    private static void addSpherePoint(BufferBuilder builder, float latitude, float longitude) {
        float ring = Mth.cos(latitude);
        builder.addVertex(ring * Mth.cos(longitude), Mth.sin(latitude), ring * Mth.sin(longitude));
    }

    public GpuBuffer sphere() {
        return sphere;
    }

    public GpuBuffer backdrop() {
        return backdrop;
    }

    public GpuBuffer topDisc() {
        return topDisc;
    }

    public GpuBuffer bottomDisc() {
        return bottomDisc;
    }

    public GpuBuffer quad() {
        return quad;
    }

    public @Nullable GpuBuffer stars() {
        return stars;
    }

    public int starIndexCount() {
        return starIndexCount;
    }

    public int groupFirstQuad(int group) {
        return groupFirstQuad[group];
    }

    public int groupQuadCount(int group) {
        return groupQuadCount[group];
    }

    @Override
    public void close() {
        topDisc.close();
        bottomDisc.close();
        quad.close();
        backdrop.close();
        sphere.close();
        if (stars != null) stars.close();
    }
}
