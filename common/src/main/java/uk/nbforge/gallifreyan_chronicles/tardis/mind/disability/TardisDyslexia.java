package uk.nbforge.gallifreyan_chronicles.tardis.mind.disability;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisEvents;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.TardisMindSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.LandingTarget;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.MindMessages;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMind;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.discovery.TardisDiscovery;

/**
 * How dyslexia affects a TARDIS when it takes off.
 * It can mix up the coordinates of its landing target, stress makes that more likely and worse, and practising flying helps it get better.
 */
public final class TardisDyslexia {
    private static final float NEW_CHANCE = 0.8f;
    private static final float FLOOR_CHANCE = 0.05f;
    private static final float STRESS_WEIGHT = 0.6f;
    private static final float STRESSED = 50f;
    private static final float PANICKED = 80f;

    // Each flight reduces the confusion it has left to learn from
    // It works out to roughly a 10 percent chance after 100 flights
    private static final float PRACTICE_KEEP = (float) Math.exp(-1.0 / 37.0);

    private TardisDyslexia() {}

    public static void init() {
        TardisEvents.TAKE_OFF.register(TardisDyslexia::onTakeOff);
    }

    /**
     * If it mixes up the coordinates the owner sees a message.
     * It gets a little practice with each flight whether or not it was mixed up.
     */
    private static void onTakeOff(Tardis tardis) {
        TardisMind mind = TardisMindSystem.mind(tardis);
        if (!mind.has(TardisDisability.DYSLEXIC)) return;
        TardisConsoleBlockEntity console = tardis.console().orElse(null);
        if (console != null && console.getLevel() instanceof ServerLevel level && muddle(tardis, level.getRandom())) {
            TardisDiscovery.disability(tardis, TardisDisability.DYSLEXIC);
            tardis.ownerPlayer()
                    .filter(owner -> owner.level() == level)
                    .ifPresent(owner -> MindMessages.say(owner, ChatFormatting.GOLD, "tardis_muddled"));
        }
        practise(tardis);
    }

    /**
     * The chance starts high and drops towards the minimum as it gains experience.
     * skill goes from 0 to 1.
     */
    public static float calmChance(float skill) {
        return FLOOR_CHANCE + (NEW_CHANCE - FLOOR_CHANCE) * (1f - skill);
    }

    /** Higher stress makes a mix up more likely and can change the coordinates several times and not just once. */
    public static boolean muddle(Tardis tardis, RandomSource random) {
        TardisMind mind = TardisMindSystem.mind(tardis);
        if (!mind.has(TardisDisability.DYSLEXIC)) return false;
        LandingTarget target = tardis.landingTarget().orElse(null);
        if (target == null) return false;

        float stress = TardisMindSystem.stress(tardis);
        float calm = calmChance(mind.skill());
        float chance = calm + (1f - calm) * (stress / TardisMind.MAX) * STRESS_WEIGHT;
        if (random.nextFloat() >= chance) return false;

        int mixups = stress >= PANICKED ? 3 : stress >= STRESSED ? 2 : 1;
        int x = target.pos().getX();
        int z = target.pos().getZ();
        for (int i = 0; i < mixups; i++) {
            int kind = i == 0 ? 0 : random.nextInt(3);
            switch (kind) {
                case 0 -> {
                    if (random.nextBoolean()) x = swapDigits(x, random);
                    else z = swapDigits(z, random);
                }
                case 1 -> {
                    int swap = x;
                    x = z;
                    z = swap;
                }
                default -> {
                    if (random.nextBoolean()) x = -x;
                    else z = -z;
                }
            }
        }

        BlockPos muddled = new BlockPos(x, target.pos().getY(), z);
        if (muddled.equals(target.pos())) return false;
        tardis.data().setLandingTarget(tardis.id(), new LandingTarget(target.dimension(), muddled, target.yaw()));
        return true;
    }

    private static void practise(Tardis tardis) {
        float skill = TardisMindSystem.skill(tardis);
        TardisMindSystem.setSkill(tardis, 1f - (1f - skill) * PRACTICE_KEEP);
    }

    /**
     * Swaps two neighbouring digits so 142 can become 124.
     * Negative values keep their sign and values with fewer than two digits are left alone.
     */
    private static int swapDigits(int value, RandomSource random) {
        char[] digits = String.valueOf(Math.abs(value)).toCharArray();
        if (digits.length < 2) return value;
        int i = random.nextInt(digits.length - 1);
        char swap = digits[i];
        digits[i] = digits[i + 1];
        digits[i + 1] = swap;
        int swapped = Integer.parseInt(new String(digits));
        return value < 0 ? -swapped : swapped;
    }
}
