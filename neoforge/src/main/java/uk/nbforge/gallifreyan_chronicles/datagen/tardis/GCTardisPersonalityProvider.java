package uk.nbforge.gallifreyan_chronicles.datagen.tardis;

import com.google.gson.JsonObject;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.SparksAction.Size;
import net.minecraft.ChatFormatting;
import net.minecraft.data.PackOutput;

import static uk.nbforge.gallifreyan_chronicles.datagen.tardis.builder.GCMind.*;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJsonDataProvider;
import uk.nbforge.gallifreyan_chronicles.datagen.tardis.builder.GCPersonalityBuilder;

public class GCTardisPersonalityProvider extends GCJsonDataProvider {
    public GCTardisPersonalityProvider(PackOutput output) {
        super(output, "tardis_personality", "TARDIS Personality");
    }

    @Override
    protected void addEntries() {
        personality("aggressive")
                .howCommon(10)
                .refusesToFlyWhen(45, 30)
                .mallet(m -> m.fearPerHit(6, 15).fearNeededToObey(65).traumaPerHit(3.0).hitsToSnapOutOfTerror(5, 8))
                .whenUnwilling(fight(2), slamDoors(1.6, 0.8, true), reveal())
                .whenItSeesTheMallet(sparks(Size.BIG, 1), shock(4, 2, 0.6, 0.3),
                        message("mallet_seen_threatens", ChatFormatting.RED), slamDoors(1.4, 0.6), reveal())
                .whenHit(hitBack(2, 6, 0.9), revealIfItReacted())
                .whenHitWhileTerrified(savage(4, 10, 3), revealIfItReacted())
                .whenYouEnter(lashOut(0.35, 2, 6, 0.9, 0.35), revealIfItReacted());

        personality("defensive")
                .howCommon(32)
                .refusesToFlyWhen(50, 30)
                .mallet(m -> m.fearPerHit(10, 25).fearNeededToObey(45).traumaPerHit(4.0).hitsToSnapOutOfTerror(3, 5))
                .whenUnwilling(addStress(2), refuse(), shield(2.5, 0.6), lockDoors(), reveal())
                .whenItSeesTheMallet(lockDoors(), sound("tardis_door_lock", 0.8, 0.8),
                        message("mallet_seen_braces", ChatFormatting.GOLD), reveal())
                .whenYouEnter(lashOut(0.65, 1, 3, 0.5, 0.25), revealIfItReacted());

        personality("obedient")
                .howCommon(28)
                .mallet(m -> m.fearPerHit(15).fearNeededToObey(0).traumaPerHit(5.0).hitsToSnapOutOfTerror(1, 2))
                .whenUnwilling()
                .whenItSeesTheMallet(sound("stop_demat", 0.4, 1.6),
                        message("mallet_seen_flinch", ChatFormatting.GRAY))
                .whenHit(reveal());

        personality("submissive")
                .howCommon(30)
                .refusesToFlyWhen(60, 20)
                .mallet(m -> m.fearPerHit(15).fearNeededToObey(25).traumaPerHit(5.0).hitsToSnapOutOfTerror(2, 3))
                .whenUnwilling(balk(), sound("tardis_drum", 0.6, 0.5), reveal())
                .whenItSeesTheMallet(sound("stop_demat", 0.4, 1.6),
                        message("mallet_seen_flinch", ChatFormatting.GRAY));
    }

    private GCPersonalityBuilder personality(String name) {
        JsonObject json = new JsonObject();
        add(name, json);
        return new GCPersonalityBuilder(json);
    }
}
