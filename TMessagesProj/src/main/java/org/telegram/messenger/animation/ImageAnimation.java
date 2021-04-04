package org.telegram.messenger.animation;

import org.telegram.messenger.R;

public class ImageAnimation extends BaseChatAnimation {

    public final static int EMOJI_SCALE_INTERPOLATOR_ID = 3;

    protected ImageAnimation(AnimationType animationType) {
        super(animationType, 4);
    }

    @Override
    public BaseAnimationSetting[] getSettings() {
        return new BaseAnimationSetting[] {
                new DurationAnimationSetting(null, -1),
                new InterpolatorAnimationSetting("AnimationXPosition", R.string.AnimationXPosition, X_POSITION_INTERPOLATOR_ID),
                new InterpolatorAnimationSetting("AnimationYPosition", R.string.AnimationYPosition, Y_POSITION_INTERPOLATOR_ID),
                new InterpolatorAnimationSetting("AnimationEmojiScale", R.string.AnimationEmojiScale, EMOJI_SCALE_INTERPOLATOR_ID),
                new InterpolatorAnimationSetting("AnimationTimeAppears", R.string.AnimationTimeAppears, TIME_APPEARS_INTERPOLATOR_ID)
        };
    }

    @Override
    void restoreToDefault() {
        setDuration(500);
        getInterpolator(X_POSITION_INTERPOLATOR_ID).setParameters(0.33f, 1, 0, 0.5f);
        getInterpolator(Y_POSITION_INTERPOLATOR_ID).setParameters(0.33f, 1, 0, 1);
        getInterpolator(EMOJI_SCALE_INTERPOLATOR_ID).setParameters(0.33f, 1, 0.17f, 0.5f);
        getInterpolator(TIME_APPEARS_INTERPOLATOR_ID).setParameters(0.33f, 1, 0.17f, 0.5f);
    }

    public Interpolator getEmojiScaleInterpolator() {
        return getInterpolator(EMOJI_SCALE_INTERPOLATOR_ID);
    }
}
