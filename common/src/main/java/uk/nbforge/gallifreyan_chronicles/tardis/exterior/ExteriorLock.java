package uk.nbforge.gallifreyan_chronicles.tardis.exterior;

import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisKeyEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorSync;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.effect.ExteriorSounds;
import uk.nbforge.gallifreyan_chronicles.tardis.key.HeldKey;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.mood.TardisMoods;
import uk.nbforge.gallifreyan_chronicles.tardis.subsystem.TardisSubsystems;
import uk.nbforge.gallifreyan_chronicles.util.GameClockUtil;
import uk.nbforge.gallifreyan_chronicles.util.MessageUtil;
import uk.nbforge.gallifreyan_chronicles.util.SchedulerUtil;

public final class ExteriorLock {
    public static final String KEY_CONTROLLER = "key_controller";

    private static final long KEY_JAM_COOLDOWN_TICKS = 40L;
    private static final long KEY_TURN_COOLDOWN_TICKS = 20L;
    private static final long WRONG_KEY_LOCKOUT_TICKS = 20L * 60L;
    private static final int WRONG_KEY_JAM_DELAY_TICKS = 10;
    private static final int WRONG_KEY_RETURN_DELAY_TICKS = 30;
    private static final UUID UNLINKED_KEY = new UUID(0L, 0L);

    private final TardisExteriorBlockEntity exterior;
    private final HeldKey heldKey = new HeldKey();
    private final Map<UUID, Long> wrongKeyLockouts = new HashMap<>();
    private boolean locked = false;
    private long keyCooldownUntil = 0L;
    private long lastLockedFeedbackAt = 0L;
    private @Nullable TardisKeyEntity keyEntity = null;
    private volatile @Nullable Vec3 cachedKeyBoneWorldPos = null;

    public ExteriorLock(TardisExteriorBlockEntity exterior) {
        this.exterior = exterior;
    }

    private static UUID keyIdOf(ItemStack key) {
        UUID linked = key.get(GCDataComponents.TARDIS_ID);
        return linked != null ? linked : UNLINKED_KEY;
    }

    public boolean isLocked() {
        return locked;
    }

    public void toggleKeyLockAnimation(@Nullable Player player) {
        if (!hasKeyRendered()) {
            return;
        }
        Level level = exterior.getLevel();
        if (level != null && !level.isClientSide()) {
            long now = GameClockUtil.now(level);
            if (now < keyCooldownUntil) {
                return;
            }
            if (TardisSubsystems.LOCKING_MECHANISM.rollJam(
                    exterior.getSubsystemHealth(TardisSubsystems.LOCKING_MECHANISM), level.getRandom())) {
                keyCooldownUntil = now + KEY_JAM_COOLDOWN_TICKS;
                announceKeyJammed(player);
                return;
            }
            keyCooldownUntil = now + KEY_TURN_COOLDOWN_TICKS;
        }
        boolean nowLocked = !locked;
        String animName = nowLocked ? "key_lock" : "key_unlock";
        locked = nowLocked;
        exterior.triggerAnim(KEY_CONTROLLER, animName);
        ExteriorSounds.keyTurn(level, exterior.getBlockPos(), locked);
        exterior.sync();

        if (level instanceof ServerLevel serverLevel) {
            UUID id = exterior.getTardisId();
            if (id != null) {
                Tardis.get(serverLevel.getServer(), id)
                        .ifPresent(tardis -> DoorSync.onLockChanged(tardis, nowLocked, false));
            }
        }
    }

    public void setLockedFromConsole(boolean nowLocked) {
        if (locked == nowLocked) return;
        locked = nowLocked;
        Level level = exterior.getLevel();
        ExteriorSounds.keyTurn(level, exterior.getBlockPos(), locked);
        exterior.sync();

        if (level instanceof ServerLevel serverLevel) {
            UUID id = exterior.getTardisId();
            if (id != null) {
                Tardis.get(serverLevel.getServer(), id)
                        .ifPresent(tardis -> DoorSync.onLockChanged(tardis, nowLocked, true));
            }
        }
    }

    public @Nullable Vec3 getCachedKeyBoneWorldPos() {
        return cachedKeyBoneWorldPos;
    }

    public void setCachedKeyBoneWorldPos(Vec3 pos) {
        this.cachedKeyBoneWorldPos = pos;
    }

    public void spawnKeyEntity() {
        Level level = exterior.getLevel();
        if (level == null || level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (keyEntity != null && keyEntity.isAlive()) {
            keyEntity.discard();
        }
        TardisKeyEntity entity = TardisKeyEntity.create(serverLevel, exterior.getBlockPos(), getKeyWorldPos());
        serverLevel.addFreshEntity(entity);
        this.keyEntity = entity;
    }

    public Vec3 getKeyWorldPos() {
        return Vec3.atCenterOf(exterior.getBlockPos())
                .add(exterior.getExterior().worldKeyOffset(exterior.getYaw()));
    }

    public void discardKeyEntity() {
        if (keyEntity != null) {
            keyEntity.discard();
            keyEntity = null;
        }
    }

    public void insertKey(Player player, InteractionHand hand, ItemStack held) {
        ItemStack toStore = held.copyWithCount(1);
        toStore.set(GCDataComponents.KEY_INSERTED, true);
        setHeldKey(player.getUUID(), toStore);

        ItemStack remaining = held.copyWithCount(held.getCount() - 1);
        player.setItemInHand(hand, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
    }

    public boolean canRemoveKey(Player player) {
        return player.getUUID().equals(heldKey.owner())
                || Tardis.of(exterior).map(tardis -> tardis.isOwner(player)).orElse(false);
    }

    public boolean hasKeyRendered() {
        return heldKey.isPresent();
    }

    public @Nullable UUID getRenderedKeyOwner() {
        return heldKey.owner();
    }

    public @Nullable ItemStack getHeldKeyStack() {
        return heldKey.stack();
    }

    public void setHeldKey(UUID owner, ItemStack stack) {
        heldKey.set(owner, stack);
        exterior.sync();
    }

    public ItemStack clearHeldKey() {
        ItemStack returned = heldKey.clear();
        exterior.sync();
        return returned;
    }

    public boolean isWrongKeyLockedOut(Player player, ItemStack key) {
        Level level = exterior.getLevel();
        if (level == null || level.isClientSide()) return false;
        Long until = wrongKeyLockouts.get(keyIdOf(key));
        if (until == null) return false;

        long remaining = until - GameClockUtil.now(level);
        if (remaining <= 0L) {
            wrongKeyLockouts.remove(keyIdOf(key));
            return false;
        }
        MessageUtil.overlay(player, ChatFormatting.RED, "key_lockout", (remaining + 19L) / 20L);
        return true;
    }

    public void insertWrongKey(Player player, InteractionHand hand, ItemStack held) {
        if (!(exterior.getLevel() instanceof ServerLevel serverLevel)) return;

        UUID keyId = keyIdOf(held);
        long now = GameClockUtil.now(serverLevel);
        wrongKeyLockouts.values().removeIf(until -> until <= now);
        wrongKeyLockouts.put(keyId, now + WRONG_KEY_LOCKOUT_TICKS);
        Tardis.get(serverLevel.getServer(), exterior.getTardisId()).ifPresent(TardisMoods::onWrongKey);

        insertKey(player, hand, held);

        UUID playerId = player.getUUID();
        MinecraftServer server = serverLevel.getServer();
        SchedulerUtil.runLater(server, WRONG_KEY_JAM_DELAY_TICKS, () -> {
            if (exterior.isRemoved() || isWrongKeyGone(playerId, keyId)) return;
            exterior.triggerAnim(KEY_CONTROLLER, "key_jammed");
            ExteriorSounds.keyJammed(exterior.getLevel(), exterior.getBlockPos());
        });
        SchedulerUtil.runLater(server, WRONG_KEY_RETURN_DELAY_TICKS, () -> {
            if (exterior.isRemoved() || isWrongKeyGone(playerId, keyId)) return;
            ItemStack returned = clearHeldKey();
            returned.set(GCDataComponents.KEY_INSERTED, false);
            ServerPlayer owner = server.getPlayerList().getPlayer(playerId);
            if (owner == null) {
                Block.popResource(serverLevel, exterior.getBlockPos(), returned);
                return;
            }
            if (!owner.getInventory().add(returned)) {
                owner.drop(returned, false, Prediction.SERVER_ONLY);
            }
            MessageUtil.overlay(owner, ChatFormatting.RED, "wrong_key");
        });
    }

    private boolean isWrongKeyGone(UUID playerId, UUID keyId) {
        ItemStack inside = heldKey.stack();
        return !hasKeyRendered()
                || !playerId.equals(heldKey.owner())
                || inside == null
                || !keyIdOf(inside).equals(keyId);
    }

    public boolean rejectKeyIfLockBroken(@Nullable Player player) {
        if (TardisSubsystems.LOCKING_MECHANISM.acceptsKey(
                exterior.getSubsystemHealth(TardisSubsystems.LOCKING_MECHANISM))) {
            return false;
        }
        ExteriorSounds.keyJammed(exterior.getLevel(), exterior.getBlockPos());
        if (player != null) {
            MessageUtil.overlay(player, ChatFormatting.RED, "lock_too_damaged");
        }
        return true;
    }

    private void announceKeyJammed(@Nullable Player player) {
        exterior.triggerAnim(KEY_CONTROLLER, "key_jammed");
        ExteriorSounds.keyJammed(exterior.getLevel(), exterior.getBlockPos());
        if (player != null) {
            MessageUtil.overlay(player, ChatFormatting.RED, "lock_jammed");
        }
    }

    private boolean lockedFeedbackThrottled() {
        long now = System.currentTimeMillis();
        if (now - lastLockedFeedbackAt < 1000L) {
            return true;
        }
        lastLockedFeedbackAt = now;
        return false;
    }

    public void announceSealed() {
        if (lockedFeedbackThrottled()) {
            return;
        }

        exterior.triggerAnim(KEY_CONTROLLER, "is_locked");
        ExteriorSounds.locked(exterior.getLevel(), exterior.getBlockPos());
    }

    public void announceLocked(Player player) {
        if (lockedFeedbackThrottled()) {
            return;
        }

        exterior.triggerAnim(KEY_CONTROLLER, "is_locked");
        ExteriorSounds.locked(exterior.getLevel(), exterior.getBlockPos());

        Component overlay = Tardis.of(exterior)
                .map(tardis -> Component.translatable(
                        "message.gallifreyan_chronicles.locked_owned",
                        tardis.ownerName()
                                .map(name -> Component.literal(name).withStyle(ChatFormatting.AQUA))
                                .orElseGet(() -> Component.translatable("message.gallifreyan_chronicles.unknown_player")
                                        .withStyle(ChatFormatting.AQUA))))
                .orElseGet(() -> Component.translatable("message.gallifreyan_chronicles.locked"));
        player.sendOverlayMessage(overlay.copy().withStyle(ChatFormatting.RED));
    }

    public AnimationController<TardisExteriorBlockEntity> createController() {
        AnimationController<TardisExteriorBlockEntity> controller =
                new AnimationController<>(KEY_CONTROLLER, 0, _ -> PlayState.CONTINUE);
        controller
                .triggerableAnim("key_lock", RawAnimation.begin().thenPlay("key_lock"))
                .triggerableAnim("key_unlock", RawAnimation.begin().thenPlay("key_unlock"))
                .triggerableAnim("is_locked", RawAnimation.begin().thenPlay("is_locked"))
                .triggerableAnim("key_jammed", RawAnimation.begin().thenPlay("key_jammed"));
        return controller;
    }

    public void copyFrom(ExteriorLock other) {
        locked = other.locked;
        heldKey.copyFrom(other.heldKey);
    }

    public void save(ValueOutput output) {
        heldKey.save(output);
        output.putBoolean("Locked", locked);
    }

    public void load(ValueInput input) {
        heldKey.load(input);
        locked = input.getBooleanOr("Locked", false);
    }
}
