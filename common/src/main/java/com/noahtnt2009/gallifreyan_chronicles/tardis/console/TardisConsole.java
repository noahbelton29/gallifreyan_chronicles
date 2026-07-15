package com.noahtnt2009.gallifreyan_chronicles.tardis.console;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.data.IdentifiableData;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record TardisConsole(String id, List<ControlEntry> controls, Map<String, String> variants, String defaultVariant) implements IdentifiableData<TardisConsole> {
    public record ControlEntry(String id, Vec3 offset, float width, float height, float depth) {
        private static final Codec<Vec3> VEC3_CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.DOUBLE.fieldOf("x").forGetter(Vec3::x),
                Codec.DOUBLE.fieldOf("y").forGetter(Vec3::y),
                Codec.DOUBLE.fieldOf("z").forGetter(Vec3::z)
        ).apply(inst, Vec3::new));

        public static final Codec<ControlEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.fieldOf("id").forGetter(ControlEntry::id),
                VEC3_CODEC.fieldOf("offset").forGetter(ControlEntry::offset),
                Codec.FLOAT.optionalFieldOf("width", TardisControlEntity.DEFAULT_WIDTH).forGetter(ControlEntry::width),
                Codec.FLOAT.optionalFieldOf("height", TardisControlEntity.DEFAULT_HEIGHT).forGetter(ControlEntry::height),
                Codec.FLOAT.optionalFieldOf("depth").forGetter(entry -> java.util.Optional.of(entry.depth))
        ).apply(inst, (id, offset, width, height, depth) ->
                new ControlEntry(id, offset, width, height, depth.orElse(width))));

        public static final StreamCodec<FriendlyByteBuf, ControlEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, ControlEntry::id,
                ByteBufCodecs.DOUBLE, e -> e.offset().x,
                ByteBufCodecs.DOUBLE, e -> e.offset().y,
                ByteBufCodecs.DOUBLE, e -> e.offset().z,
                ByteBufCodecs.FLOAT, ControlEntry::width,
                ByteBufCodecs.FLOAT, ControlEntry::height,
                ByteBufCodecs.FLOAT, ControlEntry::depth,
                (id, x, y, z, width, height, depth) -> new ControlEntry(id, new Vec3(x, y, z), width, height, depth)
        );
    }

    public TardisConsole(String id) {
        this(id, List.of(), Map.of(), "");
    }

    public TardisConsole(String id, List<ControlEntry> controls) {
        this(id, controls, Map.of(), "");
    }


    private static final Codec<Map<String, String>> VARIANTS_CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.STRING);

    public static final Codec<TardisConsole> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("id").forGetter(TardisConsole::id),
            ControlEntry.CODEC.listOf().optionalFieldOf("controls", List.of()).forGetter(TardisConsole::controls),
            VARIANTS_CODEC.optionalFieldOf("variants", Map.of()).forGetter(TardisConsole::variants),
            Codec.STRING.optionalFieldOf("default_variant", "").forGetter(TardisConsole::defaultVariant)
    ).apply(inst, TardisConsole::new));

    public static final StreamCodec<FriendlyByteBuf, TardisConsole> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TardisConsole::id,
            ControlEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), TardisConsole::controls,
            ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8), TardisConsole::variants,
            ByteBufCodecs.STRING_UTF8, TardisConsole::defaultVariant,
            TardisConsole::new
    );

    public boolean hasVariants() {
        return !variants.isEmpty();
    }

    public String resolvedDefaultVariant() {
        if (!defaultVariant.isEmpty() && variants.containsKey(defaultVariant)) return defaultVariant;
        if (!variants.isEmpty()) return variants.keySet().iterator().next();
        return "";
    }

    public boolean hasVariant(String variantId) {
        return variants.containsKey(variantId);
    }

    public String textureIdFor(String variantId) {
        String texture = variants.get(variantId);
        return texture != null ? texture : id;
    }

    @Override
    public TardisConsole withId(String id) {
        return new TardisConsole(id, controls, variants, defaultVariant);
    }
}
