package com.noahtnt2009.gallifreyan_chronicles.registry;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.noahtnt2009.gallifreyan_chronicles.command.TardisCommand;
import com.noahtnt2009.gallifreyan_chronicles.tardis.TardisManager;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class GCCommands {
    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    literal("gc")
                            .then(literal("tardis")
                                    .then(argument("id", StringArgumentType.string())
                                            .suggests((ctx, builder) -> {
                                                TardisManager.get(ctx.getSource().getServer())
                                                        .getAllIds()
                                                        .forEach(id -> builder.suggest(id.toString()));
                                                return builder.buildFuture();
                                            })
                                            .then(literal("data")
                                                    .then(literal("set")
                                                            .then(literal("exterior")
                                                                    .then(argument("exterior_id", StringArgumentType.string())
                                                                            .suggests((ctx, builder) -> {
                                                                                TardisExteriorRegistry.getAll().forEach(e -> builder.suggest(e.id()));
                                                                                return builder.buildFuture();
                                                                            })
                                                                            .executes(TardisCommand::setExterior))))
                                                    .then(literal("get")
                                                            .then(literal("exterior")
                                                                    .executes(TardisCommand::getExterior))))))
            );
        });
    }
}