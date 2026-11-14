package uk.nbforge.gallifreyan_chronicles.codec.client.model;

import com.mojang.serialization.Codec;
import uk.nbforge.gallifreyan_chronicles.client.model.ConsoleVariantSelectProperty;

public final class ConsoleVariantSelectPropertyCodec {
    public static final Codec<String> VALUE_CODEC = Codec.STRING;

    private ConsoleVariantSelectPropertyCodec() {
    }
}
