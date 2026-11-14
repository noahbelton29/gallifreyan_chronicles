package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin;

import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.MindMessages;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data.MindEnums;
import net.minecraft.ChatFormatting;

/* {"type": "message", "key": "mallet_seen_flinch", "colour": "gray"}
   key is a message.gallifreyan_chronicles.<key> lang line, shown above the hotbar */
public record MessageAction(String key, ChatFormatting colour) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":message";
    public static final MapCodec<MessageAction> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.fieldOf("key").forGetter(MessageAction::key),
            MindEnums.codec(ChatFormatting.class).optionalFieldOf("colour", ChatFormatting.GRAY).forGetter(MessageAction::colour)
    ).apply(inst, MessageAction::new));

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        if (context.player != null) MindMessages.say(context.player, colour, key);
    }
}
