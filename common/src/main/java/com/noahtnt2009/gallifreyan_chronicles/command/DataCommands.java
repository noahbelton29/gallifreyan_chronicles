package com.noahtnt2009.gallifreyan_chronicles.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.TardisLandingSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.LandingTarget;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisCrash;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisDamage;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisDestinations;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisFlightState;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisRemat;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisTravel;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.noahtnt2009.gallifreyan_chronicles.command.CommandUtil.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class DataCommands {
    private interface ExteriorAction {
        int run(TardisExteriorBlockEntity exterior, String idText) throws CommandSyntaxException;
    }

    private interface ConsoleAction {
        int run(TardisConsoleBlockEntity console, String idText) throws CommandSyntaxException;
    }

    private DataCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> data() {
        return literal("data")
                .then(tardisIdArgument()
                        .executes(DataCommands::dump)
                        .then(literal("get").executes(DataCommands::dump))
                        .then(literal("remat").executes(DataCommands::remat))
                        .then(literal("vanish").executes(DataCommands::vanish))
                        .then(owner())
                        .then(position())
                        .then(target())
                        .then(flightState())
                        .then(flightStart())
                        .then(crashed())
                        .then(increment())
                        .then(trusted())
                        .then(exteriorFlag("locked", (exterior, on) -> exterior.setLockedFromConsole(on)))
                        .then(exteriorFlag("cloaked", TardisExteriorBlockEntity::setCloaked))
                        .then(exteriorFlag("in_flight", TardisExteriorBlockEntity::setInFlight))
                        .then(exteriorFlag("glowing", TardisExteriorBlockEntity::setGlowing))
                        .then(yaw())
                        .then(controlState()));
    }

    private static int dump(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            Map<String, String> lines = new LinkedHashMap<>();
            TardisManager data = tardis.data();
            UUID id = tardis.id();

            lines.put("owner", tardis.ownerId() + " (" + tardis.ownerName().orElse("?") + ")");
            lines.put("exterior_dimension", tardis.exteriorDimension().identifier().toString());
            lines.put("exterior_pos", describe(data.resolveExteriorBlockPos(id)));
            lines.put("console_pos", describe(data.getConsoleBlockPos(id)));
            lines.put("interior_id", String.valueOf(data.getInteriorId(id)));
            lines.put("interior_origin", describe(data.getInteriorOrigin(id)));
            lines.put("target", data.getLandingTarget(id) == null ? "none" : describeTarget(data.getLandingTarget(id)));
            int incrementIndex = Math.clamp(data.getCoordIncrementIndex(id), 0, TardisDestinations.INCREMENTS.length - 1);
            lines.put("coord_increment", TardisDestinations.INCREMENTS[incrementIndex]
                    + (data.isCoordIncrementNegative(id) ? " (negative)" : ""));
            lines.put("trusted", data.trustedOf(id).toString());
            lines.put("flight_start", String.valueOf(data.getFlightStart(id)));
            lines.put("travel_ticks_left", String.valueOf(TardisTravel.remainingTicks(tardis)));
            lines.put("crashed", String.valueOf(data.isCrashed(id)));
            lines.put("crashing", String.valueOf(TardisCrash.isCrashing(tardis)));
            lines.put("stress / trust / fear", TardisMindSystem.stress(tardis) + " / "
                    + TardisMindSystem.trust(tardis) + " / " + TardisMindSystem.fear(tardis));

            tardis.exterior().ifPresent(exterior -> {
                lines.put("flight_state", exterior.getFlightState().name());
                lines.put("fully_invisible", String.valueOf(exterior.isFullyInvisible()));
                lines.put("locked", String.valueOf(exterior.isLocked()));
                lines.put("cloaked", String.valueOf(exterior.isCloaked()));
                lines.put("powered", String.valueOf(exterior.isPowered()));
                lines.put("glowing", String.valueOf(exterior.isGlowing()));
                lines.put("door_state", exterior.getDoorState().name());
                lines.put("fade", exterior.getFadeType().id());
                lines.put("yaw", String.valueOf(exterior.getYaw()));
            });

            success(ctx, false, "data_header", idText);
            lines.forEach((key, value) -> success(ctx, false, "data_line", key, value));
            return lines.size();
        });
    }

    private static String describeTarget(LandingTarget target) {
        return target.dimension().identifier() + " " + describe(target.pos()) + " yaw " + target.yaw();
    }

    private static int remat(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            if (tardis.landingTarget().isEmpty()) return fail(ctx, "data_no_target", idText);
            if (!TardisRemat.rematerialise(tardis)) return fail(ctx, "no_exterior", idText);
            return success(ctx, true, "data_remat", idText);
        });
    }

    private static int vanish(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            if (!TardisRemat.vanish(tardis)) return fail(ctx, "no_exterior", idText);
            return success(ctx, true, "data_vanish", idText);
        });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> owner() {
        return literal("owner").then(literal("set")
                .then(argument("player", EntityArgument.player()).executes(ctx -> withTardis(ctx, (tardis, idText) -> {
                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                    tardis.data().setOwner(tardis.id(), player.getUUID(), player.getGameProfile().name());
                    return changed(ctx, idText, "owner", player.getGameProfile().name());
                }))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> position() {
        return literal("position").then(literal("set")
                .then(argument("dimension", DimensionArgument.dimension())
                        .then(argument("pos", BlockPosArgument.blockPos()).executes(ctx -> withTardis(ctx, (tardis, idText) -> {
                            ServerLevel level = DimensionArgument.getDimension(ctx, "dimension");
                            BlockPos pos = BlockPosArgument.getBlockPos(ctx, "pos");
                            if (TardisLandingSystem.moveExterior(tardis, level, pos) == null) return fail(ctx, "no_exterior", idText);
                            return changed(ctx, idText, "position", level.dimension().identifier() + " " + describe(pos));
                        })))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> target() {
        return literal("target")
                .then(literal("clear").executes(ctx -> withTardis(ctx, (tardis, idText) -> {
                    tardis.clearDestination();
                    return changed(ctx, idText, "target", "none");
                })))
                .then(literal("set")
                        .then(argument("dimension", DimensionArgument.dimension())
                                .then(argument("pos", BlockPosArgument.blockPos())
                                        .then(argument("yaw", FloatArgumentType.floatArg(0f, 360f))
                                                .executes(ctx -> withTardis(ctx, (tardis, idText) -> {
                                                    ServerLevel level = DimensionArgument.getDimension(ctx, "dimension");
                                                    BlockPos pos = BlockPosArgument.getBlockPos(ctx, "pos");
                                                    float yaw = FloatArgumentType.getFloat(ctx, "yaw");
                                                    if (!tardis.setDestination(level, pos, yaw)) return fail(ctx, "destination_out_of_world");
                                                    return changed(ctx, idText, "target", describeTarget(new LandingTarget(level.dimension(), pos, yaw)));
                                                }))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> flightState() {
        return literal("flight_state").then(literal("set")
                .then(argument("state", StringArgumentType.word())
                        .suggests(DataCommands::suggestFlightStates)
                        .then(argument("ticks", IntegerArgumentType.integer(0))
                                .executes(ctx -> withExterior(ctx, (exterior, idText) -> {
                                    String name = StringArgumentType.getString(ctx, "state").toUpperCase();
                                    TardisFlightState state = TardisFlightState.parse(name, null);
                                    if (state == null) return fail(ctx, "data_bad_value", name);
                                    exterior.setFlightState(state, IntegerArgumentType.getInteger(ctx, "ticks"));
                                    exterior.sync();
                                    return changed(ctx, idText, "flight_state", state.name());
                                })))));
    }

    private static CompletableFuture<Suggestions> suggestFlightStates(CommandContext<CommandSourceStack> ctx,
                                                                      SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(
                Arrays.stream(TardisFlightState.values()).map(state -> state.name().toLowerCase()), builder);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> flightStart() {
        return literal("flight_start")
                .then(literal("clear").executes(ctx -> withTardis(ctx, (tardis, idText) -> {
                    tardis.data().setFlightStart(tardis.id(), null);
                    return changed(ctx, idText, "flight_start", "none");
                })))
                .then(literal("now").executes(ctx -> withTardis(ctx, (tardis, idText) -> {
                    long now = tardis.server().overworld().getGameTime();
                    tardis.data().setFlightStart(tardis.id(), now);
                    return changed(ctx, idText, "flight_start", String.valueOf(now));
                })));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> crashed() {
        return literal("crashed").then(argument("value", BoolArgumentType.bool())
                .executes(ctx -> withTardis(ctx, (tardis, idText) -> {
                    boolean value = BoolArgumentType.getBool(ctx, "value");
                    if (value) TardisDamage.markCrashed(tardis);
                    else TardisDamage.repair(tardis);
                    return changed(ctx, idText, "crashed", String.valueOf(value));
                })));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> increment() {
        return literal("coord_increment")
                .then(argument("index", IntegerArgumentType.integer(0, TardisDestinations.INCREMENTS.length - 1))
                        .then(argument("negative", BoolArgumentType.bool())
                                .executes(ctx -> withTardis(ctx, (tardis, idText) -> {
                                    int index = IntegerArgumentType.getInteger(ctx, "index");
                                    boolean negative = BoolArgumentType.getBool(ctx, "negative");
                                    tardis.data().setCoordIncrementIndex(tardis.id(), index);
                                    tardis.data().setCoordIncrementNegative(tardis.id(), negative);
                                    return changed(ctx, idText, "coord_increment",
                                            (negative ? "-" : "") + TardisDestinations.INCREMENTS[index]);
                                }))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> trusted() {
        return literal("trusted")
                .then(literal("add").then(trustArgument(true)))
                .then(literal("remove").then(trustArgument(false)));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, ?> trustArgument(boolean add) {
        return argument("player", EntityArgument.player()).executes(ctx -> withTardis(ctx, (tardis, idText) -> {
            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
            tardis.data().setTrusted(tardis.id(), player.getUUID(), add);
            return changed(ctx, idText, add ? "trusted +" : "trusted -", player.getGameProfile().name());
        }));
    }

    private interface ExteriorFlagSetter {
        void set(TardisExteriorBlockEntity exterior, boolean on);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> exteriorFlag(String name, ExteriorFlagSetter setter) {
        return literal(name).then(argument("value", BoolArgumentType.bool())
                .executes(ctx -> withExterior(ctx, (exterior, idText) -> {
                    boolean value = BoolArgumentType.getBool(ctx, "value");
                    setter.set(exterior, value);
                    exterior.sync();
                    return changed(ctx, idText, name, String.valueOf(value));
                })));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> yaw() {
        return literal("yaw").then(argument("degrees", FloatArgumentType.floatArg(0f, 360f))
                .executes(ctx -> withExterior(ctx, (exterior, idText) -> {
                    float yaw = FloatArgumentType.getFloat(ctx, "degrees");
                    exterior.setYaw(yaw);
                    exterior.sync();
                    return changed(ctx, idText, "yaw", String.valueOf(yaw));
                })));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> controlState() {
        return literal("control_state")
                .then(argument("control", StringArgumentType.word())
                        .suggests(DataCommands::suggestControls)
                        .then(argument("value", BoolArgumentType.bool())
                                .executes(ctx -> withConsole(ctx, (console, idText) -> {
                                    String control = StringArgumentType.getString(ctx, "control");
                                    boolean value = BoolArgumentType.getBool(ctx, "value");
                                    console.setControlActivated(control, value);
                                    return changed(ctx, idText, "control_state " + control, String.valueOf(value));
                                }))));
    }

    private static CompletableFuture<Suggestions> suggestControls(CommandContext<CommandSourceStack> ctx,
                                                                  SuggestionsBuilder builder) {
        idArgument(ctx).flatMap(id -> ConsoleCommands.consoleOf(ctx, id))
                .ifPresent(console -> console.getControlEntities().stream()
                        .map(TardisControlEntity::getControlId)
                        .forEach(builder::suggest));
        return builder.buildFuture();
    }

    private static int withExterior(CommandContext<CommandSourceStack> ctx, ExteriorAction action) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisExteriorBlockEntity> exterior = tardis.exterior();
            if (exterior.isEmpty()) return fail(ctx, "no_exterior", idText);
            return action.run(exterior.get(), idText);
        });
    }

    private static int withConsole(CommandContext<CommandSourceStack> ctx, ConsoleAction action) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisConsoleBlockEntity> console = tardis.console();
            if (console.isEmpty()) return fail(ctx, "no_console", idText);
            return action.run(console.get(), idText);
        });
    }

    private static int changed(CommandContext<CommandSourceStack> ctx, String idText, String field, String value) {
        return success(ctx, true, "data_set", field, idText, value);
    }
}
