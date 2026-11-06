package com.noahtnt2009.gallifreyan_chronicles.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

import java.util.stream.Collectors;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class GCCommands {
    private GCCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> root() {
        return literal("gc")
                .then(tardis())
                .then(debug());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> tardis() {
        return literal("tardis")
                .then(literal("list").executes(TardisCommand::listTardis))
                .then(deletion())
                .then(tardisId());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> deletion() {
        return literal("deletion")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(literal("on").executes(ctx -> TardisCommand.setDeletionMode(ctx, true)))
                .then(literal("off").executes(ctx -> TardisCommand.setDeletionMode(ctx, false)));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> tardisId() {
        return argument("id", StringArgumentType.string())
                .suggests((ctx, builder) -> {
                    TardisManager.get(ctx.getSource().getServer())
                            .getAllIds()
                            .forEach(id -> builder.suggest(id.toString()));
                    return builder.buildFuture();
                })
                .then(literal("info").executes(TardisCommand::debugTardis))
                .then(literal("summon").executes(TardisCommand::summonTardis))
                .then(literal("remove").executes(TardisCommand::removeTardis))
                .then(exterior())
                .then(interior())
                .then(console())
                .then(door())
                .then(destination())
                .then(fade())
                .then(literal("glow")
                        .then(argument("glowing", BoolArgumentType.bool())
                                .executes(TardisCommand::setGlowing)))
                .then(control("control"))
                .then(controlLegacy())
                .then(subsystem());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> exterior() {
        return literal("exterior")
                .then(literal("get").executes(TardisCommand::getExterior))
                .then(literal("set")
                        .then(argument("exterior_id", StringArgumentType.greedyString())
                                .suggests((_, builder) -> {
                                    TardisExteriorRegistry.getAll().forEach(e -> builder.suggest(e.id()));
                                    return builder.buildFuture();
                                })
                                .executes(TardisCommand::setExterior)))
                .then(literal("link").executes(TardisCommand::linkExterior))
                .then(literal("unlink").executes(TardisCommand::unlinkExterior));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> interior() {
        return literal("interior")
                .then(literal("get").executes(TardisCommand::getInterior))
                .then(literal("set")
                        .then(argument("interior_id", StringArgumentType.greedyString())
                                .suggests(TardisCommand::suggestInteriors)
                                .executes(TardisCommand::setInterior)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> console() {
        return literal("console")
                .then(literal("get").executes(TardisCommand::getConsole))
                .then(literal("set")
                        .then(argument("console_id", StringArgumentType.greedyString())
                                .suggests((_, builder) -> {
                                    TardisConsoleRegistry.getAll().forEach(e -> builder.suggest(e.id()));
                                    return builder.buildFuture();
                                })
                                .executes(TardisCommand::setConsole)))
                .then(literal("link").executes(TardisCommand::linkConsole))
                .then(literal("unlink").executes(TardisCommand::unlinkConsole))
                .then(literal("variant")
                        .then(literal("set")
                                .then(argument("variant_id", StringArgumentType.greedyString())
                                        .suggests(TardisCommand::suggestConsoleVariants)
                                        .executes(TardisCommand::setConsoleVariant))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> door() {
        return literal("door")
                .then(literal("variant")
                        .then(literal("set")
                                .then(argument("variant_id", StringArgumentType.greedyString())
                                        .suggests(TardisCommand::suggestDoorVariants)
                                        .executes(TardisCommand::setDoorVariant))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> destination() {
        return literal("destination")
                .then(literal("get").executes(TardisCommand::getDestination))
                .then(literal("here").executes(TardisCommand::getCurrentDestination))
                .then(literal("set")
                        .then(argument("dimension", DimensionArgument.dimension())
                                .then(argument("pos", BlockPosArgument.blockPos())
                                        .then(argument("facing", StringArgumentType.word())
                                                .suggests(TardisCommand::suggestFacings)
                                                .executes(TardisCommand::setDestination)))))
                .then(literal("clear").executes(TardisCommand::clearDestination));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> fade() {
        return literal("fade")
                .then(literal("get").executes(TardisCommand::getFadeType))
                .then(literal("set")
                        .then(argument("fade_type", StringArgumentType.string())
                                .suggests(TardisCommand::suggestFadeTypes)
                                .executes(TardisCommand::setFadeType)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> control(String name) {
        return literal(name)
                .then(argument("control", StringArgumentType.word())
                        .suggests(TardisCommand::suggestControls)
                        .then(literal("on").executes(ctx -> TardisCommand.setControl(ctx, true)))
                        .then(literal("off").executes(ctx -> TardisCommand.setControl(ctx, false))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> controlLegacy() {
        return literal("controls")
                .then(argument("control", StringArgumentType.word())
                        .suggests(TardisCommand::suggestControls)
                        .then(literal("set")
                                .then(literal("on").executes(ctx -> TardisCommand.setControl(ctx, true)))
                                .then(literal("off").executes(ctx -> TardisCommand.setControl(ctx, false)))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> subsystem() {
        return literal("subsystem")
                .then(argument("subsystem", StringArgumentType.word())
                        .suggests(TardisCommand::suggestSubsystems)
                        .then(literal("health")
                                .then(literal("get").executes(TardisCommand::getSubsystemHealth))
                                .then(literal("set")
                                        .then(argument("health", FloatArgumentType.floatArg(0f, 100f))
                                                .executes(TardisCommand::setSubsystemHealth)))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> debug() {
        return literal("debug")
                .then(literal("exteriors").executes(ctx -> {
                    ctx.getSource().sendSuccess(
                            () -> Component.translatable(
                                    "command.gallifreyan_chronicles.list_exterior",
                                    TardisExteriorRegistry.getAll().stream()
                                            .map(TardisExterior::id)
                                            .collect(Collectors.joining(", "))),
                            false);
                    return 1;
                }))
                .then(literal("controls").executes(TardisCommand::listControls))
                .then(literal("tardis")
                        .then(argument("id", StringArgumentType.string())
                                .suggests((ctx, builder) -> {
                                    TardisManager.get(ctx.getSource().getServer())
                                            .getAllIds()
                                            .forEach(id -> builder.suggest(id.toString()));
                                    return builder.buildFuture();
                                })
                                .executes(TardisCommand::debugTardis)));
    }
}
