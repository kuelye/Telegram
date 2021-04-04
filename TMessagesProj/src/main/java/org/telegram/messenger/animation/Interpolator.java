package org.telegram.messenger.animation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.FileLog;
import org.telegram.ui.Components.CubicBezierInterpolator;

import java.util.Arrays;

public class Interpolator {

    public final static int[] DURATIONS = { 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1500, 2000, 3000 };
    public final static int DEFAULT_DURATION = 1000;

    private int duration = Interpolator.DEFAULT_DURATION;

    private float[] cs = { 1.0f, 1.0f };
    private float[] ts = { 0.0f, 1.0f };

    private CubicBezierInterpolator animationInterpolator;

    public int getDuration() {
        return duration;
    }

    public Interpolator setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public float[] getCs() {
        return cs;
    }

    public float[] getTs() {
        return ts;
    }

    public Interpolator setParameters(float cs0, float cs1, float ts0, float ts1) {
        return setParameters(new float[] { cs0, cs1 }, new float[] { ts0, ts1 });
    }

    public Interpolator setParameters(float[] cs, float[] ts) {
        this.cs = cs;
        this.ts = ts;
        animationInterpolator = null;
        return this;
    }

    public float getInterpolation(float time) {
        if (animationInterpolator == null) {
            animationInterpolator = new CubicBezierInterpolator(cs[0], 0, 1 - cs[1], 1);
        }
        if (time < ts[0]) {
            return 0;
        } else if (time > ts[1]) {
            return 1;
        } else {
            return animationInterpolator.getInterpolation((time - ts[0]) / (ts[1] - ts[0]));
        }
    }

    JSONObject toJson(boolean skipDuration) {
        try {
            JSONObject jsonObject = new JSONObject();
            if (!skipDuration) {
                jsonObject.put("d", duration);
            }
            JSONArray jsonCs = new JSONArray();
            for (int i = 0; i < 2; ++ i) {
                jsonCs.put(i, cs[i]);
            }
            jsonObject.put("c", jsonCs);
            JSONArray jsonTs = new JSONArray();
            for (int i = 0; i < 4; ++ i) {
                jsonTs.put(i, ts[i]);
            }
            jsonObject.put("t", jsonTs);
            return jsonObject;
        } catch (JSONException e) {
            FileLog.e(e);
        }
        return null;
    }

    void applyJson(JSONObject jsonObject) {
        try {
            if (jsonObject.has("d")) {
                duration = jsonObject.getInt("d");
            }
            JSONArray jsonCs = (JSONArray) jsonObject.getJSONArray("c");
            cs[0] = (float) jsonCs.getDouble(0);
            cs[1] = (float) jsonCs.getDouble(1);
            JSONArray jsonTs = (JSONArray) jsonObject.getJSONArray("t");
            ts[0] = (float) jsonTs.getDouble(0);
            ts[1] = (float) jsonTs.getDouble(1);
        } catch (JSONException e) {
            FileLog.e(e);
        }
    }

    @Override
    public String toString() {
        return "Interpolator{" +
                "duration=" + duration +
                ", cs=" + Arrays.toString(cs) +
                ", ts=" + Arrays.toString(ts) +
                '}';
    }
}
