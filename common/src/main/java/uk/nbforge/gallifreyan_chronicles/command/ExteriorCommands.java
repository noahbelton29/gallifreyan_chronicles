package uk.nbforge.gallifreyan_chronicles.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static uk.nbforge.gallifreyan_chronicles.command.CommandUtil.*;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisExteriorRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.ExteriorLink;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade.FadeType;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade.FadeTypes;

public final class ExteriorCommands {
    private ExteriorCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> exterior() {
        return literal("exterior")
                .then(literal("link").executes(ExteriorCommands::link))
                .then(literal("unlink").executes(ExteriorCommands::unlink));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> exteriorData() {
        return literal("exterior")
                .executes(ExteriorCommands::get)
                .then(argument("exterior_id", StringArgumentType.greedyString())
                        .suggests(ExteriorCommands::suggestExteriors)
                        .executes(ExteriorCommands::set));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> glowData() {
        return literal("glow")
                .executes(ExteriorCommands::getGlow)
                .then(literal("on").executes(ctx -> setGlowing(ctx, true)))
                .then(literal("off").executes(ctx -> setGlowing(ctx, false)));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> fadeData() {
        return literal("fade")
                .executes(ExteriorCommands::getFade)
                .then(argument("fade_type", StringArgumentType.string())
                        .suggests(ExteriorCommands::suggestFadeTypes)
                        .executes(ExteriorCommands::setFade));
    }

    static CompletableFuture<Suggestions> suggestExteriors(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        TardisExteriorRegistry.getAll().forEach(exterior -> builder.suggest(exterior.id()));
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestFadeTypes(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        for (String id : FadeTypes.suggestionIds()) {
            builder.suggest(id.contains(":") ? "\"" + id + "\"" : id);
        }
        return builder.buildFuture();
    }

    static Optional<TardisExteriorBlockEntity> exteriorAt(CommandContext<CommandSourceStack> ctx, BlockPos pos) {
        return findBlockEntity(ctx, pos, TardisExteriorBlockEntity.class);
    }

    static int get(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            BlockPos pos = tardis.data().resolveExteriorBlockPos();
            String exteriorId =
                    exteriorAt(ctx, pos).map(be -> be.getExterior().id()).orElse("unknown");
            return success(ctx, false, "get_exterior", idText, exteriorId);
        });
    }

    static int set(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String exteriorId = StringArgumentType.getString(ctx, "exterior_id");
        return withTardis(ctx, (tardis, idText) -> {
            if (!TardisExteriorRegistry.contains(exteriorId)) return fail(ctx, "unknown_exterior", exteriorId);

            Optional<TardisExteriorBlockEntity> be =
                    exteriorAt(ctx, tardis.data().resolveExteriorBlockPos());
            if (be.isEmpty()) return fail(ctx, "no_exterior", idText);

            TardisExterior exterior = TardisExteriorRegistry.get(exteriorId);
            be.get().setExterior(exterior);
            return success(ctx, true, "set_exterior", idText, exteriorId);
        });
    }

    private static int link(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            Optional<BlockPos> pos = lookedAtBlock(ctx);
            if (pos.isEmpty() || exteriorAt(ctx, pos.get()).isEmpty()) {
                return fail(ctx, "not_looking_at_exterior");
            }
            ExteriorLink.link(tardis, ctx.getSource().getLevel(), pos.get());
            return success(ctx, true, "link_exterior", idText);
        });
    }

    private static int unlink(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            ExteriorLink.unlink(tardis);
            return success(ctx, true, "unlink_exterior", idText);
        });
    }

    private static int getGlow(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisExteriorBlockEntity> be =
                    exteriorAt(ctx, tardis.data().getRegisteredBlockPos());
            return be.map(tardisExteriorBlockEntity -> success(
                            ctx,
                            false,
                            "get_glow",
                            idText,
                            String.valueOf(tardisExteriorBlockEntity.lamp().isGlowing())))
                    .orElseGet(() -> fail(ctx, "no_exterior", idText));
        });
    }

    private static int setGlowing(CommandContext<CommandSourceStack> ctx, boolean glowing)
            throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisExteriorBlockEntity> be =
                    exteriorAt(ctx, tardis.data().getRegisteredBlockPos());
            if (be.isEmpty()) return fail(ctx, "no_tardis_id", idText);

            be.get().lamp().setGlowing(glowing);
            return success(ctx, true, "set_glow", idText, String.valueOf(glowing));
        });
    }

    static int getFade(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisExteriorBlockEntity> be =
                    exteriorAt(ctx, tardis.data().getRegisteredBlockPos());
            return be.map(tardisExteriorBlockEntity -> success(
                            ctx,
                            false,
                            "get_fade_type",
                            idText,
                            tardisExteriorBlockEntity.travel().getFadeType().id()))
                    .orElseGet(() -> fail(ctx, "no_exterior", idText));
        });
    }

    static int setFade(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String typeId = StringArgumentType.getString(ctx, "fade_type");
        if (FadeTypes.isUnknown(typeId)) {
            return fail(ctx, "unknown_fade_type", typeId, String.join(", ", FadeTypes.suggestionIds()));
        }
        FadeType type = FadeTypes.get(typeId);

        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisExteriorBlockEntity> be =
                    exteriorAt(ctx, tardis.data().getRegisteredBlockPos());
            if (be.isEmpty()) return fail(ctx, "no_exterior", idText);

            be.get().travel().setFadeType(type);
            return success(ctx, true, "set_fade_type", idText, type.id());
        });
    }
}
