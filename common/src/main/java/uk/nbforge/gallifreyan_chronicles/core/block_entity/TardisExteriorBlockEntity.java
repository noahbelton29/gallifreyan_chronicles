package uk.nbforge.gallifreyan_chronicles.core.block_entity;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import com.mojang.serialization.Codec;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.client.GCClientHooks;
import uk.nbforge.gallifreyan_chronicles.client.renderer.geo_layer.TardisExteriorKeyLayer;
import uk.nbforge.gallifreyan_chronicles.codec.UuidCodec;
import uk.nbforge.gallifreyan_chronicles.core.block.TardisTopBlock;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisDoorRegistry;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisExteriorRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisLinkable;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.ExteriorDoors;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.ExteriorLamp;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.ExteriorLock;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.ExteriorTravel;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import uk.nbforge.gallifreyan_chronicles.tardis.interior.TardisInteriors;
import uk.nbforge.gallifreyan_chronicles.tardis.key.TardisKeyLocator;
import uk.nbforge.gallifreyan_chronicles.tardis.subsystem.TardisSubsystem;
import uk.nbforge.gallifreyan_chronicles.tardis.subsystem.TardisSubsystems;
import uk.nbforge.gallifreyan_chronicles.util.BlockEntitySyncUtil;
import uk.nbforge.gallifreyan_chronicles.util.GameClockUtil;
import uk.nbforge.gallifreyan_chronicles.util.SavedValue;

public class TardisExteriorBlockEntity extends BlockEntity
        implements GeoBlockEntity, TardisLinkable, TardisExteriorKeyLayer.TardisExteriorRenderable {
    public static Supplier<BlockEntityType<TardisExteriorBlockEntity>> TYPE;

    private static final int TOP_BLOCK_CHECK_TICKS = 40;
    private static final int POWER_CHECK_INTERVAL_TICKS = 10;
    private static final int ENTRY_ZONE_INTERVAL_TICKS = 2;
    private static final int TRAVEL_RESYNC_TICKS = 20;

    private final SavedValue<String> exteriorId = new SavedValue<>(
            "exterior", Codec.STRING, () -> TardisExteriorRegistry.getDefault().id());
    private final SavedValue<Float> yaw = new SavedValue<>("transform", Codec.FLOAT, () -> 0f);
    private final SavedValue<UUID> tardisLink = new SavedValue<>("tardis_link", UuidCodec.CODEC);
    private final SavedValue<Map<TardisSubsystem, Float>> subsystems =
            new SavedValue<>("subsystems", TardisSubsystems.HEALTH_CODEC, TardisSubsystems::fullHealth);
    private boolean portalActive = false;

    private final ExteriorDoors doors = new ExteriorDoors(this);
    private final ExteriorLock lock = new ExteriorLock(this);
    private final ExteriorTravel travel = new ExteriorTravel(this);
    private final ExteriorLamp lamp = new ExteriorLamp(this);

    private final int tickOffset;
    private boolean lastShadowActive = true;
    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public TardisExteriorBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE.get(), pos, state);
        this.tickOffset = Math.floorMod(pos.hashCode(), POWER_CHECK_INTERVAL_TICKS);
    }

    public static void clientTick(Level level, TardisExteriorBlockEntity blockEntity) {
        blockEntity.tickClient(level);
    }

    public static void serverTick(Level level, TardisExteriorBlockEntity blockEntity) {
        blockEntity.tickServer(level);
    }

    private void tickClient(Level level) {
        GCClientHooks.trackExteriorHum(this);

        // remesh when the shadow flips or the AO on nearby blocks stays stale
        boolean active = travel.isShadowActive();
        if (active == lastShadowActive) return;
        lastShadowActive = active;
        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_IMMEDIATE);
        level.sendBlockUpdated(worldPosition.above(), state, state, Block.UPDATE_IMMEDIATE);
    }

    private void tickServer(Level level) {
        if (GameClockUtil.now(level) % TOP_BLOCK_CHECK_TICKS == 0L) {
            TardisTopBlock.placeAbove(level, worldPosition);
        }
        if (level.isClientSide()) return;

        travel.refreshFadeType();
        travel.tickLanding(level);
        long phase = GameClockUtil.now(level) + tickOffset;
        if (travel.isTravelling() && phase % TRAVEL_RESYNC_TICKS == 0) {
            sync();
        }

        lamp.updateLight(level);
        if (phase % POWER_CHECK_INTERVAL_TICKS == 0) {
            lamp.refreshPowerAndGlow();
        }

        if (phase % ENTRY_ZONE_INTERVAL_TICKS == 0) {
            TardisInteriors.tickEntryZone(level, this);
        }

        if (level instanceof ServerLevel serverLevel) {
            for (TardisSubsystem subsystem : TardisSubsystems.all()) {
                subsystem.tick(serverLevel, this, getSubsystemHealth(subsystem));
            }
            travel.tickTakeoffWind(serverLevel);
        }
    }

    public ExteriorDoors doors() {
        return doors;
    }

    public ExteriorLock lock() {
        return lock;
    }

    public ExteriorTravel travel() {
        return travel;
    }

    public ExteriorLamp lamp() {
        return lamp;
    }

    public TardisExterior getExterior() {
        return TardisExteriorRegistry.get(exteriorId.get());
    }

    public void setExterior(TardisExterior exterior) {
        storeExterior(exterior);
        doors.resetAnimationState();
        UUID id = getTardisId();
        if (id == null || level == null || level.isClientSide() || level.getServer() == null) return;
        Tardis tardis = Tardis.get(level.getServer(), id).orElse(null);
        if (tardis == null) return;
        TardisKeyLocator.refresh(tardis);
        String doorVariant = TardisDoorRegistry.interiorVariantForExterior(exterior.id());
        if (doorVariant == null) return;
        for (InteriorDoorBlockEntity door : tardis.interiorDoors()) {
            if (door.isLinked()) door.setDoorVariant(doorVariant);
        }
    }

    // no anim reset or door/key updates, use setExterior for that
    public void storeExterior(TardisExterior exterior) {
        exteriorId.set(exterior.id());
        sync();
    }

    public float getYaw() {
        return yaw.get();
    }

    public void setYaw(float yaw) {
        this.yaw.set(yaw);
        sync();
    }

    public @Nullable UUID getTardisId() {
        return getLinkedTardisId().orElse(null);
    }

    @Override
    public Optional<UUID> getLinkedTardisId() {
        return Optional.ofNullable(tardisLink.getOrNull());
    }

    @Override
    public void linkToTardis(UUID tardisId) {
        tardisLink.set(tardisId);
        sync();
    }

    @Override
    public void unlinkFromTardis() {
        tardisLink.set(null);
        sync();
    }

    public float getSubsystemHealth(TardisSubsystem subsystem) {
        return TardisSubsystems.healthOf(subsystems.get(), subsystem);
    }

    public void setSubsystemHealth(TardisSubsystem subsystem, float health) {
        subsystems.set(TardisSubsystems.withHealth(subsystems.get(), subsystem, health));
        sync();
    }

    public boolean isPortalActive() {
        return portalActive;
    }

    public void setPortalActive(boolean active) {
        if (portalActive == active) return;
        portalActive = active;
        sync();
    }

    public void copyStateFrom(TardisExteriorBlockEntity other) {
        exteriorId.copyFrom(other.exteriorId);
        yaw.copyFrom(other.yaw);
        tardisLink.copyFrom(other.tardisLink);
        subsystems.copyFrom(other.subsystems);
        portalActive = other.portalActive;
        doors.copyFrom(other.doors);
        lock.copyFrom(other.lock);
        travel.copyFrom(other.travel);
        lamp.copyFrom(other.lamp);
        doors.resetAnimationState();
    }

    @Override
    public void setLevel(@NonNull Level level) {
        super.setLevel(level);
        if (cache == null) {
            cache = GeckoLibUtil.createInstanceCache(this);
        }
        doors.onLevelSet(level);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        lock.discardKeyEntity();
    }

    public void sync() {
        BlockEntitySyncUtil.sendToClients(this);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        exteriorId.save(output);
        yaw.save(output);
        tardisLink.save(output);
        subsystems.save(output);
        lock.save(output);
        travel.save(output);
        lamp.save(output);
        doors.save(output);
    }

    @Override
    public void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        exteriorId.load(input);
        yaw.load(input);
        tardisLink.load(input);
        subsystems.load(input);
        portalActive = input.getBooleanOr("PortalActive", false);
        lock.load(input);
        travel.load(input);
        lamp.load(input);
        // last, it triggers the client door anim and reads the state loaded above
        doors.load(input);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        CompoundTag tag = saveWithoutMetadata(registries);
        tag.putBoolean("PortalActive", portalActive);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(doors.createController());
        registrar.add(lock.createController());
        doors.afterControllersRegistered();
    }

    @Override
    public @NonNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public @Nullable ItemStack gc$getHeldKeyStack() {
        return lock.getHeldKeyStack();
    }
}
