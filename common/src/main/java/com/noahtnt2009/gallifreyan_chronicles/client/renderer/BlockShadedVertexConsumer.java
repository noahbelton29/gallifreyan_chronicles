package com.noahtnt2009.gallifreyan_chronicles.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.ARGB;

public final class BlockShadedVertexConsumer implements VertexConsumer {
    private static final float SHADE_UP = 1.0F;
    private static final float SHADE_DOWN = 0.5F;
    private static final float SHADE_NORTH_SOUTH = 0.8F;
    private static final float SHADE_EAST_WEST = 0.6F;

    private final VertexConsumer delegate;

    public BlockShadedVertexConsumer(VertexConsumer delegate) {
        this.delegate = delegate;
    }

    public static float shadeFor(float normalX, float normalY, float normalZ) {
        float absX = Math.abs(normalX);
        float absY = Math.abs(normalY);
        float absZ = Math.abs(normalZ);
        if (absY >= absX && absY >= absZ) {
            return normalY >= 0.0F ? SHADE_UP : SHADE_DOWN;
        }
        return absX > absZ ? SHADE_EAST_WEST : SHADE_NORTH_SOUTH;
    }

    @Override
    public void addVertex(float x, float y, float z, int color, float u, float v, int overlay, int light,
                          float normalX, float normalY, float normalZ) {
        float shade = shadeFor(normalX, normalY, normalZ);
        int shaded = ARGB.color(ARGB.alpha(color),
                Math.round(ARGB.red(color) * shade),
                Math.round(ARGB.green(color) * shade),
                Math.round(ARGB.blue(color) * shade));
        delegate.addVertex(x, y, z, shaded, u, v, overlay, light, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        delegate.setColor(red, green, blue, alpha);
        return this;
    }

    @Override
    public VertexConsumer setColor(int color) {
        delegate.setColor(color);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        delegate.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv3(float u, float v) {
        delegate.setUv3(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
        delegate.setNormal(normalX, normalY, normalZ);
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        delegate.setLineWidth(width);
        return this;
    }
}
