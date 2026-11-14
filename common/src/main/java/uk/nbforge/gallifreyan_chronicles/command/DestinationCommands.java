package uk.nbforge.gallifreyan_chronicles.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.LandingTarget;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static uk.nbforge.gallifreyan_chronicles.util.CommandUtil.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class DestinationCommands {
    private static final Map<String, Float> FACINGS = Map.of(
            "south", 0f, "southwest", 45f, "west", 90f, "northwest", 135f,
            "north", 180f, "northeast", 225f, "east", 270f, "southeast", 315f);

    private DestinationCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> destination() {
        return literal("destination")
                .then(literal("get").executes(DestinationCommands::get))
                .then(literal("here").executes(DestinationCommands::setHere))
                .then(literal("set")
                        .then(argument("dimension", DimensionArgument.dimension())
                                .then(argument("pos", BlockPosArgument.blockPos())
                                        .then(argument("facing", StringArgumentType.word())
                                                .suggests(DestinationCommands::suggestFacings)
                                                .executes(DestinationCommands::set)))))
                .then(literal("clear").executes(DestinationCommands::clear));
    }

    private static CompletableFuture<Suggestions> suggestFacings(CommandContext<CommandSourceStack> ctx,
                                                                 SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(FACINGS.keySet(), builder);
    }

    private static String facingName(float yaw) {
        for (Map.Entry<String, Float> entry : FACINGS.entrySet()) {
            if (entry.getValue() == yaw) return entry.getKey();
        }
        return yaw + " degrees";
    }

    private static int set(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerLevel level = DimensionArgument.getDimension(ctx, "dimension");
        BlockPos pos = BlockPosArgument.getBlockPos(ctx, "pos");
        String facing = StringArgumentType.getString(ctx, "facing");

        return withTardis(ctx, (tardis, idText) -> {
            Float yaw = FACINGS.get(facing);
            if (yaw == null) return fail(ctx, "destination_bad_facing", facing);
            return setTarget(ctx, tardis, level, pos, yaw, facing);
        });
    }

    private static int setHere(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            float yaw = Mth.positiveModulo(Math.round((player.getYRot() + 180f) / 45f) * 45f, 360f);
            return setTarget(ctx, tardis, ctx.getSource().getLevel(), player.blockPosition(), yaw, facingName(yaw));
        });
    }

    private static int setTarget(CommandContext<CommandSourceStack> ctx, Tardis tardis, ServerLevel level,
                                 BlockPos pos, float yaw, String facing) {
        if (TardisApi.idOfLevel(level).isPresent()) return fail(ctx, "destination_interior");
        if (level.isOutsideBuildHeight(pos) || level.isOutsideBuildHeight(pos.above())) {
            return fail(ctx, "destination_out_of_world");
        }

        manager(ctx).setLandingTarget(tardis.id(), new LandingTarget(level.dimension(), pos, yaw));
        return success(ctx, true, "destination_set", tardis.id().toString(), pos.toShortString(),
                level.dimension().identifier().toString(), facing);
    }

    private static int get(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            String id = tardis.id().toString();
            LandingTarget target = manager(ctx).getLandingTarget(tardis.id());
            if (target == null) return success(ctx, false, "destination_none", id);

            return success(ctx, false, "destination_get", id, target.pos().toShortString(),
                    target.dimension().identifier().toString(), facingName(target.yaw()));
        });
    }

    private static int clear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            manager(ctx).setLandingTarget(tardis.id(), null);
            return success(ctx, true, "destination_cleared", tardis.id().toString());
        });
    }
}
