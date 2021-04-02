package org.telegram.messenger.animation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.SerializedData;

public class Interpolator {

    public final static int[] DURATIONS = { 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1500, 2000, 3000 };
    public final static int DEFAULT_DURATION = 1000;

    private int duration = Interpolator.DEFAULT_DURATION;

    private float[] cs = { 1.0f, 1.0f };
    private float[] ts = { 0.0f, 1.0f };

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public float[] getCs() {
        return cs;
    }

    public float[] getTs() {
        return ts;
    }

    public void setParameters(float[] cs, float ts[]) {
        this.cs = cs;
        this.ts = ts;
    }

    JSONObject toJson(boolean skipDuration) {
        try {
            JSONObject jsonObject = new JSONObject();
            if (!skipDuration) {
                jsonObject.put("d", duration);
            }
            jsonObject.put("c", new JSONArray(cs));
            jsonObject.put("t", new JSONArray(ts));
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
}
