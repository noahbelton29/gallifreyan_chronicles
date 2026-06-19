package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.noahtnt2009.gallifreyan_chronicles.GallifreyanChronicles;
import com.noahtnt2009.gallifreyan_chronicles.registry.GCSounds;
import net.fabricmc.fabric.api.client.datagen.v1.builder.SoundTypeBuilder;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricSoundsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class GCSoundsProvider extends FabricSoundsProvider {
    public GCSoundsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(HolderLookup.Provider registryLookup, SoundExporter exporter) {
        for (GCSounds.SoundEntry entry : GCSounds.ENTRIES) {
            SoundEvent event = entry.event();
            SoundTypeBuilder builder = SoundTypeBuilder.of(event);
            for (int i = 0; i <= 3; i++) {
                builder.sound(SoundTypeBuilder.RegistrationBuilder.ofFile(
                        Identifier.fromNamespaceAndPath(
                                GallifreyanChronicles.MOD_ID,
                                event.location().getPath() + "_" + i
                        )
                ));
            }
            exporter.add(event, builder);
        }
    }

    @Override
    public @NonNull String getName() {
        return "Gallifreyan Chronicles Sounds Provider";
    }
}