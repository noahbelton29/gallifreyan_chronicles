package com.noahtnt2009.gallifreyan_chronicles.tardis.console.control;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.animation.ConsoleAnimatable;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlSpec;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControlRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConsoleControlManager {
    private final ConsoleControlHost host;
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

        Vec3 worldPos = Vec3.atCenterOf(host.getHostPos()).add(spec.offset());
        TardisControlEntity entity = TardisControlEntity.create(serverLevel, host.getHostPos(), spec.id(), worldPos);
        entity.setControlSize(spec.width(), spec.height(), spec.depth());
        serverLevel.addFreshEntity(entity);
        controlEntities.add(entity);

        if (!controlStates.contains(spec.id())) {
            setControlState(spec.id(), false);
        }

        return entity;
    }

    public void respawnControls() {
        if (!(host.getHostLevel() instanceof ServerLevel)) return;
        for (TardisControlEntity existing : controlEntities) {
            existing.discard();
        }
        controlEntities.clear();
        for (ControlSpec spec : controlSpecs) {
            spawnControl(spec);
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

    public boolean getControlState(String controlId) {
        return controlStates.getBooleanOr(controlId, false);
    }

    public void setControlState(String controlId, boolean state) {
        controlStates.putBoolean(controlId, state);
        host.markHostChanged();
    }

    public void triggerControl(TardisConsoleBlockEntity console, String id, boolean activated) {
        triggerControl(console, id, activated, null);
    }

    public void triggerControl(TardisConsoleBlockEntity console, String id, boolean activated, @Nullable Player triggeringPlayer) {
        Constants.LOG.info("Triggering control: {} with state: {}", id, activated);

        setControlState(id, activated);
        playControlAnimation(id, activated, triggeringPlayer);

        TardisControl control = TardisControlRegistry.get(id);
        control.onStateChanged(console, activated);
    }

    public void playControlAnimation(String controlId, boolean activated) {
        playControlAnimation(controlId, activated, null);
    }

    public void playControlAnimation(String controlId, boolean activated, @Nullable Player triggeringPlayer) {
        TardisControl control = TardisControlRegistry.get(controlId);
        String animationName = control.getAnimation(activated);
        Constants.LOG.info("Playing animation: {} for control: {} (activated: {})", animationName, controlId, activated);

        if (animationName != null) {
            animatable.triggerAnimation(controlId, animationName, triggeringPlayer);
            controlAnimations.put(controlId, animationName);
        }
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
