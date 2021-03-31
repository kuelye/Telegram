package org.telegram.messenger.animation;

public abstract class BaseChatAnimation extends BaseAnimation {

    public final static int DEFAULT_DURATION = 500;

    private int duration = DEFAULT_DURATION;

    protected BaseChatAnimation(AnimationType animationType, int interpolatorsCount) {
        super(animationType, interpolatorsCount);
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }
}
