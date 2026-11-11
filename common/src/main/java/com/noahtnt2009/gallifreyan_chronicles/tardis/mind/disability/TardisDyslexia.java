package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.disability;

import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMind;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.discovery.TardisDiscovery;

import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisEvents;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.LandingTarget;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.util.Messages;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/* mixes up its numbers on takeoff. worse when stressed, better with practice */
public final class TardisDyslexia {
    private static final float NEW_CHANCE = 0.8f;
    private static final float FLOOR_CHANCE = 0.05f;
    private static final float STRESS_WEIGHT = 0.6f;
    private static final float STRESSED = 50f;
    private static final float PANICKED = 80f;
    /* each flight keeps this much of what's left to learn. ~10% chance after 100 flights */
    private static final float PRACTICE_KEEP = (float) Math.exp(-1.0 / 37.0);

    private TardisDyslexia() {
    }

    public static void init() {
        TardisEvents.TAKE_OFF.register(TardisDyslexia::onTakeOff);
    }

    private static void onTakeOff(Tardis tardis) {
        TardisMind mind = TardisMindSystem.mind(tardis);
        if (!mind.has(TardisDisability.DYSLEXIC)) return;
        TardisConsoleBlockEntity console = tardis.console().orElse(null);
        if (console != null && console.getLevel() instanceof ServerLevel level && muddle(tardis, level.getRandom())) {
            TardisDiscovery.disability(tardis, TardisDisability.DYSLEXIC);
            Messages.overlayNearby(level, Vec3.atCenterOf(console.getBlockPos()), ChatFormatting.GOLD, "tardis_muddled");
        }
        practise(tardis);
    }

    /* calm chance, from 80% when new down towards 5% with practice */
    public static float calmChance(float skill) {
        return FLOOR_CHANCE + (NEW_CHANCE - FLOOR_CHANCE) * (1f - skill);
    }

    /* scrambles the landing target. true if it got muddled */
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

    /* 142 -> 124 */
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
