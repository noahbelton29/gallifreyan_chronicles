package uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;
import uk.nbforge.gallifreyan_chronicles.util.SparkUtil;

public record SparksAction(Size size, double height) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":sparks";

    public enum Size {
        SMALL,
        BIG,
        BLAST
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        context.here((level, pos) -> {
            var at = pos.add(0, height, 0);
            switch (size) {
                case SMALL -> SparkUtil.small(level, at, 10);
                case BIG -> SparkUtil.big(level, at);
                case BLAST -> SparkUtil.blast(level, at);
            }
        });
    }
}
