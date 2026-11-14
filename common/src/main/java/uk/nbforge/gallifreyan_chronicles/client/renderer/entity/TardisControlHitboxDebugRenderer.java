package uk.nbforge.gallifreyan_chronicles.client.renderer.entity;

import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisKeyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.awt.*;

public class TardisControlHitboxDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;

    public TardisControlHitboxDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void emitGizmos(double camX, double camY, double camZ, @NonNull DebugValueAccess debugValues,
                           @NonNull Frustum frustum, float partialTicks) {
        if (this.minecraft.level == null) {
            return;
        }
        if (!this.minecraft.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)) {
            return;
        }

        for (Entity entity : this.minecraft.level.entitiesForRendering()) {
            if (entity instanceof TardisControlEntity control) {
                drawHitbox(entity, frustum, partialTicks, colorFor(control.getControlId()));
            } else if (entity instanceof TardisKeyEntity) {
                drawHitbox(entity, frustum, partialTicks, colorFor("tardis_key"));
            }
        }
    }

    private static void drawHitbox(Entity entity, Frustum frustum, float partialTicks, int color) {
        if (!frustum.isVisible(entity.getBoundingBox())) return;

        Vec3 currentPosition = entity.getPosition(partialTicks);
        Vec3 offset = currentPosition.subtract(entity.position());
        Gizmos.cuboid(entity.getBoundingBox().move(offset), GizmoStyle.stroke(color));
        Gizmos.point(currentPosition, color, 2.0F);
    }

    private static int colorFor(String controlId) {
        if (controlId == null || controlId.isEmpty()) {
            return -1;
        }
        float hue = (Math.abs(controlId.hashCode()) % 360) / 360.0f;
        int packed = Color.HSBtoRGB(hue, 0.85f, 1.0f);
        return 0xFF000000 | (packed & 0x00FFFFFF);
    }
}