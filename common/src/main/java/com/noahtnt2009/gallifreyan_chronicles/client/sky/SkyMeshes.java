package com.noahtnt2009.gallifreyan_chronicles.client.sky;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.noahtnt2009.gallifreyan_chronicles.sky.SkyStars;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.Random;

public final class SkyMeshes implements AutoCloseable {
    public static final int DISC_VERTICES = 10;
    private static final float DISC_RADIUS = 512.0F;
    private static final float STAR_DISTANCE = 100.0F;
    private static final int MAX_STARS = 40000;

    private final GpuBuffer topDisc;
    private final GpuBuffer bottomDisc;
    private final GpuBuffer quad;
    private final @Nullable GpuBuffer stars;
    private final int starIndexCount;

    private SkyMeshes(GpuBuffer topDisc, GpuBuffer bottomDisc, GpuBuffer quad, @Nullable GpuBuffer stars, int starIndexCount) {
        this.topDisc = topDisc;
        this.bottomDisc = bottomDisc;
        this.quad = quad;
        this.stars = stars;
        this.starIndexCount = starIndexCount;
    }

    public static SkyMeshes build(@Nullable SkyStars starSettings, float densityMultiplier) {
        GpuBuffer stars = null;
        int starIndexCount = 0;
        int starCount = starSettings == null ? 0 : Math.min(MAX_STARS, Math.round(starSettings.count() * densityMultiplier));
        if (starCount > 0) {
            try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(
                    DefaultVertexFormat.POSITION.getVertexSize() * starCount * 4)) {
                BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION);
                if (addStars(builder, starSettings, starCount)) {
                    try (MeshData mesh = builder.buildOrThrow()) {
                        starIndexCount = mesh.drawState().indexCount();
                        stars = RenderSystem.getDevice().createBuffer(() -> "GC sky stars", 40, mesh.vertexBuffer());
                    }
                }
            }
        }
        return new SkyMeshes(buildDisc("GC sky top disc", 16.0F), buildDisc("GC sky bottom disc", -16.0F), buildQuad(), stars, starIndexCount);
    }

    private static boolean addStars(BufferBuilder builder, SkyStars settings, int count) {
        Random random = new Random(settings.seed());
        float minSize = Math.min(settings.minSize(), settings.maxSize());
        float maxSize = Math.max(settings.minSize(), settings.maxSize());
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
            builder.addVertex(new Vector3f(size, -size, 0.0F).mul(rotation).add(center));
            builder.addVertex(new Vector3f(size, size, 0.0F).mul(rotation).add(center));
            builder.addVertex(new Vector3f(-size, size, 0.0F).mul(rotation).add(center));
            builder.addVertex(new Vector3f(-size, -size, 0.0F).mul(rotation).add(center));
            added++;
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

    @Override
    public void close() {
        topDisc.close();
        bottomDisc.close();
        quad.close();
        if (stars != null) stars.close();
    }
}
