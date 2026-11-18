package uk.nbforge.gallifreyan_chronicles.client.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.item.ConsoleBlockItem;

public record ConsoleVariantSelectProperty() implements SelectItemModelProperty<String> {
    public static final SelectItemModelProperty.Type<ConsoleVariantSelectProperty, String> TYPE =
            SelectItemModelProperty.Type.create(MapCodec.unit(new ConsoleVariantSelectProperty()), Codec.STRING);

    @Override
    public String get(
            ItemStack stack,
            @Nullable ClientLevel level,
            @Nullable LivingEntity entity,
            int seed,
            ItemDisplayContext displayContext) {
        return ConsoleBlockItem.selectedVariantKey(stack);
    }

    @Override
    public Codec<String> valueCodec() {
        return Codec.STRING;
    }

    @Override
    public SelectItemModelProperty.Type<ConsoleVariantSelectProperty, String> type() {
        return TYPE;
    }
}
