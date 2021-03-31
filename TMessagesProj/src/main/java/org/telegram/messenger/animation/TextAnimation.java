package org.telegram.messenger.animation;

import org.telegram.messenger.R;

public class TextAnimation extends BaseChatAnimation {

    public final static int X_POSITION_INTERPOLATOR_ID = 0;
    public final static int Y_POSITION_INTERPOLATOR_ID = 1;
    public final static int BUBBLE_SHAPE_INTERPOLATOR_ID = 2;
    public final static int TEXT_SCALE_INTERPOLATOR_ID = 3;
    public final static int COLOR_CHANGE_INTERPOLATOR_ID = 4;
    public final static int TIME_APPEARS_INTERPOLATOR_ID = 5;

    protected TextAnimation(AnimationType animationType) {
        super(animationType, 6);
    }

    @Override
    public BaseAnimationSetting[] getSettings() {
        return new BaseAnimationSetting[] {
            new DurationAnimationSetting(null, -1),
            new InterpolatorAnimationSetting("AnimationXPosition", R.string.AnimationXPosition, X_POSITION_INTERPOLATOR_ID),
            new InterpolatorAnimationSetting("AnimationYPosition", R.string.AnimationYPosition, Y_POSITION_INTERPOLATOR_ID),
            new InterpolatorAnimationSetting("AnimationBubbleShape", R.string.AnimationBubbleShape, BUBBLE_SHAPE_INTERPOLATOR_ID),
            new InterpolatorAnimationSetting("AnimationTextScale", R.string.AnimationTextScale, TEXT_SCALE_INTERPOLATOR_ID),
            new InterpolatorAnimationSetting("AnimationColorChange", R.string.AnimationColorChange, COLOR_CHANGE_INTERPOLATOR_ID),
            new InterpolatorAnimationSetting("AnimationTimeAppears", R.string.AnimationTimeAppears, TIME_APPEARS_INTERPOLATOR_ID)
        };
    }
}
