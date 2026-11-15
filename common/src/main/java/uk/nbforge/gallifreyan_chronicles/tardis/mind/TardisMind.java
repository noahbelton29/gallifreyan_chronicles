
package uk.nbforge.gallifreyan_chronicles.tardis.mind;

import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.TardisPersonalities;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.TardisTraits;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.TraitModifiers;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
   The mental state of a TARDIS.
   Stress and trauma belong to the TARDIS itself, trust and fear are tracked per player in TardisBond.
*/
public record TardisMind(String personalityId, List<String> traitIds, float stress, float ownerStartTrust,
                         long stressTick, long lastLand, Map<UUID, TardisBond> bonds,
                         List<TardisDisability> disabilities, float skill, List<String> discovered,
                         float trauma, long traumaTick, float recovery) {

    // Highest value for stress, trust and trauma
    public static final float MAX = 100f;

    // Trust it starts with when nothing else is given
    public static final float START_TRUST = 50f;

    // Used when something has never happened
    public static final long NEVER = Long.MIN_VALUE;

    public static final int MAX_TRAITS = 3;

    private static final long TICKS_PER_STRESS_POINT = 100L;
    // Fear fades a point every 15 seconds so a scare wears off in a few minutes
    private static final long TICKS_PER_FEAR_POINT = 300L;
    // Trauma fades a point every 10 minutes
    private static final long TICKS_PER_TRAUMA_POINT = 12000L;
    public static final float TRAUMA_FEAR_SHARE = 0.6f;
    private static final float PTSD_FEAR_FLOOR = 50f;

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
       Makes a fresh bond if the player has none.
       The owner starts with the configured trust, others use the starting trust from the traits if any.
    */
    public TardisBond bond(UUID player, UUID owner) {
        TardisBond bond = bonds.get(player);
        if (bond != null) return bond;
        if (player.equals(owner)) return TardisBond.fresh(ownerStartTrust);
        TraitModifiers modifiers = modifiers();
        return TardisBond.fresh(modifiers.strangerStartTrust().orElse(modifiers.startTrust().orElse(START_TRUST)));
    }

    public float stressAt(long now) {
        float pointsCalmed = Math.max(0L, now - stressTick) / (float) TICKS_PER_STRESS_POINT * modifiers().stressFade();
        return Math.max(0f, stress - pointsCalmed);
    }

    /** Trauma stops fear from fading below a certain level for players who hurt the TARDIS. */
    public float fearAt(TardisBond bond, long now) {
        float pointsFaded = Math.max(0L, now - bond.fearTick()) / (float) TICKS_PER_FEAR_POINT * modifiers().fearFade();
        float floor = bond.hurtIt() ? traumaFloor(bond, now) : 0f;
        return Math.max(floor, Math.max(0f, bond.fear() - pointsFaded));
    }

    /** The fear that scars leave behind, PTSD keeps a higher minimum until it recovers. */
    public float traumaFloor(TardisBond bond, long now) {
        float scars = Math.max(bond.traumaAt(now), PTSD_FEAR_FLOOR * ptsdStrength());
        return scars * TRAUMA_FEAR_SHARE;
    }

    public TardisMind withBond(UUID player, TardisBond bond) {
        Map<UUID, TardisBond> updated = new HashMap<>(bonds);
        updated.put(player, bond);
        return new TardisMind(personalityId, traitIds, stress, ownerStartTrust, stressTick, lastLand, updated, disabilities, skill, discovered, trauma, traumaTick, recovery);
    }

    public TardisMind withStress(float value, long now) {
        return new TardisMind(personalityId, traitIds, value, ownerStartTrust, now, lastLand, bonds, disabilities, skill, discovered, trauma, traumaTick, recovery);
    }

    /** Traits that do not suit the new personality are dropped. */
    public TardisMind withPersonality(TardisPersonality value) {
        List<String> kept = traits().stream().filter(trait -> trait.fits(value)).map(TardisTrait::id).toList();
        return new TardisMind(value.id(), kept, stress, ownerStartTrust, stressTick, lastLand, bonds, disabilities, skill, discovered, trauma, traumaTick, recovery);
    }

    public TardisMind withTraits(List<TardisTrait> value) {
        List<String> ids = value.stream().map(TardisTrait::id).toList();
        return new TardisMind(personalityId, ids, stress, ownerStartTrust, stressTick, lastLand, bonds, disabilities, skill, discovered, trauma, traumaTick, recovery);
    }

    public TardisMind withDisabilities(List<TardisDisability> value) {
        return new TardisMind(personalityId, traitIds, stress, ownerStartTrust, stressTick, lastLand, bonds, value, skill, discovered, trauma, traumaTick, recovery);
    }

    public TardisMind withSkill(float value) {
        return new TardisMind(personalityId, traitIds, stress, ownerStartTrust, stressTick, lastLand, bonds, disabilities, value, discovered, trauma, traumaTick, recovery);
    }

    public boolean knows(String key) {
        return discovered.contains(key);
    }

    public TardisMind withDiscovered(List<String> value) {
        return new TardisMind(personalityId, traitIds, stress, ownerStartTrust, stressTick, lastLand, bonds, disabilities, skill, value, trauma, traumaTick, recovery);
    }

    public TardisMind withTrauma(float value, long now) {
        return new TardisMind(personalityId, traitIds, stress, ownerStartTrust, stressTick, lastLand, bonds, disabilities, skill, discovered, value, now, recovery);
    }

    public TardisMind withRecovery(float value) {
        return new TardisMind(personalityId, traitIds, stress, ownerStartTrust, stressTick, lastLand, bonds, disabilities, skill, discovered, trauma, traumaTick, value);
    }

    /** The total trauma from everyone, this is what gives it PTSD. */
    public float traumaAt(long now) {
        return faded(trauma, traumaTick, now);
    }

    /** Trauma fades by itself, one point every TICKS_PER_TRAUMA_POINT. */
    public static float faded(float value, long since, long now) {
        return Math.max(0f, value - Math.max(0L, now - since) / (float) TICKS_PER_TRAUMA_POINT);
    }

    /** How much PTSD still affects it, 1 when it first gets it and close to 0 once it recovers. */
    public float ptsdStrength() {
        return has(TardisDisability.PTSD) ? 1f - recovery : 0f;
    }

    public TardisMind landed(long now) {
        return new TardisMind(personalityId, traitIds, stress, ownerStartTrust, stressTick, now, bonds, disabilities, skill, discovered, trauma, traumaTick, recovery);
    }

    /** The TARDIS id is the random seed so the same id always gives the same mind. */
    public static TardisMind roll(UUID tardisId) {
        long seed = tardisId.getMostSignificantBits() ^ tardisId.getLeastSignificantBits();
        RandomSource random = RandomSource.create(seed);
        TardisPersonality personality = rollPersonality(random);
        List<TardisTrait> traits = rollTraits(random, personality);
        float trust = TraitModifiers.combine(traits, false).startTrust().orElse(START_TRUST);
        return new TardisMind(personality.id(), traits.stream().map(TardisTrait::id).toList(), 0f, trust, 0L, NEVER,
                Map.of(), rollDisabilities(random), 0f, List.of(), 0f, 0L, 0f);
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

    /** Traits are weighted, traits that do not fit the personality or clash with one already picked are left out. */
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
