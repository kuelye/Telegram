package org.telegram.messenger.animation;

public abstract class BaseAnimation {

    private final AnimationType animationType;

    private final Interpolator[] interpolators;

    protected BaseAnimation(AnimationType animationType, int interpolatorsCount) {
        this.animationType = animationType;
        interpolators = new Interpolator[interpolatorsCount];
        for (int i = 0; i < interpolatorsCount; ++i) {
            interpolators[i] = new Interpolator();
        }
    }

    public Interpolator getInterpolator(int id) {
        return interpolators[id];
    }

    public abstract BaseAnimationSetting[] getSettings();
}
