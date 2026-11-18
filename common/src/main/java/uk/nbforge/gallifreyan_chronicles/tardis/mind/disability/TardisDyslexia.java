package uk.nbforge.gallifreyan_chronicles.tardis.mind.disability;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisEvents;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.LandingTarget;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.MindMessages;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisDiscovery;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMind;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMinds;

public final class TardisDyslexia {
    private static final float NEW_CHANCE = 0.8f;
    private static final float FLOOR_CHANCE = 0.05f;
    private static final float STRESS_WEIGHT = 0.6f;
    private static final float STRESSED = 50f;
    private static final float PANICKED = 80f;

    private static final float PRACTICE_KEEP = (float) Math.exp(-1.0 / 37.0);

    private TardisDyslexia() {}

    public static void init() {
        TardisEvents.TAKE_OFF.register(TardisDyslexia::onTakeOff);
    }

    private static void onTakeOff(Tardis tardis) {
        TardisMind mind = TardisMinds.mind(tardis);
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

    /** chance starts high and drops towards the minimum as it gets more experience, skill goes 0 to 1 */
    public static float calmChance(float skill) {
        return FLOOR_CHANCE + (NEW_CHANCE - FLOOR_CHANCE) * (1f - skill);
    }

    public static boolean muddle(Tardis tardis, RandomSource random) {
        TardisMind mind = TardisMinds.mind(tardis);
        if (!mind.has(TardisDisability.DYSLEXIC)) return false;
        LandingTarget target = tardis.landingTarget().orElse(null);
        if (target == null) return false;

        float stress = TardisMinds.stress(tardis);
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
        tardis.data().setLandingTarget(new LandingTarget(target.dimension(), muddled, target.yaw()));
        return true;
    }

    private static void practise(Tardis tardis) {
        float skill = TardisMinds.skill(tardis);
        TardisMinds.setSkill(tardis, 1f - (1f - skill) * PRACTICE_KEEP);
    }

    /** negative values keep their sign, anything under two digits is left alone */
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
