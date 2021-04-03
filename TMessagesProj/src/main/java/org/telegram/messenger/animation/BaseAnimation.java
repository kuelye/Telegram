package org.telegram.messenger.animation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.FileLog;

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

    public AnimationType getAnimationType() {
        return animationType;
    }

    public Interpolator getInterpolator(int id) {
        return interpolators[id];
    }

    abstract JSONObject toJson();
    abstract void restoreToDefault();

    protected JSONObject toJson(boolean skipInterpolatorsDuration) {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonInterpolators = new JSONArray();
            for (int i = 0; i < interpolators.length; ++i) {
                jsonInterpolators.put(i, interpolators[i].toJson(skipInterpolatorsDuration));
            }
            jsonObject.put("i", jsonInterpolators);
            return jsonObject;
        } catch (JSONException e) {
            FileLog.e(e);
        }
        return null;
    }

    void applyJson(JSONObject jsonObject) {
        try {
            JSONArray jsonInterpolators = (JSONArray) jsonObject.getJSONArray("i");
            for (int i = 0; i < jsonInterpolators.length(); ++i) {
                interpolators[i].applyJson((JSONObject) jsonInterpolators.get(i));
            }
        } catch (JSONException e) {
            FileLog.e(e);
        }
    }

    public abstract BaseAnimationSetting[] getSettings();
}
