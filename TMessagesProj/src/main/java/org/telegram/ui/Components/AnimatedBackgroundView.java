package org.telegram.ui.Components;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.ColorInt;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.animation.AnimationController;
import org.telegram.messenger.animation.AnimationType;
import org.telegram.messenger.animation.BackgroundAnimation;
import org.telegram.messenger.animation.Interpolator;

import java.util.Arrays;

import static java.lang.Math.max;

public class AnimatedBackgroundView extends View implements AnimationController.OnAnimationChangedListener, SensorEventListener {

    private final static PointF[] DEFAULT_POINTS = { new PointF(0.35f, 0.25f), new PointF(0.82f, 0.08f), new PointF(0.65f, 0.75f), new PointF(0.18f, 0.92f)};

    private final static float DEV_POINTS_RADIUS_FACTOR = 0.05f;
    private final static int DEV_POINTS_STROKE_WIDTH = AndroidUtilities.dp(4);
    private final static int DEV_POINTS_STROKE_COLOR = Color.WHITE;
    private final static float DEV_POINTS_START_SCALE = 2f;

    private final static float SENSOR_START_DISTANCE_SQUARE = 0.01f;
    private final static float SENSOR_STATE_SLOP = 0.02f;
    private final static float SENSOR_STATE_ANIMATE_SLOP = 0.1f;
    private final static float SENSOR_ANIMATION_DURATION = 300;

    private final Paint fillPaint;
    private final Paint strokePaint;
    private final Paint framePaint;

    private BackgroundAnimation animation;

    private boolean isSensorAnimationEnabled = false;
    private ValueAnimator animator;
    private ValueAnimator sensorAnimator;
    private float state = 0f;
    private float sensorState = 0f;
    private float previousSensorState = 0f;
    private float targetSensorState = 0f;
    private final Point[] points = new Point[4];
    private final Matrix frameMatrix = new Matrix();
    private Bitmap frame = null;

    private boolean isDevPointsVisible;
    private float devPointsAlpha;
    private float devPointsScale = DEV_POINTS_START_SCALE;
    private AnimatorSet devPointsVisibleAnimatorSet = null;

    private final SensorManager sensorManager;
    private final WindowManager wm;
    private final Sensor sensor;
    private final float[] rollBuffer = new float[3];
    private final float[] pitchBuffer = new float[3];
    private int bufferOffset;
    private final float[] pitchAndRoll = new float[2];

    public AnimatedBackgroundView(Context context) {
        super(context);

        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(DEV_POINTS_STROKE_WIDTH);
        framePaint = new Paint(Paint.FILTER_BITMAP_FLAG);

        updateAnimation();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateAnimation();
        AnimationController.addOnAnimationChangedListener(AnimationType.BACKGROUND, this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelAnimation();
        AnimationController.removeOnAnimationChangedListener(AnimationType.BACKGROUND, this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
            recalculateBitmap();
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (frame != null) {
            canvas.drawBitmap(frame, frameMatrix, framePaint);
        }

        if (devPointsAlpha != 0 && points != null) {
            float r = getWidth() * DEV_POINTS_RADIUS_FACTOR * devPointsScale;
            for (int i = 0; i < 4; ++i) {
                fillPaint.setColor(setAlpha(animation.getColor(i), devPointsAlpha));
                float x = getWidth() * points[i].x;
                float y = getHeight() * points[i].y;
                canvas.drawCircle(x, y, r, fillPaint);
                strokePaint.setColor(setAlpha(DEV_POINTS_STROKE_COLOR, devPointsAlpha));
                canvas.drawCircle(x, y, r, strokePaint);
            }
        }
    }

    @Override
    public void onAnimationChanged(AnimationType animationType) {
        updateAnimation();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isSensorAnimationEnabled) {
            return;
        }

        bufferOffset = AndroidUtilities.calculatePitchAndRoll(event, pitchAndRoll, wm, rollBuffer, pitchBuffer, bufferOffset);
        float dist2 = (float) (Math.pow(pitchAndRoll[0], 2) + Math.pow(pitchAndRoll[1], 2));
        if (dist2 < SENSOR_START_DISTANCE_SQUARE) {
//            animateSensor(0, true);
            return;
        }

        float angle = - (float) (Math.atan2(pitchAndRoll[0], pitchAndRoll[1]) - Math.PI / 2);
        if (angle < 0) angle += 2 * Math.PI;
        float targetSensorState = (float) (2 * angle / Math.PI);
        float d = clampDistance(Math.abs(sensorState - targetSensorState));
        if (d < SENSOR_STATE_SLOP) {
            return;
        }

//        d = clampDistance(Math.abs(sensorState - targetSensorState));
//        Log.v("GUB", "onSensorChanged: pitchAndRoll=" + Arrays.toString(pitchAndRoll) + ", dist2=" + dist2 + ", stateOffset=" + sensorState + ", d=" + d);
//        if (d < SENSOR_STATE_ANIMATE_SLOP && !isSensorAnimationRunning()) {
//            cancelSensorAnimation();
            sensorState = targetSensorState;
            updateByState();
//        } else {
//            animateSensor(targetSensorState, false);
//        }
        previousSensorState = sensorState;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ignore
    }

    public void animate(Interpolator interpolator) {
//        Log.v("AnimatedBackgroundView", "GUB animate: interpolator=" + interpolator);
        if (isAnimationRunning() || isSensorAnimationRunning()) return;
        float startState = state;
        animator = ValueAnimator.ofFloat(0.0f, 1f).setDuration(interpolator.getDuration());
        animator.addUpdateListener(animation -> {
            state = startState + 0.5f * interpolator.getInterpolation((float) animation.getAnimatedValue());
            updateByState();
        });
        animator.start();
    }

    public void cancelAnimation() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    public boolean isAnimationRunning() {
        return animator != null && animator.isRunning();
    }

    public void setSensorAnimationEnabled(boolean enabled) {
        Log.v("GUB", "setSensorAnimationEnabled: enabled=" + enabled);
        isSensorAnimationEnabled = enabled;
        if (enabled) {
            registerSensor();
        } else {
            unregisterSensor();
            animateSensor(0, true);
        }
    }

    private void animateSensor(float targetSensorState, boolean forced) {
        Log.v("GUB", "animateSensor: targetSensorState=" + targetSensorState + ", forced=" + forced + ", this.targetSensorState=" + this.targetSensorState + ", ?=" + clampDistance(this.targetSensorState - targetSensorState));
        if (sensorAnimator != null && sensorAnimator.isRunning()) {
            if (forced && clampDistance(this.targetSensorState - targetSensorState) != 0) {
                cancelSensorAnimation();
            } else {
                return;
            }
        }

        float startSensorState = sensorState;
        if (startSensorState > targetSensorState && startSensorState - targetSensorState > 2) {
            targetSensorState += 4;
        } else if (startSensorState < targetSensorState && targetSensorState - startSensorState > 2) {
            targetSensorState -= 4;
        }
        this.targetSensorState = targetSensorState;
        long duration = (long) (SENSOR_ANIMATION_DURATION * Math.min(2, Math.abs(targetSensorState - startSensorState)) / 2);
//        Log.v("GUB", "animateSensor: startSensorState=" + startSensorState + ", targetSensorState=" + targetSensorState + ", duration=" + duration);

        sensorAnimator = ValueAnimator.ofFloat(startSensorState, targetSensorState).setDuration(duration);
        sensorAnimator.addUpdateListener(animation -> {
            sensorState = (float) animation.getAnimatedValue();
            updateByState();
        });
        sensorAnimator.start();
    }

    private void cancelSensorAnimation() {
        if (sensorAnimator != null) {
            sensorAnimator.cancel();
            sensorAnimator = null;
        }
    }

    private boolean isSensorAnimationRunning() {
        return sensorAnimator != null && sensorAnimator.isRunning();
    }

    public boolean isDevPointsVisible() {
        return isDevPointsVisible;
    }

    public void setDevPointsVisible(Boolean isVisible) {
        isDevPointsVisible = isVisible;
        cancelPointsVisibleAnimation();
        devPointsVisibleAnimatorSet = new AnimatorSet();
        float targetAlpha = isVisible ? 1 : 0;
        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(devPointsAlpha, targetAlpha);
        alphaAnimator.addUpdateListener(animation -> {
            devPointsAlpha = (float) animation.getAnimatedValue();
            invalidate();
        });
        float targetScale = isVisible ? 1 : DEV_POINTS_START_SCALE;
        ValueAnimator scaleAnimator = ValueAnimator.ofFloat(devPointsScale, targetScale);
        scaleAnimator.setInterpolator(isVisible ? new OvershootInterpolator() : new AnticipateInterpolator());
        scaleAnimator.addUpdateListener(animation -> {
            devPointsScale = (float) animation.getAnimatedValue();
            invalidate();
        });
        devPointsVisibleAnimatorSet.setDuration(max(1, (long) (300 * Math.abs(targetAlpha - devPointsAlpha))));
        devPointsVisibleAnimatorSet.playTogether(alphaAnimator, scaleAnimator);
        devPointsVisibleAnimatorSet.start();
    }

    public void registerSensor() {
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI); // TODO [CONTEST] GAME
    }

    public void unregisterSensor() {
        sensorManager.unregisterListener(this);
    }

    private void updateAnimation() {
        animation = AnimationController.getBackgroundAnimation();
        updateByState();
    }

    private void updateByState() {
        recalculatePoints();
        recalculateBitmap();
        invalidate();
    }

    private void recalculatePoints() {
        for (int i = 0; i < 4; ++i) {
            points[i] = getPoint(i);
        }
    }

    private void recalculateBitmap() {
        frame = generateBitmap();
        if (frame != null) {
            frameMatrix.setScale((float) getWidth() / frame.getWidth(), (float) getHeight() / frame.getHeight());
        }
    }

    private Point getPoint(int i) {
        int s = (int) Math.floor((state + sensorState) * 2) / 2;
        float f = Math.abs((state + sensorState) - s);
        int fromI = clamp(i - s);
        int toI = clamp(fromI - 1);
        return new Point(
            AndroidUtilities.lerp(DEFAULT_POINTS[fromI].x, DEFAULT_POINTS[toI].x, f),
            AndroidUtilities.lerp(DEFAULT_POINTS[fromI].y, DEFAULT_POINTS[toI].y, f)
        );
    }

    private int clamp(int state) {
        state = state % 4;
        if (state < 0) state += 4;
        return state;
    }

    private float clampDistance(float distance) {
        if (distance > 0) {
            return distance - (int) Math.floor(distance / 4) * 4;
        } else {
            return distance + (int) Math.ceil(distance / 4) * 4;
        }
    }

    @ColorInt
    private int setAlpha(@ColorInt int color, float alpha) {
        return ((int) (alpha * 255) << 24) | (color & 0xFFFFFF);
    }

    private void cancelPointsVisibleAnimation() {
        if (devPointsVisibleAnimatorSet != null) {
            devPointsVisibleAnimatorSet.cancel();
            devPointsVisibleAnimatorSet = null;
        }
    }

    private Bitmap generateBitmap() {
        if (getWidth() == 0 || getHeight() == 0) {
            return null;
        }

        long now = System.currentTimeMillis();
        int s = getWidth() / 10;
        Bitmap bitmap = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        float[] points = new float[8];
        int[] colors = new int[4];
        for (int i = 0; i < 4; ++i) {
            points[i * 2] = this.points[i].x;
            points[i * 2 + 1] = this.points[i].y;
            colors[i] = animation.getColor(i);
        }
        Utilities.generateBackgroundBitmap(bitmap, points, colors);

        Log.v("GUB", "generateBitmap: elapsed=" + (System.currentTimeMillis() - now) + "ms");
        return bitmap;
    }
}
