package org.telegram.messenger.animation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.FileLog;

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
}
