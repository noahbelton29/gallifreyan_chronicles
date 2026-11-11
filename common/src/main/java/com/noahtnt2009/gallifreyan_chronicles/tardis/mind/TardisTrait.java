package com.noahtnt2009.gallifreyan_chronicles.tardis.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum TardisTrait {
    SKITTISH("skittish"),
    LOYAL("loyal"),
    STUBBORN("stubborn"),
    FORGIVING("forgiving"),
    GRUDGE_HOLDER("grudge_holder"),
    PROTECTIVE("protective"),
    POSSESSIVE("possessive"),
    WELCOMING("welcoming"),
    DISTRUSTING("distrusting");

    public static final Codec<TardisTrait> CODEC = Codec.STRING.comapFlatMap(
            id -> byId(id).map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "unknown tardis trait " + id)),
            TardisTrait::id);

    private final String id;

    TardisTrait(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public boolean clashesWith(TardisTrait other) {
        return clash(this, other) || clash(other, this);
    }

    private static boolean clash(TardisTrait a, TardisTrait b) {
        return (a == WELCOMING && b == POSSESSIVE) || (a == FORGIVING && b == GRUDGE_HOLDER)
                || (a == DISTRUSTING && (b == LOYAL || b == WELCOMING));
    }

    public boolean fits(TardisPersonality personality) {
        return switch (personality) {
            case OBEDIENT -> this != STUBBORN && this != POSSESSIVE;
            case SUBMISSIVE -> this != STUBBORN;
            case DEFENSIVE -> this != WELCOMING;
            case AGGRESSIVE -> this != WELCOMING && this != FORGIVING && this != PROTECTIVE;
        };
    }

    public boolean fits(TardisPersonality personality, List<TardisTrait> others) {
        return fits(personality) && others.stream().noneMatch(this::clashesWith);
    }

    public static Optional<TardisTrait> byId(String id) {
        return Arrays.stream(values()).filter(t -> t.id.equals(id)).findFirst();
    }
}
