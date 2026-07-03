package com.noahtnt2009.gallifreyan_chronicles.init;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.command.TardisCommand;
import com.noahtnt2009.gallifreyan_chronicles.tardis.TardisManager;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorRegistry;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

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
                                                        .then(argument("exterior_id", StringArgumentType.greedyString())
                                                                .suggests((ctx, builder) -> {
                                                                    TardisExteriorRegistry.getAll()
                                                                            .forEach(e -> builder.suggest(e.id()));
                                                                    return builder.buildFuture();
                                                                })
                                                                .executes(TardisCommand::setExterior))))))
                        .then(literal("debug")
                                .then(literal("exteriors")
                                        .executes(ctx -> {
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("Exteriors: " + TardisExteriorRegistry.getAll()
                                                            .stream().map(TardisExterior::id)
                                                            .collect(java.util.stream.Collectors.joining(", "))),
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
                                                .executes(TardisCommand::debugTardis))))
        );
    }
}