package uk.nbforge.gallifreyan_chronicles.tardis.door;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import uk.nbforge.gallifreyan_chronicles.loader.IdentifiableData;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public record TardisDoorVariant(
        String id,
        String parent,
        String texture,
        boolean isDefault,
        boolean interior,
        String exterior,
        String openSound,
        String closeSound,
        boolean openAllOnly,
        int priority,
        String block,
        String particle,
        float cooldownSeconds)
        implements IdentifiableData<TardisDoorVariant> {

    public static final String FOLDER = "tardis_door_variant";
    public static final int NO_PRIORITY = Integer.MAX_VALUE;
    public static final float DEFAULT_COOLDOWN_SECONDS = 3.0f;

    public static final Codec<TardisDoorVariant> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                    Codec.STRING.optionalFieldOf("id", "").forGetter(TardisDoorVariant::id),
                    Codec.STRING.fieldOf("parent").forGetter(TardisDoorVariant::parent),
                    Codec.STRING.fieldOf("texture").forGetter(TardisDoorVariant::texture),
                    Codec.BOOL.optionalFieldOf("default", false).forGetter(TardisDoorVariant::isDefault),
                    Codec.BOOL.optionalFieldOf("interior", false).forGetter(TardisDoorVariant::interior),
                    Codec.STRING.optionalFieldOf("exterior", "").forGetter(TardisDoorVariant::exterior),
                    Codec.STRING.optionalFieldOf("open_sound", "").forGetter(TardisDoorVariant::openSound),
                    Codec.STRING.optionalFieldOf("close_sound", "").forGetter(TardisDoorVariant::closeSound),
                    Codec.BOOL.optionalFieldOf("open_all_only", false).forGetter(TardisDoorVariant::openAllOnly),
                    Codec.INT
                            .optionalFieldOf("priority", TardisDoorVariant.NO_PRIORITY)
                            .forGetter(TardisDoorVariant::priority),
                    Codec.STRING.optionalFieldOf("block", "").forGetter(TardisDoorVariant::block),
                    Codec.STRING.optionalFieldOf("particle", "").forGetter(TardisDoorVariant::particle),
                    Codec.FLOAT
                            .optionalFieldOf("cooldown_seconds", TardisDoorVariant.DEFAULT_COOLDOWN_SECONDS)
                            .forGetter(TardisDoorVariant::cooldownSeconds))
            .apply(inst, TardisDoorVariant::new));

    public static final StreamCodec<ByteBuf, TardisDoorVariant> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public String localId() {
        return GCUtil.localPath(id);
    }

    @Override
    public TardisDoorVariant withId(String id) {
        return new TardisDoorVariant(
                id,
                parent,
                texture,
                isDefault,
                interior,
                exterior,
                openSound,
                closeSound,
                openAllOnly,
                priority,
                block,
                particle,
                cooldownSeconds);
    }
}
