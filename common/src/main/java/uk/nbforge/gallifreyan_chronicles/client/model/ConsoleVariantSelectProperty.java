package uk.nbforge.gallifreyan_chronicles.client.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import uk.nbforge.gallifreyan_chronicles.core.item.ConsoleBlockItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.codec.client.model.ConsoleVariantSelectPropertyCodec;

public record ConsoleVariantSelectProperty() implements SelectItemModelProperty<String> {
    public static final SelectItemModelProperty.Type<ConsoleVariantSelectProperty, String> TYPE =
            SelectItemModelProperty.Type.create(MapCodec.unit(new ConsoleVariantSelectProperty()), ConsoleVariantSelectPropertyCodec.VALUE_CODEC);

    @Override
    public String get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed,
                      ItemDisplayContext displayContext) {
        return ConsoleBlockItem.selectedVariantKey(stack);
    }

    @Override
    public Codec<String> valueCodec() {
        return ConsoleVariantSelectPropertyCodec.VALUE_CODEC;
    }

    @Override
    public SelectItemModelProperty.Type<ConsoleVariantSelectProperty, String> type() {
        return TYPE;
    }
}
