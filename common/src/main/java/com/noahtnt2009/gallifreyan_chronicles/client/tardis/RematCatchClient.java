package com.noahtnt2009.gallifreyan_chronicles.client.tardis;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.noahtnt2009.gallifreyan_chronicles.network.RematCatchPayload;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade.FadeType;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade.FadeTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/* client side of a tardis materialising around a player.
   - other players inside see that player fade in like the tardis
   - that player keeps the floor, and the rest of the interior fades in around them */
public final class RematCatchClient {
    private static final int HIDE_FLAGS = Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;
    /* give up if the interior never loads */
    private static final int CAPTURE_TIMEOUT_TICKS = 100;

    private record Fade(String fadeId, long start, int duration) {
        float opacity(long now, float partialTick) {
            float elapsed = (now - start) + partialTick;
            float progress = Math.clamp(elapsed / duration, 0.0f, 1.0f);
            if (progress >= 1.0f) return 1.0f;
            FadeType type = FadeTypes.get(fadeId);
            return Math.clamp(type.opacity(1.0f - progress, elapsed), 0.0f, 1.0f);
        }

        boolean done(long now) {
            return now - start >= duration;
        }
    }

    /* one block face drawn see-through, light and tint taken before the block was hidden */
    private record GhostQuad(float x, float y, float z, BakedQuad quad, int[] colors, int[] lights) {
    }

    private static final Map<UUID, Fade> PLAYERS = new ConcurrentHashMap<>();

    private static @Nullable Fade self;
    private static @Nullable RematCatchPayload pending;
    private static int pendingTicks;
    private static @Nullable ResourceKey<Level> selfDim;
    private static final List<GhostQuad> QUADS = new ArrayList<>();
    private static final Map<BlockPos, BlockState> HIDDEN = new ConcurrentHashMap<>();
    private static final QuadInstance DRAW = new QuadInstance();

    private RematCatchClient() {
    }

    public static void start(RematCatchPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Fade fade = new Fade(payload.fadeType(), mc.level.getGameTime() - payload.elapsedTicks(), payload.durationTicks());
        if (!payload.self()) {
            PLAYERS.put(payload.player(), fade);
            return;
        }
        restore();
        self = fade;
        /* the teleport and the interior chunks may still be on their way */
        pending = payload;
        pendingTicks = 0;
    }

    /* how see-through another player is right now, 1 = normal */
    public static float playerAlpha(UUID player, float partialTick) {
        Fade fade = PLAYERS.get(player);
        Minecraft mc = Minecraft.getInstance();
        if (fade == null || mc.level == null) return 1.0f;
        if (mc.player != null && mc.player.getUUID().equals(player)) return 1.0f;
        return fade.opacity(mc.level.getGameTime(), partialTick);
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            PLAYERS.clear();
            self = null;
            pending = null;
            QUADS.clear();
            HIDDEN.clear();
            return;
        }
        long now = level.getGameTime();
        PLAYERS.values().removeIf(fade -> fade.done(now));

        if (pending != null) {
            if (++pendingTicks > CAPTURE_TIMEOUT_TICKS || self == null || self.done(now)) {
                pending = null;
            } else if (captureReady(level, pending)) {
                capture(level, pending);
                pending = null;
            }
        }
        if (self != null && pending == null && self.done(now)) restore();
    }

    private static boolean captureReady(ClientLevel level, RematCatchPayload payload) {
        if (Minecraft.getInstance().player == null) return false;
        BlockPos at = Minecraft.getInstance().player.blockPosition();
        if (at.getY() < payload.boxMin().getY() - 2 || at.getY() > payload.boxMax().getY() + 2) return false;
        return level.hasChunkAt(payload.boxMin()) && level.hasChunkAt(payload.boxMax());
    }

    /* tesselate everything above the floor first (so faces between blocks stay hidden),
       then hide those blocks so only our fading copy shows */
    private static void capture(ClientLevel level, RematCatchPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        ModelBlockRenderer renderer = new ModelBlockRenderer(mc.options.ambientOcclusion().get(), true, mc.getBlockColors());
        List<BlockPos> hide = new ArrayList<>();
        QUADS.clear();
        for (BlockPos pos : BlockPos.betweenClosed(payload.boxMin(), payload.boxMax())) {
            if (pos.getY() < payload.floorY()) continue;
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || !state.getFluidState().isEmpty()) continue;
            BlockPos fixed = pos.immutable();
            if (state.getRenderShape() == RenderShape.MODEL) {
                BlockStateModel model = mc.getModelManager().getBlockStateModelSet().get(state);
                renderer.tesselateBlock((x, y, z, quad, instance) -> {
                    int[] colors = new int[4];
                    int[] lights = new int[4];
                    for (int v = 0; v < 4; v++) {
                        colors[v] = instance.getColor(v);
                        lights[v] = instance.getLightCoords(v);
                    }
                    QUADS.add(new GhostQuad(x, y, z, quad, colors, lights));
                }, fixed.getX(), fixed.getY(), fixed.getZ(), level, fixed, state, model, state.getSeed(fixed));
            }
            hide.add(fixed);
        }
        BlockState air = Blocks.AIR.defaultBlockState();
        for (BlockPos pos : hide) {
            HIDDEN.put(pos, level.getBlockState(pos));
            level.setBlock(pos, air, HIDE_FLAGS);
        }
        selfDim = level.dimension();
    }

    /* put the hidden blocks back. the server also sends the real chunks again right after */
    private static void restore() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && level.dimension().equals(selfDim)) {
            for (Map.Entry<BlockPos, BlockState> entry : HIDDEN.entrySet()) {
                if (level.getBlockState(entry.getKey()).isAir()) {
                    level.setBlock(entry.getKey(), entry.getValue(), HIDE_FLAGS);
                }
            }
        }
        HIDDEN.clear();
        QUADS.clear();
        self = null;
        selfDim = null;
    }

    /* same depth mask trick and tint as the fading exterior */
    public static final int MASK_ORDER = -1;
    public static final float MASK_PUSH = 1.00005f;

    /* the exterior's fade colour: a little blue while see-through, alpha = opacity */
    public static int fadeTint(float opacity) {
        float faded = 1.0f - opacity;
        int alpha = Math.round(opacity * 255.0f);
        int red = Math.round((1.0f - 0.25f * faded) * 255.0f);
        int green = Math.round((1.0f - 0.08f * faded) * 255.0f);
        return (alpha << 24) | (red << 16) | (green << 8) | 0xFF;
    }

    /* true while the picked player's interior is hidden for the fade */
    public static boolean hidesBlocksIn(@Nullable Level level) {
        return level != null && !HIDDEN.isEmpty() && level.dimension().equals(selfDim);
    }

    /* every frame, from the level renderer */
    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, Vec3 camera, @Nullable Level drawing) {
        Fade fade = self;
        Minecraft mc = Minecraft.getInstance();
        if (fade == null || QUADS.isEmpty() || mc.level == null || drawing == null) return;
        if (!drawing.dimension().equals(selfDim)) return;
        float alpha = fade.opacity(mc.level.getGameTime(), mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));
        if (alpha <= 0.003f) return;
        int tint = fadeTint(alpha);
        double cx = camera.x;
        double cy = camera.y;
        double cz = camera.z;

        /* depth first (pushed a hair back), so only the front faces show, like the exterior */
        collector.order(MASK_ORDER).submitCustomGeometry(poseStack, RenderTypes.waterMask(), (pose, buffer) -> {
            for (GhostQuad ghost : QUADS) {
                float ox = (float) (ghost.x() - cx);
                float oy = (float) (ghost.y() - cy);
                float oz = (float) (ghost.z() - cz);
                for (int v = 0; v < 4; v++) {
                    Vector3fc p = ghost.quad().position(v);
                    buffer.addVertex((ox + p.x()) * MASK_PUSH, (oy + p.y()) * MASK_PUSH, (oz + p.z()) * MASK_PUSH);
                }
            }
        });
        collector.submitCustomGeometry(poseStack, RenderTypes.translucentMovingBlock(), (pose, buffer) -> {
            for (GhostQuad ghost : QUADS) {
                for (int v = 0; v < 4; v++) {
                    DRAW.setColor(v, ARGB.multiply(ghost.colors()[v], tint));
                    DRAW.setLightCoords(v, ghost.lights()[v]);
                }
                buffer.putBlockBakedQuad((float) (ghost.x() - cx), (float) (ghost.y() - cy), (float) (ghost.z() - cz),
                        ghost.quad(), DRAW);
            }
        });
    }
}
