package org.telegram.messenger.animation;

public class Interpolator {

    public final static int[] DURATIONS = { 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1500, 2000, 3000 };
    public final static int DEFAULT_DURATION = 1000;

    private int duration = Interpolator.DEFAULT_DURATION;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
