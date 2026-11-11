package com.noahtnt2009.gallifreyan_chronicles.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.InteriorDoorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.door.TardisDoorRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInteriorRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInteriorSystem;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.noahtnt2009.gallifreyan_chronicles.command.CommandUtil.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class InteriorCommands {
    private InteriorCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> interior() {
        return literal("interior")
                .then(literal("get").executes(InteriorCommands::get))
                .then(literal("set")
                        .then(argument("interior_id", StringArgumentType.greedyString())
                                .suggests(InteriorCommands::suggestInteriors)
                                .executes(InteriorCommands::set)));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> door() {
        return literal("door")
                .then(literal("variant")
                        .then(literal("set")
                                .then(argument("variant_id", StringArgumentType.greedyString())
                                        .suggests(InteriorCommands::suggestDoorVariants)
                                        .executes(InteriorCommands::setDoorVariant))));
    }

    private static CompletableFuture<Suggestions> suggestInteriors(CommandContext<CommandSourceStack> ctx,
                                                                   SuggestionsBuilder builder) {
        TardisInteriorRegistry.getAll().forEach(interior -> builder.suggest(interior.id()));
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestDoorVariants(CommandContext<CommandSourceStack> ctx,
                                                                      SuggestionsBuilder builder) {
        idArgument(ctx).flatMap(id -> DoorSyncSystem.doorsOf(id).stream().findFirst()).ifPresent(door -> {
            List<String> ids = door.isLinked()
                    ? TardisDoorRegistry.interiorVariantIds()
                    : TardisDoorRegistry.variantIds(door.getVariant());
            ids.forEach(builder::suggest);
        });
        return builder.buildFuture();
    }

    private static int get(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            String interiorId = TardisInteriorSystem.getInteriorDefinition(tardis.server(), tardis.id()).id();
            return success(ctx, false, "get_interior", idText, interiorId);
        });
    }

    private static int set(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String interiorId = StringArgumentType.getString(ctx, "interior_id");
        return withTardis(ctx, (tardis, idText) -> switch (TardisInteriorSystem.changeInterior(tardis.server(), tardis.id(), interiorId)) {
            case UNKNOWN_INTERIOR -> fail(ctx, "unknown_interior", interiorId);
            case MISSING_TEMPLATE -> fail(ctx, "missing_interior_template", interiorId);
            case UNCHANGED -> fail(ctx, "interior_unchanged", idText, interiorId);
            case CHANGED -> success(ctx, true, "set_interior", idText, interiorId);
        });
    }

    private static int setDoorVariant(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String variantId = StringArgumentType.getString(ctx, "variant_id");
        return withTardisId(ctx, (id, idText) -> {
            List<InteriorDoorBlockEntity> doors = DoorSyncSystem.doorsOf(id);
            if (doors.isEmpty()) return fail(ctx, "no_interior_door", idText);

            int changed = 0;
            for (InteriorDoorBlockEntity door : doors) {
                if (!canUseVariant(door, variantId)) {
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

    private static boolean canUseVariant(InteriorDoorBlockEntity door, String variantId) {
        if (door.isLinked()) return TardisDoorRegistry.isInteriorVariant(variantId);
        return TardisDoorRegistry.hasVariant(door.getVariant(), variantId);
    }
}
