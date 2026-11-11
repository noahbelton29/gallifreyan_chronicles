package com.noahtnt2009.gallifreyan_chronicles.tardis.mind;

import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record TardisMind(TardisPersonality personality, List<TardisTrait> traits, float stress, float trust,
                         long stressTick, long lastLand, float fear, long fearTick,
                         List<TardisDisability> disabilities, float skill, List<String> discovered,
                         boolean betrayed, float trauma) {
    public static final float MAX = 100f;
    public static final float START_TRUST = 50f;
    public static final float DISTRUSTING_START_TRUST = 3f;
    public static final long NEVER = Long.MIN_VALUE;
    public static final int MAX_TRAITS = 3;
    private static final long TICKS_PER_STRESS_POINT = 200L;
    private static final float SKITTISH_CALM_SPEED = 2f;
    private static final long TICKS_PER_FEAR_POINT = 600L;
    private static final float FORGIVING_FADE_SPEED = 2f;
    private static final float GRUDGE_FADE_SPEED = 0.5f;
    private static final float TRAUMA_FEAR_SHARE = 0.6f;
    private static final float PTSD_FEAR_FLOOR = 50f;

    public static final Codec<TardisMind> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            TardisPersonality.CODEC.fieldOf("personality").forGetter(TardisMind::personality),
            TardisTrait.CODEC.listOf().optionalFieldOf("traits", List.of()).forGetter(TardisMind::traits),
            Codec.FLOAT.optionalFieldOf("stress", 0f).forGetter(TardisMind::stress),
            Codec.FLOAT.optionalFieldOf("trust", START_TRUST).forGetter(TardisMind::trust),
            Codec.LONG.optionalFieldOf("stress_tick", 0L).forGetter(TardisMind::stressTick),
            Codec.LONG.optionalFieldOf("last_land", NEVER).forGetter(TardisMind::lastLand),
            Codec.FLOAT.optionalFieldOf("fear", 0f).forGetter(TardisMind::fear),
            Codec.LONG.optionalFieldOf("fear_tick", 0L).forGetter(TardisMind::fearTick),
            TardisDisability.CODEC.listOf().optionalFieldOf("disabilities", List.of()).forGetter(TardisMind::disabilities),
            Codec.FLOAT.optionalFieldOf("skill", 0f).forGetter(TardisMind::skill),
            Codec.STRING.listOf().optionalFieldOf("discovered", List.of()).forGetter(TardisMind::discovered),
            Codec.BOOL.optionalFieldOf("betrayed", false).forGetter(TardisMind::betrayed),
            Codec.FLOAT.optionalFieldOf("trauma", 0f).forGetter(TardisMind::trauma)
    ).apply(inst, TardisMind::new));

    public TardisMind {
        traits = List.copyOf(traits);
        disabilities = List.copyOf(disabilities);
        discovered = List.copyOf(discovered);
        skill = Math.clamp(skill, 0f, 1f);
        stress = Math.clamp(stress, 0f, MAX);
        trust = Math.clamp(trust, 0f, MAX);
        fear = Math.clamp(fear, 0f, MAX);
        trauma = Math.clamp(trauma, 0f, MAX);
    }

    public boolean has(TardisTrait trait) {
        return traits.contains(trait);
    }

    public boolean has(TardisDisability disability) {
        return disabilities.contains(disability);
    }

    public float stressAt(long now) {
        float calmSpeed = (has(TardisTrait.SKITTISH) ? SKITTISH_CALM_SPEED : 1f) * fadeSpeed();
        float pointsCalmed = Math.max(0L, now - stressTick) / (float) TICKS_PER_STRESS_POINT * calmSpeed;
        return Math.max(0f, stress - pointsCalmed);
    }

    public float fearAt(long now) {
        float pointsFaded = Math.max(0L, now - fearTick) / (float) TICKS_PER_FEAR_POINT * fadeSpeed();
        return Math.max(traumaFloor(), fear - pointsFaded);
    }

    /* a traumatised tardis never stops being a bit scared of you */
    public float traumaFloor() {
        float floor = has(TardisDisability.PTSD) ? Math.max(trauma, PTSD_FEAR_FLOOR) : trauma;
        return floor * TRAUMA_FEAR_SHARE;
    }

    private float fadeSpeed() {
        if (has(TardisTrait.FORGIVING)) return FORGIVING_FADE_SPEED;
        if (has(TardisTrait.GRUDGE_HOLDER)) return GRUDGE_FADE_SPEED;
        return 1f;
    }

    public TardisMind withFear(float value, long now) {
        return new TardisMind(personality, traits, stress, trust, stressTick, lastLand, value, now, disabilities, skill, discovered, betrayed, trauma);
    }

    public TardisMind withStress(float value, long now) {
        return new TardisMind(personality, traits, value, trust, now, lastLand, fear, fearTick, disabilities, skill, discovered, betrayed, trauma);
    }

    public TardisMind withTrust(float value) {
        return new TardisMind(personality, traits, stress, value, stressTick, lastLand, fear, fearTick, disabilities, skill, discovered, betrayed, trauma);
    }

    public TardisMind withPersonality(TardisPersonality value) {
        List<TardisTrait> kept = traits.stream().filter(trait -> trait.fits(value)).toList();
        return new TardisMind(value, kept, stress, trust, stressTick, lastLand, fear, fearTick, disabilities, skill, discovered, betrayed, trauma);
    }

    public TardisMind withTraits(List<TardisTrait> value) {
        return new TardisMind(personality, value, stress, trust, stressTick, lastLand, fear, fearTick, disabilities, skill, discovered, betrayed, trauma);
    }

    public TardisMind withDisabilities(List<TardisDisability> value) {
        return new TardisMind(personality, traits, stress, trust, stressTick, lastLand, fear, fearTick, value, skill, discovered, betrayed, trauma);
    }

    public TardisMind withSkill(float value) {
        return new TardisMind(personality, traits, stress, trust, stressTick, lastLand, fear, fearTick, disabilities, value, discovered, betrayed, trauma);
    }

    public boolean knows(String key) {
        return discovered.contains(key);
    }

    public TardisMind withDiscovered(List<String> value) {
        return new TardisMind(personality, traits, stress, trust, stressTick, lastLand, fear, fearTick, disabilities, skill, value, betrayed, trauma);
    }

    public TardisMind withBetrayed(boolean value) {
        return new TardisMind(personality, traits, stress, trust, stressTick, lastLand, fear, fearTick, disabilities, skill, discovered, value, trauma);
    }

    public TardisMind withTrauma(float value) {
        return new TardisMind(personality, traits, stress, trust, stressTick, lastLand, fear, fearTick, disabilities, skill, discovered, betrayed, value);
    }

    public TardisMind landed(long now) {
        return new TardisMind(personality, traits, stress, trust, stressTick, now, fear, fearTick, disabilities, skill, discovered, betrayed, trauma);
    }

    public static TardisMind roll(UUID tardisId) {
        long seed = tardisId.getMostSignificantBits() ^ tardisId.getLeastSignificantBits();
        RandomSource random = RandomSource.create(seed);
        TardisPersonality personality = rollPersonality(random);
        List<TardisTrait> traits = rollTraits(random, personality);
        float trust = traits.contains(TardisTrait.DISTRUSTING) ? DISTRUSTING_START_TRUST : START_TRUST;
        return new TardisMind(personality, traits, 0f, trust, 0L, NEVER, 0f, 0L,
                rollDisabilities(random), 0f, List.of(), false, 0f);
    }

    private static TardisPersonality rollPersonality(RandomSource random) {
        int totalWeight = 0;
        for (TardisPersonality personality : TardisPersonality.values()) {
            totalWeight += personality.weight();
        }

        int pick = random.nextInt(totalWeight);
        for (TardisPersonality personality : TardisPersonality.values()) {
            pick -= personality.weight();
            if (pick < 0) return personality;
        }
        return TardisPersonality.OBEDIENT;
    }

    private static List<TardisTrait> rollTraits(RandomSource random, TardisPersonality personality) {
        int count = Math.min(rollTraitCount(random), MAX_TRAITS);
        List<TardisTrait> pool = new ArrayList<>(List.of(TardisTrait.values()));
        pool.removeIf(trait -> !trait.fits(personality));
        List<TardisTrait> picked = new ArrayList<>();
        while (picked.size() < count && !pool.isEmpty()) {
            TardisTrait trait = pool.remove(random.nextInt(pool.size()));
            picked.add(trait);
            pool.removeIf(trait::clashesWith);
        }
        return picked;
    }

    private static List<TardisDisability> rollDisabilities(RandomSource random) {
        List<TardisDisability> rolled = new ArrayList<>();
        for (TardisDisability disability : TardisDisability.values()) {
            if (random.nextFloat() < disability.chance()) rolled.add(disability);
        }
        return rolled;
    }

    private static int rollTraitCount(RandomSource random) {
        int roll = random.nextInt(10);
        if (roll < 3) return 1;
        if (roll < 8) return 2;
        return 3;
    }
}
