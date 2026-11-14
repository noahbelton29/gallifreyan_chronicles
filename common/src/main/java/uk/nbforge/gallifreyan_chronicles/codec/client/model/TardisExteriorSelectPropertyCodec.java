package uk.nbforge.gallifreyan_chronicles.codec.client.model;

import com.mojang.serialization.Codec;
import uk.nbforge.gallifreyan_chronicles.client.model.TardisExteriorSelectProperty;

public final class TardisExteriorSelectPropertyCodec {
    public static final Codec<String> VALUE_CODEC = Codec.STRING;

    private TardisExteriorSelectPropertyCodec() {
    }
}
