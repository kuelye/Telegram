package org.telegram.messenger.animation;

public class InterpolatorAnimationSetting extends BaseAnimationSetting {

    private final int interpolatorId;

    InterpolatorAnimationSetting(String titleKey, int titleRes, int interpolatorId) {
        super(titleKey, titleRes, BaseAnimationSetting.ContentType.INTERPOLATOR);
        this.interpolatorId = interpolatorId;
    }

    public int getInterpolatorId() {
        return interpolatorId;
    }
}
