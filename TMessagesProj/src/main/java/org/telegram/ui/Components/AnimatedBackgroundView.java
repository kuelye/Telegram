package org.telegram.ui.Components;

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

import com.google.zxing.common.detector.MathUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.animation.AnimationController;
import org.telegram.messenger.animation.AnimationType;
import org.telegram.messenger.animation.BackgroundAnimation;
import org.telegram.messenger.animation.Interpolator;

public class AnimatedBackgroundView extends View implements AnimationController.OnAnimationChangedListener {

    private final static PointF[] DEFAULT_POINTS = { new PointF(0.35f, 0.25f), new PointF(0.82f, 0.08f), new PointF(0.65f, 0.75f), new PointF(0.18f, 0.92f)};
    private final static float RADIUS_FACTOR = 0.05f;
    private final static int STROKE_WIDTH = AndroidUtilities.dp(2);
    private final static int STROKE_COLOR = Color.WHITE;

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

    public AnimatedBackgroundView(Context context) {
        super(context);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(STROKE_WIDTH);
        strokePaint.setColor(STROKE_COLOR);
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

        if (points != null) {
            float radius = getWidth() * RADIUS_FACTOR;
            for (int i = 0; i < 4; ++i) {
                fillPaint.setColor(animation.getColor(i));
                float x = getWidth() * points[i].x;
                float y = getHeight() * points[i].y;
                canvas.drawCircle(x, y, radius, fillPaint);
                canvas.drawCircle(x, y, radius, strokePaint);
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

    private Bitmap generateBitmap() {
        Log.v("GUB", "generateBitmap: start");
        if (getWidth() == 0 || getHeight() == 0) {
            return null;
        }

        long now = System.currentTimeMillis();
        int s = getWidth() / 1;
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
        float d = Math.min(ds[0], Math.min(ds[1], Math.min(ds[2], ds[3])));
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
