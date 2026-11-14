package uk.nbforge.gallifreyan_chronicles.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.TardisSubsystem;
import net.minecraft.commands.CommandSourceStack;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static uk.nbforge.gallifreyan_chronicles.util.CommandUtil.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class SubsystemCommands {
    private interface SubsystemAction {
        int run(TardisExteriorBlockEntity exterior, TardisSubsystem subsystem, String idText);
    }

    private SubsystemCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> subsystem() {
        return literal("subsystem")
                .then(argument("subsystem", StringArgumentType.word())
                        .suggests(SubsystemCommands::suggestSubsystems)
                        .then(literal("health")
                                .then(literal("get").executes(SubsystemCommands::getHealth))
                                .then(literal("set")
                                        .then(argument("health", FloatArgumentType.floatArg(0f, 100f))
                                                .executes(SubsystemCommands::setHealth)))));
    }

    private static CompletableFuture<Suggestions> suggestSubsystems(CommandContext<CommandSourceStack> ctx,
                                                                    SuggestionsBuilder builder) {
        for (TardisSubsystem subsystem : TardisSubsystem.values()) {
            builder.suggest(subsystem.id());
        }
        return builder.buildFuture();
    }

    private static int getHealth(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withSubsystem(ctx, (exterior, subsystem, idText) -> {
            float health = exterior.getSubsystemHealth(subsystem);
            success(ctx, false, "get_subsystem_health", subsystem.id(), idText, String.valueOf(health));
            return Math.round(health);
        });
    }

    private static int setHealth(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        float health = FloatArgumentType.getFloat(ctx, "health");
        return withSubsystem(ctx, (exterior, subsystem, idText) -> {
            exterior.setSubsystemHealth(subsystem, health);
            return success(ctx, true, "set_subsystem_health", subsystem.id(), idText, String.valueOf(health));
        });
    }

    private static int withSubsystem(CommandContext<CommandSourceStack> ctx, SubsystemAction action)
            throws CommandSyntaxException {
        String subsystemId = StringArgumentType.getString(ctx, "subsystem");
        Optional<TardisSubsystem> subsystem = TardisSubsystem.byId(subsystemId);
        if (subsystem.isEmpty()) return fail(ctx, "unknown_subsystem", subsystemId);

        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisExteriorBlockEntity> exterior = findBlockEntity(ctx,
                    manager(ctx).resolveExteriorBlockPos(tardis.id()), TardisExteriorBlockEntity.class);
            if (exterior.isEmpty()) return fail(ctx, "no_exterior", idText);
            return action.run(exterior.get(), subsystem.get(), idText);
        });
    }
}
