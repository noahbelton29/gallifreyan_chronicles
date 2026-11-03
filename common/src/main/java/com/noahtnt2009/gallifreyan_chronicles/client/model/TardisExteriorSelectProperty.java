package com.noahtnt2009.gallifreyan_chronicles.client.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInteriorOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record TardisExteriorSelectProperty() implements SelectItemModelProperty<String> {
    public static final Codec<String> VALUE_CODEC = Codec.STRING;
    public static final SelectItemModelProperty.Type<TardisExteriorSelectProperty, String> TYPE =
            SelectItemModelProperty.Type.create(MapCodec.unit(new TardisExteriorSelectProperty()), VALUE_CODEC);

    @Override
    public @Nullable String get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed,
                                ItemDisplayContext displayContext) {
        String interiorId = TardisInteriorOptions.effectiveInteriorId(stack.get(GCDataComponents.TARDIS_INTERIOR));
        TardisInteriorOptions.Option option = TardisInteriorOptions.find(interiorId);
        return option == null ? null : option.exteriorId();
    }

    @Override
    public Codec<String> valueCodec() {
        return VALUE_CODEC;
    }

    @Override
    public SelectItemModelProperty.Type<TardisExteriorSelectProperty, String> type() {
        return TYPE;
    }
}
