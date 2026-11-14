package uk.nbforge.gallifreyan_chronicles.tardis.mind.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Arrays;
import java.util.Locale;

/* enums json can name, plus a codec helper for them */
public final class MindEnums {
    private MindEnums() {
    }

    /* when a trait first shows itself */
    public enum Reveal {
        IT_GETS_STRESSED, IT_LOSES_TRUST, IT_REFUSES, A_STRANGER_UPSETS_IT, IT_CRASHES, IT_LANDS_UPSET,
        ITS_CONTROLS_STICK, IT_IS_HIT, IT_SEES_THE_MALLET, NEVER
    }

    public static <E extends Enum<E>> Codec<E> codec(Class<E> type) {
        return Codec.STRING.comapFlatMap(name -> Arrays.stream(type.getEnumConstants())
                        .filter(value -> value.name().equalsIgnoreCase(name))
                        .findFirst()
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(() -> "unknown " + type.getSimpleName() + " " + name)),
                value -> value.name().toLowerCase(Locale.ROOT));
    }
}
