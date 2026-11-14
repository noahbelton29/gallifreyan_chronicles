package com.noahtnt2009.gallifreyan_chronicles.client.renderer.geo_layer;

import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import java.util.Map;
import java.util.HashMap;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.PerBoneRender;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.GCRenderTypes;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.TardisExteriorBlockRenderer;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.function.BiConsumer;

/* a glowing orb on the exterior's "lamp" bone, always facing you. only during a new who takeoff or landing,
   where it pulses in and out */
public class TardisLampOrbLayer<T extends GeoAnimatable, O, R extends GeoRenderState> extends GeoRenderLayer<T, O, R> {
    public static final String LAMP_BONE = "lamp";
    private static final Identifier TEXTURE = GCUtils.modIdentifier("textures/effect/lamp_orb.png");
    /* max block and sky light packed together */
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final float SIZE = 1.8f;
    private static final float TOWARDS_CAMERA = 1.2f;
    private static final int DRAW_AFTER_BOX = 10;

    public TardisLampOrbLayer(GeoRenderer<T, O, R> renderer) {
        super(renderer);
    }

    @Override
    public void addPerBoneRender(RenderPassInfo<R> info, @NonNull BiConsumer<GeoBone, PerBoneRender<R>> consumer) {
        if (!info.willRender()) return;
        R state = info.renderState();
        if (state.getOrDefaultGeckolibData(TardisExteriorBlockRenderer.LAMP_ORB, 0f) <= 0.01f) return;
        info.model().getBone(LAMP_BONE).ifPresent(bone -> consumer.accept(bone, this::submitOrb));
    }

    private void submitOrb(RenderPassInfo<R> info, GeoBone bone, SubmitNodeCollector tasks) {
        R state = info.renderState();
        Vector3f centre = info.poseStack().last().pose().transformPosition(new Vector3f());
        rememberLampSpot(state, centre);
        draw(state, centre, tasks);
    }

    /* the box is fully see-through so geckolib skips it. draw the orb where the lamp was last seen */
    public static void drawWithoutBox(GeoRenderState state, PoseStack poseStack, SubmitNodeCollector tasks) {
        if (state.getOrDefaultGeckolibData(TardisExteriorBlockRenderer.LAMP_ORB, 0f) <= 0.01f) return;
        Vector3f local = LAMP_SPOTS.get(state.getOrDefaultGeckolibData(TardisExteriorBlockRenderer.EXTERIOR_ID, ""));
        if (local == null) return;

        Vector3f centre = new Vector3f(local).rotate(yawRotation(state)).add(0.5f, 0f, 0.5f);
        poseStack.last().pose().transformPosition(centre);
        draw(state, centre, tasks);
    }

    /* where the lamp sits on each exterior, relative to the block and with the box's turn taken off */
    private static final Map<String, Vector3f> LAMP_SPOTS = new HashMap<>();

    private static void rememberLampSpot(GeoRenderState state, Vector3f centre) {
        Vector3f local = new Vector3f(centre).sub(TardisExteriorBlockRenderer.blockOrigin).sub(0.5f, 0f, 0.5f);
        local.rotate(yawRotation(state).conjugate());
        LAMP_SPOTS.put(state.getOrDefaultGeckolibData(TardisExteriorBlockRenderer.EXTERIOR_ID, ""), local);
    }

    private static Quaternionf yawRotation(GeoRenderState state) {
        float yaw = state.getOrDefaultGeckolibData(TardisExteriorBlockRenderer.YAW, 0f);
        return new Quaternionf().rotateY((float) Math.toRadians(TardisExterior.modelRotationDegrees(yaw)));
    }

    private static void draw(GeoRenderState state, Vector3f centre, SubmitNodeCollector tasks) {
        /* the pulse only changes its size, it's always fully bright. the box going see-through doesn't touch it */
        float pulse = Math.clamp(state.getOrDefaultGeckolibData(TardisExteriorBlockRenderer.LAMP_ORB, 0f), 0f, 1f);
        if (pulse <= 0.01f) return;

        /* pulled a little towards the camera so it sits on top of the lamp, then face the camera */
        float distance = centre.length();
        if (distance > TOWARDS_CAMERA) centre.mul(1f - TOWARDS_CAMERA / distance);
        float size = SIZE * pulse;

        PoseStack orb = new PoseStack();
        orb.translate(centre.x, centre.y, centre.z);
        orb.rotate(TardisExteriorBlockRenderer.cameraOrientation);
        orb.scale(size, size, size);

        int argb = 0xFFFFFFFF;
        /* drawn after the box so the box never shows through it */
        tasks.order(DRAW_AFTER_BOX).submitCustomGeometry(orb, GCRenderTypes.glowOrb(TEXTURE), (pose, buffer) -> {
            vertex(buffer, pose, -0.5f, -0.5f, 0f, 1f, argb);
            vertex(buffer, pose, 0.5f, -0.5f, 1f, 1f, argb);
            vertex(buffer, pose, 0.5f, 0.5f, 1f, 0f, argb);
            vertex(buffer, pose, -0.5f, 0.5f, 0f, 0f, argb);
        });
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose,
                               float x, float y, float u, float v, int argb) {
        buffer.addVertex(pose, x, y, 0f)
                .setColor(argb)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(pose, 0f, 0f, 1f);
    }
}
