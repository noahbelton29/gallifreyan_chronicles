package uk.nbforge.gallifreyan_chronicles.tardis.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.RandomSource;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindIds;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.TardisPersonalities;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.TardisTraits;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.TraitModifiers;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;

/** stress and trauma belong to the tardis itself, trust and fear are per player in TardisBond */
public record TardisMind(
        String personalityId,
        List<String> traitIds,
        float stress,
        float ownerStartTrust,
        long stressTick,
        long lastLand,
        Map<UUID, TardisBond> bonds,
        List<TardisDisability> disabilities,
        float skill,
        List<String> discovered,
        float trauma,
        long traumaTick,
        float recovery) {

    public static final float MAX = 100f;

    public static final float START_TRUST = 50f;

    public static final long NEVER = Long.MIN_VALUE;

    public static final int MAX_TRAITS = 3;

    private static final long TICKS_PER_STRESS_POINT = 100L;
    private static final long TICKS_PER_FEAR_POINT = 300L;
    private static final long TICKS_PER_TRAUMA_POINT = 12000L;
    public static final float TRAUMA_FEAR_SHARE = 0.6f;
    private static final float PTSD_FEAR_FLOOR = 50f;

    public static final Codec<TardisMind> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                    MindIds.CODEC.fieldOf("personality").forGetter(TardisMind::personalityId),
                    MindIds.CODEC.listOf().optionalFieldOf("traits", List.of()).forGetter(TardisMind::traitIds),
                    Codec.FLOAT.optionalFieldOf("stress", 0f).forGetter(TardisMind::stress),
                    Codec.FLOAT.optionalFieldOf("trust", TardisMind.START_TRUST).forGetter(TardisMind::ownerStartTrust),
                    Codec.LONG.optionalFieldOf("stress_tick", 0L).forGetter(TardisMind::stressTick),
                    Codec.LONG.optionalFieldOf("last_land", TardisMind.NEVER).forGetter(TardisMind::lastLand),
                    Codec.unboundedMap(UUIDUtil.STRING_CODEC, TardisBond.CODEC)
                            .optionalFieldOf("bonds", Map.of())
                            .forGetter(TardisMind::bonds),
                    TardisDisability.CODEC
                            .listOf()
                            .optionalFieldOf("disabilities", List.of())
                            .forGetter(TardisMind::disabilities),
                    Codec.FLOAT.optionalFieldOf("skill", 0f).forGetter(TardisMind::skill),
                    Codec.STRING
                            .listOf()
                            .optionalFieldOf("discovered", List.of())
                            .forGetter(TardisMind::discovered),
                    Codec.FLOAT.optionalFieldOf("trauma", 0f).forGetter(TardisMind::trauma),
                    Codec.LONG.optionalFieldOf("trauma_tick", 0L).forGetter(TardisMind::traumaTick),
                    Codec.FLOAT.optionalFieldOf("recovery", 0f).forGetter(TardisMind::recovery))
            .apply(inst, TardisMind::new));

    public TardisMind {
        traitIds = List.copyOf(traitIds);
        bonds = Map.copyOf(bonds);
        disabilities = List.copyOf(disabilities);
        discovered = List.copyOf(discovered);
        skill = Math.clamp(skill, 0f, 1f);
        stress = Math.clamp(stress, 0f, MAX);
        ownerStartTrust = Math.clamp(ownerStartTrust, 0f, MAX);
        trauma = Math.clamp(trauma, 0f, MAX);
        recovery = Math.clamp(recovery, 0f, 1f);
    }

    public TardisPersonality personality() {
        return TardisPersonalities.get(personalityId);
    }

    public List<TardisTrait> traits() {
        return TardisTraits.of(traitIds);
    }

    public TraitModifiers modifiers() {
        return TraitModifiers.combine(traits(), false);
    }

    public TraitModifiers modifiersFor(TardisBond bond) {
        return TraitModifiers.combine(traits(), bond.betrayed());
    }

    public boolean has(TardisTrait trait) {
        return traitIds.contains(trait.id());
    }

    public boolean has(TardisDisability disability) {
        return disabilities.contains(disability);
    }

    /**
     * makes a fresh bond if the player has none. owner starts with the configured trust, everyone else gets the traits
     * starting trust if there is one
     */
    public TardisBond bond(UUID player, UUID owner) {
        TardisBond bond = bonds.get(player);
        if (bond != null) return bond;
        if (player.equals(owner)) return TardisBond.fresh(ownerStartTrust);
        TraitModifiers modifiers = modifiers();
        return TardisBond.fresh(
                modifiers.strangerStartTrust().orElse(modifiers.startTrust().orElse(START_TRUST)));
    }

    public float stressAt(long now) {
        float pointsCalmed = Math.max(0L, now - stressTick)
                / (float) TICKS_PER_STRESS_POINT
                * modifiers().stressFade();
        return Math.max(0f, stress - pointsCalmed);
    }

    /** trauma stops fear fading below a certain level for players who hurt it */
    public float fearAt(TardisBond bond, long now) {
        float pointsFaded = Math.max(0L, now - bond.fearTick())
                / (float) TICKS_PER_FEAR_POINT
                * modifiers().fearFade();
        float floor = bond.hurtIt() ? traumaFloor(bond, now) : 0f;
        return Math.max(floor, Math.max(0f, bond.fear() - pointsFaded));
    }

    public float traumaFloor(TardisBond bond, long now) {
        float scars = Math.max(bond.traumaAt(now), PTSD_FEAR_FLOOR * ptsdStrength());
        return scars * TRAUMA_FEAR_SHARE;
    }

    public TardisMind withBond(UUID player, TardisBond bond) {
        Map<UUID, TardisBond> updated = new HashMap<>(bonds);
        updated.put(player, bond);
        return new TardisMind(
                personalityId,
                traitIds,
                stress,
                ownerStartTrust,
                stressTick,
                lastLand,
                updated,
                disabilities,
                skill,
                discovered,
                trauma,
                traumaTick,
                recovery);
    }

    public TardisMind withStress(float value, long now) {
        return new TardisMind(
                personalityId,
                traitIds,
                value,
                ownerStartTrust,
                now,
                lastLand,
                bonds,
                disabilities,
                skill,
                discovered,
                trauma,
                traumaTick,
                recovery);
    }

    /** traits that dont suit the new personality get dropped */
    public TardisMind withPersonality(TardisPersonality value) {
        List<String> kept = traits().stream()
                .filter(trait -> trait.fits(value))
                .map(TardisTrait::id)
                .toList();
        return new TardisMind(
                value.id(),
                kept,
                stress,
                ownerStartTrust,
                stressTick,
                lastLand,
                bonds,
                disabilities,
                skill,
                discovered,
                trauma,
                traumaTick,
                recovery);
    }

    public TardisMind withTraits(List<TardisTrait> value) {
        List<String> ids = value.stream().map(TardisTrait::id).toList();
        return new TardisMind(
                personalityId,
                ids,
                stress,
                ownerStartTrust,
                stressTick,
                lastLand,
                bonds,
                disabilities,
                skill,
                discovered,
                trauma,
                traumaTick,
                recovery);
    }

    public TardisMind withDisabilities(List<TardisDisability> value) {
        return new TardisMind(
                personalityId,
                traitIds,
                stress,
                ownerStartTrust,
                stressTick,
                lastLand,
                bonds,
                value,
                skill,
                discovered,
                trauma,
                traumaTick,
                recovery);
    }

    public TardisMind withSkill(float value) {
        return new TardisMind(
                personalityId,
                traitIds,
                stress,
                ownerStartTrust,
                stressTick,
                lastLand,
                bonds,
                disabilities,
                value,
                discovered,
                trauma,
                traumaTick,
                recovery);
    }

    public boolean knows(String key) {
        return discovered.contains(key);
    }

    public TardisMind withDiscovered(List<String> value) {
        return new TardisMind(
                personalityId,
                traitIds,
                stress,
                ownerStartTrust,
                stressTick,
                lastLand,
                bonds,
                disabilities,
                skill,
                value,
                trauma,
                traumaTick,
                recovery);
    }

    public TardisMind withTrauma(float value, long now) {
        return new TardisMind(
                personalityId,
                traitIds,
                stress,
                ownerStartTrust,
                stressTick,
                lastLand,
                bonds,
                disabilities,
                skill,
                discovered,
                value,
                now,
                recovery);
    }

    public TardisMind withRecovery(float value) {
        return new TardisMind(
                personalityId,
                traitIds,
                stress,
                ownerStartTrust,
                stressTick,
                lastLand,
                bonds,
                disabilities,
                skill,
                discovered,
                trauma,
                traumaTick,
                value);
    }

    public float traumaAt(long now) {
        return faded(trauma, traumaTick, now);
    }

    public static float faded(float value, long since, long now) {
        return Math.max(0f, value - Math.max(0L, now - since) / (float) TICKS_PER_TRAUMA_POINT);
    }

    /** 1 when it first gets it, goes down to about 0 once it recovers */
    public float ptsdStrength() {
        return has(TardisDisability.PTSD) ? 1f - recovery : 0f;
    }

    public TardisMind landed(long now) {
        return new TardisMind(
                personalityId,
                traitIds,
                stress,
                ownerStartTrust,
                stressTick,
                now,
                bonds,
                disabilities,
                skill,
                discovered,
                trauma,
                traumaTick,
                recovery);
    }

    /** tardis id is the seed so the same id always gives the same mind */
    public static TardisMind roll(UUID tardisId) {
        long seed = tardisId.getMostSignificantBits() ^ tardisId.getLeastSignificantBits();
        RandomSource random = RandomSource.create(seed);
        TardisPersonality personality = rollPersonality(random);
        List<TardisTrait> traits = rollTraits(random, personality);
        float trust = TraitModifiers.combine(traits, false).startTrust().orElse(START_TRUST);
        return new TardisMind(
                personality.id(),
                traits.stream().map(TardisTrait::id).toList(),
                0f,
                trust,
                0L,
                NEVER,
                Map.of(),
                rollDisabilities(random),
                0f,
                List.of(),
                0f,
                0L,
                0f);
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
