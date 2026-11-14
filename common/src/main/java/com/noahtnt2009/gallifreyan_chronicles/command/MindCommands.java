package com.noahtnt2009.gallifreyan_chronicles.command;

import net.minecraft.server.level.ServerPlayer;
import java.util.Map;
import java.util.Locale;
import java.util.LinkedHashMap;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data.TardisPersonalities;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data.TardisTraits;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMind;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisPersonality;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisTrait;
import net.minecraft.commands.CommandSourceStack;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.minecraft.commands.arguments.EntityArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.noahtnt2009.gallifreyan_chronicles.command.CommandUtil.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class MindCommands {
    private MindCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> mind() {
        return literal("mind")
                .then(literal("inspect").executes(MindCommands::inspect))
                .then(literal("reroll").executes(MindCommands::reroll))
                .then(literal("undiscover").executes(MindCommands::undiscover))
                .then(literal("betrayed")
                        .then(argument("betrayed", BoolArgumentType.bool()).executes(MindCommands::setBetrayed)
                                .then(argument("player", EntityArgument.player()).executes(MindCommands::setBetrayed))))
                .then(literal("personality")
                        .then(literal("get").executes(MindCommands::getPersonality))
                        .then(literal("set")
                                .then(argument("personality", StringArgumentType.greedyString())
                                        .suggests(MindCommands::suggestPersonalities)
                                        .executes(MindCommands::setPersonality))))
                .then(literal("traits")
                        .then(literal("get").executes(MindCommands::getTraits))
                        .then(literal("add")
                                .then(argument("trait", StringArgumentType.greedyString())
                                        .suggests(MindCommands::suggestTraits)
                                        .executes(MindCommands::addTrait)))
                        .then(literal("remove")
                                .then(argument("trait", StringArgumentType.greedyString())
                                        .suggests(MindCommands::suggestTraits)
                                        .executes(MindCommands::removeTrait)))
                        .then(literal("clear").executes(MindCommands::clearTraits)))
                .then(literal("disabilities")
                        .then(literal("get").executes(MindCommands::getDisabilities))
                        .then(literal("add")
                                .then(argument("disability", StringArgumentType.word())
                                        .suggests(MindCommands::suggestDisabilities)
                                        .executes(ctx -> changeDisability(ctx, true))))
                        .then(literal("remove")
                                .then(argument("disability", StringArgumentType.word())
                                        .suggests(MindCommands::suggestDisabilities)
                                        .executes(ctx -> changeDisability(ctx, false))))
                        .then(literal("clear").executes(MindCommands::clearDisabilities)))
                .then(value(MindValue.SKILL))
                .then(value(MindValue.TRAUMA))
                .then(value(MindValue.TOTAL_TRAUMA))
                .then(value(MindValue.HITS))
                .then(literal("forgive").executes(MindCommands::forgive)
                        .then(argument("player", EntityArgument.player()).executes(MindCommands::forgive)))
                .then(value(MindValue.STRESS))
                .then(value(MindValue.TRUST))
                .then(value(MindValue.FEAR));
    }

    private enum MindValue {
        STRESS("stress"), TRUST("trust"), FEAR("fear"), SKILL("skill"), TRAUMA("trauma"),
        TOTAL_TRAUMA("total_trauma"), HITS("hits");

        private final String name;

        MindValue(String name) {
            this.name = name;
        }

        /* trust and fear are per player, null means the owner */
        private boolean perPlayer() {
            return this == TRUST || this == FEAR || this == TRAUMA || this == HITS;
        }

        private float get(Tardis tardis, @Nullable UUID player) {
            return switch (this) {
                case STRESS -> TardisMindSystem.stress(tardis);
                case TRUST -> TardisMindSystem.trust(tardis, player);
                case FEAR -> TardisMindSystem.fear(tardis, player);
                case SKILL -> TardisMindSystem.skill(tardis) * 100f;
                case TRAUMA -> TardisMindSystem.traumaFrom(tardis, player);
                case TOTAL_TRAUMA -> TardisMindSystem.trauma(tardis);
                case HITS -> TardisMindSystem.bond(tardis, player).hits();
            };
        }

        private void set(Tardis tardis, @Nullable UUID player, float value) {
            switch (this) {
                case STRESS -> TardisMindSystem.setStress(tardis, value);
                case TRUST -> TardisMindSystem.setTrust(tardis, player, value);
                case FEAR -> {
                    /* trauma keeps fear from going below a floor, so lower it enough for the new fear to stick */
                    float maxTrauma = value / TardisMind.TRAUMA_FEAR_SHARE;
                    if (TardisMindSystem.traumaFrom(tardis, player) > maxTrauma) {
                        TardisMindSystem.setTraumaFrom(tardis, player, maxTrauma);
                    }
                    TardisMindSystem.setFear(tardis, player, value);
                }
                case SKILL -> TardisMindSystem.setSkill(tardis, value / 100f);
                case TRAUMA -> TardisMindSystem.setTraumaFrom(tardis, player, value);
                case TOTAL_TRAUMA -> TardisMindSystem.setTrauma(tardis, value);
                case HITS -> TardisMindSystem.setHits(tardis, player, Math.round(value));
            }
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> value(MindValue which) {
        var get = literal("get").executes(ctx -> getValue(ctx, which));
        var setValue = argument("value", FloatArgumentType.floatArg(0f, 100f)).executes(ctx -> setValue(ctx, which));
        if (which.perPlayer()) {
            get.then(argument("player", EntityArgument.player()).executes(ctx -> getValue(ctx, which)));
            setValue.then(argument("player", EntityArgument.player()).executes(ctx -> setValue(ctx, which)));
        }
        return literal(which.name).then(get).then(literal("set").then(setValue));
    }

    private static CompletableFuture<Suggestions> suggestPersonalities(CommandContext<CommandSourceStack> ctx,
                                                                       SuggestionsBuilder builder) {
        for (TardisPersonality personality : TardisPersonalities.all()) {
            builder.suggest(shortId(personality.id()));
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestTraits(CommandContext<CommandSourceStack> ctx,
                                                                SuggestionsBuilder builder) {
        for (TardisTrait trait : TardisTraits.all()) {
            builder.suggest(shortId(trait.id()));
        }
        return builder.buildFuture();
    }

    /* everything about its mind, and how it feels about every player it knows */
    private static int inspect(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            TardisMind mind = TardisMindSystem.mind(tardis);
            long now = TardisMindSystem.now(tardis.server());
            Map<String, String> lines = new LinkedHashMap<>();

            lines.put("personality", shortId(mind.personality().id()));
            lines.put("traits", traitList(mind));
            lines.put("disabilities", disabilityList(mind));
            lines.put("skill", oneDecimal(mind.skill() * 100f));
            lines.put("stress", oneDecimal(TardisMindSystem.stress(tardis)));
            lines.put("trauma (total)", oneDecimal(TardisMindSystem.trauma(tardis)));
            lines.put("ptsd recovery", mind.has(TardisDisability.PTSD) ? oneDecimal(mind.recovery() * 100f) + "%" : "no ptsd");
            lines.put("willing to fly", String.valueOf(!TardisMindSystem.unwilling(tardis)));
            lines.put("discovered", mind.discovered().isEmpty() ? "nothing" : String.join(", ", mind.discovered()));

            success(ctx, false, "mind_header", idText);
            lines.forEach((key, value) -> success(ctx, false, "data_line", key, value));

            if (mind.bonds().isEmpty()) return success(ctx, false, "mind_no_bonds");
            mind.bonds().forEach((player, bond) -> success(ctx, false, "mind_bond",
                    playerName(tardis, player) + (tardis.isOwner(player) ? " (owner)" : ""),
                    oneDecimal(bond.trust()),
                    oneDecimal(TardisMindSystem.fear(tardis, player)),
                    TardisMindSystem.fearLevel(tardis, player).name().toLowerCase(Locale.ROOT),
                    oneDecimal(bond.traumaAt(now)),
                    bond.hits(),
                    bond.betrayed()));
            return 1;
        });
    }

    private static String playerName(Tardis tardis, UUID player) {
        ServerPlayer online = tardis.server().getPlayerList().getPlayer(player);
        return online != null ? online.getName().getString() : player.toString();
    }

    private static int reroll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            TardisMindSystem.reroll(tardis);
            return inspect(ctx);
        });
    }

    private static int setBetrayed(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        boolean betrayed = BoolArgumentType.getBool(ctx, "betrayed");
        UUID player = playerArg(ctx);
        return withTardis(ctx, (tardis, idText) -> {
            TardisMindSystem.setBetrayed(tardis, player, betrayed);
            return reply(ctx, tardis, "betrayed", String.valueOf(betrayed), true);
        });
    }

    private static int forgive(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        UUID player = playerArg(ctx);
        return withTardis(ctx, (tardis, idText) -> {
            TardisMindSystem.forgive(tardis, player);
            return reply(ctx, tardis, "fear, trauma, hits and betrayal", "cleared", true);
        });
    }

    private static int undiscover(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            TardisMindSystem.setDiscovered(tardis, List.of());
            return reply(ctx, tardis, "discovered", "none", true);
        });
    }

    /* the optional player argument, or null for the owner */
    private static @Nullable UUID playerArg(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        try {
            return EntityArgument.getPlayer(ctx, "player").getUUID();
        } catch (IllegalArgumentException missing) {
            return null;
        }
    }

    private static int getValue(CommandContext<CommandSourceStack> ctx, MindValue which) throws CommandSyntaxException {
        UUID player = playerArg(ctx);
        return withTardis(ctx, (tardis, idText) -> reply(ctx, tardis, which.name, oneDecimal(which.get(tardis, player)), false));
    }

    private static int setValue(CommandContext<CommandSourceStack> ctx, MindValue which) throws CommandSyntaxException {
        float value = FloatArgumentType.getFloat(ctx, "value");
        UUID player = playerArg(ctx);
        return withTardis(ctx, (tardis, idText) -> {
            which.set(tardis, player, value);
            return reply(ctx, tardis, which.name, String.valueOf(value), true);
        });
    }

    private static int getPersonality(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) ->
                reply(ctx, tardis, "personality", shortId(TardisMindSystem.mind(tardis).personality().id()), false));
    }

    private static int setPersonality(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "personality");
        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisPersonality> personality = TardisPersonalities.byId(id);
            if (personality.isEmpty()) return fail(ctx, "unknown_personality", id);

            TardisMindSystem.setPersonality(tardis, personality.get());
            return reply(ctx, tardis, "personality", id, true);
        });
    }

    private static int getTraits(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) ->
                reply(ctx, tardis, "traits", traitList(TardisMindSystem.mind(tardis)), false));
    }

    private static int addTrait(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "trait");
        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisTrait> trait = TardisTraits.byId(id);
            if (trait.isEmpty()) return fail(ctx, "unknown_trait", id);

            List<TardisTrait> traits = new ArrayList<>(TardisMindSystem.mind(tardis).traits());
            if (traits.contains(trait.get())) return fail(ctx, "trait_already", id);
            if (traits.size() >= TardisMind.MAX_TRAITS) return fail(ctx, "traits_full", TardisMind.MAX_TRAITS);
            TardisPersonality personality = TardisMindSystem.mind(tardis).personality();
            if (!trait.get().fits(personality)) return fail(ctx, "trait_doesnt_fit", id, shortId(personality.id()));
            Optional<TardisTrait> clash = traits.stream().filter(trait.get()::clashesWith).findFirst();
            if (clash.isPresent()) return fail(ctx, "trait_clashes", id, shortId(clash.get().id()));

            traits.add(trait.get());
            return saveTraits(ctx, tardis, traits);
        });
    }

    private static int removeTrait(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "trait");
        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisTrait> trait = TardisTraits.byId(id);
            if (trait.isEmpty()) return fail(ctx, "unknown_trait", id);

            List<TardisTrait> traits = new ArrayList<>(TardisMindSystem.mind(tardis).traits());
            if (!traits.remove(trait.get())) return fail(ctx, "trait_missing", id);

            return saveTraits(ctx, tardis, traits);
        });
    }

    private static int clearTraits(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> saveTraits(ctx, tardis, List.of()));
    }

    private static int saveTraits(CommandContext<CommandSourceStack> ctx, Tardis tardis, List<TardisTrait> traits) {
        TardisMindSystem.setTraits(tardis, traits);
        return reply(ctx, tardis, "traits", traitList(TardisMindSystem.mind(tardis)), true);
    }

    private static int reply(CommandContext<CommandSourceStack> ctx, Tardis tardis, String what, String value,
                             boolean changed) {
        return success(ctx, changed, changed ? "set_mind" : "get_mind", what, tardis.id().toString(), value);
    }

    private static CompletableFuture<Suggestions> suggestDisabilities(CommandContext<CommandSourceStack> ctx,
                                                                      SuggestionsBuilder builder) {
        for (TardisDisability disability : TardisDisability.values()) {
            builder.suggest(disability.id());
        }
        return builder.buildFuture();
    }

    private static int getDisabilities(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) ->
                reply(ctx, tardis, "disabilities", disabilityList(TardisMindSystem.mind(tardis)), false));
    }

    private static int changeDisability(CommandContext<CommandSourceStack> ctx, boolean add) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "disability");
        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisDisability> disability = TardisDisability.byId(id);
            if (disability.isEmpty()) return fail(ctx, "unknown_disability", id);

            List<TardisDisability> disabilities = new ArrayList<>(TardisMindSystem.mind(tardis).disabilities());
            if (add && disabilities.contains(disability.get())) return fail(ctx, "disability_already", id);
            if (!add && !disabilities.contains(disability.get())) return fail(ctx, "disability_missing", id);

            if (add) disabilities.add(disability.get());
            else disabilities.remove(disability.get());
            return saveDisabilities(ctx, tardis, disabilities);
        });
    }

    private static int clearDisabilities(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> saveDisabilities(ctx, tardis, List.of()));
    }

    private static int saveDisabilities(CommandContext<CommandSourceStack> ctx, Tardis tardis,
                                        List<TardisDisability> disabilities) {
        TardisMindSystem.setDisabilities(tardis, disabilities);
        return reply(ctx, tardis, "disabilities", disabilityList(TardisMindSystem.mind(tardis)), true);
    }

    private static String disabilityList(TardisMind mind) {
        if (mind.disabilities().isEmpty()) return "none";
        return String.join(", ", mind.disabilities().stream().map(TardisDisability::id).toList());
    }

    private static String traitList(TardisMind mind) {
        if (mind.traits().isEmpty()) return "none";
        return String.join(", ", mind.traits().stream().map(trait -> shortId(trait.id())).toList());
    }

    private static String shortId(String id) {
        String prefix = Constants.MOD_ID + ":";
        return id.startsWith(prefix) ? id.substring(prefix.length()) : id;
    }

    private static String oneDecimal(float value) {
        return String.format("%.1f", value);
    }
}
