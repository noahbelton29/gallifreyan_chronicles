package uk.nbforge.gallifreyan_chronicles.compat.flashback;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.jspecify.annotations.Nullable;
import qouteall.imm_ptl.core.ClientWorldLoader;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.teleportation.ClientTeleportationManager;

final class ReplayKeyframePortal {
    private static @Nullable Portal entry;
    private static @Nullable Vec3 lastPath;
    private static long lastApplyNanos;
    private static final double MAX_STEP = 16.0;

    private ReplayKeyframePortal() {}

    /** immersive portals moved the camera thru a door, free camera */
    static void onTeleport(Portal portal) {
        Minecraft mc = Minecraft.getInstance();
        ResourceKey<Level> server = ReplayVirtualDimension.serverDim();
        if (server == null || mc.player == null) return;
        if (mc.player.level().dimension().equals(server) && !portal.getDestDim().equals(server)) {
            entry = portal;
        } else if (portal.getDestDim().equals(server)) {
            entry = null;
        }
        lastPath = null;
    }

    /** keyframes are moving the camera right now so immersive portals mustnt try to as well */
    static boolean keyframesMovingCamera() {
        return System.nanoTime() - lastApplyNanos < 250_000_000L;
    }

    /**
     * a keyframe position in overworld coords is about to be applied, gives back the x, y, z, yaw and pitch to actually
     * use
     */
    static double[] apply(double x, double y, double z, float yaw, float pitch) {
        lastApplyNanos = System.nanoTime();
        Minecraft mc = Minecraft.getInstance();
        ResourceKey<Level> server = ReplayVirtualDimension.serverDim();
        if (server == null || mc.player == null) return new double[] {x, y, z, yaw, pitch};
        double eyeHeight = mc.player.getEyeHeight();
        Vec3 path = new Vec3(x, y + eyeHeight, z);

        if (entry != null
                && (entry.isRemoved()
                        || !entry.getDestDim().equals(mc.player.level().dimension()))) {
            entry = null;
        }
        if (entry == null && !mc.player.level().dimension().equals(server)) {
            entry = doorInto(server, mc.player.level().dimension(), path);
        }

        boolean jumped = lastPath == null || lastPath.distanceToSqr(path) > MAX_STEP * MAX_STEP;
        if (entry == null && !jumped) {
            Portal through = findCrossing(server, lastPath, path);
            if (through != null) {
                entry = through;
                ClientTeleportationManager.forceTeleportPlayer(through.getDestDim(), through.transformPoint(path));
            }
        } else if (entry != null) {
            boolean back = jumped ? entry.isInFrontOfPortal(path) : crossesBack(entry, lastPath, path);
            if (back) {
                entry = null;
                ClientTeleportationManager.forceTeleportPlayer(server, path);
            }
        }
        lastPath = path;

        if (entry == null) return new double[] {x, y, z, yaw, pitch};
        Vec3 eye = entry.transformPoint(path);
        Vec3 look = entry.transformLocalVecNonScale(Vec3.directionFromRotation(pitch, yaw));
        return new double[] {eye.x, eye.y - eyeHeight, eye.z, yawOf(look), pitchOf(look)};
    }

    static void onKeyframeCreated(Vector3d position, float[] yawPitch) {
        Minecraft mc = Minecraft.getInstance();
        if (entry == null || mc.player == null) return;
        if (!entry.getDestDim().equals(mc.player.level().dimension())) {
            entry = null;
            return;
        }
        double eyeHeight = mc.player.getEyeHeight();
        Vec3 eye = entry.inverseTransformPoint(new Vec3(position.x, position.y + eyeHeight, position.z));
        position.set(eye.x, eye.y - eyeHeight, eye.z);
        Vec3 look = entry.inverseTransformLocalVecNonScale(Vec3.directionFromRotation(yawPitch[1], yawPitch[0]));
        yawPitch[0] = yawOf(look);
        yawPitch[1] = pitchOf(look);
    }

    private static @Nullable Portal findCrossing(ResourceKey<Level> server, Vec3 a, Vec3 b) {
        ClientLevel level = ClientWorldLoader.getOptionalWorld(server);
        if (level == null) return null;
        for (Entity e : level.entitiesForRendering()) {
            if (!(e instanceof Portal portal) || portal.getAxisW() == null || !portal.isVisible()) continue;
            if (portal.getDestDim().equals(server)) continue;
            if (portal.isInFrontOfPortal(a) && !portal.isInFrontOfPortal(b) && throughRect(portal, a, b)) return portal;
        }
        return null;
    }

    private static @Nullable Portal doorInto(ResourceKey<Level> server, ResourceKey<Level> dest, Vec3 near) {
        ClientLevel level = ClientWorldLoader.getOptionalWorld(server);
        if (level == null) return null;
        Portal best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : level.entitiesForRendering()) {
            if (!(e instanceof Portal portal)
                    || portal.getAxisW() == null
                    || !portal.getDestDim().equals(dest)) continue;
            double dist = portal.getOriginPos().distanceToSqr(near);
            if (dist < bestDist) {
                bestDist = dist;
                best = portal;
            }
        }
        return best;
    }

    private static boolean crossesBack(Portal portal, Vec3 a, Vec3 b) {
        return !portal.isInFrontOfPortal(a) && portal.isInFrontOfPortal(b) && throughRect(portal, a, b);
    }

    private static boolean throughRect(Portal portal, Vec3 a, Vec3 b) {
        Vec3 origin = portal.getOriginPos();
        Vec3 normal = portal.getNormal();
        double da = a.subtract(origin).dot(normal);
        double db = b.subtract(origin).dot(normal);
        if ((da > 0) == (db > 0)) return false;
        Vec3 hit = a.add(b.subtract(a).scale(da / (da - db)));
        Vec3 rel = hit.subtract(origin);
        return Math.abs(rel.dot(portal.getAxisW())) <= portal.getWidth() / 2
                && Math.abs(rel.dot(portal.getAxisH())) <= portal.getHeight() / 2;
    }

    private static float yawOf(Vec3 look) {
        return (float) Math.toDegrees(Math.atan2(-look.x, look.z));
    }

    private static float pitchOf(Vec3 look) {
        return (float) Math.toDegrees(-Math.asin(Math.max(-1.0, Math.min(1.0, look.y))));
    }
}
