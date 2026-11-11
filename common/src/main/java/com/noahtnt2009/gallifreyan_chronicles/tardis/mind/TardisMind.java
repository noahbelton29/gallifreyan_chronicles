package com.noahtnt2009.gallifreyan_chronicles.tardis.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data.MindIds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data.TardisPersonalities;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data.TardisTraits;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data.TraitModifiers;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/* personality and traits are stored by id so json ones work too */
public record TardisMind(String personalityId, List<String> traitIds, float stress, float trust,
                         long stressTick, long lastLand, float fear, long fearTick,
                         List<TardisDisability> disabilities, float skill, List<String> discovered,
                         boolean betrayed, float trauma) {
    public static final float MAX = 100f;
    public static final float START_TRUST = 50f;
    public static final long NEVER = Long.MIN_VALUE;
    public static final int MAX_TRAITS = 3;
    private static final long TICKS_PER_STRESS_POINT = 200L;
    private static final long TICKS_PER_FEAR_POINT = 600L;
    private static final float TRAUMA_FEAR_SHARE = 0.6f;
    private static final float PTSD_FEAR_FLOOR = 50f;

    public static final Codec<TardisMind> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            MindIds.CODEC.fieldOf("personality").forGetter(TardisMind::personalityId),
            MindIds.CODEC.listOf().optionalFieldOf("traits", List.of()).forGetter(TardisMind::traitIds),
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
        traitIds = List.copyOf(traitIds);
        disabilities = List.copyOf(disabilities);
        discovered = List.copyOf(discovered);
        skill = Math.clamp(skill, 0f, 1f);
        stress = Math.clamp(stress, 0f, MAX);
        trust = Math.clamp(trust, 0f, MAX);
        fear = Math.clamp(fear, 0f, MAX);
        trauma = Math.clamp(trauma, 0f, MAX);
    }

    public TardisPersonality personality() {
        return TardisPersonalities.get(personalityId);
    }

    public List<TardisTrait> traits() {
        return TardisTraits.of(traitIds);
    }

    public TraitModifiers modifiers() {
        return TraitModifiers.combine(traits(), betrayed);
    }

    public boolean has(TardisTrait trait) {
        return traitIds.contains(trait.id());
    }

    public boolean has(TardisDisability disability) {
        return disabilities.contains(disability);
    }

    public float stressAt(long now) {
        float pointsCalmed = Math.max(0L, now - stressTick) / (float) TICKS_PER_STRESS_POINT * modifiers().stressFade();
        return Math.max(0f, stress - pointsCalmed);
    }

    public float fearAt(long now) {
        float pointsFaded = Math.max(0L, now - fearTick) / (float) TICKS_PER_FEAR_POINT * modifiers().fearFade();
        return Math.max(traumaFloor(), fear - pointsFaded);
    }

    /* a traumatised tardis never stops being a bit scared of you */
    public float traumaFloor() {
        float floor = has(TardisDisability.PTSD) ? Math.max(trauma, PTSD_FEAR_FLOOR) : trauma;
        return floor * TRAUMA_FEAR_SHARE;
    }

    public TardisMind withFear(float value, long now) {
        return new TardisMind(personalityId, traitIds, stress, trust, stressTick, lastLand, value, now, disabilities, skill, discovered, betrayed, trauma);
    }

    public TardisMind withStress(float value, long now) {
        return new TardisMind(personalityId, traitIds, value, trust, now, lastLand, fear, fearTick, disabilities, skill, discovered, betrayed, trauma);
    }

    public TardisMind withTrust(float value) {
        return new TardisMind(personalityId, traitIds, stress, value, stressTick, lastLand, fear, fearTick, disabilities, skill, discovered, betrayed, trauma);
    }

    /* traits that don't suit the new personality are dropped */
    public TardisMind withPersonality(TardisPersonality value) {
        List<String> kept = traits().stream().filter(trait -> trait.fits(value)).map(TardisTrait::id).toList();
        return new TardisMind(value.id(), kept, stress, trust, stressTick, lastLand, fear, fearTick, disabilities, skill, discovered, betrayed, trauma);
    }

    public TardisMind withTraits(List<TardisTrait> value) {
        List<String> ids = value.stream().map(TardisTrait::id).toList();
        return new TardisMind(personalityId, ids, stress, trust, stressTick, lastLand, fear, fearTick, disabilities, skill, discovered, betrayed, trauma);
    }

    public TardisMind withDisabilities(List<TardisDisability> value) {
        return new TardisMind(personalityId, traitIds, stress, trust, stressTick, lastLand, fear, fearTick, value, skill, discovered, betrayed, trauma);
    }

    public TardisMind withSkill(float value) {
        return new TardisMind(personalityId, traitIds, stress, trust, stressTick, lastLand, fear, fearTick, disabilities, value, discovered, betrayed, trauma);
    }

    public boolean knows(String key) {
        return discovered.contains(key);
    }

    public TardisMind withDiscovered(List<String> value) {
        return new TardisMind(personalityId, traitIds, stress, trust, stressTick, lastLand, fear, fearTick, disabilities, skill, value, betrayed, trauma);
    }

    public TardisMind withBetrayed(boolean value) {
        return new TardisMind(personalityId, traitIds, stress, trust, stressTick, lastLand, fear, fearTick, disabilities, skill, discovered, value, trauma);
    }

    public TardisMind withTrauma(float value) {
        return new TardisMind(personalityId, traitIds, stress, trust, stressTick, lastLand, fear, fearTick, disabilities, skill, discovered, betrayed, value);
    }

    public TardisMind landed(long now) {
        return new TardisMind(personalityId, traitIds, stress, trust, stressTick, now, fear, fearTick, disabilities, skill, discovered, betrayed, trauma);
    }

    public static TardisMind roll(UUID tardisId) {
        long seed = tardisId.getMostSignificantBits() ^ tardisId.getLeastSignificantBits();
        RandomSource random = RandomSource.create(seed);
        TardisPersonality personality = rollPersonality(random);
        List<TardisTrait> traits = rollTraits(random, personality);
        float trust = TraitModifiers.combine(traits, false).startTrust().orElse(START_TRUST);
        return new TardisMind(personality.id(), traits.stream().map(TardisTrait::id).toList(), 0f, trust, 0L, NEVER,
                0f, 0L, rollDisabilities(random), 0f, List.of(), false, 0f);
    }

    private static TardisPersonality rollPersonality(RandomSource random) {
        List<TardisPersonality> all = List.copyOf(TardisPersonalities.all());
        int totalWeight = 0;
        for (TardisPersonality personality : all) {
            totalWeight += Math.max(0, personality.weight());
        }
        if (totalWeight <= 0) return TardisPersonalities.get(TardisPersonality.FALLBACK.id());

        int pick = random.nextInt(totalWeight);
        for (TardisPersonality personality : all) {
            pick -= Math.max(0, personality.weight());
            if (pick < 0) return personality;
        }
        return all.getLast();
    }

    private static List<TardisTrait> rollTraits(RandomSource random, TardisPersonality personality) {
        int count = Math.min(rollTraitCount(random), MAX_TRAITS);
        List<TardisTrait> pool = new ArrayList<>(TardisTraits.all());
        pool.removeIf(trait -> !trait.fits(personality) || trait.weight() <= 0);
        List<TardisTrait> picked = new ArrayList<>();
        while (picked.size() < count && !pool.isEmpty()) {
            TardisTrait trait = weighted(pool, random);
            pool.remove(trait);
            picked.add(trait);
            pool.removeIf(trait::clashesWith);
        }
        return picked;
    }

    private static TardisTrait weighted(List<TardisTrait> pool, RandomSource random) {
        int total = pool.stream().mapToInt(TardisTrait::weight).sum();
        int pick = random.nextInt(total);
        for (TardisTrait trait : pool) {
            pick -= trait.weight();
            if (pick < 0) return trait;
        }
        return pool.getLast();
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
