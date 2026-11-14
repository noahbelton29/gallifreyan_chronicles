package uk.nbforge.gallifreyan_chronicles.tardis.control;

import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.console.animation.ConsoleAnimatable;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisControlRegistry;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConsoleControlManager {
    private static final double STRAY_SEARCH_RADIUS = 4.0;

    private final ConsoleControlHost host;
    public static final String REVERSE_SUFFIX = "_reverse";

    private final ConsoleAnimatable animatable;

    private final transient List<TardisControlEntity> controlEntities = new ArrayList<>();
    private final List<ControlSpec> controlSpecs = new ArrayList<>();
    private final Map<String, String> controlAnimations = new ConcurrentHashMap<>();
    private final CompoundTag controlStates = new CompoundTag();

    public ConsoleControlManager(ConsoleControlHost host, ConsoleAnimatable animatable) {
        this.host = host;
        this.animatable = animatable;
    }

    public List<TardisControlEntity> getControlEntities() {
        return List.copyOf(controlEntities);
    }

    public TardisControlEntity addControl(String id, Vec3 offset, float width, float height, float depth) {
        controlSpecs.removeIf(spec -> spec.id().equals(id));
        ControlSpec controlSpec = new ControlSpec(id, offset, width, height, depth);
        controlSpecs.add(controlSpec);
        host.markHostChanged();
        return spawnControl(controlSpec);
    }

    public static Vec3 rotateOffset(Vec3 o, Direction facing) {
        return switch (facing) {
            case WEST -> new Vec3(o.z, o.y, -o.x);
            case SOUTH -> new Vec3(-o.x, o.y, -o.z);
            case EAST -> new Vec3(-o.z, o.y, o.x);
            default -> o;
        };
    }

    public static Vec3 flipOffset(Vec3 o, float height) {
        return new Vec3(-o.x, -o.y - height, o.z);
    }

    public static Vec3 unrotateOffset(Vec3 o, Direction facing) {
        return rotateOffset(o, switch (facing) {
            case WEST -> Direction.EAST;
            case EAST -> Direction.WEST;
            default -> facing;
        });
    }

    @Nullable
    private TardisControlEntity spawnControl(ControlSpec spec) {
        if (!(host.getHostLevel() instanceof ServerLevel serverLevel)) return null;

        controlEntities.removeIf(e -> {
            boolean stale = !e.isAlive() || e.getControlId().equals(spec.id());
            if (stale && e.isAlive()) {
                e.discard();
            }
            return stale;
        });

        Direction facing = host.getHostFacing();
        Vec3 localOffset = host.isHostUpsideDown() ? flipOffset(spec.offset(), spec.height()) : spec.offset();
        Vec3 worldPos = Vec3.atCenterOf(host.getHostPos()).add(rotateOffset(localOffset, facing));
        TardisControlEntity entity = TardisControlEntity.create(serverLevel, host.getHostPos(), spec.id(), worldPos);

        boolean quarterTurn = facing.getAxis() == Direction.Axis.X;
        entity.setControlSize(quarterTurn ? spec.depth() : spec.width(), spec.height(),
                quarterTurn ? spec.width() : spec.depth());
        serverLevel.addFreshEntity(entity);
        controlEntities.add(entity);

        if (!controlStates.contains(spec.id())) {
            setControlState(spec.id(), defaultState(spec.id()));
        }

        return entity;
    }

    public void respawnControls() {
        if (!(host.getHostLevel() instanceof ServerLevel serverLevel)) return;
        for (TardisControlEntity existing : controlEntities) {
            existing.discard();
        }
        controlEntities.clear();
        AABB area = new AABB(host.getHostPos()).inflate(STRAY_SEARCH_RADIUS);
        for (TardisControlEntity stray : serverLevel.getEntitiesOfClass(TardisControlEntity.class, area,
                entity -> host.getHostPos().equals(entity.getConsolePos()))) {
            stray.discard();
        }
        for (ControlSpec spec : controlSpecs) {
            spawnControl(spec);
        }
    }

    public void ensureControls() {
        if (!(host.getHostLevel() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.isPositionEntityTicking(host.getHostPos())) return;
        boolean broken = controlEntities.size() != controlSpecs.size();
        for (TardisControlEntity entity : controlEntities) {
            if (broken) break;
            broken = entity.isRemoved() || serverLevel.getEntity(entity.getUUID()) != entity;
        }
        if (broken) {
            respawnControls();
        }
    }

    public void removeControl(String id) {
        controlSpecs.removeIf(spec -> spec.id().equals(id));
        controlEntities.stream()
                .filter(e -> e.getControlId().equals(id))
                .forEach(TardisControlEntity::discard);
        controlEntities.removeIf(e -> e.getControlId().equals(id));
        controlAnimations.remove(id);
        controlStates.remove(id);
        host.markHostChanged();
    }

    public void discardAll() {
        for (TardisControlEntity entity : controlEntities) {
            entity.discard();
        }
        controlEntities.clear();
    }

    public static boolean defaultState(String controlId) {
        return GCTardisControls.POWER_LEVER.equals(controlId);
    }

    public boolean getControlState(String controlId) {
        return controlStates.getBooleanOr(controlId, defaultState(controlId));
    }

    public void setControlState(String controlId, boolean state) {
        controlStates.putBoolean(controlId, state);
        host.markHostChanged();
    }

    public void triggerControl(TardisConsoleBlockEntity console, String id, boolean activated) {
        triggerControl(console, id, activated, null);
    }

    public void triggerControl(TardisConsoleBlockEntity console, String id, boolean activated, @Nullable Player triggeringPlayer) {
        Constants.LOG.debug("Triggering control: {} with state: {}", id, activated);

        setControlState(id, activated);
        playControlAnimation(id, activated, triggeringPlayer);

        TardisControl control = TardisControlRegistry.get(id);
        control.onStateChanged(console, activated);
    }

    public void playControlAnimation(String controlId, boolean activated) {
        playControlAnimation(controlId, activated, null);
    }

    public void playControlAnimation(String controlId, boolean activated, @Nullable Player triggeringPlayer) {
        String animationName = host.resolveControlAnimation(controlId, activated);
        Constants.LOG.debug("Playing animation: {} for control: {} (activated: {})", animationName, controlId, activated);

        if (animationName != null) {
            animatable.triggerAnimation(controlId, animationName, triggeringPlayer);
            controlAnimations.put(controlId, animationName);
        }
    }

    public void playControlAnimationReversed(String controlId, @Nullable Player triggeringPlayer) {
        String animationName = host.resolveControlAnimation(controlId, true);
        if (animationName == null) return;
        animationName += REVERSE_SUFFIX;
        animatable.triggerAnimation(controlId, animationName, triggeringPlayer);
        controlAnimations.put(controlId, animationName);
    }

    public String getCurrentAnimation(String controlId) {
        return controlAnimations.get(controlId);
    }

    public List<ControlSpec> getControlSpecs() {
        return controlSpecs;
    }

    public void replaceControlSpecs(List<ControlSpec> specs) {
        controlSpecs.clear();
        controlSpecs.addAll(specs);
    }

    public CompoundTag getControlStates() {
        return controlStates;
    }

    public void mergeControlStates(CompoundTag tag) {
        controlStates.merge(tag);
    }
}