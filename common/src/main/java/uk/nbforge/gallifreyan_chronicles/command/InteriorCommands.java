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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisDoorRegistry;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisInteriorRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.interior.TardisInteriors;

public final class InteriorCommands {
    private InteriorCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> interiorData() {
        return literal("interior")
                .executes(InteriorCommands::get)
                .then(argument("interior_id", StringArgumentType.greedyString())
                        .suggests(InteriorCommands::suggestInteriors)
                        .executes(InteriorCommands::set));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> doorVariantData() {
        return literal("door")
                .then(literal("variant")
                        .executes(InteriorCommands::getDoorVariant)
                        .then(argument("variant_id", StringArgumentType.greedyString())
                                .suggests(InteriorCommands::suggestDoorVariants)
                                .executes(InteriorCommands::setDoorVariant)));
    }

    private static CompletableFuture<Suggestions> suggestInteriors(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        TardisInteriorRegistry.getAll().forEach(interior -> builder.suggest(interior.id()));
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestDoorVariants(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        idArgument(ctx)
                .flatMap(id -> Tardis.get(ctx.getSource().getServer(), id))
                .flatMap(tardis -> tardis.interiorDoors().stream().findFirst())
                .ifPresent(door -> {
                    List<String> ids = door.isLinked()
                            ? TardisDoorRegistry.interiorVariantIds()
                            : TardisDoorRegistry.variantIds(door.getVariant());
                    ids.forEach(builder::suggest);
                });
        return builder.buildFuture();
    }

    static int get(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            String interiorId = tardis.interior().id();
            return success(ctx, false, "get_interior", idText, interiorId);
        });
    }

    private static int set(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String interiorId = StringArgumentType.getString(ctx, "interior_id");
        return withTardis(ctx, (tardis, idText) -> switch (TardisInteriors.changeInterior(tardis, interiorId)) {
            case UNKNOWN_INTERIOR -> fail(ctx, "unknown_interior", interiorId);
            case MISSING_TEMPLATE -> fail(ctx, "missing_interior_template", interiorId);
            case UNCHANGED -> fail(ctx, "interior_unchanged", idText, interiorId);
            case CHANGED -> success(ctx, true, "set_interior", idText, interiorId);
        });
    }

    static int getDoorVariant(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardisId(ctx, (id, idText) -> {
            List<InteriorDoorBlockEntity> doors = Tardis.get(ctx.getSource().getServer(), id)
                    .map(Tardis::interiorDoors)
                    .orElse(List.of());
            if (doors.isEmpty()) return fail(ctx, "no_interior_door", idText);
            return success(ctx, false, "get_door_variant", idText, doors.get(0).getDoorVariantId());
        });
    }

    private static int setDoorVariant(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String variantId = StringArgumentType.getString(ctx, "variant_id");
        return withTardisId(ctx, (id, idText) -> {
            List<InteriorDoorBlockEntity> doors = Tardis.get(ctx.getSource().getServer(), id)
                    .map(Tardis::interiorDoors)
                    .orElse(List.of());
            if (doors.isEmpty()) return fail(ctx, "no_interior_door", idText);

            int changed = 0;
            for (InteriorDoorBlockEntity door : doors) {
                if (isUnknownVariant(door, variantId)) {
                    fail(ctx, "unknown_door_variant", variantId, door.getVariant());
                    continue;
                }
                door.setDoorVariant(variantId);
                changed++;
            }
            if (changed == 0) return 0;

            success(ctx, true, "set_door_variant", idText, variantId, changed);
            return changed;
        });
    }

    private static boolean isUnknownVariant(InteriorDoorBlockEntity door, String variantId) {
        if (door.isLinked()) return !TardisDoorRegistry.isInteriorVariant(variantId);
        return !TardisDoorRegistry.hasVariant(door.getVariant(), variantId);
    }
}
