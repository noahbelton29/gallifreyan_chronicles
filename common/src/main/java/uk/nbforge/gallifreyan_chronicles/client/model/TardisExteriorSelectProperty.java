package uk.nbforge.gallifreyan_chronicles.client.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisInteriorOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.codec.client.model.TardisExteriorSelectPropertyCodec;

public record TardisExteriorSelectProperty() implements SelectItemModelProperty<String> {
    public static final SelectItemModelProperty.Type<TardisExteriorSelectProperty, String> TYPE =
            SelectItemModelProperty.Type.create(MapCodec.unit(new TardisExteriorSelectProperty()), TardisExteriorSelectPropertyCodec.VALUE_CODEC);

    @Override
    public @Nullable String get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed,
                                ItemDisplayContext displayContext) {
        String interiorId = TardisInteriorOptions.effectiveInteriorId(stack.get(GCDataComponents.TARDIS_INTERIOR));
        TardisInteriorOptions.Option option = TardisInteriorOptions.find(interiorId);
        return option == null ? null : option.exteriorId();
    }

    @Override
    public Codec<String> valueCodec() {
        return TardisExteriorSelectPropertyCodec.VALUE_CODEC;
    }

    @Override
    public SelectItemModelProperty.Type<TardisExteriorSelectProperty, String> type() {
        return TYPE;
    }
}
