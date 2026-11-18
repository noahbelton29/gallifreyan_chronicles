package uk.nbforge.gallifreyan_chronicles.tardis.door;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisDoorRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;

// everything plays at the door and again at the console so you hear it from the controls
public final class InteriorDoorSounds {
    private static final float HEARD_OUTSIDE_RANGE = 6.0f;

    private final InteriorDoorBlockEntity door;

    public InteriorDoorSounds(InteriorDoorBlockEntity door) {
        this.door = door;
    }

    public void door(boolean opening) {
        if (door.getLevel() == null) return;
        SoundEvent sound = doorSound(opening);
        play(sound);
        playThroughExterior(sound);
    }

    public void lock(boolean locked) {
        if (door.getLevel() == null) return;
        play(locked ? GCSounds.TARDIS_DOOR_LOCK : GCSounds.TARDIS_DOOR_UNLOCK);
    }

    public void locked() {
        if (door.getLevel() == null) return;
        play(GCSounds.TARDIS_IS_LOCKED);
    }

    private void play(SoundEvent sound) {
        Level level = door.getLevel();
        assert level != null;
        level.playSound(null, door.getBlockPos(), sound, SoundSource.BLOCKS, 1.0f, 1.0f);
        level.playSound(null, consolePos(), sound, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private BlockPos consolePos() {
        if (door.getLevel() instanceof ServerLevel serverLevel) {
            UUID id = door.getTardisId() != null
                    ? door.getTardisId()
                    : Tardis.idOfLevel(serverLevel).orElse(null);
            return Tardis.get(serverLevel.getServer(), id)
                    .flatMap(Tardis::consolePos)
                    .orElse(door.getBlockPos());
        }
        return door.getBlockPos();
    }

    // the main door is also heard at the exterior doorway, a normal overworld sound so flashback records it
    private void playThroughExterior(SoundEvent sound) {
        if (!door.isPrimary() || !(door.getLevel() instanceof ServerLevel serverLevel)) return;
        TardisExteriorBlockEntity exterior = Tardis.get(serverLevel.getServer(), door.getTardisId())
                .flatMap(Tardis::exterior)
                .orElse(null);
        if (exterior == null || !(exterior.getLevel() instanceof ServerLevel outside)) return;
        Vec3 doorway = Vec3.atBottomCenterOf(exterior.getBlockPos()).add(0.0, 1.0, 0.0);
        Holder<SoundEvent> heard =
                Holder.direct(SoundEvent.createFixedRangeEvent(sound.location(), HEARD_OUTSIDE_RANGE));
        outside.playSeededSound(
                null,
                doorway.x,
                doorway.y,
                doorway.z,
                heard,
                SoundSource.BLOCKS,
                1.0f,
                1.0f,
                outside.getRandom().nextLong());
    }

    private SoundEvent doorSound(boolean opening) {
        SoundEvent fallback = opening ? GCSounds.TARDIS_DOOR_OPEN : GCSounds.TARDIS_DOOR_CLOSE;
        TardisDoorVariant variant = TardisDoorRegistry.resolve(door.getVariant(), door.getDoorVariantId());
        if (variant == null) return fallback;

        String configured = opening ? variant.openSound() : variant.closeSound();
        if (configured.isEmpty()) return fallback;

        Identifier id = Identifier.tryParse(configured);
        if (id == null) return fallback;
        return BuiltInRegistries.SOUND_EVENT.getOptional(id).orElseGet(() -> SoundEvent.createVariableRangeEvent(id));
    }
}
