package org.telegram.messenger.animation;

public class Interpolator {

    public final static int[] DURATIONS = { 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1500, 2000, 3000 };
    public final static int DEFAULT_DURATION = 1000;

    private int duration = Interpolator.DEFAULT_DURATION;

    private float[] cs = { 1.0f, 1.0f };
    private float[] ts = { 0.0f, 1.0f };

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public float[] getCs() {
        return cs;
    }

    public float[] getTs() {
        return ts;
    }

    public void setParameters(float[] cs, float ts[]) {
        this.cs = cs;
        this.ts = ts;
    }
}
