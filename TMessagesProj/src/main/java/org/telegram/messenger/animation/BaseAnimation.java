package org.telegram.messenger.animation;

public abstract class BaseAnimation {

    private final AnimationType animationType;

    protected BaseAnimation(AnimationType animationType) {
        this.animationType = animationType;
    }

    public abstract BaseAnimationSetting[] getSettings();
}
