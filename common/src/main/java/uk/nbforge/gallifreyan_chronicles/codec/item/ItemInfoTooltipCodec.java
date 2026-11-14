package uk.nbforge.gallifreyan_chronicles.codec.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;
import uk.nbforge.gallifreyan_chronicles.core.item.tooltip.ItemInfoTooltip;

public final class ItemInfoTooltipCodec {
    public static final Codec<ItemInfoTooltip> CODEC = Codec.STRING.comapFlatMap(
            name -> {
                try {
                    return DataResult.success(ItemInfoTooltip.valueOf(name.toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException e) {
                    return DataResult.error(() -> "Unknown item info tooltip: " + name);
                }
            },
            kind -> kind.name().toLowerCase(Locale.ROOT)
    );

    private ItemInfoTooltipCodec() {
    }
}
