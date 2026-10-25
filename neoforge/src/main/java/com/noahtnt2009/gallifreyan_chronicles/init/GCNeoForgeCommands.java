package com.noahtnt2009.gallifreyan_chronicles.init;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.command.TardisCommand;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorRegistry;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.stream.Collectors;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class GCNeoForgeCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                literal("gc")
                        .then(literal("tardis")
                                .then(literal("list")
                                        .executes(TardisCommand::listTardis))
                                .then(literal("controls")
                                        .then(argument("control", StringArgumentType.word())
                                                .suggests(TardisCommand::suggestControls)
                                                .then(literal("set")
                                                        .then(literal("on")
                                                                .executes(ctx -> TardisCommand.setControl(ctx, true)))
                                                        .then(literal("off")
                                                                .executes(ctx -> TardisCommand.setControl(ctx, false)))))
                                )
                                .then(argument("id", StringArgumentType.string())
                                        .suggests((ctx, builder) -> {
                                            TardisManager.get(ctx.getSource().getServer())
                                                    .getAllIds()
                                                    .forEach(id -> builder.suggest(id.toString()));

                                            return builder.buildFuture();
                                        })
                                        .then(literal("exterior")
                                                .then(literal("get")
                                                        .executes(TardisCommand::getExterior))
                                                .then(literal("set")
                                                        .then(argument("exterior_id",
                                                                StringArgumentType.greedyString())
                                                                .suggests((ctx, builder) -> {
                                                                    TardisExteriorRegistry.getAll()
                                                                            .forEach(e -> builder.suggest(e.id()));

                                                                    return builder.buildFuture();
                                                                })
                                                                .executes(TardisCommand::setExterior)))
                                                .then(literal("link")
                                                        .executes(TardisCommand::linkExterior))
                                                .then(literal("unlink")
                                                        .executes(TardisCommand::unlinkExterior)))
                                        .then(literal("console")
                                                .then(literal("get")
                                                        .executes(TardisCommand::getConsole))
                                                .then(literal("set")
                                                        .then(argument("console_id",
                                                                StringArgumentType.greedyString())
                                                                .suggests((ctx, builder) -> {
                                                                    TardisConsoleRegistry.getAll()
                                                                            .forEach(e -> builder.suggest(e.id()));

                                                                    return builder.buildFuture();
                                                                })
                                                                .executes(TardisCommand::setConsole)))
                                                .then(literal("variant")
                                                        .then(literal("set")
                                                                .then(argument("variant_id",
                                                                        StringArgumentType.greedyString())
                                                                        .suggests(TardisCommand::suggestConsoleVariants)
                                                                        .executes(TardisCommand::setConsoleVariant))))
                                                .then(literal("link")
                                                        .executes(TardisCommand::linkConsole))
                                                .then(literal("unlink")
                                                        .executes(TardisCommand::unlinkConsole)))
                                        .then(literal("door")
                                                .then(literal("variant")
                                                        .then(literal("set")
                                                                .then(argument("variant_id",
                                                                        StringArgumentType.greedyString())
                                                                        .suggests(TardisCommand::suggestDoorVariants)
                                                                        .executes(TardisCommand::setDoorVariant)))))
                                        .then(literal("summon")
                                                .executes(TardisCommand::summonTardis))
                                        .then(literal("destination")
                                                .then(literal("get")
                                                        .executes(TardisCommand::getDestination))
                                                .then(literal("here")
                                                        .executes(TardisCommand::getCurrentDestination))
                                                .then(literal("set")
                                                        .then(argument("dimension", DimensionArgument.dimension())
                                                                .then(argument("pos", BlockPosArgument.blockPos())
                                                                        .then(argument("facing", StringArgumentType.word())
                                                                                .suggests(TardisCommand::suggestFacings)
                                                                                .executes(TardisCommand::setDestination)))))
                                                .then(literal("clear")
                                                        .executes(TardisCommand::clearDestination)))
                                        .then(literal("interior")
                                                .then(literal("get")
                                                        .executes(TardisCommand::getInterior))
                                                .then(literal("set")
                                                        .then(argument("interior_id", StringArgumentType.greedyString())
                                                                .suggests(TardisCommand::suggestInteriors)
                                                                .executes(TardisCommand::setInterior))))
                                        .then(literal("fade")
                                                .then(literal("get")
                                                        .executes(TardisCommand::getFadeType))
                                                .then(literal("set")
                                                        .then(argument("fade_type", StringArgumentType.string())
                                                                .suggests(TardisCommand::suggestFadeTypes)
                                                                .executes(TardisCommand::setFadeType))))
                                        .then(literal("glow")
                                                .then(argument("glowing", BoolArgumentType.bool())
                                                        .executes(TardisCommand::setGlowing)))
                                        .then(literal("subsystem")
                                                .then(argument("subsystem", StringArgumentType.word())
                                                        .suggests(TardisCommand::suggestSubsystems)
                                                        .then(literal("health")
                                                                .then(literal("get")
                                                                        .executes(TardisCommand::getSubsystemHealth))
                                                                .then(literal("set")
                                                                        .then(argument("health",
                                                                                FloatArgumentType.floatArg(0f, 100f))
                                                                                .executes(TardisCommand::setSubsystemHealth))))))))
                        .then(literal("debug")
                                .then(literal("exteriors")
                                        .executes(ctx -> {
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.translatable(
                                                            "command.gallifreyan_chronicles.list_exterior",
                                                            TardisExteriorRegistry.getAll()
                                                                    .stream()
                                                                    .map(TardisExterior::id)
                                                                    .collect(Collectors.joining(", "))
                                                    ),
                                                    false
                                            );

                                            return 1;
                                        }))
                                .then(literal("tardis")
                                        .then(argument("id", StringArgumentType.string())
                                                .suggests((ctx, builder) -> {
                                                    TardisManager.get(ctx.getSource().getServer())
                                                            .getAllIds()
                                                            .forEach(id -> builder.suggest(id.toString()));

                                                    return builder.buildFuture();
                                                })
                                                .executes(TardisCommand::debugTardis)))
                                .then(literal("controls")
                                        .executes(TardisCommand::listControls)))
        );

        Constants.LOG.info("Registered GC Commands (NeoForge)");
    }

}