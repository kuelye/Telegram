package org.telegram.messenger.animation;

import androidx.annotation.ColorInt;

import org.telegram.messenger.R;

public class BackgroundAnimation extends BaseAnimation {

    public final static int POINTS_COUNT = 4;
    private final static int[] DEFAULT_COLORS = { 0xFFFFF3BD, 0xFF739975, 0xFFFADF71, 0xFF2C624B };

    private final int[] colors = DEFAULT_COLORS;

    BackgroundAnimation() {
        super(AnimationType.BACKGROUND);
    }

    @Override
    public BaseAnimationSetting[] getSettings() {
        return new BaseAnimationSetting[] {
            new BaseAnimationSetting("AnimationBackgroundPreview", R.string.AnimationBackgroundPreview, BaseAnimationSetting.ContentType.BACKGROUND),
            new BackgroundColorsAnimationSetting(),
            new InterpolatorAnimationSetting("AnimationSendMessage", R.string.AnimationSendMessage),
            new InterpolatorAnimationSetting("AnimationOpenChat", R.string.AnimationOpenChat),
            new InterpolatorAnimationSetting("AnimationJumpToMessage", R.string.AnimationJumpToMessage)
        };
    }

    public int[] getColors() {
        return colors;
    }

    @ColorInt
    public int getColor(int id) {
        return colors[id];
    }

    public void setColor(int id, @ColorInt int color) {
        colors[id] = color;
    }

    public static class BackgroundColorsAnimationSetting extends BaseAnimationSetting {

        BackgroundColorsAnimationSetting() {
            super("AnimationBackgroundPreview", R.string.AnimationBackgroundPreview, ContentType.COLORS);
        }

        @ColorInt
        public int getColor(BaseAnimation animation, int id) {
            return ((BackgroundAnimation) animation).getColor(id);
        }

        public void setColor(BaseAnimation animation, int id, @ColorInt int color) {
            ((BackgroundAnimation) animation).setColor(id, color);
            AnimationController.emitAnimationChange(AnimationType.BACKGROUND);
        }
    }
}
