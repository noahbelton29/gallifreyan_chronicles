package uk.nbforge.gallifreyan_chronicles.codec.client.model;

import com.mojang.serialization.Codec;

public final class ConsoleVariantSelectPropertyCodec {
    public static final Codec<String> VALUE_CODEC = Codec.STRING;

    private ConsoleVariantSelectPropertyCodec() {}
}
