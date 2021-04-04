package org.telegram.messenger.animation;

import android.util.Log;

import androidx.annotation.ColorInt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;

import java.util.Arrays;

public class BackgroundAnimation extends BaseAnimation {

    public final static int POINTS_COUNT = 4;
    private final static int[] DEFAULT_COLORS = { 0xFF2C624B, 0xFFFFF3BD, 0xFF739975, 0xFFFADF71 }; // TODO [CONTEST] { 0xFFFFF3BD, 0xFF739975, 0xFFFADF71, 0xFF2C624B };

    public final static int SEND_MESSAGE_INTERPOLATOR_ID = 0;
    public final static int OPEN_CHAT_INTERPOLATOR_ID = 1;
    public final static int JUMP_TO_MESSAGE_INTERPOLATOR_ID = 2;

    private final int[] colors = DEFAULT_COLORS.clone();

    BackgroundAnimation() {
        super(AnimationType.BACKGROUND, 3);
    }

    @Override
    public BaseAnimationSetting[] getSettings() {
        return new BaseAnimationSetting[] {
            new BaseAnimationSetting("BackgroundPreview", R.string.BackgroundPreview, BaseAnimationSetting.ContentType.BACKGROUND),
            new BackgroundColorsAnimationSetting(),
            new InterpolatorAnimationSetting("AnimationSendMessage", R.string.AnimationSendMessage, SEND_MESSAGE_INTERPOLATOR_ID),
            new InterpolatorAnimationSetting("AnimationOpenChat", R.string.AnimationOpenChat, OPEN_CHAT_INTERPOLATOR_ID),
            new InterpolatorAnimationSetting("AnimationJumpToMessage", R.string.AnimationJumpToMessage, JUMP_TO_MESSAGE_INTERPOLATOR_ID)
        };
    }

    @Override
    JSONObject toJson() {
        try {
            JSONObject jsonObject = super.toJson(false);
            JSONArray jsonColors = new JSONArray();
            for (int i = 0; i < 4; ++ i) {
                jsonColors.put(i, colors[i]);
            }
            jsonObject.put("colors", jsonColors);
            return jsonObject;
        } catch (JSONException e) {
            FileLog.e(e);
        }
        return null;
    }

    @Override
    void applyJson(JSONObject jsonObject) {
        try {
            super.applyJson(jsonObject);
            JSONArray jsonColors = jsonObject.getJSONArray("colors");
            for (int i = 0; i < colors.length; ++i) {
                colors[i] = jsonColors.getInt(i);
            }
        } catch (JSONException e) {
            FileLog.e(e);
        }
    }

    @Override
    void restoreToDefault() {
        System.arraycopy(DEFAULT_COLORS, 0, colors, 0, colors.length);
        getInterpolator(SEND_MESSAGE_INTERPOLATOR_ID).setDuration(1000).setParameters(0.33f, 1, 0, 1);
        getInterpolator(OPEN_CHAT_INTERPOLATOR_ID).setDuration(2000).setParameters(0.16f, 1, 0, 1);
        getInterpolator(JUMP_TO_MESSAGE_INTERPOLATOR_ID).setDuration(1000).setParameters(0.33f, 1, 0, 1);
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
            super("BackgroundPreview", R.string.BackgroundPreview, ContentType.COLORS);
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
