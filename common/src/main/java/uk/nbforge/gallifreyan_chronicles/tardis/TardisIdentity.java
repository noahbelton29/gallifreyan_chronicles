package uk.nbforge.gallifreyan_chronicles.tardis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import uk.nbforge.gallifreyan_chronicles.codec.UuidCodec;

public record TardisIdentity(UUID tardisId, UUID ownerId, String ownerName) {
    public static final Codec<TardisIdentity> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                    UuidCodec.CODEC.fieldOf("tardis_id").forGetter(TardisIdentity::tardisId),
                    UuidCodec.CODEC.fieldOf("owner_id").forGetter(TardisIdentity::ownerId),
                    Codec.STRING.optionalFieldOf("owner_name", "").forGetter(TardisIdentity::ownerName))
            .apply(inst, TardisIdentity::new));
}
