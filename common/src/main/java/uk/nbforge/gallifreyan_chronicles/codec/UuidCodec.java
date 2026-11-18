package uk.nbforge.gallifreyan_chronicles.codec;

import com.mojang.serialization.Codec;
import java.util.UUID;

public final class UuidCodec {
    public static final Codec<UUID> CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    private UuidCodec() {}
}
