package uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade;


public record FadeTiming(int takeoffDelayTicks, int fadeOutTicks, int fadeInTicks) {
    public static final FadeTiming DEFAULT = new FadeTiming(50, 210, 265);

    public static final int MAX_TICKS = 24000;

}
