package uk.nbforge.gallifreyan_chronicles.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.TardisInteriorSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisTravel;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.crash.TardisDamage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Optional;

import static uk.nbforge.gallifreyan_chronicles.util.CommandUtil.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * Every low-level piece of TARDIS state, in one tree: bare = get, a trailing value = set.
 * Every setter here either calls straight through to the mod's own validated logic (the control's real
 * activate(), the door-lock control's setLockedFromConsole, TardisDamage's crash/repair, etc.) or, for the
 * handful of plain fields that have no side effects to protect (exterior model, glow, cloak, yaw...), writes
 * directly to the block entity the same way the mod's own systems do. Nothing here pokes ECS components or
 * flight state directly, so a player with permission to use this command can't get the TARDIS into a state the
 * mod wouldn't reach on its own.
 */
public final class DataCommands {
    // Deliberately generous: this is meant to allow a silly number of seconds, not just a sane one.
    private static final float MAX_FLIGHT_SECONDS = 1_000_000f;

    private DataCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> data() {
        return literal("data")
                .then(ExteriorCommands.exteriorData())
                .then(ExteriorCommands.glowData())
                .then(ExteriorCommands.fadeData())
                .then(InteriorCommands.interiorData())
                .then(InteriorCommands.doorVariantData())
                .then(ConsoleCommands.consoleData())
                .then(DestinationCommands.destinationData())
                .then(ControlCommands.controlData())
                .then(SubsystemCommands.subsystemData())
                .then(MindCommands.mindData())
                .then(lockedData())
                .then(cloakedData())
                .then(yawData())
                .then(crashedData())
                .then(flightRemainingData())
                .then(literal("dump").executes(DataCommands::dump));
    }

    /**
     * data locked [on|off], the same door lock the DOOR_LOCK control and the key use.
     */
    private static LiteralArgumentBuilder<CommandSourceStack> lockedData() {
        return literal("locked")
                .executes(DataCommands::getLocked)
                .then(literal("on").executes(ctx -> setLocked(ctx, true)))
                .then(literal("off").executes(ctx -> setLocked(ctx, false)));
    }

    private static int getLocked(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> success(ctx, false, "get_locked", idText, String.valueOf(tardis.isLocked())));
    }

    private static int setLocked(CommandContext<CommandSourceStack> ctx, boolean locked) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            boolean ok = locked ? tardis.lock() : tardis.unlock();
            if (!ok) return fail(ctx, "set_locked_failed", idText, String.valueOf(locked));
            return success(ctx, true, "set_locked", idText, String.valueOf(locked));
        });
    }

    /**
     * data cloaked [on|off]
     */
    private static LiteralArgumentBuilder<CommandSourceStack> cloakedData() {
        return literal("cloaked")
                .executes(DataCommands::getCloaked)
                .then(literal("on").executes(ctx -> setCloaked(ctx, true)))
                .then(literal("off").executes(ctx -> setCloaked(ctx, false)));
    }

    private static int getCloaked(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisExteriorBlockEntity> be = tardis.exterior();
            return be.map(tardisExteriorBlockEntity -> success(ctx, false, "get_cloaked", idText, String.valueOf(tardisExteriorBlockEntity.isCloaked()))).orElseGet(() -> fail(ctx, "no_exterior", idText));
        });
    }

    private static int setCloaked(CommandContext<CommandSourceStack> ctx, boolean cloaked) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisExteriorBlockEntity> be = tardis.exterior();
            if (be.isEmpty()) return fail(ctx, "no_exterior", idText);
            be.get().setCloaked(cloaked);
            return success(ctx, true, "set_cloaked", idText, String.valueOf(cloaked));
        });
    }

    /**
     * {@code data yaw [<degrees>]}
     */
    private static LiteralArgumentBuilder<CommandSourceStack> yawData() {
        return literal("yaw")
                .executes(DataCommands::getYaw)
                .then(argument("degrees", FloatArgumentType.floatArg())
                        .executes(DataCommands::setYaw));
    }

    private static int getYaw(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisExteriorBlockEntity> be = tardis.exterior();
            return be.map(tardisExteriorBlockEntity -> success(ctx, false, "get_yaw", idText, String.valueOf(tardisExteriorBlockEntity.getYaw()))).orElseGet(() -> fail(ctx, "no_exterior", idText));
        });
    }

    private static int setYaw(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        float yaw = Mth.positiveModulo(FloatArgumentType.getFloat(ctx, "degrees"), 360f);
        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisExteriorBlockEntity> be = tardis.exterior();
            if (be.isEmpty()) return fail(ctx, "no_exterior", idText);
            be.get().setYaw(yaw);
            return success(ctx, true, "set_yaw", idText, String.valueOf(yaw));
        });
    }

    /**
     * data crashed [on|off], goes through TardisDamage so the alarm, smoke and repair gating all still happen.
     */
    private static LiteralArgumentBuilder<CommandSourceStack> crashedData() {
        return literal("crashed")
                .executes(DataCommands::getCrashed)
                .then(literal("on").executes(ctx -> setCrashed(ctx, true)))
                .then(literal("off").executes(ctx -> setCrashed(ctx, false)));
    }

    private static int getCrashed(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> success(ctx, false, "get_crashed", idText, String.valueOf(TardisDamage.isCrashed(tardis))));
    }

    private static int setCrashed(CommandContext<CommandSourceStack> ctx, boolean crashed) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            if (TardisDamage.isCrashed(tardis) == crashed)
                return fail(ctx, "crashed_already", idText, String.valueOf(crashed));
            if (crashed) TardisDamage.markCrashed(tardis);
            else TardisDamage.repair(tardis);
            return success(ctx, true, "set_crashed", idText, String.valueOf(crashed));
        });
    }

    /**
     * {@code data flight_remaining [skip|<seconds>]}, how long is left of the CURRENT flight, lets you fast-forward through
     * it (or push it out to an absurd length) instead of waiting. It works by writing the flight's duration
     * override so that, given how long it's already been travelling, exactly the requested amount is left, the
     * override is cleared the moment the TARDIS lands, so it only ever affects the flight that's happening right
     * now, never the next one. Nothing is landed directly: the throttle lever's "still travelling" check, the
     * overlay and the autopilot all just see a different number, same as if the formula had produced it.
     */
    private static LiteralArgumentBuilder<CommandSourceStack> flightRemainingData() {
        return literal("flight_remaining")
                .executes(DataCommands::getFlightRemaining)
                .then(literal("skip").executes(ctx -> setFlightRemaining(ctx, 0f)))
                .then(argument("seconds", FloatArgumentType.floatArg(0f, MAX_FLIGHT_SECONDS))
                        .executes(ctx -> setFlightRemaining(ctx, FloatArgumentType.getFloat(ctx, "seconds"))));
    }

    private static int getFlightRemaining(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            if (tardis.data().getFlightStart(tardis.id()) == null) return fail(ctx, "not_travelling", idText);
            double seconds = TardisTravel.remainingTicks(tardis) / 20.0;
            return success(ctx, false, "get_flight_remaining", idText, formatSeconds(seconds));
        });
    }

    private static int setFlightRemaining(CommandContext<CommandSourceStack> ctx, float seconds) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            if (tardis.data().getFlightStart(tardis.id()) == null) return fail(ctx, "not_travelling", idText);

            long elapsed = TardisTravel.vortexTicks(tardis);
            long desiredRemaining = Math.round(seconds * 20.0);
            tardis.data().setFlightDurationOverride(tardis.id(), elapsed + desiredRemaining);

            return success(ctx, true, "set_flight_remaining", idText, formatSeconds(desiredRemaining / 20.0));
        });
    }

    /**
     * data dump, every low-level scalar field at once, read only, formatted as one short-id header and compact coloured
     * lines instead of a full sentence per field. Keyed fields (subsystems, controls, mind values) aren't enumerated
     * here, get those individually, or see "data mind dump" for the mind's fields.
     */
    private static int dump(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (Tardis tardis, String idText) -> {
            send(ctx, dataHeader("TARDIS data dump", tardis.id()), false);

            Optional<TardisExteriorBlockEntity> exteriorAtResolved =
                    ExteriorCommands.exteriorAt(ctx, manager(ctx).resolveExteriorBlockPos(tardis.id()));
            sendLine(ctx, dataLine("exterior", exteriorAtResolved.map(be -> stripModId(be.getExterior().id())).orElse("unknown")));

            Optional<TardisExteriorBlockEntity> exteriorAtRegistered =
                    ExteriorCommands.exteriorAt(ctx, manager(ctx).getRegisteredBlockPos(tardis.id()));
            sendLine(ctx, dataLine("fade", exteriorAtRegistered.map(be -> stripModId(be.getFadeType().id())).orElse("unknown")));
            sendLine(ctx, dataLine("glow", exteriorAtRegistered.map(be -> String.valueOf(be.isGlowing())).orElse("unknown")));

            sendLine(ctx, dataLine("interior", stripModId(TardisInteriorSystem.getInteriorDefinition(tardis.server(), tardis.id()).id())));

            List<InteriorDoorBlockEntity> doors = DoorSyncSystem.doorsOf(tardis.id());
            sendLine(ctx, dataLine("door variant", doors.isEmpty() ? "none" : stripModId(doors.get(0).getDoorVariantId())));

            Optional<TardisConsoleBlockEntity> console = ConsoleCommands.consoleOf(ctx, tardis.id());
            sendLine(ctx, dataLine("console", console.map(be -> stripModId(be.getConsole().id())).orElse("unknown")));
            sendLine(ctx, dataLine("console variant", console.map(be -> stripModId(be.getVariant())).orElse("unknown")));

            sendLine(ctx, dataLine("locked", String.valueOf(tardis.isLocked())));

            Optional<TardisExteriorBlockEntity> exterior = tardis.exterior();
            sendLine(ctx, dataLine("cloaked", exterior.map(be -> String.valueOf(be.isCloaked())).orElse("unknown")));
            sendLine(ctx, dataLine("yaw", exterior.map(be -> String.valueOf(be.getYaw())).orElse("unknown")));

            sendLine(ctx, dataLine("crashed", String.valueOf(TardisDamage.isCrashed(tardis))));

            Long flightStart = tardis.data().getFlightStart(tardis.id());
            sendLine(ctx, dataLine("flight_remaining",
                    flightStart == null ? "not travelling" : formatSeconds(TardisTravel.remainingTicks(tardis) / 20.0)));
            return 1;
        });
    }
}
