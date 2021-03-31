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
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.ColorInt;

import com.google.zxing.common.detector.MathUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.animation.AnimationController;
import org.telegram.messenger.animation.AnimationType;
import org.telegram.messenger.animation.BackgroundAnimation;
import org.telegram.messenger.animation.Interpolator;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class AnimatedBackgroundView extends View implements AnimationController.OnAnimationChangedListener {

    private final static PointF[] DEFAULT_POINTS = { new PointF(0.35f, 0.25f), new PointF(0.82f, 0.08f), new PointF(0.65f, 0.75f), new PointF(0.18f, 0.92f)};

    private final static float DEV_POINTS_RADIUS_FACTOR = 0.05f;
    private final static int DEV_POINTS_STROKE_WIDTH = AndroidUtilities.dp(4);
    private final static int DEV_POINTS_STROKE_COLOR = Color.WHITE;
    private final static float DEV_POINTS_START_SCALE = 2f;

    private final Paint fillPaint;
    private final Paint strokePaint;
    private final Paint framePaint;

    private BackgroundAnimation animation;

    private ValueAnimator valueAnimator;
    private float state = 0f;
    private final Point[] points = new Point[4];
    private final Matrix frameMatrix = new Matrix();
    private Bitmap frame = null;

    private final float[] ds = new float[4];
    private final int[] rs = new int[4];
    private final int[] gs = new int[4];
    private final int[] bs = new int[4];
    private float[] rxs = null;

    private boolean isDevPointsVisible;
    private float devPointsAlpha;
    private float devPointsScale = DEV_POINTS_START_SCALE;
    private AnimatorSet devPointsVisibleAnimatorSet = null;

    public AnimatedBackgroundView(Context context) {
        super(context);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(DEV_POINTS_STROKE_WIDTH);
        framePaint = new Paint(Paint.FILTER_BITMAP_FLAG);

        updateAnimation();
        recalculatePoints();
        recalculateBitmap();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
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
    public void onAnimationChanged() {
        updateAnimation();
    }

    public void animate(Interpolator interpolator) {
        if (valueAnimator != null && valueAnimator.isRunning()) return;
        float startState = state;
        valueAnimator = ValueAnimator.ofFloat(0.0f, 0.5f).setDuration(interpolator.getDuration());
        valueAnimator.addUpdateListener(animation -> {
            state = startState + (float) animation.getAnimatedValue();
            recalculatePoints();
            recalculateBitmap();
            invalidate();
        });
        valueAnimator.start();
    }

    public void cancelAnimation() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator = null;
        }
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

    private void updateAnimation() {
        animation = AnimationController.getBackgroundAnimation();
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
        int s = (int) Math.floor(state * 2) / 2;
        float f = Math.abs(state - s);
        int fromI = clip(i - s);
        int toI = clip(fromI - 1);
        return new Point(
            AndroidUtilities.lerp(DEFAULT_POINTS[fromI].x, DEFAULT_POINTS[toI].x, f),
            AndroidUtilities.lerp(DEFAULT_POINTS[fromI].y, DEFAULT_POINTS[toI].y, f)
        );
    }

    private int clip(int i) {
        i = i % 4;
        if (i < 0) i += 4;
        return i;
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
        Log.v("GUB", "generateBitmap: start");
        if (getWidth() == 0 || getHeight() == 0) {
            return null;
        }

        long now = System.currentTimeMillis();
        int s = getWidth() / 10;
        Bitmap bitmap = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);

        rxs = new float[s];
        for (int x = 0; x < s; ++x) {
            rxs[x] = (float) x / s;
        }
        for (int x = 0; x < s; ++x) {
            for (int y = 0; y < s; ++y) {
                bitmap.setPixel(x, y, getGradientColor(rxs[x], rxs[y]));
            }
        }
        Log.v("GUB", "generateBitmap: elapsed=" + (System.currentTimeMillis() - now) + "ms");
        return bitmap;
    }

    private int getGradientColor(float x, float y) {
        for (int i = 0; i < 4; ++i) {
            ds[i] = MathUtils.distance(x, y, points[i].x, points[i].y);
            rs[i] = Color.red(animation.getColor(i));
            gs[i] = Color.green(animation.getColor(i));
            bs[i] = Color.blue(animation.getColor(i));
        }
        float d = min(ds[0], min(ds[1], min(ds[2], ds[3])));
        for (int i = 0; i < 4; ++i) {
            ds[i] = (float) Math.pow(1 - (ds[i] - d), 5);
        }
        d = ds[0] + ds[1] + ds[2] + ds[3];
        for (int i = 0; i < 4; ++i) {
            ds[i] = ds[i] / d;
        }
        int r = (int) (rs[0] * ds[0] + rs[1] * ds[1] + rs[2] * ds[2] + rs[3] * ds[3]);
        int g = (int) (gs[0] * ds[0] + gs[1] * ds[1] + gs[2] * ds[2] + gs[3] * ds[3]);
        int b = (int) (bs[0] * ds[0] + bs[1] * ds[1] + bs[2] * ds[2] + bs[3] * ds[3]);
        return Color.rgb(r, g, b);
    }
}
