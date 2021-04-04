package org.telegram.messenger.animation;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.FileLog;

public abstract class BaseChatAnimation extends BaseAnimation {

    public final static int X_POSITION_INTERPOLATOR_ID = 0;
    public final static int Y_POSITION_INTERPOLATOR_ID = 1;
    public final static int TIME_APPEARS_INTERPOLATOR_ID = 2;

    public final static int DEFAULT_DURATION = 500;

    private int duration = DEFAULT_DURATION;

    protected BaseChatAnimation(AnimationType animationType, int interpolatorsCount) {
        super(animationType, interpolatorsCount);
    }

    @Override
    JSONObject toJson() {
        try {
            JSONObject jsonObject = super.toJson(true);
            jsonObject.put("d", duration);
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
            duration = jsonObject.getInt("d");
        } catch (JSONException e) {
            FileLog.e(e);
        }
    }

    @Override
    void restoreToDefault() {
        // TODO [CONTEST]
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public Interpolator getXInterpolator() {
        return getInterpolator(X_POSITION_INTERPOLATOR_ID);
    }

    public Interpolator getYInterpolator() {
        return getInterpolator(Y_POSITION_INTERPOLATOR_ID);
    }

    public Interpolator getTimeAppearsInterpolator() {
        return getInterpolator(TIME_APPEARS_INTERPOLATOR_ID);
    }

    public boolean isText() {
        return getAnimationType() == AnimationType.SHORT_TEXT || getAnimationType() == AnimationType.LONG_TEXT;
    }

    public boolean isEmojiOrSticker() {
        return isEmoji() || isSticker();
    }

    public boolean isEmoji() {
        return getAnimationType() == AnimationType.EMOJI;
    }

    public boolean isSticker() {
        return getAnimationType() == AnimationType.STICKER;
    }
}
