package uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin;

import uk.nbforge.gallifreyan_chronicles.tardis.mind.MindMessages;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;
import net.minecraft.ChatFormatting;

/* {"type": "message", "key": "mallet_seen_flinch", "colour": "gray"}
   key is a message.gallifreyan_chronicles.<key> lang line, shown above the hotbar */
public record MessageAction(String key, ChatFormatting colour) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":message";

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        if (context.player != null) MindMessages.say(context.player, colour, key);
    }
}
