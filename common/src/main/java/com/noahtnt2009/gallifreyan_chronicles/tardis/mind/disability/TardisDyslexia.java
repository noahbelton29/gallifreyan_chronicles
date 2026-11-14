package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.disability;

import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.MindMessages;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMind;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.discovery.TardisDiscovery;

import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisEvents;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.LandingTarget;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

/**
 * Handles how dyslexia affects a TARDIS when it takes off.
 * <p>
 * A dyslexic TARDIS can mix up the coordinates of its landing target.
 * Stress makes this more likely and can cause more severe mix-ups,
 * while practising flying helps it get better over time.
 */
public final class TardisDyslexia {
    private static final float NEW_CHANCE = 0.8f;
    private static final float FLOOR_CHANCE = 0.05f;
    private static final float STRESS_WEIGHT = 0.6f;
    private static final float STRESSED = 50f;
    private static final float PANICKED = 80f;

    // Each flight reduces the amount of confusion the TARDIS has left to learn from.
    // This works out to roughly a 10% chance after 100 flights.
    private static final float PRACTICE_KEEP = (float) Math.exp(-1.0 / 37.0);

    private TardisDyslexia() {
    }

    /**
     * Registers the takeoff event so the TARDIS can handle its dyslexia.
     */
    public static void init() {
        TardisEvents.TAKE_OFF.register(TardisDyslexia::onTakeOff);
    }

    /**
     * Checks whether the TARDIS mixes up its landing coordinates when taking off.
     * <p>
     * If it does, the owner is shown a message. The TARDIS also gets a little
     * more practice with each flight, whether or not its coordinates were mixed up.
     */
    private static void onTakeOff(Tardis tardis) {
        TardisMind mind = TardisMindSystem.mind(tardis);
        if (!mind.has(TardisDisability.DYSLEXIC)) return;
        TardisConsoleBlockEntity console = tardis.console().orElse(null);
        if (console != null && console.getLevel() instanceof ServerLevel level && muddle(tardis, level.getRandom())) {
            TardisDiscovery.disability(tardis, TardisDisability.DYSLEXIC);
            tardis.ownerPlayer().filter(owner -> owner.level() == level)
                    .ifPresent(owner -> MindMessages.say(owner, ChatFormatting.GOLD, "tardis_muddled"));
        }
        practise(tardis);
    }

    /**
     * Works out how likely the TARDIS is to get its coordinates mixed up.
     * <p>
     * The chance starts high and drops towards the minimum as the TARDIS
     * gains experience.
     *
     * @param skill how much practice the TARDIS has gained, from 0 to 1
     * @return the chance of a mix-up
     */
    public static float calmChance(float skill) {
        return FLOOR_CHANCE + (NEW_CHANCE - FLOOR_CHANCE) * (1f - skill);
    }

    /**
     * Tries to scramble the landing target's X and Z coordinates.
     * <p>
     * Higher stress makes a mix-up more likely and can cause several changes
     * to the coordinates instead of just one.
     *
     * @param tardis the TARDIS whose landing target may be changed
     * @param random the random source used to decide what gets mixed up
     * @return true if the landing target was changed
     */
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

    /**
     * Gives the TARDIS a little more practice after each flight.
     */
    private static void practise(Tardis tardis) {
        float skill = TardisMindSystem.skill(tardis);
        TardisMindSystem.setSkill(tardis, 1f - (1f - skill) * PRACTICE_KEEP);
    }

    /**
     * Swaps two neighbouring digits in a coordinate.
     * <p>
     * For example, 142 could become 124. Negative values keep their sign,
     * and values with fewer than two digits are left alone.
     *
     * @param value  the coordinate to change
     * @param random the random source used to choose which digits to swap
     * @return the coordinate with two neighbouring digits swapped
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