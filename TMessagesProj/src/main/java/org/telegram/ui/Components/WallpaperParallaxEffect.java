/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Grishka, 2013-2019.
 */

package org.telegram.ui.Components;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Surface;
import android.view.WindowManager;

import org.telegram.messenger.AndroidUtilities;

public class WallpaperParallaxEffect implements SensorEventListener {

	private float[] rollBuffer = new float[3], pitchBuffer = new float[3];
	private int bufferOffset;
	private final float[] pitchAndRoll = new float[2];
	private WindowManager wm;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private boolean enabled;
	private Callback callback;

	public WallpaperParallaxEffect(Context context) {
		wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	public void setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			this.enabled = enabled;
			if (accelerometer == null)
				return;
			if (enabled) {
				sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
			} else {
				sensorManager.unregisterListener(this);
			}
		}
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	public float getScale(int boundsWidth, int boundsHeight) {
		int offset = AndroidUtilities.dp(16);
		return Math.max(((float) boundsWidth + offset * 2) / (float) boundsWidth, ((float) boundsHeight + offset * 2) / (float) boundsHeight);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		bufferOffset = AndroidUtilities.calculatePitchAndRoll(event, pitchAndRoll, wm, rollBuffer, pitchBuffer, bufferOffset);
		int offsetX = Math.round(pitchAndRoll[0] * AndroidUtilities.dpf2(16));
		int offsetY = Math.round(pitchAndRoll[1] * AndroidUtilities.dpf2(16));
		if (callback != null)
			callback.onOffsetsChanged(offsetX, offsetY);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	public interface Callback {
		void onOffsetsChanged(int offsetX, int offsetY);
	}
}
