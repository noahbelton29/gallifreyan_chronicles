package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import com.noahtnt2009.gallifreyan_chronicles.GallifreyanChronicles;
import net.minecraft.resources.Identifier;

public record TardisExterior(String id, Identifier model, Identifier texture, Identifier animation) {
    public TardisExterior(String id) {
        this(
                id,
                Identifier.fromNamespaceAndPath(
                        GallifreyanChronicles.MOD_ID,
                        id
                ),
                Identifier.fromNamespaceAndPath(
                        GallifreyanChronicles.MOD_ID,
                        "textures/block/" + id + ".png"
                ),
                Identifier.fromNamespaceAndPath(
                        GallifreyanChronicles.MOD_ID,
                        "tardis_door/" + id
                )
        );
    }

    public TardisExterior(String id, String animationId) {
        this(
                id,
                Identifier.fromNamespaceAndPath(
                        GallifreyanChronicles.MOD_ID,
                        id
                ),
                Identifier.fromNamespaceAndPath(
                        GallifreyanChronicles.MOD_ID,
                        "textures/block/" + id + ".png"
                ),
                Identifier.fromNamespaceAndPath(
                        GallifreyanChronicles.MOD_ID,
                        animationId
                )
        );
    }
}